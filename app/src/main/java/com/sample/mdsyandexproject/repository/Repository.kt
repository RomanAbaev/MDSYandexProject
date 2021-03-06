package com.sample.mdsyandexproject.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.sample.mdsyandexproject.App
import com.sample.mdsyandexproject.R
import com.sample.mdsyandexproject.database.*
import com.sample.mdsyandexproject.di.AppScope
import com.sample.mdsyandexproject.domain.*
import com.sample.mdsyandexproject.network.*
import com.sample.mdsyandexproject.utils.*
import com.squareup.moshi.Moshi
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
@AppScope
class Repository @Inject constructor(
    var finnHubApi: FinnHubService,
    var moshi: Moshi,
    var database: StockDatabase,
    var webSocketProvider: WebSocketProvider
) {

    private val adapter = moshi.adapter(UpdatePrices::class.java)

    private val updateRequestQ = ConcurrentHashMap<String, Int>()

    val loadNextChunksException = MutableLiveData<Pair<Boolean, String>>()
    val submitSearchException = MutableLiveData<Pair<Boolean, String>>()
    val loadCandleInfoException = MutableLiveData<Pair<Boolean, String>>()
    val loadNewsException = MutableLiveData<Pair<Boolean, String>>()
    val updateRecommendationsException = MutableLiveData<Pair<Boolean, String>>()


    suspend fun openSocketChannel() {
        try {
            startSocket().consumeEach { response ->
                if (response.exception == null) {
                    response.text?.let { text ->
                        adapter.fromJson(text)?.data?.let { listData ->
                            this.database.dao.updatePrices(listData.asPrices())
                        }
                    }
                } else onSocketError(response.exception)
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    fun subscribeToTickerPriceUpdate(ticker: String) {
        try {
            webSocketProvider.webSocket?.send("{\"type\":\"subscribe\",\"symbol\":\"$ticker\"}")
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun unsubscribeToTickerPriceUpdate(ticker: String) {
        try {
            webSocketProvider.webSocket?.send("{\"type\":\"unsubscribe\",\"symbol\":\"$ticker\"}")
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun onSocketError(ex: Throwable) {
        println(ex.printStackTrace())
    }

    fun getStockList(showFavouriteList: Boolean): LiveData<List<StockItem>> {
        return if (showFavouriteList) Transformations.map(database.dao.getFavouriteList()) { it.asDomainModel() }
        else Transformations.map(database.dao.getAll()) { it.asDomainModel() }
    }


    suspend fun refreshData() {
        webSocketProvider = WebSocketProvider()
        openSocketChannel()
    }

    val isDataPrepopulating = MutableLiveData(false)
    val stockItemLoadingProgress = MutableLiveData(0)
    val stockItemLoading = MutableLiveData(false)

    suspend fun loadNextChunks() {
        try {
            if (!isPrepopulateNeeded()) {
                stockItemLoading.postValue(true)
                loadStockItems()
                stockItemLoading.postValue(false)
            } else prepopulateData()
        } catch (ex: HttpException) {
            ex.printStackTrace()
            loadNextChunksException.postValue(Pair(true, getReadableNetworkMessage(ex)))
        } catch (ex: Exception) {
            ex.printStackTrace()
            loadNextChunksException.postValue(
                Pair(
                    true,
                    App.applicationContext().getString(R.string.common_error)
                )
            )
        }
    }

    suspend fun isPrepopulateNeeded(): Boolean {
        return database.dao.getIndicesCount() == 0 || database.dao.getTotalItemCount() == 0
    }

    private suspend fun loadStockItems() {
        stockItemLoadingProgress.postValue(stockItemLoadingProgress.value?.plus(1))
        val spIndices = database.dao.getNextUnloadedIndices(limit)
        if (spIndices.isNotEmpty()) loadCompanyInfoAndQuote(spIndices)
    }

    suspend fun prepopulateData() {
        isDataPrepopulating.postValue(true)
        stockItemLoadingProgress.postValue(stockItemLoadingProgress.value?.plus(1))
        loadSPIndicesToDB()
        loadStockItems()
        isDataPrepopulating.postValue(false)
    }

    private suspend fun loadCompanyInfoAndQuote(spIndices: List<SPIndices>) {
        withContext(Dispatchers.IO) {
            // TODO make it parallel and write by one in db
            //  and if we get 429 (limit exceeded) show what we get, not what error
            //  think about auth and firebase db as cache
            //  (so we don't need to make so many request for different users)
            database.dao.loadNextChunks(
                stockList = spIndices.map {
                    delay(500)
                    val companyProfile = finnHubApi.getCompanyProfile(it.indices).await()
                    val quote = finnHubApi.getQuote(it.indices).await()
                    stockItemLoadingProgress.postValue(stockItemLoadingProgress.value?.plus(1))
                    createDatabaseStockItem(companyProfile, quote)
                },
                spIndices = spIndices.map {
                    SPIndices(it.indices, true, it.symbol)
                }
            )
        }
    }

    suspend fun updateQuoteAndCompanyInfo(stockItem: StockItem) {
        if (!updateRequestQ.containsKey(stockItem.ticker)) {
            updateRequestQ[stockItem.ticker] = 0
            delay(1000)
            var quote: Quote? = null
            var companyProfile: CompanyProfile? = null
            if (!isCompanyInfoValid(stockItem)) {
                try {
                    companyProfile = finnHubApi.getCompanyProfile(stockItem.ticker).await()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
            try {
                quote = finnHubApi.getQuote(stockItem.ticker).await()
            } catch (ex: HttpException) {
                quote = Quote(
                    errorCode = ex.code().toString(),
                    errorMessage = if (ex.code()
                            .toString() != "403"
                    ) ex.message() else App.applicationContext()
                        .getString(R.string.permission_denied_error)
                )
                ex.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            when (companyProfile) {
                null -> database.dao.updateQuote(createQuoteDb(stockItem, quote))
                else -> database.dao.updateQuoteAndCompanyProfile(
                    createQuoteAndCompanyProfileDb(
                        stockItem,
                        quote,
                        companyProfile
                    )
                )
            }
            updateRequestQ.remove(stockItem.ticker)
        }
    }

    suspend fun isExistInDb(stockItem: StockItem) {
        val _stockItem = database.dao.getStockItem(stockItem.ticker)
        if (_stockItem != null) {
            updateFavouriteStock(stockItem)
        } else {
            stockItem.isFavourite = true
            database.dao.updateSPIndices(listOf(SPIndices(stockItem.ticker, true)))
            database.dao.insertStockItem(stockItem.asDatabaseModel())
        }
    }

    suspend fun updateFavouriteStock(stockItem: StockItem) {
        database.dao.updateFavouriteStock(stockItem.asFavouriteDatabaseModel())
    }

    private suspend fun loadSPIndicesToDB() {
        try {
            val spIndices = finnHubApi.getTop500Indices().await().asDatabaseModel()
            database.dao.insertAllSPIndices(spIndices)
        } catch (socketEx: SocketTimeoutException) {
            socketEx.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    suspend fun submitSearch(query: String): MutableList<StockItem> {
        // get all from database base on this query
        val stockItemListFromDb: MutableList<StockItem> =
            database.dao.search("%$query%").asDomainModel().toMutableList()
        var stockItemListNetwork: SearchResultResponse? = null
        try {
            stockItemListNetwork = finnHubApi.submitSearch(query).await()
        } catch (ex: HttpException) {
            ex.printStackTrace()
            submitSearchException.postValue(Pair(true, getReadableNetworkMessage(ex)))
        } catch (ex: Exception) {
            ex.printStackTrace()
            submitSearchException.postValue(
                Pair(
                    true,
                    App.applicationContext().getString(R.string.common_error)
                )
            )
        }

        val resultListFromNetwork: MutableList<StockItem>? = stockItemListNetwork?.result?.map {
            delay(1000)
            var quote: Quote? = null
            var companyProfile: CompanyProfile? = null
            try {
                companyProfile = finnHubApi.getCompanyProfile(it.symbol).await()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            try {
                quote = finnHubApi.getQuote(it.symbol).await()
            } catch (ex: HttpException) {
                quote = Quote(
                    errorCode = ex.code().toString(),
                    errorMessage = if (ex.code().toString() != "403") ex.message()
                    else App.applicationContext().getString(R.string.permission_denied_error)
                )
                ex.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            if (companyProfile != null) createStockItem(it.symbol, companyProfile, quote)
            else null
        }?.filterNotNull()?.toMutableList()

        return if (resultListFromNetwork != null) {
            stockItemListFromDb.addAll(resultListFromNetwork)
            stockItemListFromDb.distinctBy { it.ticker }.toMutableList()
        } else {
            stockItemListFromDb
        }
    }

    suspend fun loadCandleInfo(ticker: String, from: Long, to: Long): Candles? {
        return try {
            finnHubApi.loadCandleInfo(
                ticker = ticker,
                from = from,
                to = to
            ).await()
        } catch (ex: HttpException) {
            ex.printStackTrace()
            loadCandleInfoException.postValue(Pair(true, getReadableNetworkMessage(ex)))
            null
        } catch (ex: Exception) {
            ex.printStackTrace()
            loadCandleInfoException.postValue(
                Pair(
                    true,
                    App.applicationContext().getString(R.string.common_error)
                )
            )
            null
        }
    }

    suspend fun loadNews(ticker: String, newsPage: Int): List<NewsItem>? {
        return try {
            val fromAndToDate = getFromAndToDateForNews(newsPage)
            val startAndEndOfDate = getStartAndEndOfDayMillis(newsPage)
            if (isNetworkAvailable()) {
                // get today's news always from network
                if (newsPage == 0) {
                    val news = loadNewsFromNetwork(ticker, fromAndToDate)
                    val newsAndRefs = news.asDatabaseModel()
                    database.dao.insertNews(newsAndRefs.first, newsAndRefs.second)
                    return news.asDomainModel()
                } else {
                    val news =
                        loadNewsFromDb(ticker, startAndEndOfDate.first, startAndEndOfDate.second)
                    return if (news.isNotEmpty())
                        news.asDomainModel()
                    else {
                        val newsNetwork = loadNewsFromNetwork(ticker, fromAndToDate)
                        val newsAndRefs = newsNetwork.asDatabaseModel()
                        database.dao.insertNews(newsAndRefs.first, newsAndRefs.second)
                        newsNetwork.asDomainModel()
                    }
                }
            } else return loadNewsFromDb(
                ticker,
                startAndEndOfDate.first,
                startAndEndOfDate.second
            ).asDomainModel()
        } catch (ex: HttpException) {
            ex.printStackTrace()
            loadNewsException.postValue(Pair(true, getReadableNetworkMessage(ex)))
            null
        } catch (ex: Exception) {
            ex.printStackTrace()
            loadNewsException.postValue(
                Pair(
                    true,
                    App.applicationContext().getString(R.string.common_error)
                )
            )
            null
        }
    }

    private suspend fun loadNewsFromDb(ticker: String, from: Long, to: Long) =
        database.dao.getNews(ticker, from, to)

    private suspend fun loadNewsFromNetwork(ticker: String, fromAndToDate: String) =
        finnHubApi.loadNews(
            ticker,
            fromAndToDate,
            fromAndToDate
        ).await()


    fun getNextRecommendationChunks(
        ticker: String,
        offset: Int,
        limit: Int
    ): LiveData<List<RecommendationItem>> {
        return Transformations.map(database.dao.getRecommendations(ticker, offset, limit)) {
            it.asDomainModel()
        }
    }

    suspend fun getRecommendationCount(ticker: String): Int =
        database.dao.getRecommendationsCount(ticker)

    suspend fun updateRecommendations(ticker: String) {
        return try {
            database.dao.insertRecommendations(
                finnHubApi.loadRecommendation(ticker).await().asDatabaseModel()
            )
        } catch (ex: HttpException) {
            ex.printStackTrace()
            updateRecommendationsException.postValue(Pair(true, getReadableNetworkMessage(ex)))
        } catch (ex: Exception) {
            ex.printStackTrace()
            updateRecommendationsException.postValue(
                Pair(
                    true,
                    App.applicationContext().getString(R.string.common_error)
                )
            )
        }
    }


    fun startSocket(): Channel<SocketResponse> =
        webSocketProvider.startSocket()


    fun closeSocket() {
        webSocketProvider.closeSocket()
    }
}

const val limit = 20


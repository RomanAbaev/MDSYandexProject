package com.sample.mdsyandexproject.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.sample.mdsyandexproject.App
import com.sample.mdsyandexproject.R
import com.sample.mdsyandexproject.database.*
import com.sample.mdsyandexproject.domain.*
import com.sample.mdsyandexproject.network.*
import com.sample.mdsyandexproject.utils.*
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentHashMap

object Repository {

    private val database = getDatabase().dao
    private val finnHubApi = FinnHubApi.finnHubService

    private lateinit var webSocketProvider: WebSocketProvider

    private val updateRequestQ = ConcurrentHashMap<String, Int>()

    val loadNextChunksException = MutableLiveData<Pair<Boolean, String>>()
    val submitSearchException = MutableLiveData<Pair<Boolean, String>>()
    val loadCandleInfoException = MutableLiveData<Pair<Boolean, String>>()
    val loadNewsException = MutableLiveData<Pair<Boolean, String>>()
    val updateRecommendationsException = MutableLiveData<Pair<Boolean, String>>()

    private var moshi: Moshi = Moshi.Builder()
        .add(DataJsonAdapter())
        .add(UpdatePricesJsonAdapter())
        .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(UpdatePrices::class.java)

    @ExperimentalCoroutinesApi
    suspend fun openSocketChannel() {
        try {
            startSocket().consumeEach {
                if (it.exception == null) {
                    val updatedPrice = adapter.fromJson(requireNotNull(it.text))
                    val data = updatedPrice?.data
                    if (data != null) {
                        this.database.updatePrices(data.asPrices())
                    }
                } else {
                    onSocketError(it.exception)
                }
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
        return when (showFavouriteList) {
            true ->
                Transformations.map(database.getFavouriteList()) {
                    it.asDomainModel()
                }
            else ->
                Transformations.map(database.getAll()) {
                    it.asDomainModel()
                }
        }
    }

    @ExperimentalCoroutinesApi
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
            loadNextChunksException.postValue(Pair(true, App.applicationContext().getString(R.string.common_error)))
        }
    }

    private suspend fun isPrepopulateNeeded(): Boolean {
        return database.getIndicesCount() == 0 || database.getTotalItemCount() == 0
    }

    private suspend fun loadStockItems() {
        stockItemLoadingProgress.postValue(stockItemLoadingProgress.value?.plus(1))
        val spIndices = database.getNextUnloadedIndices(limit)
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
            database.loadNextChunks(
                stockList = spIndices.map {
                    delay(500)
                    val cp =
                        finnHubApi.getCompanyProfile(it.indices).await()
                    val q =
                        finnHubApi.getQuote(it.indices).await()
                    stockItemLoadingProgress.postValue(stockItemLoadingProgress.value?.plus(1))
                    DatabaseStockItem(
                        ticker = cp.ticker,
                        companyName = cp.name,
                        logoUrl = cp.logo,
                        currency = cp.currency,
                        country = cp.country,
                        exchange = cp.exchange,
                        ipo = cp.ipo,
                        marketCapitalization = cp.marketCapitalization,
                        phone = cp.phone,
                        weburl = cp.weburl,
                        currentPrice = q.currentPrice,
                        currentPriceDate = DateTime.now().millis,
                        previousClosePrice = q.previousClosePrice,
                        previousClosePriceDate = q.timestamp?.times(1000L),
                        errorMessage = q.errorMessage
                    )
                },
                spIndices = spIndices.map {
                    SPIndices(
                        indices = it.indices,
                        isLoaded = true,
                        symbol = it.symbol
                    )
                }
            )
        }
    }

    suspend fun updateQuoteAndCompanyProfile(stockItem: StockItem) {
        if (!updateRequestQ.containsKey(stockItem.ticker)) {
            updateRequestQ[stockItem.ticker] = 0
            delay(1000)
            var q: Quote? = null
            var cp: CompanyProfile? = null;
            if (!isCompanyInfoValid(stockItem)) {
                try {
                    cp = finnHubApi.getCompanyProfile(stockItem.ticker).await()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
            try {
                q = finnHubApi.getQuote(stockItem.ticker).await()
            } catch (ex: HttpException) {
                q = Quote(
                    errorCode = ex.code().toString(),
                    errorMessage = if (ex.code()
                            .toString() != "403"
                    ) ex.message() else "You don't have access to this resource"
                )
                ex.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            when (cp) {
                null -> database.updateQuote(
                    QuoteDb(
                        ticker = stockItem.ticker,
                        currentPrice = q?.currentPrice,
                        currentPriceDate = DateTime.now().millis,
                        previousClosePrice = q?.previousClosePrice,
                        previousClosePriceDate = q?.timestamp?.times(1000L)
                    )
                )
                else -> database.updateQuoteAndCompanyProfile(
                    QuoteAndCompanyProfileDb(
                        ticker = stockItem.ticker,
                        currentPrice = q?.currentPrice,
                        currentPriceDate = DateTime.now().millis,
                        previousClosePrice = q?.previousClosePrice,
                        previousClosePriceDate = q?.timestamp?.times(1000L),
                        error = q?.errorCode,
                        errorMessage = q?.errorMessage,
                        companyName = cp.name,
                        logoUrl = cp.logo,
                        currency = cp.currency,
                        country = cp.country,
                        exchange = cp.exchange,
                        ipo = cp.ipo,
                        marketCapitalization = cp.marketCapitalization,
                        phone = cp.phone,
                        weburl = cp.weburl
                    )
                )
            }
            updateRequestQ.remove(stockItem.ticker)
        }
    }

    suspend fun isExistInDb(stockItem: StockItem) {
        val _stockItem = database.getStockItem(stockItem.ticker)
        if (_stockItem != null) {
            updateFavouriteStock(stockItem)
        } else {
            stockItem.isFavourite = true
            database.updateSPIndices(
                listOf(
                    SPIndices(
                        indices = stockItem.ticker,
                        isLoaded = true,
                    )
                )
            )
            database.insertStockItem(stockItem.asDatabaseModel())
        }
    }

    suspend fun updateFavouriteStock(stockItem: StockItem) {
        database.updateFavouriteStock(stockItem.asFavouriteDatabaseModel())
    }

    suspend fun loadSPIndicesToDB() {
        try {
            val spIndices = finnHubApi.getTop500Indices().await().asDatabaseModel()
            database.insertAllSPIndices(spIndices)
        } catch (socketEx: SocketTimeoutException) {
            socketEx.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    suspend fun submitSearch(query: String): MutableList<StockItem> {
        // get all from database base on this query
        val stockItemListFromDb: MutableList<StockItem> =
            database.search("%$query%").asDomainModel().toMutableList()
        var stockItemListNetwork: SearchResultResponse? = null
        try {
            stockItemListNetwork = finnHubApi.submitSearch(query).await()
        } catch (ex: HttpException) {
            ex.printStackTrace()
            submitSearchException.postValue(Pair(true, getReadableNetworkMessage(ex)))
        } catch (ex: Exception) {
            ex.printStackTrace()
            submitSearchException.postValue(Pair(true, App.applicationContext().getString(R.string.common_error)))
        }

        val resultListFromNetwork: MutableList<StockItem>? = stockItemListNetwork?.result?.map {
            delay(1000)
            var q: Quote? = null
            var cp: CompanyProfile? = null
            try {
                cp = finnHubApi.getCompanyProfile(it.symbol).await()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            try {
                q = finnHubApi.getQuote(it.symbol).await()
            } catch (ex: HttpException) {
                q = Quote(
                    errorCode = ex.code().toString(),
                    errorMessage = if (ex.code()
                            .toString() != "403"
                    ) ex.message() else "You don't have access to this resource"
                )
                ex.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            if (cp != null) {
                StockItem(
                    ticker = it.symbol,
                    companyName = cp.name,
                    logoUrl = cp.logo,
                    currency = cp.currency,
                    currentPrice = q?.currentPrice,
                    currentPriceDate = DateTime.now().millis,
                    previousClosePrice = q?.previousClosePrice,
                    previousClosePriceDate = q?.timestamp?.times(1000L),
                    country = cp.country,
                    exchange = cp.exchange,
                    ipo = cp.ipo,
                    marketCapitalization = cp.marketCapitalization,
                    phone = cp.phone,
                    weburl = cp.weburl,
                    errorMessage = q?.errorMessage
                )
            } else null
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
            loadCandleInfoException.postValue(Pair(true, App.applicationContext().getString(R.string.common_error)))
            null
        }
    }

    suspend fun loadNews(ticker: String, newsPage: Int)
            : List<NewsItem>? {
        return try {
            val fromAndToDate = getFromAndToDateForNews(newsPage)
            val startAndEndOfDate = getStartAndEndOfDayMillis(newsPage)
            when (isNetworkAvailable()) {
                true -> {
                    // get todays news always from network
                    if (newsPage == 0) {
                        val news = loadNewsFromNetwork(ticker, fromAndToDate)
                        val newsAndRefs = news.asDatabaseModel()
                        database.insertNews(newsAndRefs.first, newsAndRefs.second)
                        return news.asDomainModel()
                    } else {
                        val news = loadNewsFromDb(
                            ticker,
                            startAndEndOfDate.first,
                            startAndEndOfDate.second
                        )
                        return if (news.isNotEmpty())
                            news.asDomainModel()
                        else {
                            val newsNetwork = loadNewsFromNetwork(ticker, fromAndToDate)
                            val newsAndRefs = newsNetwork.asDatabaseModel()
                            database.insertNews(newsAndRefs.first, newsAndRefs.second)
                            newsNetwork.asDomainModel()
                        }
                    }
                }
                false -> {
                    val n = loadNewsFromDb(ticker, startAndEndOfDate.first, startAndEndOfDate.second).asDomainModel()
                    return n
                }
            }
        } catch (ex: HttpException) {
            ex.printStackTrace()
            loadNewsException.postValue(Pair(true, getReadableNetworkMessage(ex)))
            null
        } catch (ex: Exception) {
            ex.printStackTrace()
            loadNewsException.postValue(Pair(true, App.applicationContext().getString(R.string.common_error)))
            null
        }
    }

    suspend fun loadNewsFromDb(ticker: String, from: Long, to: Long) =
        database.getNews(ticker, from, to)

    suspend fun loadNewsFromNetwork(ticker: String, fromAndToDate: String): List<NewsDto> =
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
        return Transformations.map(database.getRecommendations(ticker, offset, limit)) {
            it.asDomainModel()
        }
    }

    suspend fun getRecommendationCount(ticker: String): Int =
        database.getRecommendationsCount(ticker)

    suspend fun updateRecommendations(ticker: String) {
        return try {
            database.insertRecommendations(
                finnHubApi.loadRecommendation(ticker = ticker).await().asDatabaseModel()
            )
        } catch (ex: HttpException) {
            ex.printStackTrace()
            updateRecommendationsException.postValue(Pair(true, getReadableNetworkMessage(ex)))
        } catch (ex: Exception) {
            ex.printStackTrace()
            updateRecommendationsException.postValue(Pair(true, App.applicationContext().getString(R.string.common_error)))
        }
    }

    @ExperimentalCoroutinesApi
    fun startSocket(): Channel<SocketResponse> =
        webSocketProvider.startSocket()

    @ExperimentalCoroutinesApi
    fun closeSocket() {
        webSocketProvider.closeSocket()
    }
}

const val limit = 20


package com.sample.mdsyandexproject.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.sample.mdsyandexproject.database.*
import com.sample.mdsyandexproject.domain.StockItem
import com.sample.mdsyandexproject.domain.asFavouriteDatabaseModel
import com.sample.mdsyandexproject.network.*
import com.sample.mdsyandexproject.utils.isCompanyInfoValid
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

class RepositoryImpl {

    private val database = getDatabase().dao

    //    private val marketStackApi = MarketStackApi.marketStackService
    private val finnHubApi = FinnHubApi.finnHubService

    private lateinit var webSocketProvider: WebSocketProvider

    private val updateRequestQ = ConcurrentHashMap<String, Int>()

    private val networkException = MutableLiveData<Boolean>(false)
    private var interruptedNetworkCall = null

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
                    it.asStockItemDomainModel()
                }
            else ->
                Transformations.map(database.getAll()) {
                    it.asStockItemDomainModel()
                }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun refreshData() {
        webSocketProvider = WebSocketProvider()
        openSocketChannel()
    }

    suspend fun loadNextChunks() {
        val spIndices = database.getNextUnloadedIndices(limit)
        if (spIndices.isNotEmpty()) {
            loadCompanyInfoAndQuote(spIndices)
        }
    }

    private suspend fun loadCompanyInfoAndQuote(spIndices: List<SPIndices>) {
        withContext(Dispatchers.IO) {
            database.loadNextChunks(
                stockList = spIndices.map {
                    delay(1000)
                    val cp =
                        finnHubApi.getCompanyProfile2(it.indices).await()
                    val q =
                        finnHubApi.getQuote(it.indices).await()
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
            var cp: CompanyProfile2? = null;
            if (!isCompanyInfoValid(stockItem)) {
                try {
                    cp = finnHubApi.getCompanyProfile2(stockItem.ticker).await()
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

    suspend fun updateFavouriteStock(stockItem: StockItem) {
        database.updateFavouriteStock(stockItem.asFavouriteDatabaseModel())
    }

    suspend fun loadSPIndicesToDB() {
        try {
            val spIndices = finnHubApi.getTop500Indices().await().asDatabaseModel()
            database.insertAllSPIndices(spIndices)
        } catch (timeout: SocketTimeoutException) {
            networkException.postValue(true)
//            interruptedNetworkCall = this::loadSPIndicesToDB
        }
    }

    suspend fun submitSearch(query: String): MutableList<StockItem> {
        // TODO to aggregate data from different sources (db and network)
        val stockItemList =
            finnHubApi.submitSearch(query).await()
        return stockItemList.result.map {
            delay(1000)
            var q: Quote? = null
            var cp: CompanyProfile2? = null
            try {
                cp = finnHubApi.getCompanyProfile2(it.symbol).await()
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
                    errorMessage = q?.errorMessage
                )
            } else null
        }.filterNotNull().toMutableList()
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

object Repository {
    val instance: RepositoryImpl by lazy { RepositoryImpl() }
}


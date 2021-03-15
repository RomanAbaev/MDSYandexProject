package com.sample.mdsyandexproject.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.sample.mdsyandexproject.database.*
import com.sample.mdsyandexproject.domain.StockItem
import com.sample.mdsyandexproject.domain.asFavouriteDatabaseModel
import com.sample.mdsyandexproject.network.*
import com.sample.mdsyandexproject.utils.EST
import com.sample.mdsyandexproject.utils.parseISO8601Date
import com.squareup.moshi.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import org.joda.time.DateTime
import retrofit2.HttpException
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class RepositoryImpl {

    private val stockDatabase = getDatabase().stockItemDao
    private val marketStackApi = MarketStackApi.marketStackService
    private val finnHubApi = FinnHubApi.finnHubService

    private lateinit var webSocketProvider: WebSocketProvider

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
                    data?.let {
                        val d = it.asPrices()[1].currentPriceDate
                        Log.i(
                            "WebSocket", "${it.asPrices()[1].currentPriceDate} " +
                                    "timestamp = ${it.asPrices()[1].currentPriceDate}, " +
                                    "datetime = ${DateTime(it.asPrices()[1].currentPriceDate)}, " +
                                    "localdate = ${DateTime(it.asPrices()[1].currentPriceDate)}, " +
                                    "date(java) = ${Date(requireNotNull(it.asPrices()[1].currentPriceDate))}"
                        )
                    }
                    if (data != null) {
                        stockDatabase.updatePrices(data.asPrices())
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
                Transformations.map(stockDatabase.getFavouriteList()) {
                    it.asStockItemDomainModel()
                }
            else ->
                Transformations.map(stockDatabase.getAll()) {
                    it.asStockItemDomainModel()
                }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun refreshData() {
        webSocketProvider = WebSocketProvider()
        openSocketChannel()
    }


    suspend fun loadNextChunks(limit: Int, offset: Int) {
        val list =
            marketStackApi.getNextPage(limit, offset).await()
                .asStockItemDatabaseModel().toMutableList()
        getEodPrices(list)
        loadCompanyInfo(list)
        stockDatabase.insertAll(list)
    }

    suspend fun updateInfo(databaseStockItem: DatabaseStockItem) {
        stockDatabase.updateInfo(databaseStockItem)
    }

    suspend fun updateFavouriteStock(stockItem: StockItem) {
        stockDatabase.updateFavouriteStock(stockItem.asFavouriteDatabaseModel())
    }

    suspend fun getEodPrices(databaseStockItem: DatabaseStockItem) {
        try {

            // TODO посмотреть и если ничего не починиться, то отказаться от пагинации и данного api
            val eodPrices =
                marketStackApi.getEodPrices(databaseStockItem.ticker, 2).await()
            Log.i(
                "ConcurrentHashMap", "" +
                        "-----------------------------------------------" +
                        "eodDatePrices for ${databaseStockItem.ticker} \n" +
                        "${eodPrices} \n" +
                        "-----------------------------------------------"
            )

            for ((index, price) in eodPrices.data.withIndex()) {
                val millis = parseISO8601Date(price.date)
                if (index == 0) {
                    databaseStockItem.eod = price.close
                    databaseStockItem.eodDate = millis
                    if (databaseStockItem.currentPrice == null) {
                        databaseStockItem.currentPrice = price.close
                        databaseStockItem.currentPriceDate = millis
                    }
                } else {
                    databaseStockItem.previousEod = price.close
                    databaseStockItem.previousEodDate = millis
                }
            }
        } catch (ex: HttpException) {
            ex.printStackTrace()
        }
    }

    suspend fun getEodPrices(list: MutableList<DatabaseStockItem>) {
        val map = mutableMapOf<String, DatabaseStockItem>()
        val tickers = StringBuilder()
        for ((index, databaseStockItem) in list.withIndex()) {
            tickers.append(databaseStockItem.ticker)
            if (index != list.size - 1) tickers.append(",")
            map[databaseStockItem.ticker] = databaseStockItem
        }
        val eodPrices =
            marketStackApi.getEodPrices(tickers.toString(), limit * 2).await()

        for ((index, price) in eodPrices.data.withIndex()) {
            val millis = parseISO8601Date(price.date)
            if (index < list.size) {
                map[price.ticker]?.eod = price.close
                map[price.ticker]?.eodDate = millis
                if (map[price.ticker]?.currentPrice == null) {
                    map[price.ticker]?.currentPrice = price.close
                    map[price.ticker]?.currentPriceDate = millis
                }
            } else {
                map[price.ticker]?.previousEod = price.close
                map[price.ticker]?.previousEodDate = millis
            }
        }
    }

    suspend fun loadCompanyInfo(list: MutableList<DatabaseStockItem>) {
        list.forEach {
            loadCompanyInfo(it)
        }
    }

    suspend fun loadCompanyInfo(databaseStockItem: DatabaseStockItem) {
        try {
            delay(500)
            val result =
                finnHubApi.getCompanyProfile2(databaseStockItem.ticker).await()
            databaseStockItem.logoUrl = result.logo
            databaseStockItem.country = result.country
            databaseStockItem.exchange = result.exchange
            databaseStockItem.ipo = result.ipo
            databaseStockItem.currency = result.currency
            databaseStockItem.marketCapitalization = result.marketCapitalization
            databaseStockItem.phone = result.phone
            databaseStockItem.weburl = result.weburl
        } catch (ex: Exception) {
            println("${databaseStockItem.ticker} failed to load company profile information")
            ex.printStackTrace()
        }
    }

    suspend fun submitSearch(query: String): MutableList<StockItem> {

        // TODO to aggregate data from different sources (db and network)

        val stockItemList =
            finnHubApi.submitSearch(query).await().asStockItemDatabaseModel().toMutableList()
        loadCompanyInfo(stockItemList)
        // get prices
        stockItemList.forEach {
            try {
                delay(500)
                val quote = finnHubApi.getQuote(it.ticker).await()
                it.eod = quote.previousClosePrice
                it.previousEod = quote.previousClosePrice
                it.eodDate = DateTime(quote.timestamp?.times(1000L)).withZone(EST).millis
                it.previousEodDate = DateTime(quote.timestamp?.times(1000L)).withZone(EST).millis
                it.currentPrice = quote.currentPrice
                it.currentPriceDate = DateTime.now().withZone(EST).millis
                it.errorMessage = quote.errorMessage
            } catch (ex: HttpException) {
                // TODO добавить в модель данные об ошибке и если она 403, то не перезаправшивать их
                it.error = ex.code().toString()
                if (it.error == "403") it.errorMessage = "You don't have access to this resource"
                else it.error = ex.message
                ex.printStackTrace()
            }
        }
        return stockItemList.asStockItemDomainModel().toMutableList()
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


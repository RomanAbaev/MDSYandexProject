package com.sample.mdsyandexproject.database

import androidx.room.*
import com.sample.mdsyandexproject.domain.NewsItem
import com.sample.mdsyandexproject.domain.RecommendationItem
import com.sample.mdsyandexproject.domain.StockItem
import com.sample.mdsyandexproject.network.CompanyProfile
import com.sample.mdsyandexproject.network.Quote
import org.joda.time.DateTime
import java.util.*

@Entity(indices = [Index(value = ["ticker"])])
data class DatabaseStockItem(
    @PrimaryKey
    val ticker: String,
    val companyName: String,
    var logoUrl: String? = null,
    val isFavourite: Boolean = false,
    var currentPrice: Float? = null,
    var currentPriceDate: Long? = null,
    var previousEodDate: Long? = null,
    var previousEod: Float? = null,
    var eod: Float? = null,
    var eodDate: Long? = null,
    val dayDelta: Double? = null,
    val symbol: String? = null,
    var currency: String? = null,
    var country: String? = null,
    var exchange: String? = null,
    var ipo: String? = null,
    var marketCapitalization: String? = null,
    var phone: String? = null,
    var weburl: String? = null,
    var error: String? = null,
    var errorMessage: String? = null,
    var previousClosePrice: Float? = null,
    var previousClosePriceDate: Long? = null
)

@Entity
data class SPIndices(
    @PrimaryKey
    val indices: String,
    val isLoaded: Boolean,
    val symbol: String = "^GSPC"
)

@Entity
data class News(
    @PrimaryKey
    val newsId: Long,
    val ticker: String,
    val datetime: Long,
    val headline: String,
    val image: String,
    val source: String,
    val summary: String,
    val url: String
)

@Entity(primaryKeys = ["ticker", "newsId"])
data class StockNewsCrossRef(
    val ticker: String,
    val newsId: Long
)

data class StockWithNews(
    @Embedded val stockItem: DatabaseStockItem,
    @Relation(
        parentColumn = "ticker",
        entityColumn = "newsId",
        associateBy = Junction(StockNewsCrossRef::class)
    )
    val news: List<News>
)

@Entity(primaryKeys = ["ticker", "period"])
data class Recommendation(
    val ticker: String,
    val buy: Int,
    val strongBuy: Int,
    val hold: Int,
    val sell: Int,
    val strongSell: Int,
    val period: Long
)

data class QuoteAndCompanyProfileDb(
    val ticker: String,
    val companyName: String,
    val currentPrice: Float? = null,
    val currentPriceDate: Long? = null,
    val previousClosePrice: Float? = null,
    val previousClosePriceDate: Long? = null,
    var error: String? = null,
    var errorMessage: String? = null,
    val logoUrl: String? = null,
    val currency: String? = null,
    val country: String? = null,
    val exchange: String? = null,
    val ipo: String? = null,
    val marketCapitalization: String? = null,
    val phone: String? = null,
    val weburl: String? = null
)

data class CompanyProfileDb(
    val ticker: String,
    val companyName: String,
    val logoUrl: String? = null,
    val currency: String? = null,
    val country: String? = null,
    val exchange: String? = null,
    val ipo: String? = null,
    val marketCapitalization: String? = null,
    val phone: String? = null,
    val weburl: String? = null
)

data class QuoteDb(
    val ticker: String,
    val currentPrice: Float?,
    val currentPriceDate: Long?,
    val previousClosePrice: Float?,
    val previousClosePriceDate: Long?,
    var error: String? = null,
    var errorMessage: String? = null
)

data class Prices(
    val ticker: String,
    val currentPrice: Float,
    val currentPriceDate: Long,
)

data class FavouriteDatabaseModel(
    val ticker: String,
    val isFavourite: Boolean
)

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

fun List<DatabaseStockItem>.asDomainModel() =
    map {
        StockItem(
            ticker = it.ticker,
            companyName = it.companyName,
            logoUrl = it.logoUrl,
            _isFavourite = it.isFavourite,
            currentPrice = it.currentPrice,
            currentPriceDate = it.currentPriceDate,
            previousClosePrice = it.previousClosePrice,
            previousClosePriceDate = it.previousClosePriceDate,
            currency = it.currency,
            country = it.country,
            exchange = it.exchange,
            ipo = it.ipo,
            marketCapitalization = it.marketCapitalization,
            phone = it.phone,
            weburl = it.weburl,
            error = it.error,
            errorMessage = it.errorMessage,
        )
    }

@JvmName("asDomainModelNews")
fun List<News>.asDomainModel() =
    map {
        NewsItem(
            id = it.newsId,
            logo = it.image,
            headline = it.headline,
            source = it.source,
            datetime = it.datetime,
            url = it.url,
            summary = it.summary
        )
    }

@JvmName("asDomainModelRecommendation")
fun List<Recommendation>.asDomainModel() =
    map {
        RecommendationItem(
            ticker = it.ticker,
            buy = it.buy,
            strongBuy = it.strongBuy,
            hold = it.hold,
            sell = it.sell,
            strongSell = it.strongSell,
            period = it.period
        )
    }

fun createDatabaseStockItem(companyProfile: CompanyProfile, quote: Quote) =
    DatabaseStockItem(
        ticker = companyProfile.ticker,
        companyName = companyProfile.name,
        logoUrl = companyProfile.logo,
        currency = companyProfile.currency,
        country = companyProfile.country,
        exchange = companyProfile.exchange,
        ipo = companyProfile.ipo,
        marketCapitalization = companyProfile.marketCapitalization,
        phone = companyProfile.phone,
        weburl = companyProfile.weburl,
        currentPrice = quote.currentPrice,
        currentPriceDate = DateTime.now().millis,
        previousClosePrice = quote.previousClosePrice,
        previousClosePriceDate = quote.timestamp?.times(1000L),
        errorMessage = quote.errorMessage
    )

fun createQuoteDb(stockItem: StockItem, quote: Quote?) =
    QuoteDb(
        ticker = stockItem.ticker,
        currentPrice = quote?.currentPrice,
        currentPriceDate = DateTime.now().millis,
        previousClosePrice = quote?.previousClosePrice,
        previousClosePriceDate = quote?.timestamp?.times(1000L)
    )

fun createQuoteAndCompanyProfileDb(stockItem: StockItem, quote: Quote?, companyProfile: CompanyProfile) =
    QuoteAndCompanyProfileDb(
        ticker = stockItem.ticker,
        currentPrice = quote?.currentPrice,
        currentPriceDate = DateTime.now().millis,
        previousClosePrice = quote?.previousClosePrice,
        previousClosePriceDate = quote?.timestamp?.times(1000L),
        error = quote?.errorCode,
        errorMessage = quote?.errorMessage,
        companyName = companyProfile.name,
        logoUrl = companyProfile.logo,
        currency = companyProfile.currency,
        country = companyProfile.country,
        exchange = companyProfile.exchange,
        ipo = companyProfile.ipo,
        marketCapitalization = companyProfile.marketCapitalization,
        phone = companyProfile.phone,
        weburl = companyProfile.weburl
    )

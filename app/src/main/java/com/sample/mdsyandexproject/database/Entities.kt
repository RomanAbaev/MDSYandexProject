package com.sample.mdsyandexproject.database

import androidx.room.*
import com.sample.mdsyandexproject.domain.StockItem
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
    var errorMessage: String? = null
)

data class EodAndCompanyProfile(
    var currentPrice: Float? = null,
    var currentPriceDate: Long? = null,
    var previousEodDate: Long? = null,
    var previousEod: Float? = null,
    var eod: Float? = null,
    var eodDate: Long? = null,
)

data class Prices(
    val ticker: String,
    val currentPrice: Float,
    val currentPriceDate: Long
)

data class Ticker(
    val ticker: String,
    val isFavourite: Boolean
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

fun DatabaseStockItem.asStockItemDomainModel(): StockItem {
    return StockItem(
        ticker = this.ticker,
        companyName = this.companyName,
        logoUrl = this.logoUrl,
        _isFavourite = this.isFavourite,
        currentPrice = this.currentPrice,
        currentPriceDate = this.currentPriceDate,
        previousEodDate = this.previousEodDate,
        previousEod = this.previousEod,
        eod = this.eod,
        eodDate = this.eodDate,
        dayDelta = this.dayDelta,
        currency = this.currency,
        error = this.error,
        errorMessage = this.errorMessage,
    )
}

fun List<Ticker>.asSortedTickers(): List<String> {
    return this.sortedWith(compareBy { it.isFavourite }).map {
        it.ticker
    }
}

fun List<DatabaseStockItem>.asStockItemDomainModel(): List<StockItem> {
    return map {
        StockItem(
            ticker = it.ticker,
            companyName = it.companyName,
            logoUrl = it.logoUrl,
            _isFavourite = it.isFavourite,
            currentPrice = it.currentPrice,
            currentPriceDate = it.currentPriceDate,
            previousEodDate = it.previousEodDate,
            previousEod = it.previousEod,
            eod = it.eod,
            eodDate = it.eodDate,
            dayDelta = it.dayDelta,
            currency = it.currency,
            error = it.error,
            errorMessage = it.errorMessage,
        )
    }
}

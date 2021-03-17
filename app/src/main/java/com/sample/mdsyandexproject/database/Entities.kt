package com.sample.mdsyandexproject.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
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
    var errorMessage: String? = null,
    var previousClosePrice: Float? = null,
    var previousClosePriceDate: Long? = null
)

@Entity
data class SPIndices(
    @PrimaryKey
    val indices: String,
    val isLoaded: Boolean,
    val symbol: String
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

data class CompanyProfile2Db(
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

fun List<DatabaseStockItem>.asStockItemDomainModel(): List<StockItem> {
    return map {
        StockItem(
            ticker = it.ticker,
            companyName = it.companyName,
            logoUrl = it.logoUrl,
            _isFavourite = it.isFavourite,
            currentPrice = it.currentPrice,
            currentPriceDate = it.currentPriceDate,
            previousClosePrice = it.previousClosePrice,
            previousClosePriceDate = it.previousClosePriceDate,
            dayDelta = it.dayDelta,
            currency = it.currency,
            error = it.error,
            errorMessage = it.errorMessage,
        )
    }
}

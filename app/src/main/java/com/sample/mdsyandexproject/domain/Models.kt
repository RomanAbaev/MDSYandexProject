package com.sample.mdsyandexproject.domain

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.sample.mdsyandexproject.database.DatabaseStockItem
import com.sample.mdsyandexproject.database.FavouriteDatabaseModel
import java.io.Serializable

data class StockItem(
    val ticker: String,
    val companyName: String,
    val logoUrl: String? = null,
    var _isFavourite: Boolean = false,
    var currentPrice: Float? = null,
    var currentPriceDate: Long? = null,
    val previousClosePrice: Float? = null,
    val previousClosePriceDate: Long? = null,
    val currency: String? = null,
    val country: String? = null,
    val exchange: String? = null,
    val ipo: String? = null,
    val marketCapitalization: String? = null,
    val phone: String? = null,
    val weburl: String? = null,
    var error: String? = null,
    var errorMessage: String? = null,
) : BaseObservable(), Serializable {
    var isFavourite: Boolean
        @Bindable get() = _isFavourite
        set(value) {
            _isFavourite = value
            notifyChange()
        }
}

data class NewsItem(
    val id: Long,
    val logo: String,
    val headline: String,
    val source: String,
    val datetime: Long,
    val url: String,
    val summary: String
)

fun StockItem.asFavouriteDatabaseModel(): FavouriteDatabaseModel {
    return FavouriteDatabaseModel(
        ticker = this.ticker,
        isFavourite = this.isFavourite
    )
}

fun StockItem.asDatabaseModel(): DatabaseStockItem {
    return DatabaseStockItem(
        ticker = this.ticker,
        companyName = this.companyName,
        logoUrl = this.logoUrl,
        isFavourite = this._isFavourite,
        currentPrice = this.currentPrice,
        currentPriceDate = this.currentPriceDate,
        previousClosePriceDate = this.previousClosePriceDate,
        previousClosePrice = this.previousClosePrice,
        currency = this.currency,
        country = this.country,
        exchange = this.exchange,
        ipo = this.ipo,
        marketCapitalization = this.marketCapitalization,
        phone = this.phone,
        weburl = this.weburl,
    )
}
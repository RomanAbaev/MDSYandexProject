package com.sample.mdsyandexproject.domain

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.sample.mdsyandexproject.database.DatabaseStockItem
import com.sample.mdsyandexproject.database.FavouriteDatabaseModel
import com.sample.mdsyandexproject.network.CompanyProfile
import com.sample.mdsyandexproject.network.Quote
import org.joda.time.DateTime
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

data class RecommendationItem(
    val ticker: String,
    val buy: Int,
    val strongBuy: Int,
    val hold: Int,
    val sell: Int,
    val strongSell: Int,
    val period: Long
)

fun StockItem.asFavouriteDatabaseModel() =
    FavouriteDatabaseModel(
        ticker = this.ticker,
        isFavourite = this.isFavourite
    )

fun StockItem.asDatabaseModel() =
    DatabaseStockItem(
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

fun createStockItem(ticker: String, companyProfile: CompanyProfile, quote: Quote?) =
    StockItem(
        ticker = ticker,
        companyName = companyProfile.name,
        logoUrl = companyProfile.logo,
        currency = companyProfile.currency,
        currentPrice = quote?.currentPrice,
        currentPriceDate = DateTime.now().millis,
        previousClosePrice = quote?.previousClosePrice,
        previousClosePriceDate = quote?.timestamp?.times(1000L),
        country = companyProfile.country,
        exchange = companyProfile.exchange,
        ipo = companyProfile.ipo,
        marketCapitalization = companyProfile.marketCapitalization,
        phone = companyProfile.phone,
        weburl = companyProfile.weburl,
        errorMessage = quote?.errorMessage
    )
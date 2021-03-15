package com.sample.mdsyandexproject.domain

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.sample.mdsyandexproject.database.DatabaseStockItem
import com.sample.mdsyandexproject.database.FavouriteDatabaseModel


// TODO: Подумать о том какой тип должен быть у CurrentPrice и DayDelta

data class StockItem(
    val ticker: String,
    val companyName: String,
    val logoUrl: String? = null,
    var _isFavourite: Boolean = false,
    val currentPrice: Float? = null,
    val currentPriceDate: Long? = null,
    val previousEodDate: Long? = null,
    val previousEod: Float? = null,
    val eod: Float? = null,
    val eodDate: Long? = null,
    val currency: String? = null,
    val dayDelta: Double? = null,
    val error: String? = null,
    val errorMessage: String? = null
) : BaseObservable() {
    var isFavourite: Boolean
        @Bindable get() = _isFavourite
        set(value) {
            _isFavourite = value
            notifyChange()
        }
}

fun List<StockItem>.asDatabaseModel(): List<DatabaseStockItem> {
    return map {
        DatabaseStockItem(
            ticker = it.ticker,
            companyName = it.companyName,
            logoUrl = it.logoUrl,
            isFavourite = it._isFavourite,
            currentPrice = it.currentPrice,
            currentPriceDate = it.currentPriceDate,
            previousEodDate = it.previousEodDate,
            previousEod = it.previousEod,
            eod = it.eod,
            eodDate = it.eodDate,
            dayDelta = it.dayDelta
        )
    }
}

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
        previousEodDate = this.previousEodDate,
        previousEod = this.previousEod,
        eod = this.eod,
        eodDate = this.eodDate,
        dayDelta = this.dayDelta
    )
}

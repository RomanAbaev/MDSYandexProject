package com.sample.mdsyandexproject.network

import com.sample.mdsyandexproject.database.Prices
import com.sample.mdsyandexproject.database.SPIndices
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.ToJson

data class CompanyProfile2(
    val country: String,
    val currency: String,
    val exchange: String,
    val ipo: String,
    val marketCapitalization: String,
    val name: String,
    val phone: String,
    val ticker: String,
    val weburl: String,
    val logo: String,
    @Json(name = "finnhubIndustry") val finnHubIndustry: String,
)

data class SearchResultResponse(
    val count: Int,
    val result: List<SearchResult>
)

data class SearchResult(
    val description: String,
    val displaySymbol: String,
    val symbol: String,
    val type: String
)

data class IndicesList(
    @Json(name = "constituents")
    val indicesList: List<String>,
    val symbol: String
)

data class UpdatePrices(
    val type: String? = null,
    val data: List<Data>? = null,
)

data class Data(
    @Json(name = "p") val price: Float? = null,
    @Json(name = "s") val ticker: String? = null,
    @Json(name = "t") val timestamp: Long? = null,
    @Json(name = "v") val volume: Double? = null,
    @Json(name = "c") val listOfTradesConditions: List<String>? = null
)

data class Quote(
    @Json(name = "c") val currentPrice: Float? = null,
    @Json(name = "h") val highPriceOfTheDay: Float? = null,
    @Json(name = "l") val lowPriceOfTheDay: Float? = null,
    @Json(name = "o") val openPrice: Float? = null,
    @Json(name = "pc") val previousClosePrice: Float? = null,
    @Json(name = "t") val timestamp: Long? = null,
    @Json(name = "error") val errorMessage: String? = null,
    val errorCode: String? = null
)

data class Candles(
    @Json(name = "o") val openPrices: List<Float>,
    @Json(name = "h") val highPrices: List<Float>,
    @Json(name = "l") val lowPrices: List<Float>,
    @Json(name = "c") val closePrices: List<Float>,
    @Json(name = "v") val volume: List<Float>,
    @Json(name = "t") val timestamp: List<Long>,
    @Json(name = "s") val status: String
)

data class DataJson(
    val p: Float?,
    val s: String?,
    val t: Long?,
    val v: Double?,
    val c: List<String>?
)

class DataJsonAdapter() {
    @ToJson
    fun toJson(data: Data): String {
        return ""
    }

    @FromJson
    fun dataFromJson(dataJson: DataJson): Data {
        return Data(
            price = dataJson.p,
            ticker = dataJson.s,
            timestamp = dataJson.t,
            volume = dataJson.v,
            listOfTradesConditions = dataJson.c
        )
    }
}

data class UpdatePricesJson(
    val type: String?,
    val data: List<Data>?
)

class UpdatePricesJsonAdapter() {

    @ToJson
    fun toJson(updatePrices: UpdatePrices): String {
        return ""
    }

    @FromJson
    fun updatePricesFromJson(updatePricesJson: UpdatePricesJson): UpdatePrices {
        return UpdatePrices(
            type = updatePricesJson.type,
            data = updatePricesJson.data
        )
    }
}

fun List<Data>.asPrices(): List<Prices> {
    return map {
        Prices(
            ticker = requireNotNull(it.ticker),
            currentPrice = requireNotNull(it.price),
            currentPriceDate = requireNotNull(it.timestamp)
        )
    }
}

fun IndicesList.asDatabaseModel(): List<SPIndices> {
    return this.indicesList.map {
        SPIndices(
            indices = it,
            symbol = this.symbol,
            isLoaded = false
        )
    }
}


package com.sample.mdsyandexproject.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "http://api.marketstack.com/v1/"
private const val API_KEY = "9587f2f5d4a20bab61b08350a08823d3"

interface MarketStackService {
    @GET("tickers")
    fun getNextPage(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("access_key") access_key: String = API_KEY
    ): Deferred<MarketStackTickersResponse>

    @GET("eod")
    fun getEodPrices(
        @Query("symbols") tickers: String,
        @Query("limit") limit: Int,
        @Query("access_key") access_key: String = API_KEY,
    ): Deferred<MarketStackEodPrices>
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val okHttpClient = OkHttpClient().newBuilder()
    .addInterceptor(Interceptor { chain ->
        val request = chain.request()
        val url = request.url.newBuilder().addQueryParameter("access_key", API_KEY).build()
        request.newBuilder().url(url).build()
        chain.proceed(request)
    })
    .addInterceptor(HttpLoggingInterceptor().apply {
        this.level = HttpLoggingInterceptor.Level.BASIC
    })
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .client(okHttpClient)
    .baseUrl(BASE_URL)
    .build()

object MarketStackApi {
    val marketStackService: MarketStackService by lazy { retrofit.create(MarketStackService::class.java) }
}
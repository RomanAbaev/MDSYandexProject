package com.sample.mdsyandexproject.network

import android.util.Log
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

private const val BASE_URL = "https://finnhub.io/api/v1/"
private const val API_KEY = "c0t7r1748v6r4maem760"

interface FinnHubService {
    @GET("index/constituents?symbol=^GSPC&token=c0t7r1748v6r4maem760")
    fun getTop500Indices(): Deferred<IndicesList>

    @GET("search?token=c0t7r1748v6r4maem760")
    fun submitSearch(
        @Query("q") query: String
    ): Deferred<SearchResultResponse>

    @GET("quote?token=c0t7r1748v6r4maem760")
    fun getQuote(@Query("symbol") ticker: String): Deferred<Quote>

    @GET("stock/profile2")
    fun getCompanyProfile2(
        @Query("symbol") ticker: String,
        @Query("token") token: String = "c0t7r1748v6r4maem760"
    ): Deferred<CompanyProfile2>

    @GET("stock/candle")
    fun loadCandleInfo(
        @Query("symbol") ticker: String,
        @Query("resolution") resolution: String = "1",
        @Query("from") from: Long,
        @Query("to") to: Long,
        @Query("token") token: String = "c0t7r1748v6r4maem760"
    ): Deferred<Candles>

    /**
     * @param from date YYYY-MM-DD
     * @param to date YYYY-MM-DD
     */
    @GET("company-news")
    fun loadNews(
        @Query("symbol") ticker: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("token") token: String = "c0t7r1748v6r4maem760"
    ): Deferred<List<NewsDto>>

    @GET("stock/recommendation?")
    fun loadRecommendation(
        @Query("symbol") ticker: String,
        @Query("token") token: String = "c0t7r1748v6r4maem760"
    ) : Deferred<List<RecommendationDto>>
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val okHttpClient = OkHttpClient().newBuilder()
//    .addInterceptor(Interceptor { chain ->
//        val request = chain.request()
//        val url = request.url.newBuilder().addQueryParameter("token", API_KEY).build()
//        request.newBuilder().url(url).build()
//        chain.proceed(request)
//    })
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

object FinnHubApi {
    val finnHubService: FinnHubService by lazy { retrofit.create(FinnHubService::class.java) }

}
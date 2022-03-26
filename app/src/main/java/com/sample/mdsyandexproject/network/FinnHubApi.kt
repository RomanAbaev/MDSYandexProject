package com.sample.mdsyandexproject.network

import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query


interface FinnHubService {
    @GET("index/constituents?symbol=^GSPC")
    fun getTop500Indices(): Deferred<IndicesList>

    @GET("search")
    fun submitSearch(
        @Query("q") query: String
    ): Deferred<SearchResultResponse>

    @GET("quote?")
    fun getQuote(@Query("symbol") ticker: String): Deferred<Quote>

    @GET("stock/profile2")
    fun getCompanyProfile(
        @Query("symbol") ticker: String,
    ): Deferred<CompanyProfile>

    @GET("stock/candle")
    fun loadCandleInfo(
        @Query("symbol") ticker: String,
        @Query("resolution") resolution: String = "1",
        @Query("from") from: Long,
        @Query("to") to: Long,
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
    ): Deferred<List<NewsDto>>

    @GET("stock/recommendation?")
    fun loadRecommendation(
        @Query("symbol") ticker: String,
    ): Deferred<List<RecommendationDto>>
}
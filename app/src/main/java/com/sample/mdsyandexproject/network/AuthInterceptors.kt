package com.sample.mdsyandexproject.network

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject




class AuthInterceptors @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val originalHttpUrl = originalRequest.url
        val url: HttpUrl = originalHttpUrl.newBuilder()
            .addQueryParameter(TOKEN, FINNHUB_API_KEY)
            .build()

        val requestBuilder: Request.Builder = originalRequest
            .newBuilder()
            .url(url)

        val request = requestBuilder.build()

        return chain.proceed(request)
    }

    companion object {
        const val TOKEN = "token"
        const val FINNHUB_API_KEY = "c0t7r1748v6r4maem760"
    }
}
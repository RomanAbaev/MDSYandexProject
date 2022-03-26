package com.sample.mdsyandexproject.di

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.sample.mdsyandexproject.network.AuthInterceptors
import com.sample.mdsyandexproject.network.DataJsonAdapter
import com.sample.mdsyandexproject.network.FinnHubService
import com.sample.mdsyandexproject.network.UpdatePricesJsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
interface NetworkModule {

    companion object {

        private const val BASE_URL = "https://finnhub.io/api/v1/"

        @AppScope
        @Provides
        @DefaultMoshi
        fun provideDefaultMoshi(): Moshi {
            return Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
        }

        @AppScope
        @Provides
        fun provideKotlinJsonAdapterFactory() : KotlinJsonAdapterFactory {
            return KotlinJsonAdapterFactory()
        }

        @AppScope
        @Provides
        fun provideMoshi(
            dataJsonAdapter: DataJsonAdapter,
            updatePricesJsonAdapter: UpdatePricesJsonAdapter,
            kotlinJsonAdapterFactory: KotlinJsonAdapterFactory
        ): Moshi {
            return Moshi.Builder()
                .add(dataJsonAdapter)
                .add(updatePricesJsonAdapter)
                .add(kotlinJsonAdapterFactory)
                .build()
        }

        @AppScope
        @Provides
        fun provideMoshiConverterFactory(
            @DefaultMoshi moshi: Moshi
        ): MoshiConverterFactory {
            return MoshiConverterFactory.create(moshi)
        }

        @AppScope
        @Provides
        fun provideCoroutineCallAdapterFactory(): CoroutineCallAdapterFactory {
            return CoroutineCallAdapterFactory()
        }

        @AppScope
        @Provides
        fun loggingInterceptor(): HttpLoggingInterceptor {
            return HttpLoggingInterceptor().apply {
                this.level = HttpLoggingInterceptor.Level.BASIC
            }
        }

        @AppScope
        @Provides
        fun provideOkHttpClient(
            authInterceptors: AuthInterceptors,
            loggingInterceptors: HttpLoggingInterceptor
        ): OkHttpClient {
            return OkHttpClient().newBuilder()
                .addInterceptor(loggingInterceptors)
                .addInterceptor(authInterceptors)
                .build()
        }

        @AppScope
        @Provides
        fun provideRetrofit(
            moshiConverterFactory: MoshiConverterFactory,
            coroutineCallAdapterFactory: CoroutineCallAdapterFactory,
            okHttpClient: OkHttpClient
        ): Retrofit {
            return Retrofit.Builder()
                .addConverterFactory(moshiConverterFactory)
                .addCallAdapterFactory(coroutineCallAdapterFactory)
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .build()
        }

        @AppScope
        @Provides
        fun provideFinnhubApi(
            retrofit: Retrofit
        ): FinnHubService {
            return retrofit.create(FinnHubService::class.java)
        }
    }


}
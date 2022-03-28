package com.sample.mdsyandexproject.di

import com.sample.mdsyandexproject.database.StockDatabase
import com.sample.mdsyandexproject.database.getStockDatabase
import dagger.Module
import dagger.Provides

@Module
interface RoomDbModule {
    companion object {
        @Provides
        @AppScope
        fun provideStockDatabase(): StockDatabase {
            return getStockDatabase()
        }
    }
}
package com.sample.mdsyandexproject.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration_1_2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE DatabaseStockItem ADD COLUMN error TEXT")
        database.execSQL("ALTER TABLE DatabaseStockItem ADD COLUMN errorMessage TEXT")
    }
}
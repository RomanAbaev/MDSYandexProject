package com.sample.mdsyandexproject.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration_1_2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE DatabaseStockItem ADD COLUMN error TEXT")
        database.execSQL("ALTER TABLE DatabaseStockItem ADD COLUMN errorMessage TEXT")
    }
}
object Migration_2_3 : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DELETE FROM DatabaseStockItem")
        database.execSQL("CREATE TABLE IF NOT EXISTS SPIndices(indices TEXT PRIMARY KEY NOT NULL, isLoaded INTEGER NOT NULL, symbol TEXT NOT NULL)")

        database.execSQL("ALTER TABLE DatabaseStockItem ADD COLUMN previousClosePrice REAL")
        database.execSQL("ALTER TABLE DatabaseStockItem ADD COLUMN previousClosePriceDate INTEGER")
    }
}

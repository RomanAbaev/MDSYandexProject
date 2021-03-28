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
object Migration_3_4: Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS News (" +
                "newsId INTEGER PRIMARY KEY NOT NULL, " +
                "ticker TEXT NOT NULL, " +
                "datetime INTEGER NOT NULL, " +
                "headline TEXT NOT NULL, " +
                "image TEXT NOT NULL, " +
                "source TEXT NOT NULL, " +
                "summary TEXT NOT NULL, " +
                "url TEXT NOT NULL)")
        database.execSQL("CREATE TABLE IF NOT EXISTS StockNewsCrossRef (" +
                "newsId INTEGER NOT NULL, " +
                "ticker TEXT NOT NULL, " +
                "PRIMARY KEY (ticker, newsId)" +
                ")")
    }
}
object Migration_4_5: Migration(4,5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS Recommendation (" +
                "ticker TEXT NOT NULL, " +
                "buy INTEGER NOT NULL, " +
                "strongBuy INTEGER NOT NULL, " +
                "hold INTEGER NOT NULL, " +
                "sell INTEGER NOT NULL, " +
                "strongSell INTEGER NOT NULL, " +
                "period INTEGER NOT NULL, " +
                "PRIMARY KEY (ticker, period)" +
                ")"
        )
    }
}
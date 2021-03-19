package com.sample.mdsyandexproject.database

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sample.mdsyandexproject.App
import com.sample.mdsyandexproject.repository.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Dao
interface StockItemDao {

    @Transaction
    suspend fun loadNextChunks(stockList: List<DatabaseStockItem>, spIndices: List<SPIndices>) {
        insertAll(stockList)
        updateSPIndices(spIndices)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stockList: List<DatabaseStockItem>)

    @Query("select * from DatabaseStockItem")
    fun getAll(): LiveData<List<DatabaseStockItem>>

    @Query("select * from DatabaseStockItem where isFavourite=1")
    fun getFavouriteList(): LiveData<List<DatabaseStockItem>>

    @Update(entity = DatabaseStockItem::class)
    suspend fun updateFavouriteStock(stock: FavouriteDatabaseModel)

    @Update(entity = DatabaseStockItem::class)
    suspend fun updatePrices(prices: List<Prices>)

    @Insert
    suspend fun insertAllSPIndices(spIndices: List<SPIndices>)

    @Query("select * from SPIndices where isLoaded = 0 limit :limit")
    suspend fun getNextUnloadedIndices(limit: Int): List<SPIndices>

    @Query("select COUNT(*) from SPIndices")
    suspend fun getIndicesCount(): Int

    @Update(entity = DatabaseStockItem::class)
    suspend fun updateQuote(quote: QuoteDb)

    @Update(entity = DatabaseStockItem::class)
    suspend fun updateCompanyProfile(companyProfile: CompanyProfile2Db)

    @Update(entity = DatabaseStockItem::class)
    suspend fun updateQuoteAndCompanyProfile(quoteAndCompanyProfile: QuoteAndCompanyProfileDb)

    @Update(entity = SPIndices::class)
    suspend fun updateSPIndices(spIndices: List<SPIndices>)

    @Query("select * from DatabaseStockItem where ticker like :query OR companyName like :query")
    suspend fun search(query: String): List<DatabaseStockItem>

    @Query("select * from DatabaseStockItem where ticker = :ticker")
    suspend fun getStockItem(ticker: String): DatabaseStockItem?

    @Insert
    suspend fun insertStockItem(databaseStockItem: DatabaseStockItem)
}

@Database(
    entities = [DatabaseStockItem::class, SPIndices::class],
    version = 3,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class StockDatabase : RoomDatabase() {
    abstract val dao: StockItemDao
}

private lateinit var INSTANCE: StockDatabase

private val scope = CoroutineScope(Dispatchers.IO)

fun getDatabase(): StockDatabase {
    synchronized(StockDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                App.applicationContext(),
                StockDatabase::class.java,
                "stocks"
            )
                .addCallback(object : RoomDatabase.Callback() {
                    // prepopulate database
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        scope.launch {
                            try {
                                Repository.instance.prepopulateData()
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                                Repository.instance.loadNextChunksException.postValue(Pair(true, ex.message.toString()))
                            }
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        scope.launch {
                            // check if SPIndices wasn't loaded in onCreate methods (and reload it if needed)
                            val count = INSTANCE.dao.getIndicesCount()
                            if (count == 0) {
                                try {
                                    Repository.instance.prepopulateData()
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                    Repository.instance.loadNextChunksException.postValue(Pair(true, ex.message.toString()))
                                }
                            }
                        }
                    }
                })
                .addMigrations(Migration_1_2)
                .addMigrations(Migration_2_3)
                .build()
        }
    }
    return INSTANCE
}
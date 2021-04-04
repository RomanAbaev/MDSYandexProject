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

    @Query("select COUNT(*) from DatabaseStockItem")
    suspend fun getTotalItemCount(): Int

    @Update(entity = DatabaseStockItem::class)
    suspend fun updateQuote(quote: QuoteDb)

    @Update(entity = DatabaseStockItem::class)
    suspend fun updateCompanyProfile(companyProfile: CompanyProfileDb)

    @Update(entity = DatabaseStockItem::class)
    suspend fun updateQuoteAndCompanyProfile(quoteAndCompanyProfile: QuoteAndCompanyProfileDb)

    @Update(entity = SPIndices::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSPIndices(spIndices: List<SPIndices>)

    @Query("select * from DatabaseStockItem where ticker like :query OR companyName like :query")
    suspend fun search(query: String): List<DatabaseStockItem>

    @Query("select * from DatabaseStockItem where ticker = :ticker")
    suspend fun getStockItem(ticker: String): DatabaseStockItem?

    @Insert
    suspend fun insertStockItem(databaseStockItem: DatabaseStockItem)

    @Transaction
    suspend fun insertNews(news: List<News>, stockNewsCrossRef: List<StockNewsCrossRef>) {
        // insert into news table
        insertAllNewsItem(news)
        // insert into stock-news-cross-ref table
        insertAllStockNewsCrossRef(stockNewsCrossRef)
    }

    // if news already exists in the table don't need to insert it just add relation to StockNewsCrossRef table
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllNewsItem(news: List<News>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStockNewsCrossRef(stockNewsCrossRef: List<StockNewsCrossRef>)

    @Transaction
    @Query(
        "select * from News " +
                "INNER JOIN StockNewsCrossRef as snref ON snref.newsId = News.newsId " +
                "WHERE snref.ticker = :ticker AND News.datetime >= :from AND News.datetime <= :to"
    )
    suspend fun getNews(ticker: String, from: Long, to: Long): List<News>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendations(recommendations: List<Recommendation>)

    @Query("select * from Recommendation where ticker=:ticker order by period desc limit :offset, :limit ")
    fun getRecommendations(ticker: String, offset: Int, limit: Int): LiveData<List<Recommendation>>

    @Query("select COUNT(*) from Recommendation where ticker=:ticker")
    suspend fun getRecommendationsCount(ticker: String): Int
}

@Database(
    entities = [
        DatabaseStockItem::class,
        SPIndices::class,
        News::class,
        StockNewsCrossRef::class,
        Recommendation::class
    ],
    version = 5,
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
                                Repository.prepopulateData()
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                                Repository.loadNextChunksException.postValue(
                                    Pair(
                                        true,
                                        ex.message.toString()
                                    )
                                )
                            }
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        scope.launch {
                            // check if SPIndices wasn't loaded in onCreate methods (and reload it if needed)
                            val count = INSTANCE.dao.getIndicesCount()
                            if (count == 0) {
                                try {
                                    Repository.prepopulateData()
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                    Repository.loadNextChunksException.postValue(
                                        Pair(
                                            true,
                                            ex.message.toString()
                                        )
                                    )
                                }
                            }
                        }
                    }
                })
                .addMigrations(Migration_1_2)
                .addMigrations(Migration_2_3)
                .addMigrations(Migration_3_4)
                .addMigrations(Migration_4_5)
                .build()
        }
    }
    return INSTANCE
}
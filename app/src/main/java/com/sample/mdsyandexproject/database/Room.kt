package com.sample.mdsyandexproject.database

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sample.mdsyandexproject.App
import com.sample.mdsyandexproject.network.MarketStackApi
import com.sample.mdsyandexproject.network.asStockItemDatabaseModel
import com.sample.mdsyandexproject.repository.Repository
import com.sample.mdsyandexproject.repository.limit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Dao
interface StockItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun populate(stock: List<DatabaseStockItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stock: List<DatabaseStockItem>)

    @Query("select * from DatabaseStockItem")
    fun getAll(): LiveData<List<DatabaseStockItem>>

    @Query("select * from DatabaseStockItem where isFavourite=1")
    fun getFavouriteList(): LiveData<List<DatabaseStockItem>>

    @Query("select * from DatabaseStockItem where isFavourite=1")
    fun getAllFavourite(): LiveData<List<DatabaseStockItem>>

    @Query("select * from DatabaseStockItem")
    fun getAllRegular(): List<DatabaseStockItem>

    @Query("select * from DatabaseStockItem limit :limit offset :offset")
    fun getPage(limit: Int, offset: Int): LiveData<List<DatabaseStockItem>>

    @Query("select * from DatabaseStockItem COUNT")
    fun getCount(): Int

    @Query("select ticker, isFavourite from DatabaseStockItem")
    fun getTickers(): List<Ticker>

    @Update(entity = DatabaseStockItem::class)
    suspend fun updateFavouriteStock(stock: FavouriteDatabaseModel)

    @Update(entity = DatabaseStockItem::class)
    suspend fun updatePrices(prices: List<Prices>)

    @Update(entity = DatabaseStockItem::class)
    suspend fun updateInfo(databaseStockItem: DatabaseStockItem)
}


@Database(entities = [DatabaseStockItem::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class StockDatabase : RoomDatabase() {
    abstract val stockItemDao: StockItemDao
}

private lateinit var INSTANCE: StockDatabase

private val scope = CoroutineScope(Dispatchers.Unconfined)

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
                            val list: MutableList<DatabaseStockItem> =
                                MarketStackApi.marketStackService.getNextPage(limit, 0).await()
                                    .asStockItemDatabaseModel().toMutableList()
                            Repository.instance.getEodPrices(list)
                            Repository.instance.loadCompanyInfo(list)
                            try {
                                INSTANCE.stockItemDao.insertAll(
                                    list
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                })
                .addMigrations(Migration_1_2)
                .build()
        }
    }
    return INSTANCE
}
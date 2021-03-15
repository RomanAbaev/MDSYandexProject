package com.sample.mdsyandexproject.stocklist

import android.util.Log
import androidx.lifecycle.*
import com.sample.mdsyandexproject.domain.StockItem
import com.sample.mdsyandexproject.domain.asDatabaseModel
import com.sample.mdsyandexproject.repository.Repository
import com.sample.mdsyandexproject.utils.isCompanyInfoValid
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

@ExperimentalCoroutinesApi
class StockListViewModel : ViewModel() {

    private val repository = Repository.instance

    private var limit = 20

    var loading = MutableLiveData(false)
    val showFavouriteList = MutableLiveData(false)
    val requestQ = ConcurrentHashMap<String, Int>()

    private var _stockList: LiveData<List<StockItem>> =
        Transformations.switchMap(showFavouriteList) {
            repository.getStockList(it)
        }
    val stockList: LiveData<List<StockItem>>
        get() = _stockList

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.refreshData()
        }
    }


    fun updateStockItemInformation(stockItem: StockItem) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!requestQ.containsKey(stockItem.ticker)) {
                Log.i("ConcurrentHashMap", "start loading info for: ${stockItem.ticker}")
                requestQ[stockItem.ticker] = 0
                val databaseStockItem = stockItem.asDatabaseModel()
                //make a delay because we don't want to run into Too many request error from api
                delay(15000)
                repository.getEodPrices(databaseStockItem)
                if (!isCompanyInfoValid(stockItem)) repository.loadCompanyInfo(databaseStockItem)
//                Log.i("ConcurrentHashMap", "now eod date is: ${isEodDateValid(databaseStockItem.eodDate)}")
                repository.updateInfo(databaseStockItem)
                requestQ.remove(stockItem.ticker)
                Log.i("ConcurrentHashMap", "end loading info for: ${stockItem.ticker}")
            } else {
                Log.i("ConcurrentHashMap", "ticker already contains in map: ${stockItem.ticker}")
            }
        }
    }

    fun onFavouriteButtonClicked(stockItem: StockItem) {
        stockItem.isFavourite = !stockItem.isFavourite
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFavouriteStock(stockItem)
        }
    }

    fun subscribeToTickerPriceUpdate(ticker: String) {
        repository.subscribeToTickerPriceUpdate(ticker)
    }

    fun unsubscribeToTickerPriceUpdate(ticker: String) {
        repository.unsubscribeToTickerPriceUpdate(ticker)
    }

    fun showList(list: String) {
        when (list) {
            STOCKS -> {
                showFavouriteList.value = false
            }
            FAVOURITES -> {
                showFavouriteList.value = true
            }
        }
    }

    fun loadNextChunks() {
        viewModelScope.launch {
            loading.value = true
            withContext(Dispatchers.IO) {
                repository.loadNextChunks(limit, stockList.value?.size ?: 0)
            }
            loading.value = false
        }
    }

    @ExperimentalCoroutinesApi
    override fun onCleared() {
        repository.closeSocket()
        super.onCleared()
    }

    companion object {
        const val STOCKS = "stocks"
        const val FAVOURITES = "favourites"
    }

}
package com.sample.mdsyandexproject.stocklist

import androidx.lifecycle.*
import com.sample.mdsyandexproject.domain.StockItem
import com.sample.mdsyandexproject.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalCoroutinesApi
class StockListViewModel : ViewModel() {

    private val repository = Repository.instance

    var loading = MutableLiveData(false)
    val showFavouriteList = MutableLiveData(false)

    private var _stockList: LiveData<List<StockItem>> =
        Transformations.switchMap(showFavouriteList) {
            repository.getStockList(it)
        }
    val stockList: LiveData<List<StockItem>>
        get() = _stockList

    val loadNextChunksException: LiveData<Pair<Boolean, String>> =
        repository.loadNextChunksException

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.refreshData()
        }
    }

    fun updateStockItemInformation(stockItem: StockItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateQuoteAndCompanyProfile(stockItem)
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
            if (loading.value == false) {
                loading.value = true
                withContext(Dispatchers.IO) {
                    repository.loadNextChunks()
                }
                loading.value = false
            }
        }

    }

    fun onTriedAgainBtnClick() {
        repository.loadNextChunksException.value = Pair(false, "")
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
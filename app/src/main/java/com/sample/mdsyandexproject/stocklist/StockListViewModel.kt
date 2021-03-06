package com.sample.mdsyandexproject.stocklist

import androidx.lifecycle.*
import com.sample.mdsyandexproject.domain.StockItem
import com.sample.mdsyandexproject.repository.Repository
import kotlinx.coroutines.*
import javax.inject.Inject

@DelicateCoroutinesApi
@ExperimentalCoroutinesApi
class StockListViewModel @Inject constructor(
    var repository: Repository
) : ViewModel() {

    val isDataPrepopulating = repository.isDataPrepopulating
    val stockItemLoadingProgress = repository.stockItemLoadingProgress
    var stockItemLoading = repository.stockItemLoading
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
            repository.updateQuoteAndCompanyInfo(stockItem)
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
            STOCKS -> showFavouriteList.value = false
            FAVOURITES -> showFavouriteList.value = true
        }
    }

    fun loadNextChunks() {
        viewModelScope.launch {
            if (stockItemLoading.value == false) {
                withContext(Dispatchers.IO) {
                    repository.loadNextChunks()
                }
            }
        }

    }

    fun onTriedAgainBtnClick() {
        repository.loadNextChunksException.value = Pair(false, "")
    }

    override fun onCleared() {
        repository.closeSocket()
        super.onCleared()
    }

    fun prepopulateIfNeeded() {
        viewModelScope.launch {
            if (repository.isPrepopulateNeeded()) {
                try {
                    repository.prepopulateData()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    repository.loadNextChunksException.postValue(
                        Pair(
                            true,
                            ex.message.toString()
                        )
                    )
                }
            }
        }
    }

    companion object {
        const val STOCKS = "stocks"
        const val FAVOURITES = "favourites"
    }
}
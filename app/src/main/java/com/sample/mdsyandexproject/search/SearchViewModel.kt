package com.sample.mdsyandexproject.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.mdsyandexproject.domain.StockItem
import com.sample.mdsyandexproject.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.listOf
import kotlin.collections.set

class SearchViewModel : ViewModel() {

    private val repository = Repository.instance

    private val _searchResultList = MutableLiveData<MutableList<StockItem>>()
    val searchResultList: LiveData<MutableList<StockItem>>
        get() = _searchResultList

    private val _isSearchActive = MutableLiveData(false)
    val isSearchActive: LiveData<Boolean>
        get() = _isSearchActive

    private val _popularRequestList = MutableLiveData(getMockPopularRequest())
    val popularRequestList: LiveData<List<String>>
        get() = _popularRequestList

    private val _searchHistoryList = MutableLiveData(getMockSearchHistory())
    val searchHistoryList: LiveData<List<String>>
        get() = _searchHistoryList

    val requestQ = ConcurrentHashMap<String, Int>()

    fun toggleSearch(active: Boolean) {
        _isSearchActive.value = active;
    }

    fun onFavouriteButtonClicked(stockItem: StockItem) {
        stockItem.isFavourite = !stockItem.isFavourite
        viewModelScope.launch(Dispatchers.IO) {
            // check if stockItem exist in db and if not load it form network and cache
            repository.isExistInDb(stockItem)
        }
    }

    private fun getMockPopularRequest(): List<String> =
        listOf(
            "Apple",
            "Amazon",
            "First Solar",
            "Alibaba",
            "Google",
            "Tesla",
            "Yandex",
            "Facebook",
            "Mastercard",
            "Instagram",
            "Vkontakte",
            "Nvidia",
            "Nokia",
            "Samsung",
            "Huawei",
            "Xiaomi",
            "Sony",
            "Mitsubishi"
        )

    private fun getMockSearchHistory(): List<String> =
        listOf(
            "Nvidia",
            "Apple",
            "Amazon",
            "First Solar",
            "Alibaba",
            "Google",
            "Tesla",
            "Yandex",
            "Facebook",
            "Mastercard",
            "Instagram",
            "Vkontakte",
            "Nokia",
            "Samsung",
            "Huawei",
            "Xiaomi",
            "Sony",
            "Mitsubishi"
        )

    var job: Job? = null

    fun submitSearch(query: String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            if (!requestQ.containsKey(query)) {
                requestQ[query] = 0
                _searchResultList.postValue(repository.submitSearch(query))
                requestQ.remove(query)
            }
        }
    }

    fun cancelJob() {
        job?.cancel()
    }

    fun subscribeToTickerPriceUpdate(ticker: String) {
        repository.subscribeToTickerPriceUpdate(ticker)
    }

    fun unsubscribeToTickerPriceUpdate(ticker: String) {
        repository.unsubscribeToTickerPriceUpdate(ticker)
    }
}
package com.sample.mdsyandexproject.stockitem

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.CandleEntry
import com.sample.mdsyandexproject.domain.NewsItem
import com.sample.mdsyandexproject.domain.StockItem
import com.sample.mdsyandexproject.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class StockItemViewModel : ViewModel() {

    private val repository = Repository.instance

    lateinit var stockItem: StockItem

    val news = MutableLiveData<MutableList<NewsItem>>(mutableListOf())
    var loading = MutableLiveData(false)

    var chartLoading = MutableLiveData(false)
    val candlesData = MutableLiveData<List<CandleEntry>>()
    val loadCandleInfoException: LiveData<Pair<Boolean, String>> =
        repository.loadCandleInfoException
    val loadNewsException: LiveData<Pair<Boolean, String>> =
        repository.loadNewsException

    val checkedPeriod = MutableLiveData<Int>()

    private var charLoadingJob: Job? = null

    fun loadCandlesInfo(ticker: String, from: Long, to: Long) {

        charLoadingJob?.cancel()
        charLoadingJob = viewModelScope.launch(Dispatchers.IO) {
            chartLoading.postValue(true)
            val candles = repository.loadCandleInfo(ticker, from, to)
            val candlesData = mutableListOf<CandleEntry>()

            candles?.let {
                if (candles.status == "ok") {
                    for ((index, _) in it.openPrices.withIndex()) {
                        candlesData.add(
                            CandleEntry(
                                index.toFloat(),
                                it.highPrices[index],
                                it.lowPrices[index],
                                it.openPrices[index],
                                it.closePrices[index],
                            )
                        )
                    }
                }
            }
            this@StockItemViewModel.candlesData.postValue(candlesData)
            chartLoading.postValue(false)
        }
    }

    fun onCandlePeriodClick(period: Int) {
        checkedPeriod.value = period
        when (period) {
            DAY -> {
                loadCandlesInfo(
                    stockItem.ticker,
                    DateTime.now().minusDays(1).millis.div(1000L),
                    DateTime.now().millis.div(1000L)
                )
            }
            WEEK -> {
                loadCandlesInfo(
                    stockItem.ticker,
                    DateTime.now().minusWeeks(1).millis.div(1000L),
                    DateTime.now().millis.div(1000L)
                )
            }
            MONTH -> {
                loadCandlesInfo(
                    stockItem.ticker,
                    DateTime.now().minusMonths(1).millis.div(1000L),
                    DateTime.now().millis.div(1000L)
                )
            }
            SIX_MONTH -> {
                loadCandlesInfo(
                    stockItem.ticker,
                    DateTime.now().minusMonths(6).millis.div(1000L),
                    DateTime.now().millis.div(1000L)
                )
            }
            ONE_YEAR -> {
                loadCandlesInfo(
                    stockItem.ticker,
                    DateTime.now().minusYears(1).millis.div(1000L),
                    DateTime.now().millis.div(1000L)
                )
            }
            ALL -> {
                loadCandlesInfo(
                    stockItem.ticker,
                    0,
                    DateTime.now().millis.div(1000L)
                )
            }
        }
    }

    fun onTriedAgainLoadCandlesBtnClick() {
        repository.loadCandleInfoException.value = Pair(false, "")
    }

    // assume that's one day = one page
    private var newsPage = 0

    fun loadNews() {
        viewModelScope.launch(Dispatchers.Unconfined) {
            var loadedNews: List<NewsItem>?
            val currentListNews: MutableList<NewsItem>? = this@StockItemViewModel.news.value
            val newNewsPage: MutableList<NewsItem> = mutableListOf()
            if (currentListNews != null && currentListNews.isNotEmpty()) loading.postValue(true)
            do {
                val fromAndToDate = getFromAndToDateForNews()
                loadedNews = repository.loadNews(
                    stockItem.ticker,
                    fromAndToDate,
                    fromAndToDate
                )
                if (loadedNews != null) {
                    newsPage++
                    newNewsPage.addAll(loadedNews)
                }
            } while (loadedNews != null && newNewsPage.size <= 12 )
            currentListNews?.let {
                currentListNews.addAll(newNewsPage)
                this@StockItemViewModel.news.postValue(it)
            }
            loading.postValue(false)
        }
    }

    private fun getFromAndToDateForNews(): String =
        DateTime.now().minusDays(newsPage).toLocalDate().toString()

    fun onTriedAgainLoadNewsBtnClick() {
        repository.loadNewsException.value = Pair(false, "")
    }

    fun resetNewsInformation() {
        this@StockItemViewModel.news.value = mutableListOf()
        newsPage = 0;
    }

    companion object {
        const val DAY = 0
        const val WEEK = 1
        const val MONTH = 2
        const val SIX_MONTH = 3
        const val ONE_YEAR = 4
        const val ALL = 5
    }
}
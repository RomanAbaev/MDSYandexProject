package com.sample.mdsyandexproject.stockitem

import android.graphics.Color
import androidx.lifecycle.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.sample.mdsyandexproject.domain.NewsItem
import com.sample.mdsyandexproject.domain.StockItem
import com.sample.mdsyandexproject.repository.Repository
import com.sample.mdsyandexproject.stockitem.chart.CandleChartDataPeriods.*
import com.sample.mdsyandexproject.stockitem.recommendation.XAxisValueFormatter
import com.sample.mdsyandexproject.utils.MMM_YY
import com.sample.mdsyandexproject.utils.convertLongToDate
import kotlinx.coroutines.*
import org.joda.time.DateTime

@DelicateCoroutinesApi
@ExperimentalCoroutinesApi
class StockItemViewModel : ViewModel() {

    private val repository = Repository

    lateinit var stockItem: StockItem

    var recommendationOffset = MutableLiveData(0)
    var recommendationLimit = 4
    var recommendationDataLoading = MutableLiveData(false)
    var recommendationCount: Int = -1

    val news = MutableLiveData<MutableList<NewsItem>>(mutableListOf())
    var loading = MutableLiveData(false)

    var chartLoading = MutableLiveData(false)
    val candlesData = MutableLiveData<List<CandleEntry>>()
    val loadCandleInfoException: LiveData<Pair<Boolean, String>> =
        repository.loadCandleInfoException
    val loadNewsException: LiveData<Pair<Boolean, String>> =
        repository.loadNewsException
    val loadRecommendationsException: LiveData<Pair<Boolean, String>> =
        repository.updateRecommendationsException

    val checkedPeriod = MutableLiveData<Int>()

    private var charLoadingJob: Job? = null

    fun onFavouriteButtonClicked(stockItem: StockItem) {
        stockItem.isFavourite = !stockItem.isFavourite
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFavouriteStock(stockItem)
        }
    }

    private fun loadCandlesInfo(ticker: String, from: Long, to: Long) {
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
            DAY.ordinal -> {
                loadCandlesInfo(
                    stockItem.ticker,
                    DateTime.now().minusDays(1).millis.div(1000L),
                    DateTime.now().millis.div(1000L)
                )
            }
            WEEK.ordinal -> {
                loadCandlesInfo(
                    stockItem.ticker,
                    DateTime.now().minusWeeks(1).millis.div(1000L),
                    DateTime.now().millis.div(1000L)
                )
            }
            MONTH.ordinal -> {
                loadCandlesInfo(
                    stockItem.ticker,
                    DateTime.now().minusMonths(1).millis.div(1000L),
                    DateTime.now().millis.div(1000L)
                )
            }
            SIX_MONTH.ordinal -> {
                loadCandlesInfo(
                    stockItem.ticker,
                    DateTime.now().minusMonths(6).millis.div(1000L),
                    DateTime.now().millis.div(1000L)
                )
            }
            ONE_YEAR.ordinal -> {
                loadCandlesInfo(
                    stockItem.ticker,
                    DateTime.now().minusYears(1).millis.div(1000L),
                    DateTime.now().millis.div(1000L)
                )
            }
            ALL.ordinal -> {
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
                loadedNews = repository.loadNews(
                    stockItem.ticker,
                    newsPage
                )
                if (loadedNews != null) {
                    newsPage++
                    newNewsPage.addAll(loadedNews)
                }
            } while (loadedNews != null && newNewsPage.size <= 12)
            currentListNews?.let {
                currentListNews.addAll(newNewsPage)
                this@StockItemViewModel.news.postValue(it)
            }
            loading.postValue(false)
        }
    }

    fun onTriedAgainLoadNewsBtnClick() {
        repository.loadNewsException.value = Pair(false, "")
    }

    fun resetStockItemInformationInformation() {
        this@StockItemViewModel.news.value = mutableListOf()
        recommendationOffset.value = 0
        newsPage = 0;
    }

    fun onTriedAgainGetRecommendationBtnClick() {
        repository.updateRecommendationsException.value = Pair(false, "")
    }

    fun updateRecommendations() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRecommendations(stockItem.ticker)
        }
    }

    private var _recommendations: LiveData<Pair<BarData, XAxis>> =
        Transformations.switchMap(recommendationOffset) { offset ->
            recommendationDataLoading.value = true
            return@switchMap Transformations.map(
                repository.getNextRecommendationChunks(
                    stockItem.ticker,
                    offset,
                    recommendationLimit
                )
            ) {
                val values = mutableListOf<BarEntry>()
                val periods = mutableListOf<String>()
                for ((index, item) in it.asReversed().withIndex()) {
                    val valArr = floatArrayOf(
                        item.strongSell.toFloat(),
                        item.sell.toFloat(),
                        item.hold.toFloat(),
                        item.buy.toFloat(),
                        item.strongBuy.toFloat()
                    )
                    values.add(
                        BarEntry(
                            index.toFloat(),
                            valArr
                        )
                    )
                    periods.add(convertLongToDate(MMM_YY, item.period))
                }
                val xAxis = XAxis()
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(true)
                xAxis.granularity = 1f
                xAxis.labelCount = 7
                xAxis.valueFormatter = XAxisValueFormatter(periods)
                val barDataSet = BarDataSet(values, "")
                barDataSet.colors =
                    listOf(
                        Color.rgb(129, 49, 49),
                        Color.rgb(244, 91, 91),
                        Color.rgb(185, 139, 29),
                        Color.rgb(29, 185, 84),
                        Color.rgb(23, 111, 55)
                    )
                barDataSet.stackLabels =
                    arrayOf(
                        "Strong sell",
                        "Sell",
                        "Hold",
                        "Buy",
                        "Strong Buy"
                    )
                val dataSet = listOf<IBarDataSet>(barDataSet)
                recommendationDataLoading.value = false
                Pair(BarData(dataSet), xAxis)
            }
        }
    val recommendations: LiveData<Pair<BarData, XAxis>>
        get() = _recommendations

    fun getRecommendationCount() {
        viewModelScope.launch(Dispatchers.IO) {
            recommendationCount = repository.getRecommendationCount(stockItem.ticker)
        }
    }

    fun loadPreviousMonthRecommendations() {
        val offset: Int = recommendationOffset.value ?: 0
        recommendationOffset.value = offset + recommendationLimit
    }

    fun loadNextMonthRecommendations() {
        val offset: Int = recommendationOffset.value ?: 0
        recommendationOffset.value = offset - recommendationLimit
    }
}
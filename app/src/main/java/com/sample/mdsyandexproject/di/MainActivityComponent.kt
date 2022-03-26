package com.sample.mdsyandexproject.di

import com.sample.mdsyandexproject.search.di.SearchComponent
import com.sample.mdsyandexproject.stockitem.chart.di.ChartComponent
import com.sample.mdsyandexproject.stockitem.di.StockItemComponent
import com.sample.mdsyandexproject.stockitem.news.di.NewsComponent
import com.sample.mdsyandexproject.stockitem.recommendation.di.RecommendationComponent
import com.sample.mdsyandexproject.stockitem.summary.di.SummaryComponent
import com.sample.mdsyandexproject.stocklist.di.StockListComponent
import dagger.Subcomponent

@Subcomponent(modules = [ViewModelModule::class])
@MainActivityScope
interface MainActivityComponent {
    fun searchComponent(): SearchComponent
    fun stockItemComponent(): StockItemComponent
    fun chartComponent(): ChartComponent
    fun stockListComponent(): StockListComponent
    fun newsComponent(): NewsComponent
    fun recommendationComponent(): RecommendationComponent
    fun summaryComponent(): SummaryComponent
}
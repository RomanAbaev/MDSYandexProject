package com.sample.mdsyandexproject.stockitem.chart.di

import com.sample.mdsyandexproject.stockitem.pager_screens.ChartFragment
import dagger.Subcomponent

@Subcomponent
interface ChartComponent {
    fun inject(fragment: ChartFragment)
}
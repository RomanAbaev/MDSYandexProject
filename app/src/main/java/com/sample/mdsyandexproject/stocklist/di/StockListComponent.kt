package com.sample.mdsyandexproject.stocklist.di

import com.sample.mdsyandexproject.stocklist.StockListFragment
import dagger.Subcomponent

@Subcomponent
interface StockListComponent {
    fun inject(fragment: StockListFragment)
}
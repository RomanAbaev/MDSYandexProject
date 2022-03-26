package com.sample.mdsyandexproject.stockitem.di

import com.sample.mdsyandexproject.stockitem.StockItemFragment
import dagger.Subcomponent

@Subcomponent
interface StockItemComponent {
    fun inject(fragment: StockItemFragment)
}
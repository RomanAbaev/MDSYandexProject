package com.sample.mdsyandexproject.stockitem.summary.di

import com.sample.mdsyandexproject.stockitem.summary.SummaryFragment
import dagger.Subcomponent

@Subcomponent
interface SummaryComponent {
    fun inject(fragment: SummaryFragment)
}
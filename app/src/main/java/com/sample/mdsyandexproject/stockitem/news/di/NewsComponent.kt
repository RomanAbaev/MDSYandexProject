package com.sample.mdsyandexproject.stockitem.news.di

import com.sample.mdsyandexproject.stockitem.news.NewsFragment
import dagger.Subcomponent

@Subcomponent
interface NewsComponent {
    fun inject(fragment: NewsFragment)
}
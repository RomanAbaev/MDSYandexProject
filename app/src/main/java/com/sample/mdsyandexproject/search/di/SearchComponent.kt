package com.sample.mdsyandexproject.search.di

import com.sample.mdsyandexproject.search.SearchFragment
import dagger.Subcomponent

@Subcomponent
interface SearchComponent {
    fun inject(fragment: SearchFragment)
}
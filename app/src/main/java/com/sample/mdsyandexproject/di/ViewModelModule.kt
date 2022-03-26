package com.sample.mdsyandexproject.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sample.mdsyandexproject.search.SearchViewModel
import com.sample.mdsyandexproject.stockitem.StockItemViewModel
import com.sample.mdsyandexproject.stocklist.StockListViewModel
import com.sample.mdsyandexproject.utils.ViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ViewModelModule {

    @Binds
    @MainActivityScope
    fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(StockListViewModel::class)
    fun stockListItemViewModel(viewModel: StockListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StockItemViewModel::class)
    fun stockItemViewModel(viewModel: StockItemViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    fun searchViewModel(viewModel: SearchViewModel): ViewModel
}
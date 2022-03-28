package com.sample.mdsyandexproject.di

import dagger.Component
import dagger.Module

@AppScope
@Component(modules = [NetworkModule::class, RoomDbModule::class])
interface ApplicationComponent {

    fun activityComponent(): MainActivityComponent

    @Component.Factory
    interface Factory {
        fun create(): ApplicationComponent
    }
}
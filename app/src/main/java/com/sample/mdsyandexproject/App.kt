package com.sample.mdsyandexproject

import android.app.Application
import android.content.Context
import com.sample.mdsyandexproject.di.ApplicationComponent
import com.sample.mdsyandexproject.di.DaggerApplicationComponent

class App : Application() {

    lateinit var appComponent: ApplicationComponent
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        appComponent = DaggerApplicationComponent.factory().create()
    }

    companion object {
        private lateinit var instance: Application

        fun applicationContext(): Context = instance.applicationContext
    }
}
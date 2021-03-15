package com.sample.mdsyandexproject

import android.app.Application

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private lateinit var instance: Application

        fun applicationContext() = instance.applicationContext
    }
}
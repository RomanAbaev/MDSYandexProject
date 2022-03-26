package com.sample.mdsyandexproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sample.mdsyandexproject.di.DaggerApplicationComponent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
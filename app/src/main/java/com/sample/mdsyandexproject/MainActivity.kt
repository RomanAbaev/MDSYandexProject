package com.sample.mdsyandexproject

import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val connectivityManager = getSystemService(ConnectivityManager::class.java)

        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network : Network) {
                Toast.makeText(App.applicationContext(), R.string.connected, Toast.LENGTH_SHORT).show()
            }

            override fun onLost(network : Network) {
                Toast.makeText(App.applicationContext(), R.string.lost_connection, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
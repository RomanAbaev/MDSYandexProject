package com.sample.mdsyandexproject.network

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.util.concurrent.TimeUnit

class WebSocketProvider {
     var webSocket: WebSocket? = null

    private val socketOkHttpClient = OkHttpClient.Builder()
//        .readTimeout(1, TimeUnit.MINUTES)
//        .callTimeout(5, TimeUnit.SECONDS)
        .connectTimeout(2, TimeUnit.MINUTES)
        .hostnameVerifier { _, _ -> true }
        .build()

    @ExperimentalCoroutinesApi
    private var webSocketListener: FinnHubWebSocketListener? = null

    @ExperimentalCoroutinesApi
    fun startSocket(): Channel<SocketResponse> =
        with(FinnHubWebSocketListener()) {
            startSocket(this)
            this@with.socketEventChannel
        }


    @ExperimentalCoroutinesApi
    fun startSocket(webSocketListener: FinnHubWebSocketListener) {
        this.webSocketListener = webSocketListener
        webSocket = socketOkHttpClient.newWebSocket(
            Request.Builder().url("wss://ws.finnhub.io?token=$FINNHUB_API_KEY").build(),
            webSocketListener
        )
        socketOkHttpClient.dispatcher.executorService.shutdown()
    }

    @ExperimentalCoroutinesApi
    fun closeSocket() {
        try {
            webSocket?.close(NORMAL_CLOSURE_STATUS, null)
            webSocket = null
            webSocketListener?.socketEventChannel?.close()
            webSocketListener = null
        } catch (ex: Exception) {
        }
    }

    companion object {
        const val NORMAL_CLOSURE_STATUS = 1000
    }
}
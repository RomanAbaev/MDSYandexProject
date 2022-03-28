package com.sample.mdsyandexproject.network

import com.sample.mdsyandexproject.di.AppScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@DelicateCoroutinesApi
@ExperimentalCoroutinesApi
@AppScope
class WebSocketProvider @Inject constructor() {
    var webSocket: WebSocket? = null

    private val socketOkHttpClient = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.MINUTES)
        .hostnameVerifier { _, _ -> true }
        .build()


    private var webSocketListener: FinnHubWebSocketListener? = null


    fun startSocket(): Channel<SocketResponse> =
        with(FinnHubWebSocketListener()) {
            startSocket(this)
            this@with.socketEventChannel
        }


    fun startSocket(webSocketListener: FinnHubWebSocketListener) {
        this.webSocketListener = webSocketListener
        webSocket = socketOkHttpClient.newWebSocket(
            Request.Builder()
                .url("wss://ws.finnhub.io?${AuthInterceptors.TOKEN}=${AuthInterceptors.FINNHUB_API_KEY}")
                .build(),
            webSocketListener
        )
        socketOkHttpClient.dispatcher.executorService.shutdown()
    }


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
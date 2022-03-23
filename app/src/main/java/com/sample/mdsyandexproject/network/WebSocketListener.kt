package com.sample.mdsyandexproject.network

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
class FinnHubWebSocketListener : WebSocketListener() {

    val socketEventChannel: Channel<SocketResponse> = Channel(10)

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        GlobalScope.launch {
            if (!socketEventChannel.isClosedForSend) socketEventChannel.send(
                SocketResponse(
                    exception = SocketAbortedException()
                )
            )
        }
        webSocket.close(666, "close from closing")
        socketEventChannel.close()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        GlobalScope.launch {
            if (!socketEventChannel.isClosedForSend) socketEventChannel.send(
                SocketResponse(
                    exception = t
                )
            )
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        GlobalScope.launch {
            socketEventChannel.send(SocketResponse(text))
        }
    }
}

class SocketAbortedException : Exception()

data class SocketResponse(
    val text: String? = null,
    val byteString: ByteString? = null,
    val exception: Throwable? = null
)
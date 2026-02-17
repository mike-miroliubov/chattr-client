package org.chats.client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.chats.dto.ChatMessageDto
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
private data class WebsocketIncomingMessageDto(
    val id: String,
    val from: String,
    val text: String,
)

@Serializable
private data class WebsocketOutgoingMessageDto(
    val id: String,
    val from: String,
    val text: String,
)


@OptIn(ExperimentalTime::class)
class ChattrClient(val userName: String, val host: String, val port: Int) {
    private val sendChan: Channel<ChatMessageDto> = Channel()
    private val receiveFlow: MutableSharedFlow<WebsocketIncomingMessageDto> =
        MutableSharedFlow(extraBufferCapacity = 100_500)
    private val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    val messages: Flow<ChatMessageDto>
        get() = receiveFlow.asSharedFlow()
            .map { ChatMessageDto(it.id, it.from, it.text, Clock.System.now()) }

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            client.webSocket(
                method = HttpMethod.Get,
                host = host,
                port = port,
                path = "/messenger/api/connect",
                request = { parameter("userName", userName) }
            ) {
                // this runs on a new coroutine to allow async parallel sending
                val senderJob = launch {
                    while (true) {
                        sendSerialized(sendChan.receive().let { WebsocketOutgoingMessageDto(it.id, it.from, it.text) })
                    }
                }

                // this runs on the main client's coroutine so that its scope is coupled with the scope of the websocket session
                try {
                    while (true) {
                        receiveFlow.emit(receiveDeserialized<WebsocketIncomingMessageDto>())
                    }
                } catch (e: Exception) {
                    // catch all exceptions
                    senderJob.cancel(CancellationException("Websocket closed", e))
                    throw e
                }
            }
        }
    }

    suspend fun send(msg: ChatMessageDto) = sendChan.send(msg)

    fun close() {
        client.close()
    }
}
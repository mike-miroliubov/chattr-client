package org.chats.client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.io.discardingSink
import kotlinx.serialization.json.Json
import org.chats.dto.MessageDto

private data class WebsocketIncomingMessageDto(
    val id: String,
    val from: String,
    val text: String,
)

private data class WebsocketOutgoingMessageDto(
    val id: String,
    val from: String,
    val text: String,
)


class ChattrClient(val userName: String) {
    private val sendChan: Channel<MessageDto> = Channel()
    private val receiveChan: Channel<WebsocketIncomingMessageDto> = Channel(capacity = 100_500)
    private val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        install(ContentNegotiation)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        scope.launch {
            client.webSocket(
                method = HttpMethod.Get,
                host = "localhost",
                path = "/messenger/api/connect",
                request = { parameter("userName", userName) }
            ) {
                launch {
                    while (true) {
                        receiveChan.send(receiveDeserialized<WebsocketIncomingMessageDto>())
                    }
                }
                launch {
                    while (true) {
                        sendSerialized(sendChan.receive().let { WebsocketIncomingMessageDto(it.id, it.from, it.text) })
                    }
                }
            }
        }
    }

    fun send(msg: MessageDto) = sendChan::send
}
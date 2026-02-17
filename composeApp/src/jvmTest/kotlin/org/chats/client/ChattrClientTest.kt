@file:OptIn(ExperimentalTime::class)

package org.chats.client

import io.ktor.websocket.send
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.assertj.core.api.Assertions.assertThat
import org.chats.dto.ChatMessageDto
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val WELCOME_MESSAGE_JSON = """
    {
        "from":"",
        "id":"",
        "text":"You joined the chat"
    }
"""

class ChattrClientTest {
    private val mockServer = MockSocketServer(endpoints = mapOf(
        "/messenger/api/connect" to {}
    ))

    @BeforeEach
    fun setUp() {
        mockServer.start()
    }

    @AfterEach
    fun tearDown() = mockServer.stop()

    @Test
    fun testReceiveMessage() {
        mockServer.onConnect("/messenger/api/connect") {
            send(WELCOME_MESSAGE_JSON)
        }

        runBlocking {
            // when
            val client = ChattrClient("testUser", "localhost", mockServer.port)
            client.send(ChatMessageDto("1", "foo", "hey", receivedAt = Clock.System.now()))

            val received = withTimeoutOrNull(5000) {
                client.messages.take(2).toList()
            } ?: emptyList()

            // then
            assertThat(received)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("receivedAt")
                .containsExactly(
                    ChatMessageDto("", "", "You joined the chat", Clock.System.now()),
                    ChatMessageDto("1", "bar", "ho", Clock.System.now())
                )
        }
    }
}

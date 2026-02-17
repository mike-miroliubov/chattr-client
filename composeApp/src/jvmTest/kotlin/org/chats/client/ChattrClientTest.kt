@file:OptIn(ExperimentalTime::class)

package org.chats.client

import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.chats.dto.ChatMessageDto
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ChattrClientTest {
    private val mockServer = MockSocketServer()

    @Before
    fun setUp() {
        mockServer.start()
        Thread.sleep(500)
    }

    @After
    fun tearDown() = mockServer.stop()

    @Test
    fun testReceiveMessage() {
//        mockServer.addEndpoint("/messenger/api/connect") {
//            send("{\"from\":\"\",\"id\":\"\",\"text\":\"You joined the chat\"}")
//            for (frame in incoming) {
//                frame as? Frame.Text ?: continue
//                send("{\"from\":\"bar\",\"id\":\"1\",\"text\":\"ho\"}")
//            }
//        }

        runBlocking {
            val client = ChattrClient("testUser", "localhost", mockServer.port)
            client.send(ChatMessageDto("1", "foo", "hey", receivedAt = Clock.System.now()))

            val received = withTimeoutOrNull(5000) {
                client.messages.take(2).toList()
            } ?: emptyList()

            assertEquals(2, received.size, "Should have received 2 messages")
            assertEquals("You joined the chat", received[0].text)
            assertEquals("ho", received[1].text)
        }
    }
}

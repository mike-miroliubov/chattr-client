package org.chats.client

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.ServerSocket

class MockSocketServer(val port: Int = findFreePort()) {
    val endpoints = mutableMapOf<String, suspend DefaultWebSocketServerSession.() -> Unit>()
    val endpointsMutex = Mutex()
    private var launchHandle: Job? = null

    val server = embeddedServer(CIO, port = port) {
        install(WebSockets)

        routing {
            plugin(WebSockets) // early require

//            route(Regex(".*"), HttpMethod.Get) {
//                webSocketRaw(protocol, negotiateExtensions) {
//                    if (call.request.uri in endpoints.keys) {
//                        endpoints[call.request.uri]?.invoke(this)
//                    } else {
//                        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Endpoint not found"))
//                    }
//                }
//            }

            webSocket("/messenger/api/connect") {
                send("{\"from\":\"\",\"id\":\"\",\"text\":\"You joined the chat\"}")
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    println("Received text: $receivedText")
                    send("{\"from\":\"bar\",\"id\":\"1\",\"text\":\"ho\"}")
                }
            }
        }
    }

    fun start() {
        launchHandle = GlobalScope.launch { server.start(wait = true) }
    }

    fun stop() {
        server.stop(gracePeriodMillis = 0, timeoutMillis = 0)
        launchHandle?.cancel()
    }

    fun addEndpoint(uri: String, handler: suspend DefaultWebSocketServerSession.() -> Unit) {
        runBlocking {
            endpointsMutex.withLock {
                endpoints[uri] = handler
            }
        }
    }
}

private fun findFreePort(): Int {
    return ServerSocket(0).use { it.localPort }
}
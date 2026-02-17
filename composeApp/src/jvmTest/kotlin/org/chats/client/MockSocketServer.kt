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

typealias EndpointHandler = suspend DefaultWebSocketServerSession.() -> Unit

class MockSocketServer(val port: Int = findFreePort(), endpoints: Map<String, suspend DefaultWebSocketServerSession.() -> Unit>) {
    private val onEndpointConnect: MutableMap<String, EndpointHandler> = mutableMapOf()
    private val endpointsMutex = Mutex()
    private var serverJob: Job? = null

    val server = embeddedServer(CIO, port = port) {
        install(WebSockets)

        routing {
            plugin(WebSockets) // early require

            for ((uri, handler) in endpoints) {
                webSocket(uri) {
                    val onConnect = onEndpointConnect[uri]
                    if (onConnect != null) {
                        onConnect(this)
                    }

                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue
                        val receivedText = frame.readText()
                        println("Received text: $receivedText")
                        send("{\"from\":\"bar\",\"id\":\"1\",\"text\":\"ho\"}")
                    }
                }
            }
        }
    }

    fun start() {
        serverJob = GlobalScope.launch { server.start(wait = true) }
    }

    fun stop() {
        server.stop(gracePeriodMillis = 0, timeoutMillis = 0)
        serverJob?.cancel()
    }

    fun onConnect(uri: String, onConnectHandler: suspend DefaultWebSocketServerSession.() -> Unit): MockSocketServer {
        runBlocking {
            endpointsMutex.withLock {
                onEndpointConnect[uri] = onConnectHandler
            }
        }
        return this
    }
}

private fun findFreePort(): Int {
    return ServerSocket(0).use { it.localPort }
}
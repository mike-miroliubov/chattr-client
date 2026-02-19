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
typealias TextMessageMatcher = (String) -> Boolean

class MockSocketServer(val port: Int = findFreePort(), endpoints: Map<String, suspend DefaultWebSocketServerSession.() -> Unit>) {
    private val onEndpointConnect: MutableMap<String, EndpointHandler> = mutableMapOf()
    private val onMessageHandlers: MutableMap<String, MutableList<Pair<TextMessageMatcher, EndpointHandler>>> = mutableMapOf()
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
                        onMessageHandlers[uri]?.find { (matcher, _) -> matcher(receivedText) }?.second?.invoke(this)
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

    fun onConnect(uri: String, onConnectHandler: EndpointHandler): EndpointConfigurator {
        runBlocking {
            endpointsMutex.withLock {
                onEndpointConnect[uri] = onConnectHandler
            }
        }
        return EndpointConfigurator(uri)
    }

    inner class EndpointConfigurator(val uri: String) {
        fun onMessage(messageMatcher: (String) -> Boolean, onMessageHandler: EndpointHandler): EndpointConfigurator {
            runBlocking {
                endpointsMutex.withLock {
                    onMessageHandlers.getOrPut(uri) { mutableListOf() } += (messageMatcher to onMessageHandler)
                }
            }
            return this
        }

        fun also(): MockSocketServer = this@MockSocketServer
    }
}

private fun findFreePort(): Int {
    return ServerSocket(0).use { it.localPort }
}
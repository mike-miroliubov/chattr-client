@file:OptIn(ExperimentalTime::class)

package org.chats.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.f4b6a3.uuid.UuidCreator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.chats.client.ChattrClient
import org.chats.dto.ChatDto
import org.chats.dto.ChatMessageDto
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ConversationViewModel {
    var userName: String? by mutableStateOf(null)
        private set

    private val _chats = mutableStateListOf<ChatDto>()
    val chats: List<ChatDto> get() = _chats

    private val _messages = mutableStateMapOf<String, List<ChatMessageDto>>()
    val messages: Map<String, List<ChatMessageDto>> get() = _messages

    private var client: ChattrClient? = null

    private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val toastEvents: Flow<String> = _toastEvents.asSharedFlow()

    fun connect(username: String, host: String, port: Int, scope: CoroutineScope) {
        userName = username
        client = ChattrClient(username, host, port)
        scope.launch {
            client!!.messages.collect { msg ->
                if (msg.from.isEmpty()) {
                    // for system messages
                    _toastEvents.emit(msg.text)
                } else {
                    // TODO: this will not work for group chats, need a better way
                    val chatId = listOf(msg.from, username).sorted().joinToString("#")
                    _messages[chatId] = (_messages[chatId] ?: emptyList()) + msg
                    upsertChat(chatId, msg.from, msg)
                }
            }
        }
    }

    suspend fun sendMessage(to: String, text: String) {
        val from = userName ?: return
        val id = UuidCreator.getTimeOrderedEpoch().toString()
        val msg = ChatMessageDto(id, from, to, text, Clock.System.now())
        client?.send(msg)
        val chatId = listOf(from, to).sorted().joinToString("#")
        _messages[chatId] = (_messages[chatId] ?: emptyList()) + msg
        upsertChat(chatId, to, msg)
    }

    fun openChat(recipient: String) {
        val from = userName ?: return
        val chatId = listOf(from, recipient).sorted().joinToString("#")
        if (_chats.none { it.id == chatId }) {
            _chats.add(ChatDto(chatId, recipient, Clock.System.now(), ""))
        }
    }

    private fun upsertChat(chatId: String, otherUser: String, msg: ChatMessageDto) {
        val chat = ChatDto(chatId, otherUser, msg.receivedAt, msg.text)
        val idx = _chats.indexOfFirst { it.id == chatId }
        if (idx >= 0) _chats[idx] = chat else _chats.add(chat)
    }
}
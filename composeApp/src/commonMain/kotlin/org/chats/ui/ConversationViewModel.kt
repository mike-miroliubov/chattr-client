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
import org.chats.repository.ChatRepository
import org.chats.repository.MessageRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ConversationViewModel(
    private val messageRepo: MessageRepository? = null,
    private val chatRepo: ChatRepository? = null
) {
    var userName: String? by mutableStateOf(null)
        private set

    private val _chats = mutableStateListOf<ChatDto>()
    val chats: List<ChatDto> get() = _chats

    private val _messages = mutableStateMapOf<String, List<ChatMessageDto>>()
    val messages: Map<String, List<ChatMessageDto>> get() = _messages

    private var client: ChattrClient? = null
    private var scope: CoroutineScope? = null

    private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val toastEvents: Flow<String> = _toastEvents.asSharedFlow()

    suspend fun loadPersistedData() {
        val username = userName ?: return
        val chats = chatRepo?.getAll(username) ?: return
        _chats.clear()
        _chats.addAll(chats)
        for (chat in chats) {
            val msgs = messageRepo?.getMessages(chat.id, username) ?: emptyList()
            if (msgs.isNotEmpty()) {
                _messages[chat.id] = msgs
            }
        }
    }

    fun connect(username: String, host: String, port: Int, scope: CoroutineScope) {
        userName = username
        this.scope = scope
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
                    scope.launch {
                        messageRepo?.saveMessage(msg, username)
                        chatRepo?.upsert(ChatDto(chatId, msg.from, msg.receivedAt, msg.text), username)
                    }
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
        messageRepo?.saveMessage(msg, from)
        chatRepo?.upsert(ChatDto(chatId, to, msg.receivedAt, msg.text), from)
    }

    fun openChat(recipient: String) {
        val from = userName ?: return
        val chatId = listOf(from, recipient).sorted().joinToString("#")
        if (_chats.none { it.id == chatId }) {
            val chat = ChatDto(chatId, recipient, Clock.System.now(), "")
            _chats.add(chat)
            scope?.launch { chatRepo?.upsert(chat, from) }
        }
    }

    private fun upsertChat(chatId: String, otherUser: String, msg: ChatMessageDto) {
        val chat = ChatDto(chatId, otherUser, msg.receivedAt, msg.text)
        val idx = _chats.indexOfFirst { it.id == chatId }
        if (idx >= 0) _chats[idx] = chat else _chats.add(chat)
    }
}

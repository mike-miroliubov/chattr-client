package org.chats.repository

import org.chats.dto.ChatMessageDto

interface MessageRepository {
    suspend fun saveMessage(msg: ChatMessageDto, userId: String)
    suspend fun getMessages(chatId: String, userId: String): List<ChatMessageDto>
}
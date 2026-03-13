package org.chats.repository

import org.chats.dto.ChatDto

interface ChatRepository {
    suspend fun getAll(userId: String): List<ChatDto>
    suspend fun upsert(chat: ChatDto, userId: String)
}

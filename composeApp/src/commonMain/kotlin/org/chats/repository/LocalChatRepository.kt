package org.chats.repository

import org.chats.db.dao.ChatDao
import org.chats.db.toDto
import org.chats.db.toEntity
import org.chats.dto.ChatDto

class LocalChatRepository(private val dao: ChatDao) : ChatRepository {
    override suspend fun getAll(userId: String): List<ChatDto> = dao.getAllByUser(userId).map { it.toDto() }
    override suspend fun upsert(chat: ChatDto, userId: String) = dao.upsert(chat.toEntity(userId))
}

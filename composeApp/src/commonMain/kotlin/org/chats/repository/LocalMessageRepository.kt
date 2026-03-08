package org.chats.repository

import org.chats.db.dao.MessageDao
import org.chats.db.toDto
import org.chats.db.toEntity
import org.chats.dto.ChatMessageDto

class LocalMessageRepository(private val dao: MessageDao) : MessageRepository {
    override suspend fun saveMessage(msg: ChatMessageDto, userId: String) {
        dao.insert(msg.toEntity(userId))
    }

    override suspend fun getMessages(chatId: String, userId: String): List<ChatMessageDto> =
        dao.getByUserAndChat(userId, chatId).map { it.toDto() }
}

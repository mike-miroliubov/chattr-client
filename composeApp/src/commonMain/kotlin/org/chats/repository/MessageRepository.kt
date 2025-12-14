package org.chats.repository

import org.chats.dto.MessageDto

interface MessageRepository {
    suspend fun saveMessage(msg: MessageDto)
}
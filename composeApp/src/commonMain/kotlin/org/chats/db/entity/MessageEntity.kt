package org.chats.db.entity

import androidx.room.Entity

@Entity(
    tableName = "messages",
    primaryKeys = ["userId", "id"]
)
data class MessageEntity(
    val userId: String,
    val id: String,
    val chatId: String,
    val from: String,
    val to: String,
    val text: String,
    val receivedAt: String
)

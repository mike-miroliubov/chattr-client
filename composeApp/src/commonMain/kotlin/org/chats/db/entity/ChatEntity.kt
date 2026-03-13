package org.chats.db.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "chats",
    primaryKeys = ["userId", "id"],
    indices = [Index(value = ["lastMessageAt"], orders = [Index.Order.DESC])]
)
data class ChatEntity(
    val userId: String,
    val id: String,
    val fromUserId: String,
    val lastMessageAt: String,
    val lastText: String
)

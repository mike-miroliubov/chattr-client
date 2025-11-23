@file:OptIn(ExperimentalTime::class)

package org.chats.dto

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class MessageDto(
    val id: String,
    val from: String,
    val text: String,
    val receivedAt: Instant
)

data class ChatDto(
    val id: String,
    val fromUserId: String,
    val lastMessageAt: Instant,
    val lastMessageText: String
)
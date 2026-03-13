@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.chats.db

import kotlin.time.Instant
import org.chats.db.entity.ChatEntity
import org.chats.db.entity.MessageEntity
import org.chats.dto.ChatDto
import org.chats.dto.ChatMessageDto

fun ChatEntity.toDto(): ChatDto = ChatDto(
    id = id,
    fromUserId = fromUserId,
    lastMessageAt = Instant.parse(lastMessageAt),
    lastText = lastText
)

fun ChatDto.toEntity(userId: String): ChatEntity = ChatEntity(
    id = id,
    userId = userId,
    fromUserId = fromUserId,
    lastMessageAt = lastMessageAt.toString(),
    lastText = lastText
)

fun MessageEntity.toDto(): ChatMessageDto = ChatMessageDto(
    id = id,
    from = from,
    to = to,
    text = text,
    receivedAt = Instant.parse(receivedAt)
)

fun ChatMessageDto.toEntity(userId: String): MessageEntity {
    val chatId = listOf(from, to).sorted().joinToString("#")
    return MessageEntity(
        id = id,
        userId = userId,
        chatId = chatId,
        from = from,
        to = to,
        text = text,
        receivedAt = receivedAt.toString()
    )
}

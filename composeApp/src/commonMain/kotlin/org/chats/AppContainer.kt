package org.chats

import org.chats.db.ChattDatabase
import org.chats.repository.LocalChatRepository
import org.chats.repository.LocalMessageRepository
import org.chats.repository.LocalUserRepository
import org.chats.repository.MessageRepository
import org.chats.repository.UserRepository

class AppContainer(
    val serverHost: String = "localhost",
    val serverPort: Int = 80,
    private val database: ChattDatabase
) {
    val messageRepository: MessageRepository = LocalMessageRepository(database.messageDao())
    val chatRepository = LocalChatRepository(database.chatDao())
    val userRepository: UserRepository = LocalUserRepository(database.userDao())
}

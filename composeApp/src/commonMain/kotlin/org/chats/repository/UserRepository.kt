package org.chats.repository

interface UserRepository {
    suspend fun getByUsername(username: String): String?
    suspend fun insertIfNotExists(username: String)
}

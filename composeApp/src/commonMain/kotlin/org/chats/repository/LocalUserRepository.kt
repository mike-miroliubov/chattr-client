package org.chats.repository

import org.chats.db.dao.UserDao
import org.chats.db.entity.UserEntity

class LocalUserRepository(private val dao: UserDao) : UserRepository {
    override suspend fun getByUsername(username: String): String? =
        dao.getByUsername(username)?.username

    override suspend fun insertIfNotExists(username: String) =
        dao.insert(UserEntity(username))
}

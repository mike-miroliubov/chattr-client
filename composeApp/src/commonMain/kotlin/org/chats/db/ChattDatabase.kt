package org.chats.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import org.chats.db.dao.ChatDao
import org.chats.db.dao.MessageDao
import org.chats.db.dao.UserDao
import org.chats.db.entity.ChatEntity
import org.chats.db.entity.MessageEntity
import org.chats.db.entity.UserEntity

@Database(entities = [UserEntity::class, ChatEntity::class, MessageEntity::class], version = 4)
@ConstructedBy(ChattDatabaseConstructor::class)
abstract class ChattDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object ChattDatabaseConstructor : RoomDatabaseConstructor<ChattDatabase> {
    override fun initialize(): ChattDatabase
}

package org.chats.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.chats.db.entity.MessageEntity

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE userId = :userId AND chatId = :chatId ORDER BY receivedAt ASC")
    suspend fun getByUserAndChat(userId: String, chatId: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: MessageEntity)
}

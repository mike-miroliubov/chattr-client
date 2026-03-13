package org.chats.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import org.chats.db.entity.ChatEntity

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats WHERE userId = :userId ORDER BY lastMessageAt DESC")
    suspend fun getAllByUser(userId: String): List<ChatEntity>

    @Upsert
    suspend fun upsert(chat: ChatEntity)
}

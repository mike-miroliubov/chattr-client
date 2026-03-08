@file:OptIn(ExperimentalTime::class)

package org.chats.repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.chats.db.ChattDatabase
import org.chats.dto.ChatDto
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class LocalChatRepositoryTest {
    private lateinit var db: ChattDatabase
    private lateinit var repository: LocalChatRepository

    @BeforeEach
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder<ChattDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        repository = LocalChatRepository(db.chatDao())
    }

    @AfterEach
    fun tearDown() {
        db.close()
    }

    @Test
    fun `should return empty list when no chats exist`() = runBlocking {
        // given
        // no chats saved

        // when
        val result = repository.getAll("alice")

        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `should save and retrieve a chat`(): Unit = runBlocking {
        // given
        val chat = ChatDto(
            id = "alice#bob",
            fromUserId = "bob",
            lastMessageAt = Instant.parse("2025-01-01T12:00:00Z"),
            lastText = "hello"
        )

        // when
        repository.upsert(chat, "alice")
        val result = repository.getAll("alice")

        // then
        assertThat(result).containsExactly(chat)
    }

    @Test
    fun `should return chats ordered by lastMessageAt descending`(): Unit = runBlocking {
        // given
        val older = ChatDto("alice#bob", "bob", Instant.parse("2025-01-01T10:00:00Z"), "old")
        val newer = ChatDto("alice#carol", "carol", Instant.parse("2025-01-01T12:00:00Z"), "new")
        val newest = ChatDto("alice#dave", "dave", Instant.parse("2025-01-01T14:00:00Z"), "newest")

        repository.upsert(older, "alice")
        repository.upsert(newest, "alice")
        repository.upsert(newer, "alice")

        // when
        val result = repository.getAll("alice")

        // then
        assertThat(result).containsExactly(newest, newer, older)
    }

    @Test
    fun `should update existing chat on upsert`(): Unit = runBlocking {
        // given
        val original = ChatDto("alice#bob", "bob", Instant.parse("2025-01-01T10:00:00Z"), "hello")
        repository.upsert(original, "alice")

        val updated = ChatDto("alice#bob", "bob", Instant.parse("2025-01-01T12:00:00Z"), "updated message")

        // when
        repository.upsert(updated, "alice")
        val result = repository.getAll("alice")

        // then
        assertThat(result).hasSize(1)
        assertThat(result.first().lastText).isEqualTo("updated message")
        assertThat(result.first().lastMessageAt).isEqualTo(Instant.parse("2025-01-01T12:00:00Z"))
    }

    @Test
    fun `should only return chats for the given user`(): Unit = runBlocking {
        // given
        val aliceChat = ChatDto("alice#carol", "carol", Instant.parse("2025-01-01T12:00:00Z"), "hi carol")
        val bobChat = ChatDto("bob#carol", "carol", Instant.parse("2025-01-01T12:00:00Z"), "hi carol")

        repository.upsert(aliceChat, "alice")
        repository.upsert(bobChat, "bob")

        // when
        val aliceResult = repository.getAll("alice")
        val bobResult = repository.getAll("bob")

        // then
        assertThat(aliceResult).containsExactly(aliceChat)
        assertThat(bobResult).containsExactly(bobChat)
    }
}

@file:OptIn(ExperimentalTime::class)

package org.chats.repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.chats.db.ChattDatabase
import org.chats.dto.ChatMessageDto
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class LocalMessageRepositoryTest {
    private lateinit var db: ChattDatabase
    private lateinit var repository: LocalMessageRepository

    @BeforeEach
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder<ChattDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        repository = LocalMessageRepository(db.messageDao())
    }

    @AfterEach
    fun tearDown() {
        db.close()
    }

    @Test
    fun `should return empty list when no messages exist for chat`() = runBlocking {
        // given
        val chatId = "alice#bob"

        // when
        val result = repository.getMessages(chatId, "alice")

        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `should save and retrieve messages by chatId`(): Unit = runBlocking {
        // given
        val msg = ChatMessageDto(
            id = "1",
            from = "alice",
            to = "bob",
            text = "hello",
            receivedAt = Instant.parse("2025-01-01T12:00:00Z")
        )

        // when
        repository.saveMessage(msg, "alice")
        val result = repository.getMessages("alice#bob", "alice")

        // then
        assertThat(result).containsExactly(msg)
    }

    @Test
    fun `should return messages ordered by receivedAt ascending`(): Unit = runBlocking {
        // given
        val msg1 = ChatMessageDto("1", "alice", "bob", "first", Instant.parse("2025-01-01T10:00:00Z"))
        val msg2 = ChatMessageDto("2", "bob", "alice", "second", Instant.parse("2025-01-01T11:00:00Z"))
        val msg3 = ChatMessageDto("3", "alice", "bob", "third", Instant.parse("2025-01-01T12:00:00Z"))

        repository.saveMessage(msg3, "alice")
        repository.saveMessage(msg1, "alice")
        repository.saveMessage(msg2, "alice")

        // when
        val result = repository.getMessages("alice#bob", "alice")

        // then
        assertThat(result).containsExactly(msg1, msg2, msg3)
    }

    @Test
    fun `should not return messages from a different chat`(): Unit = runBlocking {
        // given
        val msg1 = ChatMessageDto("1", "alice", "bob", "hello", Instant.parse("2025-01-01T12:00:00Z"))
        val msg2 = ChatMessageDto("2", "alice", "carol", "hey", Instant.parse("2025-01-01T12:00:00Z"))

        repository.saveMessage(msg1, "alice")
        repository.saveMessage(msg2, "alice")

        // when
        val result = repository.getMessages("alice#bob", "alice")

        // then
        assertThat(result).containsExactly(msg1)
    }

    @Test
    fun `should ignore duplicate message on insert`(): Unit = runBlocking {
        // given
        val msg = ChatMessageDto("1", "alice", "bob", "hello", Instant.parse("2025-01-01T12:00:00Z"))
        repository.saveMessage(msg, "alice")

        // when
        repository.saveMessage(msg, "alice")
        val result = repository.getMessages("alice#bob", "alice")

        // then
        assertThat(result).hasSize(1)
    }

    @Test
    fun `should only return messages for the given user`(): Unit = runBlocking {
        // given
        val aliceMsg = ChatMessageDto("1", "alice", "carol", "hi", Instant.parse("2025-01-01T12:00:00Z"))
        val bobMsg = ChatMessageDto("2", "bob", "carol", "hey", Instant.parse("2025-01-01T12:00:00Z"))

        repository.saveMessage(aliceMsg, "alice")
        repository.saveMessage(bobMsg, "bob")

        // when
        val aliceResult = repository.getMessages("alice#carol", "alice")
        val bobResult = repository.getMessages("bob#carol", "bob")

        // then
        assertThat(aliceResult).containsExactly(aliceMsg)
        assertThat(bobResult).containsExactly(bobMsg)
    }
}

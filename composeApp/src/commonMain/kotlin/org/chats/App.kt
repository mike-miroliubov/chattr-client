@file:OptIn(ExperimentalTime::class)

package org.chats

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.chats.dto.ChatDto
import org.chats.dto.MessageDto
import org.chats.ui.ConversationViewModel
import org.chats.ui.Conversations
import org.chats.ui.Theme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private val chats = listOf(
    ChatDto("foo#kite", "foo", Clock.System.now(), "ho"),
    ChatDto("bar#kite", "bar", Clock.System.now(),
        "Lorem ipsum dolor sit amet consectetur adipiscing elit quisque faucibus ex sapien vitae pellentesque sem.")
)

private val messages = mapOf(
    "foo#kite" to listOf(
        MessageDto(
            "", "foo", """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit, 
            sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. 
            Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip 
            ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore 
            eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt 
            mollit anim id est laborum.
        """.trimIndent().replace("\n", ""), Clock.System.now()
        ),
        MessageDto("", "foo", "hey", Clock.System.now()),
        MessageDto(id = "", from = "kite", text = "lol", receivedAt = Clock.System.now()),
        MessageDto("", "foo", "yeah", Clock.System.now()),
    ),
    "bar#kite" to listOf(
        MessageDto("", "bar", "let's go!", Clock.System.now()),
    )
)

@Composable
@Preview
fun App() {
    val viewModel = remember { ConversationViewModel() }

    Theme {
        Scaffold { padding ->
            Conversations(
                viewModel = viewModel,
                chats = chats,
                messages = messages,
                modifier = Modifier.padding(padding)
            )
        }
    }
}
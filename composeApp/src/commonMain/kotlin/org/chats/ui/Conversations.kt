package org.chats.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.chats.dto.ChatDto

@Composable
fun Conversations(chats: List<ChatDto>, onSelect: (ChatDto) -> Unit) {
    var selectedChat: ChatDto? by remember { mutableStateOf(null) }
    LazyColumn {
        items(chats) {
            Conversation(it, it == selectedChat, onSelect = { _ ->
                selectedChat = it
                onSelect(it)
            })
        }
    }
}

@Composable
fun Conversation(chat: ChatDto, isSelected: Boolean, onSelect: (ChatDto) -> Unit) {
    val surfaceColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
    )

    Surface(
        modifier = Modifier.padding(4.dp).clickable { onSelect(chat) },
        shadowElevation = 5.dp,
        shape = MaterialTheme.shapes.extraSmall,
        color = surfaceColor,
    ) {
        Column(modifier = Modifier.padding(4.dp).safeContentPadding().fillMaxWidth()) {
            Text(
                text = chat.fromUserId,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = chat.lastMessageText,
                modifier = Modifier.padding(4.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
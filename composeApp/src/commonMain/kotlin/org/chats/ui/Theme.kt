package org.chats.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = darkColorScheme(),
        typography = Typography,
        shapes = Shapes(),
        content = content
    )
}
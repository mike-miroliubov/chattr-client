@file:OptIn(ExperimentalTime::class)

package org.chats

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlin.time.ExperimentalTime

fun main() = application {
    val container = AppContainer()

    Window(
        onCloseRequest = ::exitApplication,
        title = "chattr-messenger-client",
        state = rememberWindowState(
            width = 900.dp
        )
    ) {
        App(container)
    }
}
@file:OptIn(ExperimentalTime::class)

package org.chats

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.time.ExperimentalTime

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "chattr-messenger-client",
    ) {
        App()
    }
}
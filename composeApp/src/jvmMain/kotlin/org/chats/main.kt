package org.chats

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.chats.db.ChattDatabase
import java.io.File

fun main() = application {
    val container = AppContainer(database = createDatabase())

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

private fun createDatabase(): ChattDatabase {
    val dbPath = "${System.getProperty("user.home")}/.chattr/chattr.db"
    File(dbPath).parentFile?.mkdirs()
    return Room.databaseBuilder<ChattDatabase>(name = dbPath)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(true)
        .build()
}

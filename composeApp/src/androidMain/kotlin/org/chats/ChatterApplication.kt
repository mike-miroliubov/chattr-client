package org.chats

import android.app.Application
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import org.chats.db.ChattDatabase

class ChatterApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(
            database = Room.databaseBuilder<ChattDatabase>(
                context = this,
                name = getDatabasePath("chattr.db").absolutePath
            ).setDriver(AndroidSQLiteDriver())
                .fallbackToDestructiveMigration(true)
                .build()
        )
    }
}

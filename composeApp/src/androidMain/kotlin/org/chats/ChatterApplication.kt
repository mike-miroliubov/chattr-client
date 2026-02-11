package org.chats

import android.app.Application

class ChatterApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
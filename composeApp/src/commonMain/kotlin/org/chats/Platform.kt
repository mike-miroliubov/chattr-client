package org.chats

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
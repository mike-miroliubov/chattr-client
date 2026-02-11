package org.chats

import org.chats.ui.ConversationViewModel

class AppContainer {
    fun getConversationViewModel(): ConversationViewModel {
        return ConversationViewModel()
    }
}
package org.chats.ui

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.chats.repository.UserRepository

class LoginViewModel(private val userRepository: UserRepository) {
    private val _loginFlow: MutableSharedFlow<String> = MutableSharedFlow()
    val loginFlow: SharedFlow<String> = _loginFlow.asSharedFlow()

    suspend fun login(username: String) {
        userRepository.insertIfNotExists(username)
        _loginFlow.emit(username)
    }
}

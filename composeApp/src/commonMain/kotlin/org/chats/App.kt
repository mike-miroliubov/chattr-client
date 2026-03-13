package org.chats

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.chats.ui.ConversationViewModel
import org.chats.ui.Conversations
import org.chats.ui.LoginScreen
import org.chats.ui.LoginViewModel
import org.chats.ui.Theme

@Composable
fun App(container: AppContainer) {
    val loginViewModel = remember { LoginViewModel(container.userRepository) }
    val viewModel = remember { ConversationViewModel(container.messageRepository, container.chatRepository) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        scope.launch {
            viewModel.toastEvents.collect { message ->
                snackbarHostState.showSnackbar(message)
            }
        }
        loginViewModel.loginFlow.take(1).collect { userName ->
            viewModel.connect(userName, container.serverHost, container.serverPort, scope)
            viewModel.loadPersistedData()
        }
    }

    Theme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            if (viewModel.userName == null) {
                LoginScreen(loginViewModel)
            } else {
                Conversations(
                    viewModel = viewModel,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

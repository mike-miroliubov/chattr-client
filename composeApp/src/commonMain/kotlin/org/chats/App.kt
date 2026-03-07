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
import org.chats.ui.ConversationViewModel
import org.chats.ui.Conversations
import org.chats.ui.LoginScreen
import org.chats.ui.Theme

@Composable
fun App(container: AppContainer) {
    val viewModel = remember { ConversationViewModel() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.toastEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Theme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            if (viewModel.userName == null) {
                LoginScreen { username ->
                    viewModel.connect(username, container.serverHost, container.serverPort, scope)
                }
            } else {
                Conversations(
                    viewModel = viewModel,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}
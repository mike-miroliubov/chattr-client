@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalTime::class)

package org.chats.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.*
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.chats.dto.ChatDto
import org.chats.dto.ChatMessageDto
import kotlin.time.ExperimentalTime

/*
 * Copied from https://github.com/android/adaptive-apps-samples/blob/main/CanonicalLayouts/list-detail-compose/app/src/main/java/com/example/listdetailcompose/ui/ListDetailSample.kt
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)
@Composable
fun Conversations(viewModel: ConversationViewModel, modifier: Modifier) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()
    val scope = rememberCoroutineScope()

    var selectedChatId: String? by rememberSaveable { mutableStateOf(null) }

    BackHandler(navigator.canNavigateBack()) {
        scope.launch { navigator.navigateBack() }
    }

    SharedTransitionLayout {
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                val isDetailVisible =
                    navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded
                val chatId = selectedChatId

                AnimatedPane {
                    ListContent(
                        chats = viewModel.chats,
                        selectionState = if (isDetailVisible && chatId != null) {
                            SelectionVisibilityState.ShowSelection(chatId)
                        } else {
                            SelectionVisibilityState.NoSelection
                        },
                        onChatClick = { chat ->
                            selectedChatId = chat.id
                            scope.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                            }
                        },
                        onNewChat = { recipient ->
                            viewModel.openChat(recipient)
                            val from = viewModel.userName ?: return@ListContent
                            selectedChatId = listOf(from, recipient).sorted().joinToString("#")
                            scope.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                            }
                        },
                        isDetailsVisible = isDetailVisible,
                        modifier = modifier
                    )
                }
            },
            detailPane = {
                val chatId = selectedChatId
                val chat = chatId?.let { id -> viewModel.chats.find { it.id == id } }
                val messages = chatId?.let(viewModel.messages::get)

                AnimatedPane {
                    DetailContent(
                        messages = messages,
                        chatPartner = chat?.fromUserId,
                        onSend = { text ->
                            val to = chat?.fromUserId ?: return@DetailContent
                            scope.launch { viewModel.sendMessage(to, text) }
                        },
                        modifier = modifier
                    )
                }
            },
            paneExpansionState = rememberPaneExpansionState(navigator.scaffoldValue),
            paneExpansionDragHandle = { state ->
                val interactionSource = remember { MutableInteractionSource() }
                VerticalDragHandle(
                    modifier =
                        Modifier.paneExpansionDraggable(
                            state,
                            LocalMinimumInteractiveComponentSize.current,
                            interactionSource
                        ), interactionSource = interactionSource
                )
            }
        )
    }
}

/**
 * The description of the selection state for the [ListContent]
 */
sealed interface SelectionVisibilityState {

    /**
     * No selection should be shown, and each item should be clickable.
     */
    object NoSelection : SelectionVisibilityState

    /**
     * Selection state should be shown, and each item should be selectable.
     */
    data class ShowSelection(
        /**
         * The index of the word that is selected.
         */
        val selectedChatId: String
    ) : SelectionVisibilityState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListContent(
    chats: List<ChatDto>,
    selectionState: SelectionVisibilityState,
    onChatClick: (chat: ChatDto) -> Unit,
    onNewChat: (recipient: String) -> Unit,
    modifier: Modifier = Modifier,
    isDetailsVisible: Boolean,
) {
    val (searchQuery, setSearchQuery) = rememberSaveable { mutableStateOf("") }
    val filteredChats = if (searchQuery.isBlank()) chats
    else chats.filter { it.fromUserId.contains(searchQuery, ignoreCase = true) }
    var showNewChatDialog by remember { mutableStateOf(false) }

    if (showNewChatDialog) {
        NewChatDialog(
            onConfirm = { recipient ->
                showNewChatDialog = false
                onNewChat(recipient)
            },
            onDismiss = { showNewChatDialog = false }
        )
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SearchField(
                    query = searchQuery,
                    onQueryChange = { setSearchQuery(it) },
                    onSearch = {},
                    onExpandedChange = {},
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { showNewChatDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "New chat",
                    )
                }
            }
            HorizontalDivider()
            SearchResults(modifier, selectionState, filteredChats, onChatClick, isDetailsVisible)
        }
        if (isDetailsVisible) {
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun NewChatDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var recipient by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New conversation") },
        text = {
            OutlinedTextField(
                value = recipient,
                onValueChange = { recipient = it },
                label = { Text("Recipient username") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (recipient.isNotBlank()) onConfirm(recipient.trim()) },
                enabled = recipient.isNotBlank(),
            ) {
                Text("Start")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SearchResults(
    modifier: Modifier,
    selectionState: SelectionVisibilityState,
    chats: List<ChatDto>,
    onChatClick: (ChatDto) -> Unit,
    isDetailsVisible: Boolean
) {
    LazyColumn(
        modifier = modifier
            .then(
                when (selectionState) {
                    SelectionVisibilityState.NoSelection -> Modifier
                    is SelectionVisibilityState.ShowSelection -> Modifier.selectableGroup()
                }
            )
            .fillMaxHeight(),
    ) {
        items(chats) { chat ->

            val interactionModifier = when (selectionState) {
                is SelectionVisibilityState.NoSelection -> {
                    Modifier.clickable(
                        onClick = { onChatClick(chat) }
                    )
                }

                is SelectionVisibilityState.ShowSelection -> {
                    Modifier.selectable(
                        selected = chat.id == selectionState.selectedChatId,
                        onClick = { onChatClick(chat) }
                    )
                }
            }
            val containerColor = when (selectionState) {
                is SelectionVisibilityState.NoSelection -> MaterialTheme.colorScheme.surface
                is SelectionVisibilityState.ShowSelection ->
                    if (chat.id == selectionState.selectedChatId) {
                        MaterialTheme.colorScheme.surfaceTint
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = containerColor),
                modifier = Modifier
                    .then(interactionModifier)
                    .fillMaxWidth(if (isDetailsVisible) 0.99f else 1f),
                shape = RoundedCornerShape(0.dp)
            ) {

                Column {
                    Row(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Avatar(
                            chat.fromUserId,
                            color = if (shouldShowSelection(
                                    selectionState,
                                    chat.id
                                )
                            ) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
                            textColor = if (shouldShowSelection(
                                    selectionState,
                                    chat.id
                                )
                            ) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        Column(verticalArrangement = Arrangement.SpaceBetween) {
                            Text(text = chat.fromUserId, style = MaterialTheme.typography.titleMedium)
                            Text(
                                chat.lastText,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

private fun shouldShowSelection(selectionState: SelectionVisibilityState, chatId: String): Boolean {
    return selectionState is SelectionVisibilityState.ShowSelection && selectionState.selectedChatId == chatId
}

@Composable
fun Avatar(
    userId: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Box(
        modifier = modifier.then(
            Modifier.size(40.dp)
                .clip(CircleShape)
                .background(color)
        ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = userId.firstOrNull()?.uppercase() ?: "",
            color = textColor,
            fontSize = 18.sp
        )
    }
}

/**
 * The content for the detail pane.
 */
@Composable
private fun DetailContent(
    modifier: Modifier = Modifier,
    messages: List<ChatMessageDto>?,
    chatPartner: String?,
    onSend: (String) -> Unit,
) {
    val msgs = messages ?: listOf()
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(msgs.size) {
        if (msgs.isNotEmpty()) listState.animateScrollToItem(msgs.size - 1)
    }

    fun shouldShowFrom(index: Int, msg: ChatMessageDto): Boolean =
        index == 0 || msgs[index - 1].from != msg.from

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp, horizontal = 16.dp)
        ) {
            itemsIndexed(msgs) { index, it ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    if (shouldShowFrom(index, it)) {
                        Avatar(it.from, modifier = Modifier.padding(top = 2.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                    } else {
                        Spacer(modifier = Modifier.width(52.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        if (shouldShowFrom(index, it)) {
                            Text(text = it.from, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(text = it.text)
                    }

                    Text(
                        it.receivedAt
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .let { t -> "${t.hour}:${t.minute.toString().padStart(2, '0')}" },
                        textAlign = TextAlign.End,
                    )
                }
            }
        }

        if (chatPartner != null) {
            HorizontalDivider()
            MessageInput(onSend = onSend)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageInput(onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }
    val focused = interactionSource.collectIsFocusedAsState().value
    val colors = SearchBarDefaults.inputFieldColors()
    val textColor = colors.textColor(enabled = true, isError = false, focused = focused)

    fun submit() {
        if (text.isNotBlank()) {
            onSend(text.trim())
            text = ""
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp).fillMaxWidth()
    ) {
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .weight(1f)
                .height(SearchBarDefaults.InputFieldHeight),
            singleLine = true,
            textStyle = LocalTextStyle.current.merge(TextStyle(color = textColor)),
            cursorBrush = SolidColor(colors.cursorColor),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { submit() }),
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = text,
                    innerTextField = innerTextField,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    placeholder = { Text("Message...") },
                    shape = SearchBarDefaults.inputFieldShape,
                    colors = colors,
                    contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(),
                    enabled = true,
                    interactionSource = interactionSource,
                    container = {
                        val containerColor by animateColorAsState(
                            targetValue = colors.containerColor(
                                enabled = true,
                                isError = false,
                                focused = focused,
                            )
                        )
                        Box(Modifier.background(containerColor, SearchBarDefaults.inputFieldShape))
                    },
                )
            },
        )
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(onClick = ::submit, enabled = text.isNotBlank()) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = if (text.isNotBlank()) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
    }
}
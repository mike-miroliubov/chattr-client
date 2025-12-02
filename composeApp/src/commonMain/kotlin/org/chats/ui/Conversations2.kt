@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalTime::class)

package org.chats.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.chats.dto.ChatDto
import org.chats.dto.MessageDto
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/*
 * Copied from https://github.com/android/adaptive-apps-samples/blob/main/CanonicalLayouts/list-detail-compose/app/src/main/java/com/example/listdetailcompose/ui/ListDetailSample.kt
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)
@Composable
fun Conversations(chats: List<ChatDto>, messages: Map<String, List<MessageDto>>, modifier: Modifier) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()
    val scope = rememberCoroutineScope()
    val isListAndDetailVisible =
        navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded &&
                navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded

    var selectedChatId: String? by rememberSaveable { mutableStateOf(null) }

    BackHandler(navigator.canNavigateBack()) {
        scope.launch { navigator.navigateBack() }
    }

    SharedTransitionLayout {
        AnimatedContent(targetState = isListAndDetailVisible) { _ ->
            ListDetailPaneScaffold(
                directive = navigator.scaffoldDirective,
                value = navigator.scaffoldValue,
                listPane = {
                    val isDetailVisible =
                        navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded
                    val chatId = selectedChatId

                    AnimatedPane {
                        ListContent(
                            chats = chats,
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
                            isDetailsVisible = isDetailVisible,
                            modifier = modifier
                        )
                    }
                },
                detailPane = {
                    val messages = selectedChatId?.let(messages::get)

                    AnimatedPane {
                        DetailContent(messages, modifier)
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
    modifier: Modifier = Modifier,
    isDetailsVisible: Boolean,
) {
    val (searchQuery, setSearchQuery) = rememberSaveable { mutableStateOf("") }
    var filteredChats by rememberSaveable { mutableStateOf(chats) }
    var expanded by rememberSaveable { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier.padding(16.dp)
                .fillMaxWidth()
            ) {
                SearchField(
                    query = searchQuery,
                    onQueryChange = {
                        setSearchQuery(it)
                        filteredChats = listOf(
                            ChatDto("example", "foo", Clock.System.now(),
                                "This is a search result"),
                        )
                    },
                    onSearch = {},
                    onExpandedChange = {
                        expanded = it
                        println("Expanded: $it")
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            HorizontalDivider()
            SearchResults(modifier, selectionState, filteredChats, onChatClick, isDetailsVisible)
        }
        if (isDetailsVisible) {
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(), // Makes the divider span the full height of the Row
            )
        }
    }
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
                                chat.lastMessageText,
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
    color: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    modifier: Modifier = Modifier
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
    messages: List<MessageDto>?,
    modifier: Modifier = Modifier,
) {

    val msgs = messages ?: listOf()
    fun shouldShowFrom(index: Int, msg: MessageDto): Boolean = index == 0 || msgs[index - 1].from != msg.from

    LazyColumn(
        modifier = modifier
            .padding(vertical = 16.dp, horizontal = 16.dp)
    ) {
        itemsIndexed(msgs) { index, it ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                if (shouldShowFrom(index, it)) {
                    Avatar(it.from, modifier = modifier.padding(top = 2.dp))
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
                        .let { "${it.hour}:${it.minute}" }, textAlign = TextAlign.Right)

            }

        }
    }
}
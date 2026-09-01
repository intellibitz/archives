/**
 * BLUKIT GROUP FIELD
 *
 * A high-connection field for specific Group contexts.
 */
package cc.thevar.blukit.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Group
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.viewmodels.ConnectionUiState
import cc.thevar.blukit.ui.viewmodels.ConnectionViewModel
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.components.BlukitToolbar
import cc.thevar.blukit.ui.components.MessageCanvas
import cc.thevar.blukit.ui.components.MessageActionMenu
import cc.thevar.blukit.ui.components.BlukitFieldScaffold
import cc.thevar.blukit.ui.components.MessageHub
import org.koin.androidx.compose.koinViewModel

import androidx.compose.foundation.lazy.LazyColumn
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import androidx.paging.compose.itemContentType

import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.Flow

import androidx.compose.ui.res.stringResource
import cc.thevar.blukit.R

/**
 * THE GROUP FIELD: Focuses on a specific Group.
 */
@Composable
fun GroupField(
    state: ConnectionUiState,
    localDeviceId: String,
    header: @Composable () -> Unit,
    groupId: String,
    pagedMessagesFlow: Flow<PagingData<Message>>,
    highConnectionMessages: List<Message> = emptyList(),
    onVote: (String, Int) -> Unit = { _, _ -> },
    isSearchActive: Boolean = false,
    onSearchToggle: () -> Unit = {},
    onNavigateToMessage: (String) -> Unit = {},
    onSourceLongClick: (Source) -> Unit = {},
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    userNickname: String = "",
    onShowTimeline: () -> Unit = {},
    onResetProfile: () -> Unit = {},
    onBack: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    onSend: (String) -> Unit = {},
    onAcceptRadio: (Source) -> Unit = {},
    onDenyRadio: (Source) -> Unit = {},
    onNavigateToLiveFeed: () -> Unit = {},
    onStartWhisper: () -> Unit = {},
    onStartSubGroup: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onInputFocusChange: (Boolean) -> Unit = {},
    trend: String? = null
) {
    val pagedMessages = pagedMessagesFlow.collectAsLazyPagingItems()
    val group = state.session.groups.find { it.id == groupId }
    val members = state.crowd.scannedDevices.filter { it.id in (group?.allMemberIds ?: emptySet()) || it.persistentId in (group?.allMemberIds ?: emptySet()) }
    
    // We map the paged messages to Source-Message pairs
    // In a production app, we'd use paging's mapping functions in the ViewModel/Repository
    // But for this refactor, we'll use an adapter approach.
    
    var selectedMessageForMenu by remember { mutableStateOf<Message?>(null) }
    var messageText by remember { mutableStateOf("") }

    BlukitFieldScaffold(
        header = header,
        entries = {
            Column(modifier = Modifier.fillMaxSize()) {
                MessageCanvas(
                    highConnectionMessages = highConnectionMessages,
                    themeColor = StealthRose,
                    onMessageClick = { onNavigateToMessage(it) }
                )

                // ConnectionTicker with paged items
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    reverseLayout = true,
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                ) {
                    items(
                        count = pagedMessages.itemCount,
                        key = pagedMessages.itemKey { it.messageId }
                    ) { index ->
                        val message = pagedMessages[index]
                        if (message != null) {
                            val source = members.find { it.id == message.senderId || it.persistentId == message.senderId } 
                                ?: Source(id = message.senderId, name = message.senderName, emoji = message.senderEmoji ?: "👤")
                            
                            MessageItem(
                                message = message,
                                isSelected = source.id in state.crowd.selectedDevices,
                                senderSource = source,
                                replyCount = 0,
                                isPulsed = source.id in state.crowd.pulsedPeers,
                                isMe = message.senderId == localDeviceId,
                                isGrouped = true,
                                isMutual = source.id in state.session.connectedTies,
                                rowId = source.id,
                                onMessageClick = { onNavigateToMessage(message.messageId) },
                                onSourceLongClick = { onSourceLongClick(source) }
                            )
                        }
                    }
                }

                MessageHub(
                    currentRoute = Route.GroupField(groupId),
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSend = { 
                        onSend(messageText)
                        messageText = ""
                    },
                    messageCount = pagedMessages.itemCount,
                    incomingRadioRequests = state.crowd.incomingRadioRequests,
                    selectedDevices = state.crowd.selectedDevices,
                    onAcceptRadio = onAcceptRadio,
                    onDenyRadio = onDenyRadio,
                    onStartWhisper = onStartWhisper,
                    onStartSubGroup = onStartSubGroup, 
                    onClearSelection = onClearSelection,
                    onAttachFile = { },
                    isSearchMode = isSearchActive,
                    onSearchToggle = onSearchToggle,
                    onFocusChange = onInputFocusChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (selectedMessageForMenu != null) {
                MessageActionMenu(
                    message = selectedMessageForMenu!!,
                    isMe = selectedMessageForMenu!!.senderId == localDeviceId,
                    onInvite = { onStartWhisper() },
                    onDelete = { },
                    onDismiss = { selectedMessageForMenu = null },
                    onBroadcast = { },
                    onVote = { weight -> onVote(selectedMessageForMenu!!.messageId, weight) }
                )
            }
        }
    )
}

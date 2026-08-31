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

/**
 * THE GROUP FIELD: Focuses on a specific Group.
 */
@Composable
fun GroupField(
    state: ConnectionUiState,
    localDeviceId: String,
    header: @Composable () -> Unit,
    groupId: String,
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
    val group = state.session.groups.find { it.id == groupId }
    val members = state.crowd.scannedDevices.filter { it.id in (group?.allMemberIds ?: emptySet()) || it.persistentId in (group?.allMemberIds ?: emptySet()) }
    
    val connectionList by remember(state.session.messages, members, groupId) {
        derivedStateOf {
            state.session.messages
                .filter { it.groupId == groupId }
                .sortedByDescending { it.timestamp }
                .map { message ->
                    val source = members.find { it.id == message.senderId || it.persistentId == message.senderId } 
                        ?: Source(id = message.senderId, name = message.senderName, emoji = message.senderEmoji ?: "👤")
                    source to message
                }
        }
    }

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

                ConnectionTicker(
                    state = state,
                    connectionList = connectionList,
                    messageCounts = emptyMap(),
                    localDeviceId = localDeviceId,
                    localNickname = userNickname,
                    pulsedPeers = emptySet(),
                    reverseLayout = true,
                    onMessageClick = { onNavigateToMessage(it) },
                    onSourceClick = { dev -> onNavigateToMessage(dev.id) },
                    onSourceLongClick = onSourceLongClick,
                    modifier = Modifier.weight(1f),
                    themeColor = StealthRose,
                    trend = trend
                )

                MessageHub(
                    currentRoute = Route.GroupField(groupId),
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSend = { 
                        onSend(messageText)
                        messageText = ""
                    },
                    messageCount = state.session.messages.count { it.groupId == groupId },
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

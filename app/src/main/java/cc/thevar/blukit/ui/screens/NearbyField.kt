/**
 * BLUKIT NEARBY FIELD
 *
 * The root entry point of the connection field (Landing).
 * Provides a view of all nearby Groups and Sources.
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
import cc.thevar.blukit.ui.components.ConnectionNearbyView
import cc.thevar.blukit.ui.viewmodels.ConnectionUiState
import cc.thevar.blukit.ui.viewmodels.ConnectionViewModel
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.components.*
import org.koin.androidx.compose.koinViewModel

/**
 * THE NEARBY FIELD: The master feed for finding Groups and Sources.
 */
@Composable
fun NearbyField(
    state: ConnectionUiState,
    localDeviceId: String,
    header: @Composable () -> Unit,
    viewModel: ConnectionViewModel = koinViewModel(),
    pulsedPeers: Set<String> = emptySet(),
    onIdentifyUser: (String) -> Unit = {},
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    userNickname: String = "",
    onShowTimeline: () -> Unit = {},
    onResetProfile: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    harmonyReport: cc.thevar.blukit.domain.power.HarmonyReport? = null,
    onSearchToggle: (() -> Unit)? = null,
    isSearchActive: Boolean = false,
    onCreatePublicRoom: ((String, String?) -> Unit)? = null,
    onNavigateToGroup: (String) -> Unit = {},
    onNavigateToMessage: (String) -> Unit = {},
    onSourceLongClick: (Source) -> Unit = {},
    onAcceptRadio: (Source) -> Unit = {},
    onDenyRadio: (Source) -> Unit = {},
    onRestoreCrowd: (String) -> Unit = {},
    onNavigateToLiveFeed: () -> Unit = {},
    onStartWhisper: () -> Unit = {},
    onStartSubGroup: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    showAssistantGhost: Boolean = false,
    onShowAssistantGhost: () -> Unit = {},
    onDismissAssistantGhost: () -> Unit = {},
) {
    val connectionList by viewModel.connectionList.collectAsStateWithLifecycle()
    
    val messageCounts = remember(state.session.messages) {
        state.session.messages.groupBy { it.groupId ?: Group.ID_GLOBAL }.mapValues { it.value.size }
    }

    var groupNameProposal by remember { mutableStateOf("") }
    var showVault by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }


    BlukitFieldScaffold(
        header = header,
        entries = {
            Column(modifier = Modifier.fillMaxSize()) {
                IdentityStage(
                    title = "NEARBY",
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    activeGroups = state.session.groups,
                    onShowTimeline = onShowTimeline,
                    onResetProfile = onResetProfile,
                    onBack = onBack,
                    themeColor = StealthPrimary,
                    userCount = state.crowd.scannedDevices.size,
                    isDiscovery = true,
                    onModeChange = { onNavigateToLiveFeed() },
                    trailingContent = {
                        if (onSearchToggle != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(onClick = onSearchToggle, modifier = Modifier.size(28.dp)) {
                                    Icon(imageVector = if (isSearchActive) Icons.Rounded.Search else Icons.Rounded.People, contentDescription = "Toggle Search", tint = if (isSearchActive) StealthAmber else StealthPrimary, modifier = Modifier.size(20.dp))
                                }
                                Text(text = if (isSearchActive) "SEARCH" else "PEOPLE", style = MaterialTheme.typography.labelSmall, color = (if (isSearchActive) StealthAmber else StealthPrimary).copy(alpha = StealthAlphaHigh))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                )

                if (state.crowd.scannedDevices.isNotEmpty()) {
                    ConnectionTicker(
                        state = state,
                        connectionList = connectionList.map { it.source to it.latestMessage },
                        messageCounts = messageCounts,
                        localDeviceId = localDeviceId,
                        localNickname = userNickname,
                        pulsedPeers = pulsedPeers,
                        onMessageClick = { id -> if (state.session.groups.any { it.id == id }) onNavigateToGroup(id) else onNavigateToMessage(id) },
                        onSourceClick = { onNavigateToMessage(it.id) },
                        onSourceLongClick = onSourceLongClick,
                        modifier = Modifier.weight(1f),
                        reverseLayout = false,
                        trend = harmonyReport?.trendLabel
                    )
                } else {
                    ConnectionNearbyView(onSignalPresence = { onShowAssistantGhost() }, modifier = Modifier.weight(1f))
                }

                MessageHub(
                    currentRoute = Route.Nearby,
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSend = { }, 
                    messageCount = state.session.messages.size,
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
                    onCreatePublicRoom = onCreatePublicRoom,
                    onFocusChange = { },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )

    if (showAssistantGhost) {
        GroupRitualGhost(
            onNameChange = { groupNameProposal = it },
            onDone = { templateId ->
                onCreatePublicRoom?.invoke(groupNameProposal, templateId)
                onDismissAssistantGhost()
            },
            onDismiss = onDismissAssistantGhost,
            nearbyGroups = state.session.groups.filter { it.scope == Group.SCOPE_PUBLIC && it.id != Group.ID_GLOBAL },
            onJoinGroup = { gid -> onNavigateToGroup(gid); onDismissAssistantGhost() }
        )
    }

    if (showVault) {
        SunkRecordVault(archivedGroups = state.session.archivedGroups, onRestore = onRestoreCrowd, onDismiss = { showVault = false })
    }
}

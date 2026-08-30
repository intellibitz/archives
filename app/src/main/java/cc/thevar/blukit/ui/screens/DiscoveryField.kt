/**
 * BLUKIT DISCOVERY FIELD
 *
 * The root entry point of the mesh (Landing).
 * Provides a view of all nearby rooms and people.
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
import cc.thevar.blukit.domain.model.MeshMessage
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MeshRoom
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.components.MeshSearchingView
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.navigation.Route

/**
 * THE DISCOVERY FIELD: The master feed for finding rooms and peers.
 */
@Composable
fun DiscoveryField(
    state: BluetoothUiState,
    localDeviceId: String,
    header: @Composable () -> Unit,
    pulsedPeers: Set<String> = emptySet(),
    onIdentifyUser: (String) -> Unit = {},
    // Humanity Stage Props
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    userNickname: String = "",
    activeCrowds: List<MeshRoom> = emptyList(),
    onShowTimeline: () -> Unit = {},
    onResetProfile: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    supremeReport: cc.thevar.blukit.domain.power.SupremePowerReport? = null,
    // Hub Callbacks
    messageText: String = "",
    onMessageChange: (String) -> Unit = {},
    onSearchToggle: (() -> Unit)? = null,
    isSearchActive: Boolean = false,
    onCreatePublicRoom: ((String, String?) -> Unit)? = null,
    onNavigateToGroup: (String) -> Unit = {},
    onNavigateToPulse: (String) -> Unit = {},
    onAcceptRadio: (P2PDevice) -> Unit = {},
    onDenyRadio: (P2PDevice) -> Unit = {},
    onRestoreCrowd: (String) -> Unit = {},
    onNavigateToLiveFeed: () -> Unit = {},
    showAirGhost: Boolean = false,
    onShowAirGhost: () -> Unit = {},
    onDismissAirGhost: () -> Unit = {},
) {
    val eventMetas = state.session.groups.filter { it.scope == MeshRoom.SCOPE_PUBLIC }
    
    val pulsesData = remember(state.session.messages, eventMetas) {
        val groupedByTie = state.session.messages.groupBy { it.groupId ?: MeshRoom.ID_GLOBAL }
        val counts = groupedByTie.mapValues { it.value.size }
        val filtered = groupedByTie.map { it.value.maxBy { msg -> msg.timestamp } }
        val sorted = filtered.sortedByDescending { it.timestamp }
        Triple(sorted, counts, false)
    }

    val (_, pulseCounts, _) = pulsesData

    val combinedEnergy = remember(eventMetas, state.session.messages) {
        val grouped = mutableListOf<Pair<P2PDevice, MeshMessage?>>()
        val pinned = eventMetas.filter { it.isPinned }.sortedByDescending { it.lastMessageTimestamp }
        val others = eventMetas.filter { !it.isPinned }.sortedBy { it.lastMessageTimestamp }
        val sortedEvents = others + pinned 
        
        sortedEvents.forEach { room ->
            val latestAiSummary = state.session.messages.findLast { 
                (it.groupId == room.id) && (it.type == MeshMessage.TYPE_AI_SUMMARY)
            }
            val roomMessages = state.session.messages.asSequence().filter { 
                (it.groupId == room.id || (room.id == MeshRoom.ID_GLOBAL && it.groupId == null)) && (it.senderId != localDeviceId)
            }.sortedBy { it.timestamp }.toList().takeLast(3)
            
            roomMessages.forEach { msg ->
                val dev = P2PDevice(id = msg.senderId, name = msg.senderName, emoji = msg.senderEmoji ?: "👤", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
                grouped.add(dev to msg)
            }
            val headDev = P2PDevice(id = room.id, name = if (room.id == MeshRoom.ID_GLOBAL) "Global Mesh" else room.name, emoji = room.projectionEmoji ?: "✨", medium = P2PDevice.ConnectionMedium.BLUETOOTH, statusLabel = latestAiSummary?.content)
            grouped.add(headDev to null)
        }
        grouped
    }

    var roomNameProposal by remember { mutableStateOf("") }
    var showVault by remember { mutableStateOf(false) }

    val globalAiSummary = remember(state.session.messages) {
        state.session.messages.findLast { 
            (it.groupId == MeshRoom.ID_GLOBAL || it.groupId == null) && (it.type == MeshMessage.TYPE_AI_SUMMARY) 
        }
    }

    BlukitFieldScaffold(
        header = header,
        entries = {
            Column(modifier = Modifier.fillMaxSize()) {
                IdentityStage(
                    title = "DISCOVERY",
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    activeRooms = activeCrowds,
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
                    globalAiSummary?.let { summary ->
                        BlukitWidget(header = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = StealthAmber, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Mesh AI Insights", style = MaterialTheme.typography.labelSmall, color = StealthAmber)
                                }
                            },
                            entries = { Text(text = summary.content, color = Color.White, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(12.dp)) },
                            themeColor = StealthAmber, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    LiveMessageTicker(
                        state = state,
                        energyList = combinedEnergy,
                        pulseCounts = pulseCounts,
                        localDeviceId = localDeviceId,
                        localNickname = userNickname,
                        pulsedPeers = pulsedPeers,
                        onPulseClick = { id -> if (state.session.groups.any { it.id == id }) onNavigateToGroup(id) else onNavigateToPulse(id) },
                        onDeviceClick = { onNavigateToPulse(it.id) },
                        onDeviceLongClick = { },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    MeshSearchingView(onSignalPresence = { onShowAirGhost() })
                }
            }

            MessageHub(
                currentRoute = Route.Discovery,
                messageText = messageText,
                onMessageChange = onMessageChange,
                onSend = { }, 
                messageCount = state.session.messages.size,
                incomingRadioRequests = state.crowd.incomingRadioRequests,
                selectedDevices = state.crowd.selectedDevices,
                onAcceptRadio = onAcceptRadio,
                onDenyRadio = onDenyRadio,
                onStartSidePulse = { }, 
                onStartChain = { }, 
                onClearSelection = { },
                onAttachFile = { },
                isSearchMode = isSearchActive,
                onSearchToggle = onSearchToggle,
                onCreatePublicRoom = onCreatePublicRoom,
                onFocusChange = { },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            )
        }
    )

    if (showAirGhost) {
        RoomRitualGhost(
            onNameChange = { roomNameProposal = it },
            onDone = { templateId ->
                onCreatePublicRoom?.invoke(roomNameProposal, templateId)
                onDismissAirGhost()
            },
            onDismiss = onDismissAirGhost,
            nearbyAirs = state.session.groups.filter { it.scope == MeshRoom.SCOPE_PUBLIC && it.id != MeshRoom.ID_GLOBAL },
            onJoinAir = { gid -> onNavigateToGroup(gid); onDismissAirGhost() }
        )
    }

    if (showVault) {
        SunkPulseVault(archivedCrowds = state.session.archivedGroups, onRestore = onRestoreCrowd, onDismiss = { showVault = false })
    }
}

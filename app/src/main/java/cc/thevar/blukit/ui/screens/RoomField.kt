/**
 * BLUKIT ROOM FIELD
 *
 * A high-resonance field for specific mesh room contexts.
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
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.navigation.Route

/**
 * THE ROOM FIELD: Focuses on a specific Room (formerly Crowd).
 */
@Composable
fun RoomField(
    state: BluetoothUiState,
    localDeviceId: String,
    header: @Composable () -> Unit,
    roomId: String,
    highResonanceMessages: List<MeshMessage> = emptyList(),
    onVote: (String, Int) -> Unit = { _, _ -> },
    isSearchActive: Boolean = false,
    onSearchToggle: () -> Unit = {},
    onNavigateToPulse: (String) -> Unit = {},
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    userNickname: String = "",
    onShowTimeline: () -> Unit = {},
    onResetProfile: () -> Unit = {},
    onBack: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    messageText: String = "",
    onMessageChange: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onAcceptRadio: (P2PDevice) -> Unit = {},
    onDenyRadio: (P2PDevice) -> Unit = {},
    onNavigateToLiveFeed: () -> Unit = {},
    onStartSidePulse: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onInputFocusChange: (Boolean) -> Unit = {},
    isStealthMode: Boolean = false,
    lowPowerMode: Boolean = false,
    onToggleStealth: (Boolean) -> Unit = {},
    onToggleLowPower: (Boolean) -> Unit = {}
) {
    val room = state.session.groups.find { it.id == roomId }
    val members = state.crowd.scannedDevices.filter { it.id in (room?.allMemberIds ?: emptySet()) || it.persistentId in (room?.allMemberIds ?: emptySet()) }
    
    val roomMessages = state.session.messages.filter { it.groupId == roomId }.sortedByDescending { it.timestamp }
    val energyList = remember(roomMessages, members) {
        val list = mutableListOf<Pair<P2PDevice, MeshMessage?>>()
        roomMessages.forEach { msg ->
            val dev = members.find { it.id == msg.senderId || it.persistentId == msg.senderId } ?: P2PDevice(id = msg.senderId, name = msg.senderName, emoji = msg.senderEmoji ?: "👤")
            list.add(dev to msg)
        }
        list
    }

    var selectedPulseForMenu by remember { mutableStateOf<MeshMessage?>(null) }

    BlukitFieldScaffold(
        header = header,
        entries = {
            Column(modifier = Modifier.fillMaxSize()) {
                IdentityStage(
                    title = room?.name ?: "ROOM",
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    activeRooms = state.session.groups,
                    onShowTimeline = onShowTimeline,
                    onResetProfile = onResetProfile,
                    onBack = onBack,
                    themeColor = StealthRose,
                    userCount = members.size,
                    onModeChange = { onNavigateToLiveFeed() },
                    trailingContent = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = onSearchToggle, modifier = Modifier.size(28.dp)) {
                                Icon(imageVector = if (isSearchActive) Icons.Rounded.Search else Icons.Rounded.People, contentDescription = "Toggle Search", tint = if (isSearchActive) StealthAmber else StealthRose, modifier = Modifier.size(20.dp))
                            }
                            Text(text = if (isSearchActive) "SEARCH" else "PEOPLE", style = MaterialTheme.typography.labelSmall, color = (if (isSearchActive) StealthAmber else StealthRose).copy(alpha = StealthAlphaHigh))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                )

                MessageCanvas(
                    highResonanceMessages = highResonanceMessages,
                    themeColor = StealthRose,
                    onPulseClick = { onNavigateToPulse(it) }
                )

                LiveMessageTicker(
                    state = state,
                    energyList = energyList,
                    pulseCounts = emptyMap(),
                    localDeviceId = localDeviceId,
                    localNickname = userNickname,
                    pulsedPeers = emptySet(),
                    onPulseClick = { onNavigateToPulse(it) },
                    onDeviceClick = { dev -> onNavigateToPulse(dev.id) },
                    onDeviceLongClick = { },
                    modifier = Modifier.weight(1f),
                    themeColor = StealthRose
                )
            }

            if (selectedPulseForMenu != null) {
                MessageActionMenu(
                    pulse = selectedPulseForMenu!!,
                    isMe = selectedPulseForMenu!!.senderId == localDeviceId,
                    onInvite = { onStartSidePulse() },
                    onDelete = { },
                    onDismiss = { selectedPulseForMenu = null },
                    onBroadcast = { },
                    onVote = { weight -> onVote(selectedPulseForMenu!!.messageId, weight) }
                )
            }

            MessageHub(
                currentRoute = Route.RoomField(roomId),
                messageText = messageText,
                onMessageChange = onMessageChange,
                onSend = onSend,
                messageCount = roomMessages.size,
                incomingRadioRequests = state.crowd.incomingRadioRequests,
                selectedDevices = state.crowd.selectedDevices,
                onAcceptRadio = onAcceptRadio,
                onDenyRadio = onDenyRadio,
                onStartSidePulse = onStartSidePulse,
                onStartChain = { }, 
                onClearSelection = onClearSelection,
                onAttachFile = { },
                isSearchMode = isSearchActive,
                onSearchToggle = onSearchToggle,
                onFocusChange = onInputFocusChange,
                isStealthMode = isStealthMode,
                lowPowerMode = lowPowerMode,
                onToggleStealth = onToggleStealth,
                onToggleLowPower = onToggleLowPower,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            )
        }
    )
}

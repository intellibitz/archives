/**
 * BLUKIT SPHERE FIELD
 *
 * A high-resonance field for specific Sphere contexts.
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
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Sphere
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.navigation.Route

/**
 * THE SPHERE FIELD: Focuses on a specific Sphere.
 */
@Composable
fun SphereField(
    state: BluetoothUiState,
    localDeviceId: String,
    header: @Composable () -> Unit,
    sphereId: String,
    highResonanceMessages: List<Echo> = emptyList(),
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
    onSend: (String) -> Unit = {},
    onAcceptRadio: (Source) -> Unit = {},
    onDenyRadio: (Source) -> Unit = {},
    onNavigateToLiveFeed: () -> Unit = {},
    onStartSidePulse: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onInputFocusChange: (Boolean) -> Unit = {},
    isStealthMode: Boolean = false,
    lowPowerMode: Boolean = false,
    onToggleStealth: (Boolean) -> Unit = {},
    onToggleLowPower: (Boolean) -> Unit = {}
) {
    val sphere = state.session.groups.find { it.id == sphereId }
    val members = state.crowd.scannedDevices.filter { it.id in (sphere?.allMemberIds ?: emptySet()) || it.persistentId in (sphere?.allMemberIds ?: emptySet()) }
    
    val roomMessages = state.session.messages.filter { it.groupId == sphereId }.sortedByDescending { it.timestamp }
    val resonanceList = remember(roomMessages, members) {
        val list = mutableListOf<Pair<Source, Echo?>>()
        roomMessages.forEach { echo ->
            val source = members.find { it.id == echo.senderId || it.persistentId == echo.senderId } ?: Source(id = echo.senderId, name = echo.senderName, emoji = echo.senderEmoji ?: "👤")
            list.add(source to echo)
        }
        list
    }

    var selectedEchoForMenu by remember { mutableStateOf<Echo?>(null) }
    var messageText by remember { mutableStateOf("") }

    BlukitFieldScaffold(
        header = header,
        entries = {
            Column(modifier = Modifier.fillMaxSize()) {
                IdentityStage(
                    title = sphere?.name ?: "SPHERE",
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
                            Text(text = if (isSearchActive) "SEARCH" else "SOURCES", style = MaterialTheme.typography.labelSmall, color = (if (isSearchActive) StealthAmber else StealthRose).copy(alpha = StealthAlphaHigh))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                )

                EchoCanvas(
                    highResonanceEchoes = highResonanceMessages,
                    themeColor = StealthRose,
                    onEchoClick = { onNavigateToPulse(it) }
                )

                ResonanceTicker(
                    state = state,
                    resonanceList = resonanceList,
                    echoCounts = emptyMap(),
                    localDeviceId = localDeviceId,
                    localNickname = userNickname,
                    pulsedPeers = emptySet(),
                    onEchoClick = { onNavigateToPulse(it) },
                    onSourceClick = { dev -> onNavigateToPulse(dev.id) },
                    onSourceLongClick = { },
                    modifier = Modifier.weight(1f),
                    themeColor = StealthRose
                )
            }

            if (selectedEchoForMenu != null) {
                EchoActionMenu(
                    echo = selectedEchoForMenu!!,
                    isMe = selectedEchoForMenu!!.senderId == localDeviceId,
                    onInvite = { onStartSidePulse() },
                    onDelete = { },
                    onDismiss = { selectedEchoForMenu = null },
                    onBroadcast = { },
                    onVote = { weight -> onVote(selectedEchoForMenu!!.messageId, weight) }
                )
            }

            EchoHub(
                currentRoute = Route.SphereField(sphereId),
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSend = { 
                    onSend(messageText)
                    messageText = ""
                },
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

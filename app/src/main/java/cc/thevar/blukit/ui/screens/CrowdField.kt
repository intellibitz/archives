/**
 * BLUKIT CROWD FIELD
 *
 * Public frequency view for a specific location or hub.
 * Reorganizes the mesh experience into a focused context for public interaction.
 */
package cc.thevar.blukit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BubbleChart
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.Resonance
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CROWD FIELD: Public frequency view. 
 * Lists Ties and Shouts within a specific Crowd container.
 * Features collaborative Crowd Canvas for pinned pulses and spectral tips.
 */
/**
 * CROWD FIELD: The primary public interaction layer.
 * 
 * Architectural Pattern:
 * - Header: Harmony Top Bar with Crowd Breadcrumbs.
 * - Entries: Ripples Background, Resonance List, Pulse Ticker, Active Pulse Hub.
 */
@Composable
fun CrowdField(
    state: BluetoothUiState,
    localDeviceId: String,
    crowdId: String?,
    // Hub Callbacks
    messageText: String = "",
    onMessageChange: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onSearchToggle: (() -> Unit)? = null,
    isSearchActive: Boolean = false,
    onAcceptRadio: (P2PDevice) -> Unit = {},
    onDenyRadio: (P2PDevice) -> Unit = {},
    onStartSidePulse: () -> Unit = {},
    onStartChain: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onNavigateToPulse: (String) -> Unit = {},
    onNavigateToLiveFeed: () -> Unit = {},
    externalFocusedId: String? = null,
    onInputFocusChange: (Boolean) -> Unit = {},
    // Humanity Stage Props
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    userNickname: String = "",
    activeCrowds: List<Resonance> = emptyList(),
    onShowTimeline: () -> Unit = {},
    onResetProfile: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    highResonancePulses: List<MessagePayload> = emptyList(),
    onVote: (String, Int) -> Unit = { _, _ -> },
    isStealthMode: Boolean = false,
    lowPowerMode: Boolean = false,
    onToggleStealth: (Boolean) -> Unit = {},
    onToggleLowPower: (Boolean) -> Unit = {},
    header: @Composable () -> Unit,
) {
    var showTip by remember { mutableStateOf(value = true) }
    var localFocusedId by remember(externalFocusedId) { mutableStateOf(externalFocusedId) }
    var selectedPulseForMenu by remember { mutableStateOf<MessagePayload?>(null) }

    val crowd = remember(crowdId, state.session.groups) {
        state.session.groups.find { it.id == crowdId }
    }

    val childGroups = remember(state.session.groups, crowdId) {
        state.session.groups.filter { it.parentId == crowdId }
    }

    val pulsesData = remember(state.session.messages, crowdId, localDeviceId, localFocusedId) {
        if (crowdId == null) {
            Triple(emptyList(), emptyMap(), false)
        } else {
            val basePulses = state.session.messages.filter { (it.groupId == crowdId) && (it.parentMessageId == null) }
            val counts = basePulses.groupBy { it.senderId }.mapValues { it.value.size }
            val sorted = basePulses.sortedBy { it.timestamp }
            Triple(sorted, counts, localFocusedId != null)
        }
    }

    val (chatPulses, pulseCounts, _) = pulsesData
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    BlukitFieldScaffold(
        themeColor = StealthPrimary,
        glowIntensityTarget = 0.6f,
        header = header,
        entries = {
            // MODULE 1: BASE CONTENT (Humanity Stage + Ticker/Radar)
            Column(modifier = Modifier.fillMaxSize()) {
                val crowdName = crowd?.name ?: "CROWD"

                // Humanity Stage (Breadcrumbs)
                BlukitHumanityStage(
                    title = crowdName,
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    activeCrowds = activeCrowds,
                    onShowTimeline = onShowTimeline,
                    onResetProfile = onResetProfile,
                    onTitleClick = onTitleClick,
                    onBack = onBack,
                    themeColor = StealthPrimary,
                    userCount = crowd?.memberIds?.size ?: state.crowd.scannedDevices.size,
                    onModeChange = { onNavigateToLiveFeed() },
                    trailingContent = {
                        // Tactical Radar Toggles
                        if (onSearchToggle != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = onSearchToggle,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSearchActive) Icons.Rounded.WifiTethering else Icons.Rounded.Radar,
                                        contentDescription = "Toggle Search",
                                        tint = if (isSearchActive) StealthAmber else StealthPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = if (isSearchActive) "SCAN" else "RADAR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = (if (isSearchActive) StealthAmber else StealthPrimary).copy(alpha = StealthAlphaHigh),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                )
                
                // SWARM LOGIC: Crowd Canvas for High-Resonance Pulses
                CrowdCanvas(
                    highResonancePulses = highResonancePulses,
                    themeColor = StealthPrimary,
                    onPulseClick = { onNavigateToPulse(it) },
                )

                // MODULE 2: UNIFIED RESONANCE TICKER (With integrated Radar)
                PulsingResonanceTicker(
                    state = state,
                    energyList = chatPulses.map { msg -> 
                        val dev = P2PDevice(id = msg.senderId, name = msg.senderName, emoji = msg.senderEmoji ?: "👤", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
                        dev to msg 
                    },
                    pulseCounts = pulseCounts,
                    localDeviceId = localDeviceId,
                    localNickname = userNickname,
                    pulsedPeers = emptySet(),
                    isGrouped = false,
                    onPulseClick = { onNavigateToPulse(it) },
                    onDeviceClick = { dev -> onNavigateToPulse(dev.id) },
                    onDeviceLongClick = { dev -> 
                        chatPulses.find { it.senderId == dev.id }?.let { selectedPulseForMenu = it }
                    },
                    modifier = Modifier.weight(1f),
                    themeColor = StealthPrimary
                )
            }

            // Pulse Action Menu for Swarm Voting
            if (selectedPulseForMenu != null) {
                PulseActionMenu(
                    pulse = selectedPulseForMenu!!,
                    isMe = selectedPulseForMenu!!.senderId == localDeviceId,
                    onInvite = { /* Handle invite */ },
                    onDelete = { /* Handle delete */ },
                    onBroadcast = { /* Handle broadcast */ },
                    onVote = { weight -> onVote(selectedPulseForMenu!!.messageId, weight) },
                    onDismiss = { selectedPulseForMenu = null }
                )
            }

            // MODULE 3: PULSE HUB (Bottom Overlay)
            BlukitPulseHub(
                currentRoute = cc.thevar.blukit.ui.navigation.Route.GroupField(crowdId ?: ""),
                messageText = messageText,
                onMessageChange = onMessageChange,
                onSend = onSend,
                pulseCount = state.session.messages.size,
                incomingRadioRequests = state.crowd.incomingRadioRequests,
                selectedDevices = state.crowd.selectedDevices,
                resonances = state.session.groups,
                onAcceptRadio = onAcceptRadio,
                onDenyRadio = onDenyRadio,
                onStartSidePulse = onStartSidePulse,
                onStartChain = onStartChain,
                onClearSelection = onClearSelection,
                onAttachFile = { },
                onSearchToggle = onSearchToggle,
                onFocusChange = onInputFocusChange,
                isStealthMode = isStealthMode,
                lowPowerMode = lowPowerMode,
                onToggleStealth = onToggleStealth,
                onToggleLowPower = onToggleLowPower,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )

            // MODULE 4: FLOATING TIPS
            AnimatedVisibility(
                visible = showTip && chatPulses.isEmpty() && childGroups.isEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                BlukitTip(
                    text = "THIS GROUP IS SILENT. START A CONVERSATION OR SEND A MESSAGE TO CONNECT.",
                    onDismiss = { showTip = false }
                )
            }
        }
    )
}

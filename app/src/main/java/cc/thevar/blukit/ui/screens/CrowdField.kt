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
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
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
    onAcceptRadio: (P2PDevice) -> Unit = {},
    onDenyRadio: (P2PDevice) -> Unit = {},
    onStartSidePulse: () -> Unit = {},
    onStartChain: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onNavigateToGroup: (String) -> Unit = {},
    onNavigateToPulse: (String) -> Unit = {},
    externalFocusedId: String? = null,
    isInputFocused: Boolean = false,
    onInputFocusChange: (Boolean) -> Unit = {},
    // Humanity Stage Props
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    userNickname: String = "",
    userEmoji: String = "",
    onUserNicknameChange: (String) -> Unit = {},
    activeCrowds: List<Resonance> = emptyList(),
    onShowTimeline: () -> Unit = {},
    onResetProfile: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    highResonancePulses: List<MessagePayload> = emptyList(),
    onVote: (String, Int) -> Unit = { _, _ -> },
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

    val childCrowds = childGroups.filter { it.scope == Resonance.SCOPE_PUBLIC }
    val childTies = childGroups.filter { it.scope != Resonance.SCOPE_PUBLIC }

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
            // MODULE 1: BASE CONTENT (Radar + Lists)
            Column(modifier = Modifier.fillMaxSize()) {
                val crowdName = crowd?.name ?: "CROWD"
                
                // SWARM LOGIC: Crowd Canvas for High-Resonance Pulses
                CrowdCanvas(
                    highResonancePulses = highResonancePulses,
                    themeColor = StealthPrimary,
                    onPulseClick = { onNavigateToPulse(it) },
                )

                RipplesField(
                    state = state,
                    activeBubbles = emptyList(),
                    pulsedPeers = emptySet(),
                    onDeviceClick = { },
                    // Humanity Stage
                    title = crowdName,
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    userNickname = userNickname,
                    userEmoji = userEmoji,
                    activeCrowds = activeCrowds,
                    onShowTimeline = onShowTimeline,
                    onResetProfile = onResetProfile,
                    onTitleClick = onTitleClick,
                    onBack = onBack,
                    onNicknameChange = onUserNicknameChange,
                    isDimmed = isInputFocused || state.crowd.selectedDevices.isNotEmpty(),
                    themeColor = StealthPrimary,
                    modifier = Modifier.fillMaxWidth().height(320.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    // Meta Sections
                    if (childCrowds.isNotEmpty() || childTies.isNotEmpty()) {
                        Text(
                            text = "EVENT & TIES", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = StealthPrimary, 
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        
                        LazyColumn(modifier = Modifier.weight(0.4f)) {
                            items(childCrowds) { nestedCrowd ->
                                ResonanceSummary(
                                    title = nestedCrowd.name,
                                    subtitle = "NESTED CROWD",
                                    icon = Icons.Rounded.Grain,
                                    themeColor = StealthPrimary,
                                    count = nestedCrowd.memberIds.size,
                                    lastUpdate = sdf.format(Date(nestedCrowd.lastPulseTimestamp)),
                                    onClick = { onNavigateToGroup(nestedCrowd.id) },
                                    showJoin = true
                                )
                            }
                            items(childTies) { tie ->
                                ResonanceSummary(
                                    title = tie.name,
                                    subtitle = if (tie.scope == Resonance.SCOPE_PUBLIC) "CHAIN" else "LOCAL CHAIN",
                                    icon = if (tie.scope == Resonance.SCOPE_PRIVATE) Icons.Rounded.Hearing else Icons.Rounded.CellTower,
                                    themeColor = if (tie.scope == Resonance.SCOPE_PRIVATE) StealthRose else StealthPrimary,
                                    count = tie.memberIds.size,
                                    lastUpdate = sdf.format(Date(tie.lastPulseTimestamp)),
                                    onClick = { onNavigateToGroup(tie.id) },
                                    showJoin = true
                                )
                            }
                        }
                    }

                    // Pulses
                    Text(
                        text = "PULSES", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = StealthPrimary, 
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    LazyColumn(modifier = Modifier.weight(0.6f)) {
                        items(chatPulses) { pulse ->
                            if (pulse.isMeta) {
                                ResonanceSummary(
                                    title = pulse.content.take(20),
                                    subtitle = "RESONANCE",
                                    icon = Icons.Rounded.BubbleChart,
                                    themeColor = StealthPrimary,
                                    count = state.session.messages.count { it.parentMessageId == pulse.messageId },
                                    lastUpdate = sdf.format(Date(pulse.timestamp)),
                                    onClick = { onNavigateToPulse(pulse.messageId) }
                                )
                            } else {
                                AnimatedPulseItem(
                                    msg = pulse,
                                    isSelected = false,
                                    senderDevice = null,
                                    pulseCount = 0,
                                    isPulsed = false,
                                    isMe = pulse.senderId == localDeviceId,
                                    isGrouped = false,
                                    isMutual = false,
                                    rowId = pulse.messageId,
                                    onPulseClick = { onNavigateToPulse(pulse.messageId) },
                                    onDeviceLongClick = { selectedPulseForMenu = pulse }
                                )
                            }
                        }
                    }
                }
                
                // Bottom padding to avoid occlusion by the floating ticker and hub
                Spacer(modifier = Modifier.height(140.dp))
            }

            // MODULE 2: TICKER (Floating Overlay)
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
                onDeviceClick = { dev -> onNavigateToPulse(dev.id) }, // Default action for context
                onDeviceLongClick = { dev -> 
                    chatPulses.find { it.senderId == dev.id }?.let { selectedPulseForMenu = it }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(bottom = 100.dp) // Room for Hub
            )

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
                    text = "THE CROWD IS SILENT. CREATE A TIE OR SPREAD A PULSE TO START.",
                    onDismiss = { showTip = false }
                )
            }
        }
    )
}

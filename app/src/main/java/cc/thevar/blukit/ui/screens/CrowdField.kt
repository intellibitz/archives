/**
 * BLUKIT CROWD FIELD
 *
 * Public frequency view for a specific location or hub.
 * Reorganizes the mesh experience into a focused context for public interaction.
 */
package cc.thevar.blukit.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.Resonance
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
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
    onDisconnect: () -> Unit,
    onSendMessage: (String, String) -> Unit,
    crowdIsStill: Boolean = false,
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
    onShowPrivacy: () -> Unit = {},
    onNavigateToGroup: (String) -> Unit = {},
    onNavigateToPulse: (String) -> Unit = {},
    externalFocusedId: String? = null,
    // Humanity Stage Props
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    activeCrowds: List<Resonance> = emptyList(),
    onShowTimeline: () -> Unit = {},
    onResetProfile: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    header: @Composable () -> Unit
) {
    var showTip by remember { mutableStateOf(true) }
    var localFocusedId by remember(externalFocusedId) { mutableStateOf(externalFocusedId) }

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
            Triple(emptyList<MessagePayload>(), emptyMap<String, Int>(), false)
        } else {
            val basePulses = state.session.messages.filter { it.groupId == crowdId && it.parentMessageId == null }
            val counts = basePulses.groupBy { it.senderId }.mapValues { it.value.size }
            val sorted = basePulses.sortedBy { it.timestamp }
            Triple(sorted, counts, localFocusedId != null)
        }
    }

    val (chatPulses, pulseCounts, isPulseFocused) = pulsesData
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    BlukitFieldScaffold(
        themeColor = StealthPrimary,
        glowIntensityTarget = 0.6f,
        header = header,
        entries = {
            // MODULE 1: BASE CONTENT (Radar + Lists)
            Column(modifier = Modifier.fillMaxSize()) {
                val crowdName = crowd?.name ?: "CROWD"
                RipplesField(
                    state = state,
                    localDeviceId = localDeviceId,
                    activeBubbles = emptyList(),
                    pulsedPeers = emptySet(),
                    drawBackground = false,
                    drawNodes = false,
                    onDeviceClick = { },
                    // Humanity Stage
                    title = crowdName,
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    activeCrowds = activeCrowds,
                    onShowTimeline = onShowTimeline,
                    onResetProfile = onResetProfile,
                    onTitleClick = onTitleClick,
                    onBack = onBack,
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
                                    resonance = crowd,
                                    rowId = pulse.messageId,
                                    onPulseClick = { /* Handle Unit Click */ },
                                    onDeviceLongClick = { },
                                    onDelete = { }
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
                pulsedPeers = emptySet(),
                isGrouped = false,
                onPulseClick = { onNavigateToPulse(it) },
                onDeviceLongClick = { },
                onDeletePulse = { },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(bottom = 100.dp) // Room for Hub
            )

            // MODULE 3: PULSE HUB (Bottom Overlay)
            BlukitPulseHub(
                currentRoute = cc.thevar.blukit.ui.navigation.Route.GroupField(crowdId ?: ""),
                messageText = messageText,
                onMessageChange = onMessageChange,
                onSend = onSend,
                pulseCount = state.session.messages.size,
                crowdIsStill = crowdIsStill,
                incomingRadioRequests = state.crowd.incomingRadioRequests,
                selectedDevices = state.crowd.selectedDevices,
                pulsedPeers = emptySet(),
                resonances = state.session.groups,
                onAcceptRadio = onAcceptRadio,
                onDenyRadio = onDenyRadio,
                onStartSidePulse = onStartSidePulse,
                onStartChain = onStartChain,
                onClearSelection = onClearSelection,
                onAttachFile = { },
                onSearchToggle = onSearchToggle,
                onShowPrivacy = onShowPrivacy,
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

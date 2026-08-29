/**
 * BLUKIT EVENT FIELD
 *
 * The root entry point of the mesh (Landing).
 * Provides a Global Spectrum View of all nearby frequencies.
 * 
 * Scoping: discovery and formation. Sending pulses is locked here to maintain
 * conceptual integrity and prevent global spam.
 */
package cc.thevar.blukit.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.Resonance
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState

/**
 * THE EVENT FIELD: The master spectral radar and resonance feed.
 * 
 * Architectural Pattern:
 * - Header: Harmony Top Bar.
 * - Entries: Radar (Background), Resonance Ticker (Overlay), Pulse Hub (Locked).
 */
@Composable
fun EventField(
    state: BluetoothUiState,
    localDeviceId: String,
    header: @Composable () -> Unit,
    pulsedPeers: Set<String> = emptySet(),
    onIdentifyUser: (String) -> Unit = {},
    // Humanity Stage Props
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    userNickname: String = "",
    activeCrowds: List<Resonance> = emptyList(),
    onShowTimeline: () -> Unit = {},
    onResetProfile: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    supremeReport: cc.thevar.blukit.domain.power.SupremePowerReport? = null,
    // Hub Callbacks (Simplified for root)
    messageText: String = "",
    onMessageChange: (String) -> Unit = {},
    onSearchToggle: (() -> Unit)? = null,
    isSearchActive: Boolean = false,
    onCreatePublicResonance: ((String, String?) -> Unit)? = null,
    onNavigateToGroup: (String) -> Unit = {},
    onNavigateToPulse: (String) -> Unit = {},
    onAcceptRadio: (P2PDevice) -> Unit = {},
    onDenyRadio: (P2PDevice) -> Unit = {},
    onRestoreCrowd: (String) -> Unit = {},
    showAirGhost: Boolean = false,
    onShowAirGhost: () -> Unit = {},
    onDismissAirGhost: () -> Unit = {},
) {
    val eventMetas = state.session.groups.filter { it.scope == Resonance.SCOPE_PUBLIC }
    
    val pulsesData = remember(state.session.messages, eventMetas) {
        val groupedByTie = state.session.messages.groupBy { it.groupId ?: Resonance.ID_CROWD }
        
        val counts = groupedByTie.mapValues { it.value.size }
        val filtered = groupedByTie.map { it.value.maxBy { msg -> msg.timestamp } }
        val sorted = filtered.sortedByDescending { it.timestamp }
        Triple(sorted, counts, false)
    }

    val (_, pulseCounts, _) = pulsesData

    // Event ticker shows the latest global energy (Grouped by Resonance: Header + Entries)
    val combinedEnergy = remember(eventMetas, state.session.messages) {
        val grouped = mutableListOf<Pair<P2PDevice, MessagePayload?>>()
        
        // Sort events by latest activity (ascending for reverseLayout)
        val sortedEvents = eventMetas.sortedBy { it.lastPulseTimestamp }
        
        sortedEvents.forEach { resonance ->
            // Fetch latest AI summary for this resonance if it exists
            val latestAiSummary = state.session.messages.findLast { 
                (it.groupId == resonance.id) && (it.type == MessagePayload.TYPE_AI_SUMMARY)
            }

            // 1. ADD LATEST ENTRIES for this resonance (up to 3)
            // TACTICAL FILTER: Exclude own pulses from the landing spectrum to focus on discovery
            val resonancePulses = state.session.messages.asSequence().filter { 
                (it.groupId == resonance.id || (resonance.id == Resonance.ID_CROWD && it.groupId == null)) && (it.senderId != localDeviceId)
            }.sortedBy { it.timestamp }
             .toList()
             .takeLast(3)
            
            resonancePulses.forEach { msg ->
                val dev = P2PDevice(id = msg.senderId, name = msg.senderName, emoji = msg.senderEmoji ?: "👤", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
                grouped.add(dev to msg)
            }

            // 2. ADD HEADER (above pulses in UI)
            val headDev = P2PDevice(
                id = resonance.id, 
                name = resonance.name, 
                emoji = resonance.projectionEmoji ?: "⚡", 
                medium = P2PDevice.ConnectionMedium.BLUETOOTH,
                statusLabel = latestAiSummary?.content
            )
            grouped.add(headDev to null)
        }
        
        grouped
    }

    var airProposalName by remember { mutableStateOf("") }
    var showVault by remember { mutableStateOf(false) }

    BlukitFieldScaffold(
        header = header,
        entries = {
            // MODULE 1: BASE CONTENT (Humanity Stage + Unified Ticker/Radar)
            Column(modifier = Modifier.fillMaxSize()) {
                // Humanity Stage (Breadcrumbs)
                BlukitHumanityStage(
                    title = "THE SPECTRUM",
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    activeCrowds = activeCrowds,
                    onShowTimeline = onShowTimeline,
                    onResetProfile = onResetProfile,
                    onTitleClick = onTitleClick,
                    onBack = onBack,
                    themeColor = StealthPrimary,
                    userCount = state.crowd.scannedDevices.size,
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
                                    fontSize = 5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = (if (isSearchActive) StealthAmber else StealthPrimary).copy(alpha = 0.5f),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                )

                // MODULE 2: UNIFIED RESONANCE TICKER (With integrated Radar)
                PulsingResonanceTicker(
                    state = state,
                    energyList = combinedEnergy,
                    pulseCounts = pulseCounts,
                    localDeviceId = localDeviceId,
                    localNickname = userNickname,
                    pulsedPeers = pulsedPeers,
                    onPulseClick = onNavigateToPulse,
                    onDeviceClick = { onNavigateToPulse(it.id) },
                    onDeviceLongClick = { },
                    modifier = Modifier.weight(1f)
                )
            }

            // MODULE 3: PULSE HUB (Bottom Overlay)
            BlukitPulseHub(
                currentRoute = cc.thevar.blukit.ui.navigation.Route.Event,
                messageText = messageText,
                onMessageChange = onMessageChange,
                onSend = { }, // Locked in root
                pulseCount = state.session.messages.size,
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
                onCreatePublicResonance = onCreatePublicResonance,
                onFocusChange = { },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }
    )

    if (showVault) {
        SunkPulseVault(
            archivedCrowds = state.session.archivedGroups,
            onRestore = onRestoreCrowd,
            onDismiss = { showVault = false }
        )
    }
}

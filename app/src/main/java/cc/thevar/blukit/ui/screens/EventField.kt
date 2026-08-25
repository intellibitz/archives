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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.Resonance
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import cc.thevar.blukit.ui.theme.StealthAmber
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.text.style.TextAlign
import org.koin.compose.koinInject
import kotlinx.coroutines.launch

/**
 * THE EVENT FIELD: The top-level spectrum view of the mesh.
 * Displays all nearby pulses and event on a discovery radar.
 * Integrates spectral tips for onboarding and radar discovery.
 */
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
    noiseFilterEnabled: Boolean = false,
    onStartScan: () -> Unit,
    onDeviceClick: (P2PDevice) -> Unit,
    onDeletePulse: (String) -> Unit,
    onWhisper: (P2PDevice) -> Unit,
    onIdentifyUser: (String) -> Unit = {},
    // Hub Callbacks (Simplified for root)
    messageText: String = "",
    onSearchToggle: (() -> Unit)? = null,
    onCreatePublicResonance: ((String, String?) -> Unit)? = null,
    onAcceptRadio: (P2PDevice) -> Unit = {},
    onDenyRadio: (P2PDevice) -> Unit = {},
    onNavigateToGroup: (String) -> Unit = {},
    isSearchActive: Boolean = false,
    onRestoreCrowd: (String) -> Unit = {},
    showAirGhost: Boolean = false,
    onShowAirGhost: () -> Unit = {},
    onDismissAirGhost: () -> Unit = {},
) {
    var showTip by remember { mutableStateOf(value = true) }
    var airProposalName by remember { mutableStateOf("") }
    val activePulseId = LocalActivePulseId.current
    var pulseGhostData by remember { mutableStateOf<GhostPulseData?>(null) }
    var showVault by remember { mutableStateOf(false) }

    val eventMetas = remember(state.session.groups) {
        state.session.groups.filter { 
            (it.scope == Resonance.SCOPE_PUBLIC) && ((it.parentId == null) || (it.parentId == Resonance.ID_CROWD))
        }
    }

    val pulsesData = remember(state.session.messages, state.session.groups, pulsedPeers, noiseFilterEnabled, isSearchActive, messageText) {
        val basePulses = if (noiseFilterEnabled && pulsedPeers.isNotEmpty()) {
            state.session.messages.filter { it.senderId in pulsedPeers || it.senderId == localDeviceId }
        } else {
            state.session.messages
        }
        
        val searchFiltered = if (!isSearchActive || messageText.isBlank()) basePulses else {
            basePulses.filter { msg ->
                msg.content.contains(messageText, ignoreCase = true) || msg.senderName.contains(messageText, ignoreCase = true)
            }
        }
        
        val groupedByTie = searchFiltered.groupBy { msg ->
            when {
                msg.pulseType == MessagePayload.PULSE_SILENCE -> Resonance.ID_SILENCE
                msg.groupId != null -> msg.groupId!!
                else -> Resonance.ID_CROWD
            }
        }
        
        val counts = groupedByTie.mapValues { it.value.size }
        val filtered = groupedByTie.map { it.value.maxBy { msg -> msg.timestamp } }
        val sorted = filtered.sortedByDescending { it.timestamp }
        Triple(sorted, counts, false)
    }

    val (_, pulseCounts, _) = pulsesData

    BlukitFieldScaffold(
        header = header,
        entries = {
            // MODULE 1: RADAR (Background Entry)
            RipplesField(
                state = state,
                localDeviceId = localDeviceId,
                activeBubbles = emptyList(),
                selectedDevices = state.crowd.selectedDevices,
                pulsedPeers = pulsedPeers,
                isFilterMode = noiseFilterEnabled,
                pulseGhostData = pulseGhostData,
                onDismissGhost = { pulseGhostData = null; activePulseId.value = null },
                onDeviceClick = onDeviceClick,
                onDeviceLongClick = { targetDevice ->
                    val menuId = targetDevice.persistentId ?: targetDevice.id
                    activePulseId.value = menuId
                    pulseGhostData = GhostPulseData(
                        emoji = targetDevice.emoji,
                        title = targetDevice.name ?: "PERSONA",
                        subtitle = "CROWD NODE",
                        themeColor = StealthPrimary,
                        sourceId = menuId,
                        actions = mutableListOf<GhostAction>().apply {
                            add(GhostAction(Icons.Rounded.Hearing, "WHISPER", StealthPrimary) { onWhisper(targetDevice) })
                            add(GhostAction(Icons.Rounded.Radar, "IDENTIFY", Color.White) { onIdentifyUser(menuId) })
                        }
                    )
                },
                onStartScan = onStartScan,
                onCreateEvent = onShowAirGhost,
                onSearchToggle = onSearchToggle,
                isSearchActive = isSearchActive,
                drawBackground = false, // Background handled by Scaffold
                drawNodes = true,
                airRitualGhost = {
                    if (showAirGhost) {
                        CrowdRitualGhost(
                            onNameChange = { airProposalName = it },
                            onDone = { templateId -> onCreatePublicResonance?.invoke(airProposalName, templateId) },
                            onDismiss = onDismissAirGhost,
                            nearbyAirs = state.session.groups,
                            onJoinAir = onNavigateToGroup,
                            title = "EVENT RITUAL",
                            hint = "NAME THE EVENT"
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // MODULE 2: TICKER (Overlay Entry)
            // Event ticker shows the latest global energy (Grouped by Resonance: Header + Entries)
            val combinedEnergy = remember(eventMetas, state.session.messages) {
                val grouped = mutableListOf<Pair<P2PDevice, MessagePayload?>>()
                
                // Sort events by latest activity (ascending for reverseLayout)
                val sortedEvents = eventMetas.sortedBy { it.lastPulseTimestamp }
                
                sortedEvents.forEach { resonance ->
                    // 1. ADD LATEST ENTRIES for this resonance (up to 3)
                    val resonancePulses = state.session.messages.filter { 
                        it.groupId == resonance.id || (resonance.id == Resonance.ID_CROWD && it.groupId == null)
                    }.sortedBy { it.timestamp }
                     .takeLast(3)
                    
                    resonancePulses.forEach { msg ->
                        val dev = P2PDevice(id = msg.senderId, name = msg.senderName, emoji = msg.senderEmoji ?: "👤", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
                        grouped.add(dev to msg)
                    }

                    // 2. ADD HEADER (above pulses in UI)
                    val headDev = P2PDevice(id = resonance.id, name = resonance.name, emoji = "🌬️", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
                    grouped.add(headDev to null)
                }
                
                grouped
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp) 
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // MODULE 2.1: INCOMING REQUESTS (High Priority)
                    if (state.crowd.incomingRadioRequests.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            state.crowd.incomingRadioRequests.forEach { device ->
                                RadioRequestTickerItem(
                                    device = device, 
                                    onAccept = onAcceptRadio, 
                                    onDeny = onDenyRadio
                                )
                            }
                        }
                    }

                    // MODULE 2.2: TICKER (Standard Spectrum)
                    TickerSectionHeader(title = "ACTIVE EVENTS")
                    
                    PulsingResonanceTicker(
                        state = state,
                        energyList = combinedEnergy,
                        pulseCounts = pulseCounts,
                        localDeviceId = localDeviceId,
                        pulsedPeers = pulsedPeers,
                        isGrouped = true,
                        onPulseClick = { onNavigateToGroup(it) },
                        onDeviceLongClick = { },
                        onDeletePulse = onDeletePulse,
                        modifier = Modifier.weight(1f)
                    )
                }

                // FLOATING OVERLAY (Tips / Nudges)
                Box(modifier = Modifier.fillMaxSize().padding(bottom = 16.dp), contentAlignment = Alignment.BottomCenter) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AnimatedContent(
                            targetState = when {
                                eventMetas.isEmpty() && state.crowd.scannedDevices.isEmpty() -> "empty_mesh"
                                else -> null
                            },
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "EventTips"
                        ) { tipState ->
                            if (tipState == "empty_mesh" && showTip) {
                                BlukitTip(
                                    text = "THE MESH IS SILENT. AWAKEN A CROWD OR WAIT FOR NEARBY PERSONAS.",
                                    onDismiss = { showTip = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        themeColor = StealthPrimary,
        glowIntensityTarget = 0.4f
    )

    if (showVault) {
        VaultOverlay(
            archivedGroups = state.session.archivedGroups,
            onRestore = { onRestoreCrowd(it); showVault = false },
            onDismiss = { showVault = false }
        )
    }
}

@Composable
fun VaultOverlay(
    archivedGroups: List<Resonance>,
    onRestore: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A0C14),
        titleContentColor = StealthRose,
        title = { Text("SUNK PULSE VAULT", fontWeight = FontWeight.Black, fontSize = 16.sp) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(archivedGroups) { group ->
                    Surface(
                        onClick = { onRestore(group.id) },
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (group.scope == Resonance.SCOPE_LOCAL) "📱" else "🌬️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = group.name.uppercase(), fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = "INACTIVE FOR 30+ DAYS", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                            }
                            Icon(Icons.Rounded.Unarchive, contentDescription = null, tint = StealthPrimary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE", color = Color.White.copy(alpha = 0.5f)) }
        }
    )
}

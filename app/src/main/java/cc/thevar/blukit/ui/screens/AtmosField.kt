package cc.thevar.blukit.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.VibeGroup
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
 * THE ATMOS FIELD: The top-level spectrum view of the mesh.
 * Displays all nearby vibes and airs on a discovery radar.
 * Integrates spectral tips for onboarding and radar discovery.
 */
@Composable
fun AtmosField(
    state: BluetoothUiState,
    localDeviceId: String,
    localNickname: String,
    localEmoji: String,
    vibedPeers: Set<String> = emptySet(),
    noiseFilterEnabled: Boolean = false,
    onStartScan: () -> Unit,
    onDeviceClick: (P2PDevice) -> Unit,
    onDeleteVibe: (String) -> Unit,
    onWhisper: (P2PDevice) -> Unit,
    onIdentifyUser: (String) -> Unit = {},
    onNicknameChange: (String) -> Unit = {},
    onOnboardingDone: () -> Unit = {},
    showOnboarding: Boolean = false,
    airIsStill: Boolean = false,
    // Hub Callbacks
    messageText: String = "",
    onMessageChange: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onSearchToggle: (() -> Unit)? = null,
    onCreatePublicTie: ((String) -> Unit)? = null,
    onAcceptLink: (P2PDevice) -> Unit = {},
    onDenyLink: (P2PDevice) -> Unit = {},
    onStartSideVibe: () -> Unit = {},
    onStartTie: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onAttachFile: () -> Unit = {},
    onShowPrivacy: () -> Unit = {},
    onNavigateToGroup: (String) -> Unit = {},
    isSearchActive: Boolean = false,
    onRestoreAir: (String) -> Unit = {}
) {
    var showTip by remember { mutableStateOf(true) }
    val activeVibeId = LocalActiveVibeId.current
    var vibeGhostData by remember { mutableStateOf<GhostVibeData?>(null) }
    var showVault by remember { mutableStateOf(false) }

    val airMetas = remember(state.session.groups) {
        state.session.groups.filter { it.scope == VibeGroup.SCOPE_PUBLIC && it.parentId == null }
    }

    val vibesData = remember(state.session.messages, state.session.groups, vibedPeers, noiseFilterEnabled, isSearchActive, messageText) {
        val baseVibes = if (noiseFilterEnabled && vibedPeers.isNotEmpty()) {
            state.session.messages.filter { it.senderId in vibedPeers || it.senderId == localDeviceId }
        } else {
            state.session.messages
        }
        
        val searchFiltered = if (!isSearchActive || messageText.isBlank()) baseVibes else {
            baseVibes.filter { msg ->
                msg.content.contains(messageText, ignoreCase = true) || msg.senderName.contains(messageText, ignoreCase = true)
            }
        }
        
        val groupedByTie = searchFiltered.groupBy { msg ->
            when {
                msg.vibeType == MessagePayload.VIBE_SILENCE -> VibeGroup.ID_SILENCE
                msg.groupId != null -> msg.groupId!!
                else -> VibeGroup.ID_AIR
            }
        }
        
        val counts = groupedByTie.mapValues { it.value.size }
        val filtered = groupedByTie.map { it.value.maxBy { msg -> msg.timestamp } }
        val sorted = filtered.sortedByDescending { it.timestamp }
        Triple(sorted, counts, false)
    }

    val (vibes, vibeCounts, _) = vibesData
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    BlukitFieldScaffold(
        themeColor = StealthPrimary,
        glowIntensityTarget = 0.4f,
        floatingContent = {
            AnimatedContent(
                targetState = when {
                    airMetas.isEmpty() && state.crowd.scannedDevices.isEmpty() -> "empty_mesh"
                    else -> null
                },
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "AtmosTips"
            ) { tipState ->
                if (tipState == "empty_mesh" && showTip) {
                    BlukitTip(
                        text = "THE MESH IS SILENT. AWAKEN AN AIR OR WAIT FOR NEARBY PERSONAS.",
                        onDismiss = { showTip = false }
                    )
                }
            }
        },
        fieldContent = {
            Column(modifier = Modifier.fillMaxSize().padding(top = 80.dp)) {
                Text(
                    text = "AIR METAS", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = StealthPrimary, 
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(airMetas) { air ->
                        MetaVibeItem(
                            title = air.name,
                            subtitle = "PUBLIC FREQUENCY",
                            icon = Icons.Rounded.Grain,
                            themeColor = StealthPrimary,
                            count = air.memberIds.size,
                            lastUpdate = sdf.format(Date(air.lastVibeTimestamp)),
                            onClick = { onNavigateToGroup(air.id) }
                        )
                    }
                }
            }

            // Radar remains for persona discovery
            RipplesField(
                state = state,
                localDeviceId = localDeviceId,
                localNickname = localNickname,
                localEmoji = localEmoji,
                activeBubbles = emptyList(),
                selectedDevices = state.crowd.selectedDevices,
                vibedPeers = vibedPeers,
                isFilterMode = noiseFilterEnabled,
                showGhostOnboarding = showOnboarding,
                onOnboardingDone = onOnboardingDone,
                onNicknameChange = onNicknameChange,
                vibeGhostData = vibeGhostData,
                onDismissGhost = { vibeGhostData = null; activeVibeId.value = null },
                onDeviceClick = onDeviceClick,
                onDeviceLongClick = { targetDevice ->
                    val menuId = targetDevice.persistentId ?: targetDevice.id
                    activeVibeId.value = menuId
                    vibeGhostData = GhostVibeData(
                        emoji = targetDevice.emoji,
                        title = targetDevice.name ?: "PERSONA",
                        subtitle = "ATMOS NODE",
                        themeColor = StealthPrimary,
                        sourceId = menuId,
                        actions = mutableListOf<GhostAction>().apply {
                            add(GhostAction(Icons.Rounded.Hearing, "WHISPER", StealthPrimary) { onWhisper(targetDevice) })
                            add(GhostAction(Icons.Rounded.Radar, "IDENTIFY", Color.White) { onIdentifyUser(menuId) })
                        }
                    )
                },
                onStartScan = onStartScan,
                drawBackground = false, // Background handled by Scaffold
                drawNodes = true
            )
        },
        tickerContent = {
            // Atmos ticker shows the latest global energy
            VibingVibesTicker(
                state = state,
                energyList = vibes.map { msg -> 
                    val dev = P2PDevice(id = msg.senderId, name = msg.senderName, emoji = msg.senderEmoji ?: "👤", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
                    dev to msg 
                },
                vibeCounts = vibeCounts,
                localDeviceId = localDeviceId,
                vibedPeers = vibedPeers,
                isGrouped = true,
                onVibeClick = { onNavigateToGroup(it) }, // Simplified for now
                onDeviceLongClick = { },
                onDeleteVibe = onDeleteVibe,
                modifier = Modifier.fillMaxSize()
            )
        },
        inputContent = {
            BlukitVibeHub(
                currentRoute = cc.thevar.blukit.ui.navigation.Route.Atmos,
                messageText = messageText,
                onMessageChange = onMessageChange,
                onSend = onSend,
                vibeCount = vibes.size,
                airIsStill = airIsStill,
                incomingLinkRequests = state.crowd.incomingLinkRequests,
                selectedDevices = state.crowd.selectedDevices,
                vibedPeers = vibedPeers,
                groups = state.session.groups,
                onAcceptLink = onAcceptLink,
                onDenyLink = onDenyLink,
                onStartSideVibe = onStartSideVibe,
                onStartTie = onStartTie,
                onClearSelection = onClearSelection,
                onAttachFile = onAttachFile,
                onSearchToggle = onSearchToggle,
                onCreatePublicTie = onCreatePublicTie,
                isSearchMode = isSearchActive,
                onShowPrivacy = onShowPrivacy,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )

    if (showVault) {
        VaultOverlay(
            archivedGroups = state.session.archivedGroups,
            onRestore = { onRestoreAir(it); showVault = false },
            onDismiss = { showVault = false }
        )
    }
}

@Composable
fun VaultOverlay(
    archivedGroups: List<VibeGroup>,
    onRestore: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A0C14),
        titleContentColor = StealthRose,
        title = { Text("SUNK VIBE VAULT", fontWeight = FontWeight.Black, fontSize = 16.sp) },
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
                            Text(text = if (group.scope == VibeGroup.SCOPE_LOCAL) "📱" else "🌬️", fontSize = 20.sp)
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

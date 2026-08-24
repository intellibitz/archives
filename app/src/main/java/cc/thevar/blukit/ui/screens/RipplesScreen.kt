package cc.thevar.blukit.ui.screens

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
 * THE VIBES: BLUKIT ENERGY TICKER.
 */
@Composable
fun RipplesScreen(
    state: BluetoothUiState,
    localDeviceId: String,
    localNickname: String,
    localEmoji: String,
    vibedPeers: Set<String> = emptySet(),
    noiseFilterEnabled: Boolean = false,
    onStartScan: () -> Unit,
    onDeviceClick: (P2PDevice) -> Unit,
    onBroadcastMessage: (String, Int) -> Unit,
    onDeleteVibe: (String) -> Unit,
    onManageTie: (String) -> Unit = {},
    onBlockUser: (String) -> Unit,
    onUnblockUser: (String) -> Unit,
    onWhisper: (P2PDevice) -> Unit,
    onToggleSelection: (String) -> Unit,
    onIdentifyUser: (String) -> Unit = {},
    onNicknameChange: (String) -> Unit = {},
    onRestoreAir: (String) -> Unit = {},
    onClearFocus: () -> Unit,
    searchText: String = "",
    airIsStill: Boolean = false,
    showOnboarding: Boolean = false,
    onOnboardingDone: () -> Unit = {},
    showAirGhost: Boolean = false,
    onAirNameChange: (String) -> Unit = {},
    onCreateAir: () -> Unit = {},
    onDismissAirGhost: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val activeBubbles = remember { mutableStateListOf<BubbleData>() }
    val activeVibeId = LocalActiveVibeId.current
    var messageToDelete by remember { mutableStateOf<String?>(null) }
    var vibeGhostData by remember { mutableStateOf<GhostVibeData?>(null) }
    var showVault by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        val vibesData = remember(state.session.messages, state.session.groups, vibedPeers, noiseFilterEnabled, searchText) {
            val baseVibes = if (noiseFilterEnabled && vibedPeers.isNotEmpty()) {
                state.session.messages.filter { it.senderId in vibedPeers || it.senderId == localDeviceId }
            } else {
                state.session.messages
            }
            
            val searchFiltered = if (searchText.isBlank()) baseVibes else {
                baseVibes.filter { msg ->
                    msg.content.contains(searchText, ignoreCase = true) || msg.senderName.contains(searchText, ignoreCase = true)
                }
            }
            
            // TACTICAL GROUPING: Ensure silence vibes always group under ID_SILENCE
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

        val energyList = remember(state.session.groups, vibes) {
            val groups = state.session.groups.associateBy { it.id }
            val existingGids = vibes.mapNotNull { it.groupId }.toSet()
            
            // Collect groups that have NO vibes yet
            val emptyGroups = state.session.groups.filter { 
                it.id !in existingGids && it.id != VibeGroup.ID_AIR && it.id != VibeGroup.ID_SILENCE 
            }
            
            val messagePairs = vibes.map { msg ->
                val gid = msg.groupId ?: VibeGroup.ID_AIR
                val group = groups[gid]
                val tieIdentity = if (group != null) {
                    val emoji = when (group.scope) {
                        VibeGroup.SCOPE_LOCAL -> "📱"
                        VibeGroup.SCOPE_PRIVATE -> "🔒"
                        else -> "🌬️"
                    }
                    P2PDevice(id = gid, name = group.name, emoji = emoji, medium = P2PDevice.ConnectionMedium.BLUETOOTH)
                } else {
                    P2PDevice(id = VibeGroup.ID_AIR, name = "THE AIR", emoji = "🌬️", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
                }
                Pair(tieIdentity, msg)
            }

            val emptyPairs = emptyGroups.map { group ->
                val emoji = when (group.scope) {
                    VibeGroup.SCOPE_LOCAL -> "📱"
                    VibeGroup.SCOPE_PRIVATE -> "🔒"
                    else -> "🌬️"
                }
                Pair(P2PDevice(id = group.id, name = group.name, emoji = emoji, medium = P2PDevice.ConnectionMedium.BLUETOOTH), null as MessagePayload?)
            }

            (messagePairs + emptyPairs).sortedByDescending { it.second?.timestamp ?: Long.MAX_VALUE }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            RipplesField(
                state = state,
                localDeviceId = localDeviceId,
                localNickname = localNickname,
                localEmoji = localEmoji,
                activeBubbles = activeBubbles,
                selectedDevices = state.crowd.selectedDevices,
                vibedPeers = vibedPeers,
                isFilterMode = noiseFilterEnabled,
                showGhostOnboarding = showOnboarding,
                onOnboardingDone = onOnboardingDone,
                onNicknameChange = onNicknameChange,
                vibeGhostData = vibeGhostData,
                onDismissGhost = { vibeGhostData = null; activeVibeId.value = null },
                onDeviceClick = { onDeviceClick(it) },
                onDeviceLongClick = { targetDevice ->
                    val menuId = targetDevice.persistentId ?: targetDevice.id
                    activeVibeId.value = menuId
                    vibeGhostData = GhostVibeData(
                        emoji = targetDevice.emoji,
                        title = targetDevice.name ?: "PERSONA",
                        subtitle = "AIR NODE",
                        themeColor = StealthPrimary,
                        sourceId = menuId,
                        actions = mutableListOf<GhostAction>().apply {
                            add(GhostAction(Icons.Rounded.Hearing, "WHISPER", StealthPrimary) { onWhisper(targetDevice) })
                            add(GhostAction(Icons.Rounded.Radar, "IDENTIFY", Color.White) { onIdentifyUser(menuId) })
                            if (menuId in state.crowd.blockedUsers) add(GhostAction(Icons.Rounded.LockOpen, "UNBLOCK", StealthPrimary) { onUnblockUser(menuId) })
                            else add(GhostAction(Icons.Rounded.Block, "BLOCK", Color.Red) { onBlockUser(menuId) })
                        }
                    )
                },
                onStartScan = onStartScan,
                modifier = Modifier.height(240.dp),
                airList = energyList.map { it.first to (vibeCounts[it.first.id] ?: 0) },
                showAirGhost = showAirGhost,
                airRitualGhost = {
                    if (showAirGhost) {
                        AirRitualGhost(
                            onNameChange = onAirNameChange,
                            onDone = onCreateAir,
                            onDismiss = onDismissAirGhost,
                            nearbyAirs = state.session.groups,
                            onJoinAir = onManageTie
                        )
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE VIBES",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (state.session.archivedGroups.isNotEmpty()) {
                    Surface(
                        onClick = { showVault = true },
                        color = StealthRose.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, StealthRose.copy(alpha = 0.2f))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Archive, contentDescription = null, tint = StealthRose, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "VAULT", color = StealthRose, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            VibingVibesTicker(
                state = state,
                energyList = energyList,
                vibeCounts = vibeCounts,
                localDeviceId = localDeviceId,
                vibedPeers = vibedPeers,
                isGrouped = true,
                onManageTie = onManageTie,
                onStartWhisper = onWhisper,
                reverseLayout = false,
                onVibeClick = { messageId -> 
                    val msg = state.session.messages.find { it.messageId == messageId }
                    if (msg != null) {
                        val isMyVibe = msg.senderId == localDeviceId
                        activeVibeId.value = messageId
                        vibeGhostData = GhostVibeData(
                            emoji = msg.senderEmoji ?: "💬",
                            title = msg.senderName.uppercase(),
                            subtitle = msg.content,
                            themeColor = if (msg.vibeType == MessagePayload.VIBE_SHOUT) StealthPrimary else StealthRose,
                            sourceId = messageId,
                            actions = mutableListOf<GhostAction>().apply {
                                if (isMyVibe) {
                                    if (msg.vibeType == MessagePayload.VIBE_SILENCE) {
                                        add(GhostAction(Icons.Rounded.Grain, "SHOUT", StealthPrimary) { onBroadcastMessage(msg.content, MessagePayload.VIBE_SHOUT) })
                                    }
                                } else {
                                    if (msg.vibeType == MessagePayload.VIBE_SHOUT) {
                                        add(GhostAction(Icons.Rounded.Grain, "BOOST SHOUT", StealthPrimary) { onBroadcastMessage(msg.content, MessagePayload.VIBE_SHOUT) })
                                    }
                                }
                                
                                add(GhostAction(Icons.Rounded.Hearing, "WHISPER", StealthRose) {
                                    onWhisper(P2PDevice(id = msg.senderId, name = msg.senderName, emoji = msg.senderEmoji ?: "👤"))
                                })
                                
                                add(GhostAction(Icons.Rounded.Delete, "DELETE VIBE", Color.Red) { onDeleteVibe(msg.messageId) })
                            }
                        )
                    }
                },
                onDeviceLongClick = { targetTie -> 
                    val gid = targetTie.id
                    val group = state.session.groups.find { it.id == gid }
                    activeVibeId.value = gid
                    vibeGhostData = GhostVibeData(
                        emoji = targetTie.emoji,
                        title = targetTie.name ?: "AIR",
                        subtitle = if (gid == VibeGroup.ID_AIR) "AIR" else "TIE",
                        themeColor = if (group?.scope == VibeGroup.SCOPE_PRIVATE) StealthRose else StealthPrimary,
                        sourceId = gid,
                        actions = mutableListOf<GhostAction>().apply {
                            add(GhostAction(Icons.Rounded.Settings, "MANAGE", StealthPrimary) { onManageTie(gid) })
                            if (gid != VibeGroup.ID_AIR && gid != VibeGroup.ID_SILENCE) {
                                add(GhostAction(Icons.Rounded.Delete, "DELETE TIE", Color.Red) { 
                                    // TODO: onDeleteTie(gid)
                                })
                            }
                        }
                    )
                },
                onToggleSelection = onToggleSelection,
                onDeleteVibe = onDeleteVibe,
                modifier = Modifier.weight(1f)
            )
        }

        if (messageToDelete != null) {
            AlertDialog(onDismissRequest = { messageToDelete = null }, containerColor = Color.Black, titleContentColor = StealthRose, textContentColor = Color.White, title = { Text("REMOVE VIBE?", fontWeight = FontWeight.Black) }, text = { Text("THIS VIBE WILL BE REMOVED FROM YOUR LOCAL TICKER.") }, confirmButton = { TextButton(onClick = { onDeleteVibe(messageToDelete!!); messageToDelete = null }) { Text("REMOVE", color = Color.Red, fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = { messageToDelete = null }) { Text("CANCEL") } })
        }

        if (showVault) {
            VaultOverlay(
                archivedGroups = state.session.archivedGroups,
                onRestore = { onRestoreAir(it); showVault = false },
                onDismiss = { showVault = false }
            )
        }
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

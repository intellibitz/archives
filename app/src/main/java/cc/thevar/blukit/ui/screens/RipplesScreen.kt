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
import cc.thevar.blukit.BlukitApplication
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
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
    energySurge: Float = 0f,
    vibedPeers: Set<String> = emptySet(),
    noiseFilterEnabled: Boolean = false,
    lowPowerMode: Boolean = false,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceClick: (P2PDevice) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit = {},
    onBroadcastMessage: (String, Int) -> Unit,
    onDeleteVibe: (String) -> Unit,
    onBlockUser: (String) -> Unit,
    onUnblockUser: (String) -> Unit,
    onWhisper: (P2PDevice) -> Unit,
    onToggleSelection: (String) -> Unit,
    onAcceptLink: (P2PDevice) -> Unit,
    onDenyLink: (P2PDevice) -> Unit,
    onDisconnect: () -> Unit,
    onIdentifyUser: (String) -> Unit = {},
    onClearFocus: () -> Unit,
    hasSidebar: Boolean = false,
    externalFocusedId: String? = null,
    onFocusChange: (String?) -> Unit = {},
    searchText: String = "",
    modifier: Modifier = Modifier
) {
    val hapticManager: cc.thevar.blukit.data.system.HapticManager = koinInject()
    
    val activeBubbles = remember { mutableStateListOf<BubbleData>() }
    val processedMessageIds = remember { mutableSetOf<String>() }
    var selectedPersonaForMenu by remember { mutableStateOf<P2PDevice?>(null) }
    var messageToDelete by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.session.messages) {
        val newMessages = state.session.messages.filter { it.messageId !in processedMessageIds }
        newMessages.forEach { msg ->
            activeBubbles.add(
                BubbleData(
                    msg.senderId,
                    msg.content,
                    System.currentTimeMillis(),
                    msg.messageId,
                    isPrivate = !msg.receiverId.isNullOrBlank()
                )
            )
            processedMessageIds.add(msg.messageId)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            activeBubbles.removeAll { now - it.timestamp > 4000 }
            delay(1000)
        }
    }

    val coordinates = LocalPersonaCoordinates.current
    var selectedVibeForMenu by remember { mutableStateOf<cc.thevar.blukit.domain.model.MessagePayload?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        val vibesData = remember(state.session.messages, vibedPeers, noiseFilterEnabled, externalFocusedId, searchText) {
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
            
            val counts = searchFiltered.groupBy { it.senderId }.mapValues { it.value.size }
            val filtered = if (externalFocusedId != null) {
                searchFiltered.filter { it.senderId == externalFocusedId }
            } else {
                searchFiltered.groupBy { it.senderId }.map { it.value.maxBy { msg -> msg.timestamp } }
            }
            
            val sorted = filtered.sortedBy { it.timestamp }
            Triple(sorted, counts, externalFocusedId != null)
        }

        val (vibes, vibeCounts, isVibeFocused) = vibesData

        val energyList = remember(state.crowd.scannedDevices, vibes, localDeviceId) {
            val devices = state.crowd.scannedDevices
            val deviceMap = devices.associateBy { it.persistentId ?: it.id }
            
            val tickerItems = vibes.map { msg ->
                val device = if (msg.senderId == localDeviceId) {
                    P2PDevice(id = localDeviceId, name = localNickname, emoji = localEmoji, medium = P2PDevice.ConnectionMedium.BLUETOOTH)
                } else {
                    deviceMap[msg.senderId] ?: P2PDevice(id = msg.senderId, name = msg.senderName, emoji = msg.senderEmoji ?: "👤", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
                }
                device to msg
            }

            // Also add silent devices (those without vibes) to keep them in the energy list if needed?
            // Actually, for a ticker, we only care about messages.
            // But the user might want to select silent personas too.
            // Let's stick to vibes for now as the user said "vibe is in ticker".
            tickerItems.sortedByDescending { it.second.timestamp }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            RipplesField(
                state = state,
                localDeviceId = localDeviceId,
                localEmoji = localEmoji,
                activeBubbles = activeBubbles,
                selectedDevices = state.crowd.selectedDevices,
                vibedPeers = vibedPeers,
                externalEnergy = energySurge,
                onlyTies = false,
                isFilterMode = noiseFilterEnabled || isVibeFocused,
                lowPowerMode = lowPowerMode,
                subjectId = externalFocusedId,
                onDeviceClick = onDeviceClick,
                onDeviceLongClick = { selectedPersonaForMenu = it },
                onStartScan = onStartScan,
                onVibeSurge = { hapticManager.triggerProximityVibe(it) },
                drawBackground = true,
                drawNodes = false,
                modifier = Modifier.weight(1f).padding(end = if (hasSidebar) 72.dp else 0.dp)
            ) {
                VibingVibesTicker(
                    state = state,
                    energyList = energyList,
                    vibeCounts = vibeCounts,
                    localDeviceId = localDeviceId,
                    vibedPeers = vibedPeers,
                    isGrouped = !isVibeFocused,
                    onVibeClick = { messageId -> 
                        val msg = state.session.messages.find { it.messageId == messageId }
                        if (msg != null) {
                            selectedVibeForMenu = msg
                        }
                    },
                    onDeviceLongClick = { selectedPersonaForMenu = it },
                    onToggleSelection = onToggleSelection,
                    onDeleteVibe = { messageToDelete = it },
                    onAcceptLink = onAcceptLink,
                    onDenyLink = onDenyLink,
                    onFocusChange = onFocusChange,
                    modifier = Modifier.fillMaxSize().zIndex(10f)
                )
            }
        }

        if (selectedVibeForMenu != null) {
            val msg = selectedVibeForMenu!!
            VibeActionMenu(
                message = msg,
                isBroadcasted = msg.vibeType == cc.thevar.blukit.domain.model.MessagePayload.VIBE_PUBLIC,
                onBroadcast = { onBroadcastMessage(msg.content, cc.thevar.blukit.domain.model.MessagePayload.VIBE_PUBLIC); selectedVibeForMenu = null },
                onInvite = {
                    if (state.crowd.selectedDevices.isNotEmpty()) {
                        onBroadcastMessage(msg.content, cc.thevar.blukit.domain.model.MessagePayload.VIBE_TIE)
                        onClearFocus() 
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("SELECT PERSONAS TO INVITE FIRST") }
                    }
                    selectedVibeForMenu = null
                },
                onDelete = { messageToDelete = msg.messageId; selectedVibeForMenu = null },
                onDismiss = { selectedVibeForMenu = null }
            )
        }

        if (selectedPersonaForMenu != null) {
            val menuDevice = selectedPersonaForMenu!!
            val menuId = menuDevice.persistentId ?: menuDevice.id
            PersonaOptionsMenu(device = menuDevice, isTied = menuDevice.id in state.session.connectedLinks, isBlocked = menuId in state.crowd.blockedUsers, isSelected = menuDevice.id in state.crowd.selectedDevices, isRequesting = state.crowd.incomingLinkRequests.any { it.id == menuDevice.id }, onVibe = { onDeviceLongClick(menuDevice); selectedPersonaForMenu = null }, onSelect = { onToggleSelection(menuDevice.id); selectedPersonaForMenu = null }, onAccept = { onAcceptLink(menuDevice); selectedPersonaForMenu = null }, onDeny = { onDenyLink(menuDevice); selectedPersonaForMenu = null }, onDisconnect = { onDisconnect(); selectedPersonaForMenu = null }, onIdentify = { onIdentifyUser(menuId); selectedPersonaForMenu = null }, onBlock = { onBlockUser(menuId); selectedPersonaForMenu = null }, onUnblock = { onUnblockUser(menuId); selectedPersonaForMenu = null }, onDismiss = { selectedPersonaForMenu = null })
        }

        if (messageToDelete != null) {
            AlertDialog(onDismissRequest = { messageToDelete = null }, containerColor = Color.Black, titleContentColor = StealthPrimary, textContentColor = Color.White, title = { Text("DELETE VIBE?", fontWeight = FontWeight.Black) }, text = { Text("THIS WILL REMOVE THIS MESSAGE FROM YOUR HISTORY.") }, confirmButton = { TextButton(onClick = { onDeleteVibe(messageToDelete!!); messageToDelete = null }) { Text("DELETE", color = Color.Red, fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = { messageToDelete = null }) { Text("CANCEL") } })
        }
        
        if (noiseFilterEnabled && vibedPeers.isNotEmpty()) {
            val focusPulse by rememberInfiniteTransition(label = "FocusPulse").animateFloat(initialValue = 0.4f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "Alpha")
            Box(modifier = Modifier.fillMaxWidth().padding(top = 110.dp), contentAlignment = Alignment.TopCenter) {
                Surface(onClick = onClearFocus, color = StealthPrimary.copy(alpha = 0.08f * focusPulse), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, StealthPrimary.copy(alpha = 0.2f * focusPulse))) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.FilterCenterFocus, contentDescription = null, tint = StealthPrimary, modifier = Modifier.size(12.dp))
                        Text(text = "FOCUS MODE: ${vibedPeers.size} PERSONAS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = StealthPrimary, letterSpacing = 1.sp)
                        Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = StealthPrimary.copy(alpha = 0.6f), modifier = Modifier.size(10.dp))
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp))
    }
}

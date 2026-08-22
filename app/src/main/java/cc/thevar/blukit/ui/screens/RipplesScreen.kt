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
                searchFiltered
            }
            
            val sorted = filtered.sortedBy { it.timestamp }
            Triple(sorted, counts, externalFocusedId != null)
        }

        val (vibes, vibeCounts, isDetailView) = vibesData

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
                isFilterMode = noiseFilterEnabled || isDetailView,
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
                    isGrouped = !isDetailView,
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

@Composable
private fun VibingVibesTicker(
    state: BluetoothUiState,
    energyList: List<Pair<P2PDevice, cc.thevar.blukit.domain.model.MessagePayload?>>,
    vibeCounts: Map<String, Int>,
    localDeviceId: String,
    vibedPeers: Set<String>,
    isGrouped: Boolean,
    onVibeClick: (String) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit,
    onToggleSelection: (String) -> Unit,
    onDeleteVibe: (String) -> Unit,
    onAcceptLink: (P2PDevice) -> Unit = {},
    onDenyLink: (P2PDevice) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Box(modifier = modifier) {
        if (energyList.isEmpty() && state.crowd.incomingLinkRequests.isEmpty() && state.crowd.outgoingLinkRequests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "NO VIBES IN THE STADIUM", color = Color.White.copy(alpha = 0.2f), fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
        LazyColumn(state = listState, reverseLayout = true, modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp), contentPadding = PaddingValues(top = 40.dp, bottom = 8.dp)) {
            items(energyList.asReversed(), key = { it.second?.messageId ?: it.first.id }) { (device, msg) ->
                val id = device.persistentId ?: device.id
                AnimatedVibeItem(msg = msg, senderDevice = device, isMe = (msg?.senderId ?: id) == localDeviceId, vibeCount = vibeCounts[id] ?: 1, isVibed = id in vibedPeers, isMutual = id in state.session.connectedLinks, isSelected = device.id in state.crowd.selectedDevices, isGrouped = isGrouped, timestamp = if (msg != null) timeFormatter.format(Date(msg.timestamp)) else "", onClick = { if (state.crowd.selectedDevices.isNotEmpty()) onToggleSelection(device.id) else if (msg != null) onVibeClick(msg.messageId) }, onLongClick = { onDeviceLongClick(device) }, onDelete = { msg?.let { onDeleteVibe(it.messageId) } })
            }
            if (state.crowd.incomingLinkRequests.isNotEmpty() || state.crowd.outgoingLinkRequests.isNotEmpty()) {
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
            items(state.crowd.incomingLinkRequests.toList(), key = { "in_${it.id}" }) { VibeRequestTickerItem(it, onAcceptLink, onDenyLink) }
            items(state.crowd.outgoingLinkRequests.toList(), key = { "out_${it.id}" }) { OutgoingVibeRequestTickerItem(it, onDenyLink) }
        }
    }
}

@Composable
private fun VibeRequestTickerItem(device: P2PDevice, onAccept: (P2PDevice) -> Unit, onDeny: (P2PDevice) -> Unit) {
    val coordinates = LocalPersonaCoordinates.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).onGloballyPositioned { val center = Offset(it.size.width - with(density) { 4.dp.toPx() }, it.size.height / 2f); val current = coordinates[device.persistentId ?: device.id] ?: PersonaConnectionPoints(); coordinates[device.persistentId ?: device.id] = current.copy(ticker = it.positionInRoot() + center) }.background(StealthPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).border(0.5.dp, StealthPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 6.dp)) {
        Text(text = device.emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = (device.name ?: "?").uppercase(), fontWeight = FontWeight.Black, color = Color.White, fontSize = 9.sp)
            Text(text = "REQUESTING RESONANCE", fontSize = 6.sp, color = StealthPrimary, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "DENY", modifier = Modifier.clickable { onDeny(device) }, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 8.sp)
            Text(text = "JOIN", modifier = Modifier.clickable { onAccept(device) }, color = StealthPrimary, fontWeight = FontWeight.Black, fontSize = 8.sp)
        }
    }
}

@Composable
private fun OutgoingVibeRequestTickerItem(device: P2PDevice, onCancel: (P2PDevice) -> Unit) {
    val coordinates = LocalPersonaCoordinates.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).onGloballyPositioned { val center = Offset(it.size.width - with(density) { 4.dp.toPx() }, it.size.height / 2f); val current = coordinates[device.persistentId ?: device.id] ?: PersonaConnectionPoints(); coordinates[device.persistentId ?: device.id] = current.copy(ticker = it.positionInRoot() + center) }.background(StealthRose.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).border(0.5.dp, StealthRose.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 6.dp)) {
        Text(text = device.emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = (device.name ?: "?").uppercase(), fontWeight = FontWeight.Black, color = Color.White, fontSize = 9.sp)
            Text(text = "AWAITING RESONANCE...", fontSize = 6.sp, color = StealthRose, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
        Text(text = "CANCEL", modifier = Modifier.clickable { onCancel(device) }, color = StealthRose.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 8.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AnimatedVibeItem(msg: cc.thevar.blukit.domain.model.MessagePayload?, isMe: Boolean, senderDevice: P2PDevice?, vibeCount: Int, isVibed: Boolean, isMutual: Boolean, isSelected: Boolean = false, isGrouped: Boolean, timestamp: String, onClick: () -> Unit, onLongClick: () -> Unit, onDelete: () -> Unit) {
    val coordinates = LocalPersonaCoordinates.current
    val rowId = if (isMe) "YOU" else (senderDevice?.persistentId ?: senderDevice?.id ?: msg?.senderId ?: "UNKNOWN")
    val density = androidx.compose.ui.platform.LocalDensity.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).onGloballyPositioned { val center = Offset(with(density) { 32.dp.toPx() }, it.size.height / 2f); val current = coordinates[rowId] ?: PersonaConnectionPoints(); coordinates[rowId] = current.copy(ticker = it.positionInRoot() + center) }.animateContentSize().background(if (isMe) StealthPrimary.copy(alpha = 0.12f) else if (isSelected) Color.White.copy(alpha = 0.1f) else if (isMutual) StealthRose.copy(alpha = 0.05f) else if (isVibed) StealthPrimary.copy(alpha = 0.03f) else Color.Transparent, RoundedCornerShape(8.dp)).border(if (isMe) 1.5.dp else if (isSelected) 1.dp else 0.dp, if (isMe) StealthPrimary.copy(alpha = 0.3f) else if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(8.dp)).combinedClickable(onClick = onClick, onLongClick = if (isMe) onDelete else onLongClick).padding(horizontal = 4.dp, vertical = 4.dp)) {
        val signatureDevice = senderDevice ?: cc.thevar.blukit.domain.model.P2PDevice(id = "YOU", name = "YOU", emoji = "👤", medium = cc.thevar.blukit.domain.model.P2PDevice.ConnectionMedium.BLUETOOTH)
        VibePersonaSignature(device = signatureDevice, isVibed = isMutual, isSelected = isSelected, isPeerVibed = isVibed, onlyTies = false, size = 22.dp, isStatic = true, modifier = Modifier.padding(end = 4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = (if (isMe) "YOU" else (senderDevice?.name ?: msg?.senderName ?: "?")).uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = if (isMe) StealthPrimary else Color.White, letterSpacing = 1.sp)
                if (timestamp.isNotEmpty()) { Spacer(modifier = Modifier.width(6.dp)); Text(text = timestamp, fontSize = 8.sp, color = Color.White.copy(alpha = 0.2f), fontWeight = FontWeight.Bold) }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (msg?.type == cc.thevar.blukit.domain.model.MessagePayload.TYPE_IMAGE) {
                    AsyncImage(
                        model = msg.content,
                        contentDescription = "Image",
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                    )
                } else {
                    Text(text = msg?.content ?: "Awaiting resonance...", fontSize = 11.sp, color = Color.White.copy(alpha = if (msg != null) 0.7f else 0.2f), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                }
                
                if (msg != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    val (typeLabel, typeColor) = when (msg.vibeType) {
                        cc.thevar.blukit.domain.model.MessagePayload.VIBE_PUBLIC -> "PUBLIC" to StealthPrimary
                        cc.thevar.blukit.domain.model.MessagePayload.VIBE_LOCAL -> "LOCAL" to Color.White.copy(alpha = 0.4f)
                        else -> "SECURE" to StealthRose
                    }
                    Text(text = typeLabel, fontSize = 6.sp, fontWeight = FontWeight.Black, color = typeColor, modifier = Modifier.background(typeColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp))
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        if (isMutual) { Icon(imageVector = Icons.Rounded.Flare, contentDescription = null, tint = StealthRose.copy(alpha = 0.4f), modifier = Modifier.size(10.dp)) } else if (isSelected) { Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(12.dp)) }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.onGloballyPositioned { val center = Offset(it.size.width / 2f, it.size.height / 2f); val current = coordinates[rowId] ?: PersonaConnectionPoints(); coordinates[rowId] = current.copy(uph = it.positionInRoot() + center) }.width(48.dp)) { Surface(modifier = Modifier.size(24.dp), shape = CircleShape, color = when { isSelected -> Color.White.copy(alpha = 0.2f); isMutual -> StealthRose.copy(alpha = 0.2f); isVibed -> StealthPrimary.copy(alpha = 0.2f); else -> Color.White.copy(alpha = 0.05f) }, border = BorderStroke(if (isSelected || isMutual || isVibed) 1.dp else 0.5.dp, when { isSelected -> Color.White; isMutual -> StealthRose; isVibed -> StealthPrimary; else -> Color.White.copy(alpha = 0.1f) })) { Box(contentAlignment = Alignment.Center) { Text(text = signatureDevice.emoji, fontSize = 12.sp) } }
        Text(text = (signatureDevice.name ?: "?").take(5).uppercase(), fontSize = 6.sp, fontWeight = FontWeight.Black, color = if (isMutual) StealthRose else if (isVibed) StealthPrimary else Color.White.copy(alpha = 0.3f), maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

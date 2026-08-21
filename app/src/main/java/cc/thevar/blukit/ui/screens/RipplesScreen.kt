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
import cc.thevar.blukit.BlukitApplication
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import org.koin.compose.koinInject

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
    onlyTies: Boolean = false,
    vibedPeers: Set<String> = emptySet(),
    noiseFilterEnabled: Boolean = false,
    lowPowerMode: Boolean = false,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceClick: (P2PDevice) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit = {},
    onBroadcastMessage: (String, Boolean) -> Unit,
    onDeleteVibe: (String) -> Unit,
    onBlockUser: (String) -> Unit,
    onUnblockUser: (String) -> Unit,
    onWhisper: (P2PDevice) -> Unit,
    onClearFocus: () -> Unit,
    hasSidebar: Boolean = false,
    externalFocusedId: String? = null,
    onFocusChange: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hapticManager: cc.thevar.blukit.data.system.HapticManager = koinInject()
    
    val activeBubbles = remember { mutableStateListOf<BubbleData>() }
    val processedMessageIds = remember { mutableSetOf<String>() }
    var selectedPersonaForMenu by remember { mutableStateOf<P2PDevice?>(null) }
    var messageToDelete by remember { mutableStateOf<String?>(null) }

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

    Box(modifier = modifier.fillMaxSize()) {
        val vibesData = remember(state.session.messages, onlyTies, vibedPeers, noiseFilterEnabled, externalFocusedId) {
            val baseVibes = if (onlyTies) {
                state.session.messages.filter { !it.receiverId.isNullOrBlank() }
            } else if (noiseFilterEnabled && vibedPeers.isNotEmpty()) {
                state.session.messages.filter { 
                    it.receiverId.isNullOrBlank() && (it.senderId in vibedPeers || it.senderId == localDeviceId)
                }
            } else {
                state.session.messages.filter { it.receiverId.isNullOrBlank() }
            }
            
            val counts = baseVibes.groupBy { it.senderId }.mapValues { it.value.size }
            
            val filtered = if (externalFocusedId != null) {
                baseVibes.filter { it.senderId == externalFocusedId }
            } else {
                baseVibes.groupBy { it.senderId }
                    .map { entry -> entry.value.maxBy { it.timestamp } }
            }
            
            val sorted = filtered.sortedBy { it.timestamp }.distinctBy { it.messageId }
            Triple(sorted, counts, externalFocusedId != null)
        }

        val (vibes, vibeCounts, isDetailView) = vibesData

        // LAYER 1: Atmosphere (Background + Arcs + Ripples)
        Column(modifier = Modifier.fillMaxSize()) {
            RipplesField(
                state = state,
                localDeviceId = localDeviceId,
                localEmoji = localEmoji,
                activeBubbles = activeBubbles,
                selectedDevices = state.crowd.selectedDevices,
                vibedPeers = vibedPeers,
                externalEnergy = energySurge,
                onlyTies = onlyTies,
                isFilterMode = noiseFilterEnabled || isDetailView,
                lowPowerMode = lowPowerMode,
                onDeviceClick = { selectedPersonaForMenu = it },
                onDeviceLongClick = { selectedPersonaForMenu = it },
                onStartScan = onStartScan,
                onVibeSurge = { hapticManager.triggerProximityVibe(it) },
                drawBackground = true,
                drawNodes = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = if (hasSidebar) 72.dp else 0.dp)
            ) {
                // LAYER 2: Top-level Interactive Ticker
                VibingVibesTicker(
                    state = state,
                    vibes = vibes,
                    vibeCounts = vibeCounts,
                    localDeviceId = localDeviceId,
                    vibedPeers = vibedPeers,
                    isGrouped = !isDetailView,
                    onVibeClick = { senderId -> 
                        if (externalFocusedId == null) {
                            onFocusChange(senderId)
                        } else {
                            onFocusChange(null) 
                        }
                    },
                    onDeviceLongClick = { selectedPersonaForMenu = it },
                    onDeleteVibe = { messageToDelete = it },
                    modifier = Modifier.fillMaxSize().zIndex(10f)
                )
            }
        }

        // LAYER 4: Persona Context Menu
        if (selectedPersonaForMenu != null) {
            PersonaOptionsMenu(
                device = selectedPersonaForMenu!!,
                isVibed = (selectedPersonaForMenu!!.persistentId ?: selectedPersonaForMenu!!.id) in vibedPeers,
                isTied = selectedPersonaForMenu!!.id in state.session.connectedLinks,
                isBlocked = (selectedPersonaForMenu!!.persistentId ?: selectedPersonaForMenu!!.id) in state.crowd.blockedUsers,
                onFocus = {
                    onDeviceClick(selectedPersonaForMenu!!)
                    selectedPersonaForMenu = null
                },
                onVibe = {
                    onDeviceLongClick(selectedPersonaForMenu!!)
                    selectedPersonaForMenu = null
                },
                onWhisper = {
                    onWhisper(selectedPersonaForMenu!!)
                    selectedPersonaForMenu = null
                },
                onBlock = {
                    val id = selectedPersonaForMenu!!.persistentId ?: selectedPersonaForMenu!!.id
                    onBlockUser(id)
                    selectedPersonaForMenu = null
                },
                onUnblock = {
                    val id = selectedPersonaForMenu!!.persistentId ?: selectedPersonaForMenu!!.id
                    onUnblockUser(id)
                    selectedPersonaForMenu = null
                },
                onDismiss = { selectedPersonaForMenu = null }
            )
        }

        // LAYER 5: Delete Confirmation
        if (messageToDelete != null) {
            AlertDialog(
                onDismissRequest = { messageToDelete = null },
                containerColor = Color.Black,
                titleContentColor = StealthPrimary,
                textContentColor = Color.White,
                title = { Text("DELETE VIBE?", fontWeight = FontWeight.Black) },
                text = { Text("THIS WILL REMOVE THIS MESSAGE FROM YOUR HISTORY.") },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteVibe(messageToDelete!!)
                        messageToDelete = null
                    }) {
                        Text("DELETE", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { messageToDelete = null }) {
                        Text("CANCEL")
                    }
                }
            )
        }

        // LAYER 6: Empty State Hints
        if (onlyTies && state.session.connectedLinks.isEmpty()) {
            EmptyFocusHint()
        }
        
        // LAYER 7: Noise Filter HUD
        if (noiseFilterEnabled && !onlyTies) {
            val focusPulse by rememberInfiniteTransition(label = "FocusPulse").animateFloat(
                initialValue = 0.4f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "Alpha"
            )
            
            Box(modifier = Modifier.fillMaxWidth().padding(top = 110.dp), contentAlignment = Alignment.TopCenter) {
                Surface(
                    onClick = onClearFocus, // Clear the focus circle
                    color = StealthPrimary.copy(alpha = 0.08f * focusPulse),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, StealthPrimary.copy(alpha = 0.2f * focusPulse))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.FilterCenterFocus, contentDescription = null, tint = StealthPrimary, modifier = Modifier.size(12.dp))
                        Text(
                            text = "FOCUS MODE: ${vibedPeers.size} PERSONAS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = StealthPrimary,
                            letterSpacing = 1.sp
                        )
                        Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = StealthPrimary.copy(alpha = 0.6f), modifier = Modifier.size(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFocusHint() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "NO GROUPS YET",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.2f),
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ESTABLISH SECURE LINKS TO VIBE.",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.15f),
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun VibingVibesTicker(
    state: BluetoothUiState,
    vibes: List<cc.thevar.blukit.domain.model.MessagePayload>,
    vibeCounts: Map<String, Int>,
    localDeviceId: String,
    vibedPeers: Set<String>,
    isGrouped: Boolean,
    onVibeClick: (String) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit,
    onDeleteVibe: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    LaunchedEffect(vibes.size) {
        if (vibes.isNotEmpty()) {
            listState.animateScrollToItem(vibes.size - 1)
        }
    }

    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
             ) {
            items(vibes, key = { if (isGrouped) it.senderId else it.messageId }) { msg ->
                val isMe = msg.senderId == localDeviceId
                val senderDevice = remember(msg.senderId, state.crowd.scannedDevices) {
                    state.crowd.scannedDevices.find { it.id == msg.senderId || it.persistentId == msg.senderId }
                }
                
                    AnimatedVibeItem(
                        msg = msg,
                        isMe = isMe,
                        senderDevice = senderDevice,
                        vibeCount = vibeCounts[msg.senderId] ?: 1,
                        isVibed = msg.senderId in vibedPeers,
                        isMutual = msg.senderId in state.session.connectedLinks,
                        isGrouped = isGrouped,
                        timestamp = timeFormatter.format(Date(msg.timestamp)),
                        onClick = { onVibeClick(msg.senderId) },
                        onLongClick = { senderDevice?.let { onDeviceLongClick(it) } },
                        onDelete = { onDeleteVibe(msg.messageId) }
                    )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AnimatedVibeItem(
    msg: cc.thevar.blukit.domain.model.MessagePayload,
    isMe: Boolean,
    senderDevice: P2PDevice?,
    vibeCount: Int,
    isVibed: Boolean,
    isMutual: Boolean,
    isGrouped: Boolean,
    timestamp: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    val coordinates = LocalPersonaCoordinates.current
    val rowId = if (isMe) "YOU" else msg.senderId

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .onGloballyPositioned { 
                val current = coordinates[rowId] ?: PersonaConnectionPoints()
                coordinates[rowId] = current.copy(ticker = it.positionInRoot())
            }
            .animateContentSize()
            .combinedClickable(
                onClick = onClick,
                onLongClick = if (isMe) onDelete else onLongClick
            )
            .background(
                if (isMe) StealthPrimary.copy(alpha = 0.12f)
                else if (isMutual) StealthRose.copy(alpha = 0.05f) 
                else if (isVibed) StealthPrimary.copy(alpha = 0.03f) 
                else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .border(
                if (isMe) 1.5.dp else 0.dp,
                if (isMe) StealthPrimary.copy(alpha = 0.3f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // PREFIX: (YOU) or PERSONA
        if (isMe) {
            Surface(
                color = StealthPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Text(
                    text = "YOU",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    letterSpacing = 0.5.sp
                )
            }
        }

        // PERSONA: Emoji + Name
        val displayName = (senderDevice?.name ?: msg.senderName).uppercase().take(8)
        val emoji = senderDevice?.emoji ?: msg.senderEmoji ?: "👤"
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 10.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = displayName,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isMe) StealthPrimary else if (isMutual) StealthRose else Color.White.copy(alpha = 0.7f),
                letterSpacing = 0.5.sp
            )
        }

        Text(
            text = " : ",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.1f)
        )

        // TIMESTAMP
        Text(
            text = timestamp,
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.3f)
        )

        Text(
            text = " . ",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = if (isMutual) StealthRose else StealthPrimary,
            modifier = Modifier.offset(y = (-1).dp)
        )

        // MESSAGE CONTENT
        Text(
            text = msg.content,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )

        // VIBE COUNT & EXPAND INDICATOR (Moved after message)
        if (isGrouped && vibeCount > 1) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .background(
                        (if (isMutual) StealthRose else StealthPrimary).copy(alpha = 0.15f),
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        0.5.dp,
                        (if (isMutual) StealthRose else StealthPrimary).copy(alpha = 0.3f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "+$vibeCount",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isMutual) StealthRose else StealthPrimary
                    )
                    Icon(
                        imageVector = Icons.Rounded.UnfoldMore,
                        contentDescription = null,
                        tint = (if (isMutual) StealthRose else StealthPrimary).copy(alpha = 0.6f),
                        modifier = Modifier.size(8.dp)
                    )
                }
            }
        } else if (!isGrouped) {
             Spacer(modifier = Modifier.width(6.dp))
             Icon(
                imageVector = Icons.Rounded.History,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.size(10.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        
        if (isMutual) {
            Icon(
                imageVector = Icons.Rounded.Flare,
                contentDescription = null,
                tint = StealthRose.copy(alpha = 0.4f),
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

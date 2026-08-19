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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticManager = remember { (context.applicationContext as BlukitApplication).hapticManager }
    
    val activeBubbles = remember { mutableStateListOf<BubbleData>() }
    val processedMessageIds = remember { mutableSetOf<String>() }
    var selectedPersonaForMenu by remember { mutableStateOf<P2PDevice?>(null) }
    var messageToDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.messages) {
        val newMessages = state.messages.filter { it.messageId !in processedMessageIds }
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
        val vibes = remember(state.messages, onlyTies, vibedPeers, noiseFilterEnabled) {
            val filtered = if (onlyTies) {
                state.messages.filter { !it.receiverId.isNullOrBlank() }
            } else if (noiseFilterEnabled && vibedPeers.isNotEmpty()) {
                state.messages.filter { 
                    it.receiverId.isNullOrBlank() && (it.senderId in vibedPeers || it.senderId == localDeviceId)
                }
            } else {
                state.messages.filter { it.receiverId.isNullOrBlank() }
            }
            filtered.distinctBy { it.messageId }
        }

        // LAYER 1: Atmosphere (Background + Arcs + Ripples)
        Column(modifier = Modifier.fillMaxSize()) {
            val titleIcon = if (onlyTies) Icons.Rounded.Flare else Icons.Rounded.Groups
            val titleText = if (onlyTies) "GROUPS" else "ALL"
            
            Box(modifier = Modifier.fillMaxWidth()) {
                BlukitTopTitle(title = titleText, icon = titleIcon)
            }
            
            RipplesField(
                state = state,
                localDeviceId = localDeviceId,
                localEmoji = localEmoji,
                activeBubbles = activeBubbles,
                selectedDevices = state.selectedDevices,
                vibedPeers = vibedPeers,
                externalEnergy = energySurge,
                onlyTies = onlyTies,
                isFilterMode = noiseFilterEnabled,
                lowPowerMode = lowPowerMode,
                onDeviceClick = { selectedPersonaForMenu = it },
                onDeviceLongClick = { selectedPersonaForMenu = it },
                onStartScan = onStartScan,
                onVibeSurge = { hapticManager.triggerProximityVibe(it) },
                drawBackground = true,
                drawNodes = true,
                modifier = Modifier.weight(1f)
            ) {
                // LAYER 2: Top-level Interactive Ticker
                VibingVibesTicker(
                    state = state,
                    vibes = vibes,
                    localDeviceId = localDeviceId,
                    onlyTies = onlyTies,
                    vibedPeers = vibedPeers,
                    onDeviceClick = { selectedPersonaForMenu = it },
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
                isTied = selectedPersonaForMenu!!.id in state.connectedLinks,
                isBlocked = (selectedPersonaForMenu!!.persistentId ?: selectedPersonaForMenu!!.id) in state.blockedUsers,
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
        if (onlyTies && state.connectedLinks.isEmpty()) {
            EmptyFocusHint()
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
    localDeviceId: String,
    onlyTies: Boolean,
    vibedPeers: Set<String>,
    onDeviceClick: (P2PDevice) -> Unit,
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
            verticalArrangement = Arrangement.Bottom,
            contentPadding = PaddingValues(top = 100.dp, bottom = 40.dp)
             ) {
            items(vibes, key = { it.messageId }) { msg ->
                val isMe = msg.senderId == localDeviceId
                val senderDevice = remember(msg.senderId, state.scannedDevices) {
                    state.scannedDevices.find { it.id == msg.senderId || it.persistentId == msg.senderId }
                }
                
                    AnimatedVibeItem(
                        msg = msg,
                        isMe = isMe,
                        senderDevice = senderDevice,
                        isVibed = msg.senderId in vibedPeers,
                        isMutual = msg.senderId in state.connectedLinks,
                        onlyTies = onlyTies,
                        timestamp = timeFormatter.format(Date(msg.timestamp)),
                        onClick = { senderDevice?.let { onDeviceClick(it) } },
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
    isVibed: Boolean,
    isMutual: Boolean,
    onlyTies: Boolean,
    timestamp: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).animateContentSize()
    ) {
        if (!isMe) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(
                        if (isMutual) StealthRose.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.04f),
                        RoundedCornerShape(12.dp, 12.dp, 12.dp, 2.dp)
                    )
                    .border(
                        0.5.dp, 
                        if (isMutual) StealthRose.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(12.dp, 12.dp, 12.dp, 2.dp)
                    )
                    .combinedClickable(
                        onClick = onClick, 
                        onLongClick = onLongClick
                    )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Message Content
                    Text(
                        text = msg.content,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.95f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Unified Footer: Identity + Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = timestamp, 
                            fontSize = 7.sp, 
                            color = Color.White.copy(alpha = 0.2f),
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Unified Identity Node inside bubble (Right side)
                        if (senderDevice != null) {
                            VibeNode(
                                device = senderDevice,
                                isVibed = isMutual,
                                isSelected = false,
                                isPeerVibed = isVibed,
                                onlyTies = onlyTies,
                                onClick = onClick,
                                onLongClick = onLongClick,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // My Message
            Box(
                modifier = Modifier
                    .widthIn(max = 240.dp)
                    .background(StealthPrimary.copy(alpha = 0.12f), RoundedCornerShape(12.dp, 12.dp, 2.dp, 12.dp))
                    .border(0.5.dp, StealthPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp, 12.dp, 2.dp, 12.dp))
                    .combinedClickable(
                        onClick = {}, 
                        onLongClick = onDelete
                    )
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = msg.content,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "YOU".uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = StealthPrimary.copy(alpha = 0.4f)
                        )
                        Text(
                            text = timestamp, 
                            fontSize = 7.sp, 
                            color = StealthPrimary.copy(alpha = 0.3f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

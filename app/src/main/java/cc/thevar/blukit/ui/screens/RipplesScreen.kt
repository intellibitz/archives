package cc.thevar.blukit.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cc.thevar.blukit.R
import cc.thevar.blukit.BlukitApplication
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.viewmodels.AirConnectionState
import cc.thevar.blukit.ui.rememberSpreadPermissionsState
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthRose
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * THE VIBES: BLUKIT ENERGY TICKER.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RipplesScreen(
    state: BluetoothUiState,
    localDeviceId: String,
    localNickname: String,
    localEmoji: String,
    energySurge: Float = 0f,
    onlyTies: Boolean = false,
    isFilterMode: Boolean = false,
    vibedPeers: Set<String> = emptySet(),
    lowPowerMode: Boolean = false,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceClick: (P2PDevice) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit = {},
    onBroadcastMessage: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticManager = remember { (context.applicationContext as BlukitApplication).hapticManager }
    
    val activeBubbles = remember { mutableStateListOf<BubbleData>() }
    val processedMessageIds = remember { mutableSetOf<String>() }
    var selectedStudentForMenu by remember { mutableStateOf<P2PDevice?>(null) }

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
        val vibes = remember(state.messages, onlyTies, vibedPeers, isFilterMode) {
            val filtered = if (onlyTies) {
                state.messages.filter { !it.receiverId.isNullOrBlank() }
            } else if (isFilterMode) {
                state.messages.filter { 
                    it.receiverId.isNullOrBlank() && (it.senderId in vibedPeers || it.senderId == localDeviceId)
                }
            } else {
                state.messages.filter { it.receiverId.isNullOrBlank() }
            }
            filtered.distinctBy { it.messageId }
        }

        val fieldBubbles = remember(activeBubbles, onlyTies, vibedPeers, isFilterMode) {
            val filtered = if (onlyTies) {
                activeBubbles.filter { it.isPrivate } 
            } else if (isFilterMode) {
                activeBubbles.filter { 
                    !it.isPrivate && (it.senderId in vibedPeers || it.senderId == localDeviceId)
                }
            } else {
                activeBubbles.filter { !it.isPrivate }
            }
            filtered.distinctBy { it.messageId }
        }

        // LAYER 1: Atmosphere (Background + Arcs + Ripples)
        Column(modifier = Modifier.fillMaxSize()) {
            val titleIcon = when {
                onlyTies -> Icons.Rounded.Flare
                isFilterMode -> Icons.Rounded.FilterCenterFocus
                else -> Icons.Rounded.Groups
            }
            val titleText = when {
                onlyTies -> "VIBES"
                isFilterMode -> "FOCUS"
                else -> "ALL"
            }
            
            BlukitTopTitle(title = titleText, icon = titleIcon)
            
            RipplesField(
                state = state,
                localDeviceId = localDeviceId,
                localEmoji = localEmoji,
                activeBubbles = activeBubbles,
                selectedDevices = state.selectedDevices,
                vibedPeers = vibedPeers,
                externalEnergy = energySurge,
                onlyTies = onlyTies,
                isFilterMode = isFilterMode,
                lowPowerMode = lowPowerMode,
                onDeviceClick = { selectedStudentForMenu = it },
                onDeviceLongClick = { selectedStudentForMenu = it },
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
                    localNickname = localNickname,
                    onlyTies = onlyTies,
                    vibedPeers = vibedPeers,
                    onDeviceClick = { selectedStudentForMenu = it },
                    onDeviceLongClick = { selectedStudentForMenu = it },
                    modifier = Modifier.fillMaxSize().zIndex(10f)
                )
            }
        }

        // LAYER 4: Student Context Menu
        if (selectedStudentForMenu != null) {
            StudentOptionsMenu(
                device = selectedStudentForMenu!!,
                isVibed = (selectedStudentForMenu!!.persistentId ?: selectedStudentForMenu!!.id) in vibedPeers,
                isTied = selectedStudentForMenu!!.id in state.connectedLinks,
                onFocus = {
                    onDeviceClick(selectedStudentForMenu!!)
                    selectedStudentForMenu = null
                },
                onVibe = {
                    onDeviceLongClick(selectedStudentForMenu!!)
                    selectedStudentForMenu = null
                },
                onDismiss = { selectedStudentForMenu = null }
            )
        }

        // LAYER 5: Empty State Hints
        if (isFilterMode && vibedPeers.isEmpty()) {
            EmptyFocusHint("FOCUS")
        } else if (onlyTies && state.connectedLinks.isEmpty()) {
            EmptyFocusHint("VIBES")
        }
    }
}

@Composable
private fun EmptyFocusHint(tab: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "NO $tab YET",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.2f),
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (tab == "FOCUS") "TAP SOULS IN BLUKIT FIELD TO FOCUS." else "ESTABLISH SECURE LINKS TO VIBE.",
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
    localNickname: String,
    onlyTies: Boolean,
    vibedPeers: Set<String>,
    onDeviceClick: (P2PDevice) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit,
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
            contentPadding = PaddingValues(top = 100.dp, bottom = 180.dp)
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
                    localNickname = localNickname,
                    isVibed = msg.senderId in vibedPeers,
                    isMutual = msg.senderId in state.connectedLinks,
                    onlyTies = onlyTies,
                    timestamp = timeFormatter.format(Date(msg.timestamp)),
                    onClick = { senderDevice?.let { onDeviceClick(it) } },
                    onLongClick = { senderDevice?.let { onDeviceLongClick(it) } }
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
    localNickname: String,
    isVibed: Boolean,
    isMutual: Boolean,
    onlyTies: Boolean,
    timestamp: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.Bottom, // Align with bubble bottom
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).animateContentSize()
    ) {
        if (!isMe) {
            // Attached Persona Node
            if (senderDevice != null) {
                VibeNode(
                    device = senderDevice,
                    isVibed = isMutual,
                    isSelected = false,
                    isPeerVibed = isVibed,
                    onlyTies = onlyTies,
                    onClick = onClick,
                    onLongClick = onLongClick,
                    modifier = Modifier.size(56.dp).padding(bottom = 4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                color = if (isMutual) StealthRose.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 2.dp),
                border = BorderStroke(0.5.dp, if(isMutual) StealthRose.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)),
                modifier = Modifier
                    .widthIn(max = 240.dp)
                    .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayName = if (!onlyTies && isMutual) "${msg.senderName}+" else msg.senderName
                        Text(
                            text = displayName.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isMutual) StealthRose else StealthPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = timestamp, 
                            fontSize = 7.sp, 
                            color = Color.White.copy(alpha = 0.2f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = msg.content,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.95f)
                    )
                }
            }
        } else {
            Surface(
                color = StealthPrimary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp, 12.dp, 2.dp, 12.dp),
                border = BorderStroke(0.5.dp, StealthPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.widthIn(max = 240.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "YOU ($localNickname)".uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = StealthPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = timestamp, 
                            fontSize = 7.sp, 
                            color = StealthPrimary.copy(alpha = 0.3f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = msg.content,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

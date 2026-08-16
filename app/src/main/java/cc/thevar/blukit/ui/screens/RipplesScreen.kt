package cc.thevar.blukit.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Warning
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
 * VIBES: CROWD ENERGY.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RipplesScreen(
    state: BluetoothUiState,
    localDeviceId: String,
    localEmoji: String,
    energySurge: Float = 0f,
    onlyTies: Boolean = false,
    lowPowerMode: Boolean = false,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceClick: (P2PDevice) -> Unit,
    onBroadcastMessage: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticManager = remember { (context.applicationContext as BlukitApplication).hapticManager }
    
    val permissions = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
    }

    val permissionState = rememberSpreadPermissionsState(permissions = permissions)

    // Chat Bubbles logic for Radar visualization
    val activeBubbles = remember { mutableStateListOf<BubbleData>() }
    val processedMessageIds = remember { mutableSetOf<String>() }

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
            activeBubbles.removeAll { now - it.timestamp > 5000 }
            delay(1000)
        }
    }

    DisposableEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            onStartScan()
        }
        onDispose { }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            // Space for global badge
            Spacer(modifier = Modifier.statusBarsPadding().height(64.dp))
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        val vibes = remember(state.messages, onlyTies) {
            if (onlyTies) {
                state.messages.filter { !it.receiverId.isNullOrBlank() }
            } else {
                state.messages.filter { it.receiverId.isNullOrBlank() }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val fieldBubbles = remember(activeBubbles, onlyTies) {
                if (onlyTies) {
                    activeBubbles.filter { it.isPrivate }
                } else {
                    activeBubbles.filter { !it.isPrivate }
                }
            }

            RipplesField(
                state = state,
                localDeviceId = localDeviceId,
                localEmoji = localEmoji,
                activeBubbles = fieldBubbles,
                externalEnergy = energySurge,
                onlyTies = onlyTies,
                lowPowerMode = lowPowerMode,
                onDeviceClick = onDeviceClick,
                onStartScan = onStartScan,
                onVibeSurge = { proximity ->
                    hapticManager.triggerProximityVibe(proximity)
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Full Screen Vibes Ticker
            VibingVibesTicker(
                state = state,
                vibes = vibes,
                localDeviceId = localDeviceId,
                onlyTies = onlyTies,
                modifier = Modifier.fillMaxSize()
            )

            state.uiError?.let { error ->
                Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp)) {
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Text(text = error.message)
                    }
                }
            }
        }
    }
}

@Composable
private fun VibingVibesTicker(
    state: BluetoothUiState,
    vibes: List<cc.thevar.blukit.domain.model.MessagePayload>,
    localDeviceId: String,
    onlyTies: Boolean,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    var focusedVibeId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(vibes.size) {
        if (vibes.isNotEmpty()) {
            listState.animateScrollToItem(vibes.size - 1)
        }
    }

    Box(
        modifier = modifier
    ) {
            if (vibes.isEmpty()) {
                // Empty state handled by Hub
            } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Bottom,
                contentPadding = PaddingValues(top = 120.dp, bottom = 120.dp)
            ) {
                items(vibes, key = { it.messageId }) { msg ->
                    val isMe = msg.senderId == localDeviceId
                    val isFocused = focusedVibeId == msg.senderId
                    
                    // Look up proximity factor (0.0 to 1.0)
                    val proximity = if (isMe) 1.0f else {
                        state.scannedDevices.find { it.id == msg.senderId }?.proximityFactor ?: 0.5f
                    }
                    
                    val alpha by animateFloatAsState(
                        if (focusedVibeId == null || isFocused || isMe) proximity.coerceAtLeast(0.3f) else 0.1f,
                        label = "VibeAlpha"
                    )

                    AnimatedVibeItem(
                        msg = msg,
                        isMe = isMe,
                        isMutual = msg.senderId in state.connectedLinks,
                        isFocused = isFocused,
                        onlyTies = onlyTies,
                        timestamp = timeFormatter.format(Date(msg.timestamp)),
                        alpha = alpha,
                        onClick = {
                            focusedVibeId = if (focusedVibeId == msg.senderId) null else msg.senderId
                        }
                    )
                }
            }
        }
        
    }
}

@Composable
private fun AnimatedVibeItem(
    msg: cc.thevar.blukit.domain.model.MessagePayload,
    isMe: Boolean,
    isMutual: Boolean,
    isFocused: Boolean,
    onlyTies: Boolean,
    timestamp: String,
    alpha: Float,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (isFocused) 1.05f else 1f, label = "Scale")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { 
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }
            .clickable { onClick() }
            .padding(vertical = 6.dp)
    ) {
        if (!isMe) {
            Text(text = msg.senderEmoji ?: "👤", fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp))
            Column {
                val displayName = if (!onlyTies && isMutual) {
                    "${msg.senderName.uppercase()} (MUTUAL)"
                } else {
                    msg.senderName.uppercase()
                }
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isFocused) StealthAmber else if (isMutual) StealthPrimary else Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = msg.content.uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isFocused) StealthAmber else Color.White.copy(alpha = 0.9f),
                    fontWeight = if (isFocused) FontWeight.Black else FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.2f),
                    fontSize = 7.sp
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${msg.senderName.uppercase()} (YOU)",
                    style = MaterialTheme.typography.labelSmall,
                    color = StealthPrimary.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = msg.content.uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    color = StealthPrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = StealthPrimary.copy(alpha = 0.3f),
                    fontSize = 7.sp
                )
            }
            Text(text = msg.senderEmoji ?: "👤", fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

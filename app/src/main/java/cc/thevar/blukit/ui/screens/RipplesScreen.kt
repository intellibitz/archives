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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.viewmodels.MeshConnectionState
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthAmber
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ripples Screen: Seeing the ripples of souls around you.
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RipplesScreen(
    state: BluetoothUiState,
    localDeviceId: String,
    localEmoji: String,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceClick: (P2PDevice) -> Unit,
    onStartServer: () -> Unit,
    onBroadcastMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
    var isInputVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    val permissions = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            // Pre-Android 12, location IS unfortunately technically required for Bluetooth scanning
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
    }

    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    // Chat Bubbles logic for Radar visualization
    val activeBubbles = remember { mutableStateListOf<BubbleData>() }
    val processedMessageIds = remember { mutableSetOf<String>() }

    LaunchedEffect(state.messages) {
        val newMessages = state.messages.filter { 
            it.receiverId.isNullOrBlank() && it.messageId !in processedMessageIds 
        }
        newMessages.forEach { msg ->
            activeBubbles.add(BubbleData(msg.senderId, msg.content, System.currentTimeMillis(), msg.messageId))
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
            // Uniform Global Header Actions only (Title moved to Badge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onStartServer,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = StealthPrimary.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(Icons.Rounded.Bolt, contentDescription = null, tint = StealthAmber)
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        val vibes = remember(state.messages) {
            state.messages.filter { it.receiverId.isNullOrBlank() }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            RipplesField(
                state = state,
                localDeviceId = localDeviceId,
                localEmoji = localEmoji,
                activeBubbles = activeBubbles,
                onDeviceClick = onDeviceClick,
                onStartScan = onStartScan,
                modifier = Modifier.fillMaxSize()
            )
            
            // Full Screen Vibes Ticker
            VibingVibesTicker(
                vibes = vibes,
                localDeviceId = localDeviceId,
                localEmoji = localEmoji,
                scannedDevices = state.scannedDevices,
                onSendVibeClick = { isInputVisible = true }, // Explicitly show input
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Input is visible if triggered OR if air is still (to let users start the vibe)
                val effectiveInputVisible = isInputVisible || vibes.isEmpty()
                
                AnimatedVisibility(
                    visible = effectiveInputVisible,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    BlukitInput(
                        value = message,
                        onValueChange = { message = it },
                        onSend = {
                            if (message.isNotBlank()) {
                                onBroadcastMessage(message)
                                message = ""
                                isInputVisible = false
                                focusManager.clearFocus()
                            }
                        },
                        placeholder = stringResource(R.string.shout_type_placeholder)
                    )
                }
            }

                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                        .fillMaxWidth()
                        .padding(top = 80.dp) // Offset below the global badge
                        .align(Alignment.TopStart)
                ) {
                    val isLocationMandatory = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                    val showWarning = !state.isBluetoothEnabled || 
                                    (isLocationMandatory && !state.isLocationEnabled) || 
                                    !permissionState.allPermissionsGranted
                    
                    if (showWarning) {
                        RadioStateWarning(
                            isBluetoothOff = !state.isBluetoothEnabled,
                            isLocationOff = isLocationMandatory && !state.isLocationEnabled,
                            isPermissionMissing = !permissionState.allPermissionsGranted,
                            onEnableBluetooth = {
                                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                            },
                            onEnableLocation = {
                                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            },
                            onRequestPermissions = { permissionState.launchMultiplePermissionRequest() }
                        )
                    }

                    state.errorMessage?.let { error ->
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Text(text = error)
                    }
                }
            }
        }
    }
}

@Composable
private fun VibingVibesTicker(
    vibes: List<cc.thevar.blukit.domain.model.MessagePayload>,
    localDeviceId: String,
    localEmoji: String,
    scannedDevices: List<cc.thevar.blukit.domain.model.P2PDevice>,
    onSendVibeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    // Vibe Nudge: Track peer count to trigger a visual nudge
    var lastPeerCount by remember { mutableIntStateOf(scannedDevices.size) }
    var vibeNudge by remember { mutableStateOf(false) }

    LaunchedEffect(scannedDevices.size) {
        if (scannedDevices.size > lastPeerCount) {
            vibeNudge = true
            delay(2000)
            vibeNudge = false
        }
        lastPeerCount = scannedDevices.size
    }

    LaunchedEffect(vibes.size) {
        if (vibes.isNotEmpty()) {
            listState.animateScrollToItem(vibes.size - 1)
        }
    }

    val glowAlpha by animateFloatAsState(
        targetValue = if (vibeNudge) 0.8f else 0.2f,
        animationSpec = tween(1000),
        label = "TickerGlow"
    )

    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, StealthAmber.copy(alpha = glowAlpha * 0.5f), Color.Transparent)
                ),
                shape = androidx.compose.ui.graphics.RectangleShape
            )
    ) {
        if (vibes.isEmpty()) {
            // Full screen stillness
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "THE AIR IS STILL...",
                        style = MaterialTheme.typography.labelSmall,
                        color = StealthAmber.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "FEEL THE VIBES AROUND YOU",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Bottom, // Bubble from bottom
                contentPadding = PaddingValues(top = 120.dp, bottom = 100.dp) // Leave room for badge and input
            ) {
                items(vibes, key = { it.messageId }) { msg ->
                    val isMe = msg.senderId == localDeviceId
                    val peer = if (isMe) null else scannedDevices.find { it.id == msg.senderId }
                    val displayEmoji = when {
                        isMe -> localEmoji
                        peer != null -> peer.emoji
                        else -> msg.senderEmoji ?: "🎭"
                    }
                    val timestamp = remember(msg.timestamp) { timeFormatter.format(Date(msg.timestamp)) }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        if (!isMe) {
                            Text(
                                text = displayEmoji,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            
                            Text(
                                text = timestamp,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 9.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )

                            Text(
                                text = msg.content.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (peer != null && peer.isConnected) StealthAmber else Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        } else {
                            // My Vibe: Right Aligned, reversed order for flow
                            Text(
                                text = msg.content.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = StealthPrimary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            
                            Text(
                                text = timestamp,
                                style = MaterialTheme.typography.labelSmall,
                                color = StealthPrimary.copy(alpha = 0.4f),
                                fontSize = 9.sp,
                                modifier = Modifier.padding(start = 12.dp)
                            )

                            Text(
                                text = displayEmoji,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                
                if (vibeNudge) {
                    item {
                        Text(
                            text = "✨ A NEW VIBE JOINED THE AIR",
                            style = MaterialTheme.typography.labelSmall,
                            color = StealthAmber,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(vertical = 8.dp),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
        
        // Floating Nudge button fixed at bottom right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .padding(bottom = 16.dp)
        ) {
            IconButton(
                onClick = onSendVibeClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(StealthAmber, CircleShape)
            ) {
                Icon(
                    Icons.Rounded.Forum, 
                    contentDescription = null, 
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun RadioStateWarning(
    isBluetoothOff: Boolean,
    isLocationOff: Boolean,
    isPermissionMissing: Boolean,
    onEnableBluetooth: () -> Unit,
    onEnableLocation: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    Surface(
        color = Color.Black.copy(alpha = 0.95f),
        contentColor = Color.White,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, StealthAmber.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Warning, contentDescription = null, tint = StealthAmber, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "THE AIR IS STILL",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = when {
                    isPermissionMissing -> "Blukit needs permission to feel the vibes around you."
                    isBluetoothOff && isLocationOff -> "Bluetooth and Location must be awake to join the Vibing Air."
                    isBluetoothOff -> "Your signal is quiet. Awaken Bluetooth to vibe with the crowd."
                    isLocationOff -> "Location must be awake to feel nearby ripples on this device."
                    else -> "Vibe with the crowd to join the Vibing Air."
                },
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f),
                lineHeight = 16.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPermissionMissing) {
                    Button(
                        onClick = onRequestPermissions,
                        colors = ButtonDefaults.buttonColors(containerColor = StealthAmber, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("ALLOW ACCESS", fontWeight = FontWeight.Black, fontSize = 10.sp)
                    }
                } else {
                    if (isBluetoothOff) {
                        TextButton(onClick = onEnableBluetooth) {
                            Text("AWAKEN BT", fontWeight = FontWeight.Black, color = StealthAmber, fontSize = 10.sp)
                        }
                    }
                    if (isLocationOff) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = onEnableLocation) {
                            Text("AWAKEN GPS", fontWeight = FontWeight.Black, color = StealthAmber, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestContent(
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.permission_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.permission_desc),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = StealthPrimary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRequestPermissions,
            colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(stringResource(R.string.permission_grant).uppercase(), fontWeight = FontWeight.Black)
        }
    }
}

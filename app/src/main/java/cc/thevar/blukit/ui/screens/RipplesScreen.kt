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
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.viewmodels.AirConnectionState
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthRose
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
    energySurge: Float = 0f,
    onlyTies: Boolean = false,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceClick: (P2PDevice) -> Unit,
    onBroadcastMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
    var showSmartFlowPrompt by remember { mutableStateOf(false) }

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
            // Space for global badge
            Spacer(modifier = Modifier.statusBarsPadding().height(64.dp))
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
                externalEnergy = energySurge,
                onlyTies = onlyTies,
                onDeviceClick = onDeviceClick,
                onStartScan = onStartScan,
                modifier = Modifier.fillMaxSize()
            )
            
            // Full Screen Vibes Ticker
            val effectiveInputVisible = isInputVisible || vibes.isEmpty()
            VibingVibesTicker(
                vibes = vibes,
                localDeviceId = localDeviceId,
                isInputVisible = effectiveInputVisible,
                onSendVibeClick = { isInputVisible = true },
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                                if (state.isDiscovering || state.isAdvertising) {
                                    onBroadcastMessage(message)
                                    message = ""
                                    isInputVisible = false
                                    focusManager.clearFocus()
                                } else {
                                    showSmartFlowPrompt = true
                                }
                            }
                        },
                        placeholder = stringResource(R.string.shout_type_placeholder)
                    )
                }
            }

            // Error Snackbar
            state.errorMessage?.let { error ->
                Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp)) {
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

    if (showSmartFlowPrompt) {
        AlertDialog(
            onDismissRequest = { showSmartFlowPrompt = false },
            title = { Text(stringResource(R.string.smart_flow_title)) },
            text = { Text(stringResource(R.string.smart_flow_desc)) },
            confirmButton = {
                Button(
                    onClick = {
                        showSmartFlowPrompt = false
                        if (permissionState.allPermissionsGranted) {
                            onStartScan()
                        } else {
                            permissionState.launchMultiplePermissionRequest()
                        }
                    }
                ) {
                    Text(stringResource(R.string.smart_flow_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSmartFlowPrompt = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun VibingVibesTicker(
    vibes: List<cc.thevar.blukit.domain.model.MessagePayload>,
    localDeviceId: String,
    isInputVisible: Boolean,
    onSendVibeClick: () -> Unit,
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "THE VIBES ARE QUIET",
                        style = MaterialTheme.typography.labelSmall,
                        color = StealthAmber.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "WAITING FOR THE VIBES",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f),
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
                verticalArrangement = Arrangement.Bottom,
                contentPadding = PaddingValues(top = 120.dp, bottom = 120.dp)
            ) {
                items(vibes, key = { it.messageId }) { msg ->
                    val isMe = msg.senderId == localDeviceId
                    val isFocused = focusedVibeId == msg.senderId
                    
                    val alpha by animateFloatAsState(
                        if (focusedVibeId == null || isFocused || isMe) 1f else 0.15f,
                        label = "VibeAlpha"
                    )

                    AnimatedVibeItem(
                        msg = msg,
                        isMe = isMe,
                        isFocused = isFocused,
                        timestamp = timeFormatter.format(Date(msg.timestamp)),
                        alpha = alpha,
                        onClick = {
                            focusedVibeId = if (focusedVibeId == msg.senderId) null else msg.senderId
                        }
                    )
                }
            }
        }
        
        if (!isInputVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .padding(bottom = 32.dp)
            ) {
                IconButton(
                    onClick = onSendVibeClick,
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Brush.linearGradient(listOf(StealthAmber, StealthRose)),
                            CircleShape
                        )
                        .shadow(12.dp, CircleShape)
                ) {
                    Icon(
                        Icons.Rounded.Forum, 
                        contentDescription = null, 
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
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
    isFocused: Boolean,
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
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                tint = if (isFocused) StealthAmber else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp).padding(end = 12.dp)
            )
            
            Column {
                Text(
                    text = msg.content.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
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
                    text = msg.content.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
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
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                tint = StealthPrimary,
                modifier = Modifier.size(16.dp).padding(start = 12.dp)
            )
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

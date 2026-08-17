package cc.thevar.blukit.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.R
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.viewmodels.AirConnectionState
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthRose
import androidx.compose.foundation.BorderStroke
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ties: Secure private vibes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TieScreen(
    state: BluetoothUiState,
    localDeviceId: String,
    localEmoji: String,
    vibeId: String?,
    vibeName: String?,
    vibeEmoji: String?,
    onDisconnect: () -> Unit,
    onNavigateBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onBlockUser: (String) -> Unit,
    onEnterPip: () -> Unit,
) {
    var vibeText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    var userToBlock by remember { mutableStateOf<MessagePayload?>(null) }

    val chatVibes = remember(state.messages, vibeId, localDeviceId) {
        if (vibeId == null) {
            emptyList()
        } else {
            state.messages.filter { 
                (it.senderId == localDeviceId && it.receiverId == vibeId) ||
                (it.senderId == vibeId && it.receiverId == localDeviceId)
            }
        }
    }

    LaunchedEffect(chatVibes.size) {
        if (chatVibes.isNotEmpty()) {
            listState.animateScrollToItem(chatVibes.size - 1)
        }
    }

    if (userToBlock != null) {
        AlertDialog(
            onDismissRequest = { userToBlock = null },
            title = { Text(stringResource(R.string.mod_block_title)) },
            text = { Text(stringResource(R.string.mod_block_desc, userToBlock?.senderName ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        userToBlock?.let { onBlockUser(it.senderId) }
                        userToBlock = null
                    }
                ) {
                    Text(stringResource(R.string.btn_block))
                }
            },
            dismissButton = {
                TextButton(onClick = { userToBlock = null }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            // Space for global badge + actions in top right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                IconButton(onClick = onEnterPip) {
                    Icon(Icons.Rounded.Info, contentDescription = "PiP", tint = Color.White)
                }
                IconButton(onClick = onDisconnect) {
                    Icon(Icons.Rounded.Close, contentDescription = "Disconnect", tint = Color.White)
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chatVibes, key = { it.messageId }) { payload ->
                    ChatMessage(
                        payload = payload,
                        isFromLocalUser = payload.senderId == localDeviceId,
                        localEmoji = localEmoji,
                        onLongClick = { if (payload.senderId != localDeviceId) userToBlock = payload }
                    )
                }
            }
            
            BlukitInput(
                value = vibeText,
                onValueChange = { vibeText = it },
                onSend = {
                    if (vibeText.isNotBlank()) {
                        onSendMessage(vibeText)
                        vibeText = ""
                        focusManager.clearFocus()
                    }
                },
                placeholder = "SPREAD VIBES..."
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessage(
    payload: MessagePayload,
    isFromLocalUser: Boolean,
    localEmoji: String,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeString = remember(payload.timestamp) { timeFormatter.format(Date(payload.timestamp)) }

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = if (isFromLocalUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isFromLocalUser) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .padding(bottom = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow Halo
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                listOf(StealthAmber.copy(alpha = 0.2f), Color.Transparent)
                            ),
                            CircleShape
                        )
                )
                
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF0A0C14),
                    modifier = Modifier.size(28.dp),
                    border = BorderStroke(1.dp, Brush.linearGradient(listOf(StealthAmber, StealthRose)))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = null,
                            tint = StealthAmber,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isFromLocalUser) Alignment.End else Alignment.Start) {
            if (!isFromLocalUser) {
                Text(
                    text = payload.senderName.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    fontWeight = FontWeight.Black,
                    color = StealthPrimary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
            
            Surface(
                color = if (isFromLocalUser) StealthPrimary else Color(0xFF151921).copy(alpha = 0.8f),
                contentColor = if (isFromLocalUser) Color.Black else Color.White,
                shape = MaterialTheme.shapes.medium.copy(
                    bottomEnd = if (isFromLocalUser) CornerSize(2.dp) else CornerSize(12.dp),
                    bottomStart = if (isFromLocalUser) CornerSize(12.dp) else CornerSize(2.dp)
                ),
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .combinedClickable(onClick = {}, onLongClick = onLongClick)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(text = payload.content, style = MaterialTheme.typography.bodyMedium)
                    Row(modifier = Modifier.align(Alignment.End).padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.labelSmall,
                            color = (if (isFromLocalUser) Color.Black else Color.White).copy(alpha = 0.5f)
                        )
                        if (isFromLocalUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (payload.status) {
                                    MessagePayload.STATUS_SENT -> "✓"
                                    MessagePayload.STATUS_DELIVERED -> "✓✓"
                                    else -> "⏳"
                                },
                                fontSize = 9.sp,
                                color = Color.Black.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

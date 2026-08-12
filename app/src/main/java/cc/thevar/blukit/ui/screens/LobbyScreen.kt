package cc.thevar.blukit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.PictureInPicture
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.R
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    state: BluetoothUiState,
    localDeviceId: String,
    onAutoConnectPeer: (String) -> Unit,
    onBroadcastMessage: (String) -> Unit,
    onBlockUser: (String) -> Unit,
    onEnterPip: () -> Unit
) {
    // Auto-connect to the first discovered peer who isn't connected yet (Power 5 - whisper)
    var triedPeers by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    LaunchedEffect(state.isConnected, state.scannedDevices) {
        if (state.isConnected) {
            triedPeers = emptySet()
        } else if (state.scannedDevices.isNotEmpty()) {
            val pendingPeer = state.scannedDevices.firstOrNull { 
                !state.connectedPeers.contains(it.id) && !triedPeers.contains(it.id) 
            }
            pendingPeer?.let {
                triedPeers = triedPeers + it.id
                onAutoConnectPeer(it.id)
            }
        }
    }
    var message by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    var userToBlock by remember { mutableStateOf<MessagePayload?>(null) }

    val lobbyMessages = remember(state.messages) {
        state.messages.filter { it.receiverId.isNullOrBlank() }
    }

    LaunchedEffect(lobbyMessages.size) {
        if (lobbyMessages.isNotEmpty()) {
            listState.animateScrollToItem(lobbyMessages.size - 1)
        }
    }

    if (userToBlock != null) {
        AlertDialog(
            onDismissRequest = { userToBlock = null },
            title = { Text(stringResource(R.string.mod_report_block_title)) },
            text = { Text(stringResource(R.string.mod_report_block_desc, userToBlock?.senderName ?: "")) },
            confirmButton = {
                TextButton(onClick = {
                    userToBlock?.let { onBlockUser(it.senderId) }
                    userToBlock = null
                }) {
                    Text(stringResource(R.string.mod_report_block_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { userToBlock = null }) {
                    Text(stringResource(R.string.mod_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(stringResource(R.string.chat_stadium_lobby), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.chat_broadcast_desc, state.scannedDevices.size), 
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEnterPip) {
                        Icon(Icons.Rounded.PictureInPicture, contentDescription = "Enter PiP")
                    }
                }
            )
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
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (lobbyMessages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "🏙️", fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.square_welcome_title),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.square_welcome_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "✨ Autonomous Chat ✨",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Shout something! Your message will automatically find its way to others, even after you leave.",
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = stringResource(R.string.square_empty_hint),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    items(lobbyMessages, key = { it.messageId }) { payload ->
                        ChatMessage(
                            payload = payload,
                            isFromLocalUser = payload.senderId == localDeviceId,
                            onLongClick = { 
                                if (payload.senderId != localDeviceId) {
                                    userToBlock = payload
                                }
                            }
                        )
                    }
                }
            }
            
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.chat_type_placeholder)) },
                        maxLines = 6,
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (message.isNotBlank()) {
                                onBroadcastMessage(message)
                                message = ""
                                focusManager.clearFocus()
                            }
                        },
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = stringResource(R.string.chat_send))
                    }
                }
            }
        }
    }
}

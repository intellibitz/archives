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
import androidx.compose.ui.unit.dp
import cc.thevar.blukit.R
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    state: BluetoothUiState,
    localDeviceId: String,
    onBroadcastMessage: (String) -> Unit,
    onBlockUser: (String) -> Unit,
    onEnterPip: () -> Unit
) {
    var message by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    var userToBlock by remember { mutableStateOf<MessagePayload?>(null) }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
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
                        // Power 4: Mesh is the context.
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
                    // Lobby is intentional, no disconnect needed here as it's the landing hub
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
                // Filter messages to show only those in the lobby (receiverId == null)
                val lobbyMessages = state.messages.filter { it.receiverId == null }
                items(lobbyMessages) { payload ->
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
                                // Power 2: User can chat even without peers.
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

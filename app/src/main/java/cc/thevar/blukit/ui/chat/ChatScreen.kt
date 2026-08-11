package cc.thevar.blukit.ui.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PictureInPicture
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cc.thevar.blukit.R
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.ui.discovery.BluetoothUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    state: BluetoothUiState,
    localDeviceId: String,
    onDisconnect: () -> Unit,
    onSendMessage: (String) -> Unit,
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
                        Text(stringResource(R.string.chat_title), style = MaterialTheme.typography.titleMedium)
                        if (state.isConnected) {
                            Text(
                                stringResource(R.string.chat_connected_desc), 
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onEnterPip) {
                        Icon(Icons.Rounded.PictureInPicture, contentDescription = "Enter PiP")
                    }
                    IconButton(onClick = onDisconnect) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.chat_disconnect))
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing // Handle all safe areas including IME
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
                items(state.messages) { payload ->
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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.chat_type_placeholder)) },
                        maxLines = 4,
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
                                onSendMessage(message)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessage(
    payload: MessagePayload,
    isFromLocalUser: Boolean,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeString = remember(payload.timestamp) { timeFormatter.format(Date(payload.timestamp)) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromLocalUser) Alignment.End else Alignment.Start
    ) {
        val arrangement = if (isFromLocalUser) Arrangement.End else Arrangement.Start
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = arrangement,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isFromLocalUser) {
                Text(
                    text = payload.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
        }
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isFromLocalUser) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = MaterialTheme.shapes.large.copy(
                bottomEnd = if (isFromLocalUser) CornerSize(0.dp) else CornerSize(16.dp),
                bottomStart = if (isFromLocalUser) CornerSize(16.dp) else CornerSize(0.dp)
            ),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = payload.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isFromLocalUser) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelSmall,
                    color = (if (isFromLocalUser) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                )
            }
        }
    }
}

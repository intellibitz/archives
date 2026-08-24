package cc.thevar.blukit.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.VibeGroup
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConversationsScreen(
    state: BluetoothUiState,
    onVibeClick: (VibeGroup) -> Unit,
    onDeleteGroup: (String) -> Unit,
    onAcceptLink: (P2PDevice) -> Unit,
    onDenyLink: (P2PDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    var groupToDelete by remember { mutableStateOf<VibeGroup?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.crowd.incomingLinkRequests.isNotEmpty()) {
                item {
                    Text(
                        text = "INCOMING VIBE REQUESTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = StealthPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(state.crowd.incomingLinkRequests.toList(), key = { it.id }) { request ->
                    VibeRequestItem(request, onAcceptLink, onDenyLink)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            if (state.crowd.outgoingLinkRequests.isNotEmpty()) {
                item {
                    Text(
                        text = "OUTGOING VIBE REQUESTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = StealthRose,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(state.crowd.outgoingLinkRequests.toList(), key = { it.id }) { request ->
                    OutgoingVibeRequestItem(request, onDenyLink)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            val conversations = state.session.groups
            if (conversations.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "NO PRIVATE VIBES YET", 
                            color = Color.White.copy(alpha = 0.2f), 
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
            items(conversations, key = { it.id }) { group ->
                VibeTickerTitleEntry(
                    group = group,
                    state = state,
                    onClick = { onVibeClick(group) },
                    onLongClick = { groupToDelete = it }
                )
            }
        }
    }

    if (groupToDelete != null) {
        AlertDialog(
            onDismissRequest = { groupToDelete = null },
            containerColor = Color.Black,
            titleContentColor = StealthRose,
            textContentColor = Color.White,
            title = { Text("DELETE AIR?", fontWeight = FontWeight.Black) },
            text = { Text("THIS WILL PERMANENTLY REMOVE THIS CONVERSATION.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteGroup(groupToDelete!!.id)
                    groupToDelete = null
                }) {
                    Text("DELETE", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { groupToDelete = null }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
private fun OutgoingVibeRequestItem(
    device: P2PDevice,
    onCancel: (P2PDevice) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(StealthRose.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .border(1.dp, StealthRose.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = device.emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = (device.name ?: "?").uppercase(), fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)
                Text(text = "Connecting...", fontSize = 8.sp, color = Color.White.copy(alpha = 0.4f))
            }
            TextButton(onClick = { onCancel(device) }) {
                Text("CANCEL", color = StealthRose, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun VibeRequestItem(
    device: P2PDevice,
    onAccept: (P2PDevice) -> Unit,
    onDeny: (P2PDevice) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(StealthPrimary.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .border(1.dp, StealthPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = device.emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = (device.name ?: "?").uppercase(), fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)
                Text(text = "Wants to vibe with you", fontSize = 8.sp, color = Color.White.copy(alpha = 0.4f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onDeny(device) }) {
                    Text("DENY", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
                Button(
                    onClick = { onAccept(device) },
                    colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text("JOIN", fontWeight = FontWeight.Black, fontSize = 10.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VibeTickerTitleEntry(
    group: VibeGroup,
    state: BluetoothUiState,
    onClick: () -> Unit,
    onLongClick: (VibeGroup) -> Unit
) {
    val lastMessage = remember(group.id, state.session.messages) {
        state.session.messages.filter { it.groupId == group.id }.lastOrNull()
    }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    val members = remember(group.memberIds, state.crowd.scannedDevices) {
        state.crowd.scannedDevices.filter { it.id in group.memberIds || it.persistentId in group.memberIds }
    }

    val themeColor = when(group.scope) {
        VibeGroup.SCOPE_PRIVATE -> StealthRose
        VibeGroup.SCOPE_LOCAL -> Color.White.copy(alpha = 0.4f)
        else -> StealthPrimary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onLongClick(group) }
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Participant Emojis Ticker
            Row(horizontalArrangement = Arrangement.spacedBy((-4).dp)) {
                if (members.isEmpty()) {
                    Text(text = if(group.scope == VibeGroup.SCOPE_LOCAL) "📱" else "👤", fontSize = 16.sp, modifier = Modifier.alpha(0.5f))
                } else {
                    members.take(3).forEach { member ->
                        Text(text = member.emoji, fontSize = 16.sp)
                    }
                    if (members.size > 3) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+${members.size - 3}", fontSize = 6.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val scopeLabel = when(group.scope) {
                        VibeGroup.SCOPE_PUBLIC -> "SHOUT"
                        VibeGroup.SCOPE_PRIVATE -> "WHISPER"
                        else -> "SILENCE"
                    }
                    Text(
                        text = scopeLabel,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        color = themeColor.copy(alpha = 0.8f),
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .border(0.5.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = group.name.uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = lastMessage?.content ?: "Connecting...",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (lastMessage != null) {
                Text(
                    text = timeFormatter.format(Date(lastMessage.timestamp)),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

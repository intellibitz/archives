package cc.thevar.blukit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.R
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.VibeGroup
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.viewmodels.AirConnectionState
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthRose
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material.icons.rounded.AutoAwesome
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ties: Secure private vibes.
 */
@Composable
fun TieScreen(
    state: BluetoothUiState,
    localDeviceId: String,
    localEmoji: String,
    groupId: String?,
    onDisconnect: () -> Unit,
    onSendMessage: (String, String) -> Unit,
    onStartSideVibe: (String) -> Unit = {},
    onToggleFocus: (P2PDevice) -> Unit = {},
    onBlockUser: (String) -> Unit,
    onAddMember: (String, String) -> Unit = { _, _ -> },
    onRemoveMember: (String, String) -> Unit = { _, _ -> },
    showMemberManagement: Boolean = false,
    onDismissManagement: () -> Unit = {},
    onEnterPip: () -> Unit,
    externalFocusedId: String? = null,
    onFocusChange: (String?) -> Unit = {},
) {
    var vibeText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    val group = remember(groupId, state.session.groups) {
        state.session.groups.find { it.id == groupId }
    }

    var userToBlock by remember { mutableStateOf<MessagePayload?>(null) }

    val vibesData = remember(state.session.messages, groupId, localDeviceId, externalFocusedId) {
        if (groupId == null) {
            Triple(emptyList<MessagePayload>(), emptyMap<String, Int>(), false)
        } else {
            val baseVibes = state.session.messages.filter { it.groupId == groupId }.distinctBy { it.messageId }
            val counts = baseVibes.groupBy { it.senderId }.mapValues { it.value.size }
            
            val filtered = if (externalFocusedId != null) {
                baseVibes.filter { it.senderId == externalFocusedId }
            } else {
                baseVibes.groupBy { it.senderId }
                    .map { entry -> entry.value.maxBy { it.timestamp } }
            }
            val sorted = filtered.sortedBy { it.timestamp }
            Triple(sorted, counts, externalFocusedId != null)
        }
    }

    val (chatVibes, vibeCounts, isDetailView) = vibesData

    LaunchedEffect(chatVibes.size) {
        if (chatVibes.isNotEmpty()) {
            listState.animateScrollToItem(chatVibes.size - 1)
        }
    }

    if (userToBlock != null) {
        AlertDialog(
            onDismissRequest = { userToBlock = null },
            containerColor = Color.Black,
            titleContentColor = StealthRose,
            textContentColor = Color.White,
            title = { Text("BLOCK USER?", fontWeight = FontWeight.Black) },
            text = { Text("YOU WILL NO LONGER RECEIVE VIBES FROM ${userToBlock?.senderName?.uppercase()}.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userToBlock?.let { onBlockUser(it.senderId) }
                        userToBlock = null
                    }
                ) {
                    Text("BLOCK", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToBlock = null }) {
                    Text("CANCEL")
                }
            }
        )
    }

    if (showMemberManagement && group != null) {
        AlertDialog(
            onDismissRequest = onDismissManagement,
            containerColor = Color.Black,
            titleContentColor = StealthPrimary,
            textContentColor = Color.White,
            title = { Text("MANAGE TIE", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("MEMBERS", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    group.memberIds.forEach { memberId ->
                        val member = state.crowd.scannedDevices.find { it.id == memberId || it.persistentId == memberId }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(text = member?.emoji ?: "👤", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = (member?.name ?: if(memberId == localDeviceId) "YOU" else "UNKNOWN").uppercase(), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            if (memberId != localDeviceId) {
                                IconButton(onClick = { onRemoveMember(group.id, memberId) }) {
                                    Icon(Icons.Rounded.RemoveCircleOutline, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Text("ADD NEARBY", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    val nearbyNotMembers = state.crowd.scannedDevices.filter { it.id !in group.memberIds && it.persistentId !in group.memberIds }
                    if (nearbyNotMembers.isEmpty()) {
                        Text("No one else nearby", fontSize = 10.sp, color = Color.White.copy(alpha = 0.2f))
                    }
                    nearbyNotMembers.forEach { device ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(text = device.emoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = (device.name ?: "?").uppercase(), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            IconButton(onClick = { onAddMember(group.id, device.id) }) {
                                Icon(Icons.Rounded.AddCircleOutline, contentDescription = "Add", tint = StealthPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissManagement) {
                    Text("DONE", color = StealthPrimary, fontWeight = FontWeight.Black)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Contextual tips
        if (chatVibes.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "TIP: TAP THE PEOPLE ICON TO MANAGE THIS TIE.",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = StealthPrimary.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
            }
        }

        // Contextual Persona Cloud: Users in this Tie
        if (group != null) {
            val groupMembers = remember(group.memberIds, state.crowd.scannedDevices) {
                state.crowd.scannedDevices.filter { it.id in group.memberIds || it.persistentId in group.memberIds }
            }
            
            UnifiedPersonaCloud(
                devices = groupMembers,
                vibedPeers = state.crowd.vibedPeers,
                connectedLinks = state.session.connectedLinks,
                activeBubbles = state.session.messages.map { msg ->
                    BubbleData(
                        msg.senderId,
                        msg.content,
                        msg.timestamp,
                        msg.messageId,
                        !msg.receiverId.isNullOrBlank()
                    )
                },
                onDeviceClick = onToggleFocus
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatVibes, key = { if (!isDetailView) it.senderId else it.messageId }) { payload ->
                ChatMessage(
                    payload = payload,
                    isFromLocalUser = payload.senderId == localDeviceId,
                    localEmoji = localEmoji,
                    vibeCount = vibeCounts[payload.senderId] ?: 1,
                    isGrouped = !isDetailView,
                    onClick = {
                        if (externalFocusedId == null) {
                            onFocusChange(payload.senderId)
                        } else {
                            onFocusChange(null)
                        }
                    },
                    onLongClick = { if (payload.senderId != localDeviceId) userToBlock = payload }
                )
            }
        }
        
        BlukitInput(
            airIsStill = !state.harmony.isBluetoothEnabled || !state.harmony.permissionsGranted,
            value = vibeText,
            onValueChange = { vibeText = it },
            onSend = {
                if (vibeText.isNotBlank() && groupId != null) {
                    onSendMessage(vibeText, groupId)
                    vibeText = ""
                    focusManager.clearFocus()
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessage(
    payload: MessagePayload,
    isFromLocalUser: Boolean,
    localEmoji: String,
    vibeCount: Int = 1,
    isGrouped: Boolean = false,
    onClick: () -> Unit = {},
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = payload.senderName.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        fontWeight = FontWeight.Black,
                        color = StealthPrimary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }
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
                    .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = payload.content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = if (isGrouped) 1 else Int.MAX_VALUE,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        if (isGrouped && vibeCount > 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        (if (isFromLocalUser) Color.Black else StealthPrimary).copy(alpha = 0.15f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        0.5.dp,
                                        (if (isFromLocalUser) Color.Black else StealthPrimary).copy(alpha = 0.3f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "+$vibeCount",
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isFromLocalUser) Color.Black else StealthPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.Rounded.UnfoldMore,
                                        contentDescription = null,
                                        tint = (if (isFromLocalUser) Color.Black else StealthPrimary).copy(alpha = 0.6f),
                                        modifier = Modifier.size(8.dp)
                                    )
                                }
                            }
                        } else if (!isGrouped && !isFromLocalUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier.size(8.dp)
                            )
                        }
                    }
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

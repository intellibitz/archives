package cc.thevar.blukit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import androidx.compose.ui.zIndex
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose

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
    onDeviceLongClick: (P2PDevice) -> Unit = {},
    onBlockUser: (String) -> Unit,
    onAddMember: (String, String) -> Unit = { _, _ -> },
    onRemoveMember: (String, String) -> Unit = { _, _ -> },
    showMemberManagement: Boolean = false,
    onShowManagement: () -> Unit = {},
    onDismissManagement: () -> Unit = {},
    onEnterPip: () -> Unit,
    onAttachFile: () -> Unit = {},
    onShowPrivacy: () -> Unit = {},
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
                    .map { entry -> entry.value.maxBy { msg -> msg.timestamp } }
            }
            val sorted = filtered.sortedBy { it.timestamp }
            Triple(sorted, counts, externalFocusedId != null)
        }
    }

    val (chatVibes, vibeCounts, isVibeFocused) = vibesData

    val energyList = remember(state.crowd.scannedDevices, chatVibes, localDeviceId) {
        val devices = state.crowd.scannedDevices
        val deviceMap = devices.associateBy { it.persistentId ?: it.id }
        
        chatVibes.map { msg: MessagePayload ->
            val device = if (msg.senderId == localDeviceId) {
                P2PDevice(id = localDeviceId, name = "YOU", emoji = localEmoji, medium = P2PDevice.ConnectionMedium.BLUETOOTH)
            } else {
                deviceMap[msg.senderId] ?: P2PDevice(id = msg.senderId, name = msg.senderName, emoji = msg.senderEmoji ?: "👤", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
            }
            device to msg
        }.sortedByDescending { it.second.timestamp }
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
        val memberSet = remember(group, localDeviceId) { 
            (group?.memberIds ?: emptySet()) - localDeviceId
        }
        RipplesField(
            state = state,
            localDeviceId = localDeviceId,
            localEmoji = localEmoji,
            activeBubbles = emptyList(), 
            selectedDevices = emptySet(),
            vibedPeers = if (externalFocusedId != null) setOf(externalFocusedId) else memberSet,
            onlyTies = true,
            isFilterMode = isVibeFocused || externalFocusedId != null,
            subjectId = externalFocusedId,
            onDeviceClick = { onToggleFocus(it) },
            onDeviceLongClick = { onDeviceLongClick(it) },
            onStartScan = { /* Already scanning in app level */ },
            drawBackground = true,
            drawNodes = true,
            modifier = Modifier.weight(1f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (chatVibes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "AWAITING RESONANCE IN THIS TIE...",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = StealthRose.copy(alpha = 0.4f),
                                letterSpacing = 1.sp
                            )
                            if (group?.memberIds?.size == 1) { // Only YOU
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onShowManagement,
                                    colors = ButtonDefaults.buttonColors(containerColor = StealthRose.copy(alpha = 0.1f)),
                                    border = BorderStroke(1.dp, StealthRose.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Rounded.PersonAdd, contentDescription = null, tint = StealthRose, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("EXPAND TIE", color = StealthRose, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }

                VibingVibesTicker(
                    state = state,
                    energyList = energyList,
                    vibeCounts = vibeCounts,
                    localDeviceId = localDeviceId,
                    vibedPeers = if (externalFocusedId != null) setOf(externalFocusedId) else emptySet(),
                    isGrouped = !isVibeFocused,
                    onVibeClick = { /* Handled via recurrence/focus */ },
                    onDeviceLongClick = onDeviceLongClick,
                    onToggleSelection = { /* Not applicable in Tie */ },
                    onDeleteVibe = { /* Add delete logic if needed */ },
                    onFocusChange = onFocusChange,
                    modifier = Modifier.fillMaxSize().zIndex(10f)
                )
            }
        }
        
        BlukitInput(
            airIsStill = !state.harmony.isBluetoothEnabled || !state.harmony.permissionsGranted,
            isPrivate = true,
            targetName = group?.name?.uppercase(),
            value = vibeText,
            onValueChange = { vibeText = it },
            onSend = {
                if (vibeText.isNotBlank() && groupId != null) {
                    onSendMessage(vibeText, groupId)
                    vibeText = ""
                    focusManager.clearFocus()
                }
            },
            onAttachFile = onAttachFile,
            onManage = onShowManagement
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PRIVACY",
                fontSize = 5.sp,
                fontWeight = FontWeight.Black,
                color = StealthRose,
                letterSpacing = 0.5.sp,
                modifier = Modifier.clickable { onShowPrivacy() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "BLUKIT:VIBES",
                fontSize = 5.sp,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.15f),
                letterSpacing = 1.sp
            )
        }
    }
}

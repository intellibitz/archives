/**
 * BLUKIT CHANNEL FIELD
 *
 * Private/Secure room view for families and study groups.
 */
package cc.thevar.blukit.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.MeshMessage
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MeshRoom
import cc.thevar.blukit.domain.model.RoomEvent
import cc.thevar.blukit.ui.components.AssignmentCreator
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.navigation.Route
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChannelField(
    state: cc.thevar.blukit.ui.viewmodels.BluetoothUiState,
    localDeviceId: String,
    roomId: String?,
    onRemoveMember: (String, String) -> Unit = { _, _ -> },
    onVaultGroup: (String, Boolean) -> Unit = { _, _ -> },
    onSeniorVaultGroup: (String, Boolean) -> Unit = { _, _ -> },
    onAssignRole: (String, String, String) -> Unit = { _, _, _ -> },
    onUpdateNote: (String, String, String?, Int) -> Unit = { _, _, _, _ -> },
    onPushRitual: (String, RoomEvent) -> Unit = { _, _ -> },
    showMemberManagement: Boolean = false,
    onShowManagement: () -> Unit = {},
    onDismissManagement: () -> Unit = {},
    // Hub Callbacks
    messageText: String = "",
    onMessageChange: (String) -> Unit = {},
    onSend: () -> Unit = {},
    isSearchActive: Boolean = false,
    onSearchToggle: (() -> Unit)? = null,
    onAcceptRadio: (P2PDevice) -> Unit = {},
    onDenyRadio: (P2PDevice) -> Unit = {},
    onStartSidePulse: () -> Unit = {},
    onSendMessage: (String, String?) -> Unit = { _, _ -> },
    onClearSelection: () -> Unit = {},
    onNavigateToGroup: (String) -> Unit = {},
    onNavigateToPulse: (String) -> Unit = {},
    onNavigateToLiveFeed: () -> Unit = {},
    onInputFocusChange: (Boolean) -> Unit = {},
    // Humanity Stage Props
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    userNickname: String = "",
    activeCrowds: List<MeshRoom> = emptyList(),
    onShowTimeline: () -> Unit = {},
    onResetProfile: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    isStealthMode: Boolean = false,
    lowPowerMode: Boolean = false,
    onToggleStealth: (Boolean) -> Unit = {},
    onToggleLowPower: (Boolean) -> Unit = {},
    header: @Composable () -> Unit,
) {
    var showTip by remember { mutableStateOf(value = true) }
    
    val room = remember(roomId, state.session.groups) {
        state.session.groups.find { it.id == roomId }
    }

    val childChannels = remember(state.session.groups, roomId) {
        state.session.groups.filter { (it.parentId == roomId) && (it.scope != MeshRoom.SCOPE_PUBLIC) }
    }

    val pulsesData = remember(state.session.messages, roomId, localDeviceId) {
        if (roomId == null) {
            Triple(emptyList(), emptyMap(), false)
        } else {
            val basePulses = state.session.messages.filter { it.groupId == roomId && it.parentMessageId == null }
            val counts = basePulses.groupBy { it.senderId }.mapValues { it.value.size }
            val sorted = basePulses.sortedBy { it.timestamp }
            Triple(sorted, counts, false)
        }
    }

    val (chatPulses, pulseCounts, _) = pulsesData
    val memberSet = remember(room, localDeviceId) { (room?.memberIds ?: emptySet()) - localDeviceId }
    val isPrivate = room?.scope == MeshRoom.SCOPE_PRIVATE
    val themeColor = if (isPrivate) StealthRose else StealthPrimary

    var showNoteEditor by remember { mutableStateOf(value = false) }
    var activeNote by remember { mutableStateOf<MeshMessage?>(null) }
    var showAssignmentCreator by remember { mutableStateOf(false) }

    BlukitFieldScaffold(
        themeColor = themeColor,
        glowIntensityTarget = 0.8f,
        header = header,
        entries = {
            Column(modifier = Modifier.fillMaxSize()) {
                IdentityStage(
                    title = room?.name ?: "CHANNEL",
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    activeRooms = activeCrowds,
                    onShowTimeline = onShowTimeline,
                    onResetProfile = onResetProfile,
                    onBack = onBack,
                    themeColor = themeColor,
                    userCount = room?.memberIds?.size ?: 0,
                    onModeChange = { onNavigateToLiveFeed() },
                    trailingContent = {
                        if (onSearchToggle != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = onSearchToggle,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSearchActive) Icons.Rounded.Search else Icons.Rounded.People,
                                        contentDescription = "Toggle Search",
                                        tint = if (isSearchActive) StealthAmber else themeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = if (isSearchActive) "SEARCH" else "PEOPLE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = (if (isSearchActive) StealthAmber else themeColor).copy(alpha = StealthAlphaHigh),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                )

                if (childChannels.isNotEmpty()) {
                    TickerSectionHeader(title = "PRIVATE ROOMS", color = themeColor)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(childChannels) { tie ->
                            RoomSummary(
                                title = tie.name,
                                subtitle = "SUB-GROUP",
                                icon = Icons.Rounded.Hearing,
                                themeColor = StealthRose,
                                count = tie.memberIds.size,
                                lastUpdate = "ACTIVE",
                                onClick = { onNavigateToGroup(tie.id) },
                                modifier = Modifier.width(280.dp)
                            )
                        }
                    }
                }

                LiveMessageTicker(
                    state = state,
                    energyList = chatPulses.map { msg -> 
                        val dev = P2PDevice(id = msg.senderId, name = msg.senderName, emoji = msg.senderEmoji ?: "👤", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
                        dev to msg 
                    },
                    pulseCounts = pulseCounts,
                    localDeviceId = localDeviceId,
                    localNickname = userNickname,
                    pulsedPeers = memberSet,
                    isGrouped = false,
                    onPulseClick = { onNavigateToPulse(it) },
                    onDeviceClick = { dev -> onNavigateToPulse(dev.id) },
                    onDeviceLongClick = { },
                    modifier = Modifier.weight(1f),
                    themeColor = themeColor
                )
            }

            MessageHub(
                currentRoute = Route.RoomField(roomId ?: ""),
                messageText = messageText,
                onMessageChange = onMessageChange,
                onSend = onSend,
                messageCount = state.session.messages.filter { it.groupId == roomId }.size,
                incomingRadioRequests = state.crowd.incomingRadioRequests,
                selectedDevices = state.crowd.selectedDevices,
                onAcceptRadio = onAcceptRadio,
                onDenyRadio = onDenyRadio,
                onStartSidePulse = onStartSidePulse,
                onStartChain = { }, 
                onClearSelection = onClearSelection,
                onAttachFile = { },
                isSearchMode = isSearchActive,
                onSearchToggle = onSearchToggle,
                onManage = onShowManagement,
                onNote = { showNoteEditor = true; activeNote = null },
                onTask = { showAssignmentCreator = true },
                onFocusChange = onInputFocusChange,
                isStealthMode = isStealthMode,
                lowPowerMode = lowPowerMode,
                onToggleStealth = onToggleStealth,
                onToggleLowPower = onToggleLowPower,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )

            AnimatedVisibility(
                visible = showTip && chatPulses.isEmpty() && childChannels.isEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                BlukitTip(
                    text = "THIS ROOM IS SILENT. START A CONVERSATION TO COLLABORATE.",
                    themeColor = themeColor,
                    onDismiss = { showTip = false }
                )
            }
        }
    )

    if (showNoteEditor && room != null) {
        NoteEditor(
            note = activeNote,
            onSave = { content ->
                onUpdateNote(room.id, content, activeNote?.messageId, (activeNote?.noteVersion ?: 0) + 1)
                showNoteEditor = false
                activeNote = null
            },
            onDismiss = {
                showNoteEditor = false
                activeNote = null
            }
        )
    }

    if (showAssignmentCreator && room != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StealthBlack.copy(alpha = 0.8f))
                .clickable { showAssignmentCreator = false },
            contentAlignment = Alignment.Center
        ) {
            AssignmentCreator(
                onAssignmentCreated = { content, _ ->
                    onSendMessage(content, room.id)
                },
                themeColor = themeColor,
                onDismiss = { showAssignmentCreator = false }
            )
        }
    }

    if (showMemberManagement && room != null) {
        AlertDialog(
            onDismissRequest = onDismissManagement,
            containerColor = StealthBlack,
            titleContentColor = StealthPrimary,
            textContentColor = Color.White,
            title = { Text("MANAGE ROOM", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("PEOPLE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    room.memberIds.forEach { memberId ->
                        val member = state.crowd.scannedDevices.find { it.id == memberId || it.persistentId == memberId }
                        val currentRole = room.userRoles[memberId]
                        
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(text = member?.emoji ?: "👤", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = member?.name ?: if (memberId == localDeviceId) "You" else "Unknown", 
                                    modifier = Modifier.weight(1f), 
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (memberId != localDeviceId) {
                                    IconButton(onClick = { onRemoveMember(room.id, memberId) }) {
                                        Icon(Icons.Rounded.RemoveCircleOutline, contentDescription = "Remove", tint = StealthError.copy(alpha = 0.6f))
                                    }
                                }
                            }
                            
                            val template = cc.thevar.blukit.domain.model.RoomTemplates.ALL.find { it.id == room.templateId }
                            if (template != null && template.roles.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(start = 24.dp)) {
                                    items(template.roles) { role ->
                                        val isAssigned = currentRole == role
                                        Surface(
                                            onClick = { onAssignRole(room.id, memberId, role) }, 
                                            color = if (isAssigned) StealthPrimary.copy(alpha = StealthAlphaLow) else Color.White.copy(alpha = 0.05f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, if (isAssigned) StealthPrimary else Color.White.copy(alpha = 0.1f))
                                        ) {
                                            Text(
                                                text = role, 
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), 
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isAssigned) StealthPrimary else Color.White.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("ROOM ARCHIVE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
                        Switch(
                            checked = room.isVaulted,
                            onCheckedChange = { onVaultGroup(room.id, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = StealthPrimary, checkedTrackColor = StealthPrimary.copy(alpha = 0.3f))
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SENIOR ARCHIVE", style = MaterialTheme.typography.labelSmall, color = StealthRose, fontWeight = FontWeight.Black)
                            Text("EXEMPT FROM ALL DECAY", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = room.isSeniorVault,
                            onCheckedChange = { onSeniorVaultGroup(room.id, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = StealthRose, checkedTrackColor = StealthRose.copy(alpha = 0.3f))
                        )
                    }
                    if (room.schedules.isNotEmpty()) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("SHARE EVENTS", style = MaterialTheme.typography.labelSmall, color = StealthPrimary.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
                            IconButton(onClick = { room.schedules.firstOrNull()?.let { onPushRitual(room.id, it) } }) {
                                Icon(Icons.Rounded.IosShare, contentDescription = "Push", tint = StealthPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = onDismissManagement) { Text("DONE", color = StealthPrimary, style = MaterialTheme.typography.labelLarge) } }
        )
    }
}

@Composable
fun NoteEditor(
    note: MeshMessage?,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(note?.content ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StealthBlack,
        title = { Text(if (note == null) "NEW NOTE" else "EDIT NOTE", style = MaterialTheme.typography.titleMedium, color = StealthRose) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().height(200.dp),
                textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 14.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StealthRose,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) {
                Text("SEND", color = StealthRose, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("DISCARD", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}

package cc.thevar.blukit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.VibeGroup
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import androidx.compose.ui.zIndex
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
private fun ScopeButton(
    label: String,
    scope: Int,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isSelected) color else Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.height(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(text = label, fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (isSelected) color else Color.White.copy(alpha = 0.4f))
        }
    }
}

/**
 * TIE FIELD: The deepest level of resonance. Focus on whispers and notes.
 */
@Composable
fun TieField(
    state: BluetoothUiState,
    localDeviceId: String,
    localNickname: String,
    localEmoji: String,
    groupId: String?,
    onDisconnect: () -> Unit,
    onSendMessage: (String, String) -> Unit,
    onStartWhisper: (String) -> Unit = {},
    onToggleFocus: (P2PDevice) -> Unit = {},
    onDeviceLongClick: (P2PDevice) -> Unit = {},
    onBlockUser: (String) -> Unit,
    onAddMember: (String, String) -> Unit = { _, _ -> },
    onRemoveMember: (String, String) -> Unit = { _, _ -> },
    onUpdateScope: (String, Int) -> Unit = { _, _ -> },
    onVaultGroup: (String, Boolean) -> Unit = { _, _ -> },
    onUpdateNote: (String, String, String?, Int) -> Unit = { _, _, _, _ -> },
    showMemberManagement: Boolean = false,
    onShowManagement: () -> Unit = {},
    onDismissManagement: () -> Unit = {},
    onEnterPip: () -> Unit,
    onAttachFile: () -> Unit = {},
    onShowPrivacy: () -> Unit = {},
    externalFocusedId: String? = null,
    onFocusChange: (String?) -> Unit = {},
    // Global State for Scaffold
    userCount: Int = 0,
    isStealthMode: Boolean = false,
    lowPowerMode: Boolean = false,
    isBluetoothOff: Boolean = false,
    isLocationOff: Boolean = false,
    isWifiOff: Boolean = false,
    isPermissionMissing: Boolean = false,
    isPermanentlyDenied: Boolean = false,
    onToggleStealth: (Boolean) -> Unit = {},
    onToggleLowPower: (Boolean) -> Unit = {},
    onAwakenBluetooth: () -> Unit = {},
    onAwakenLocation: () -> Unit = {},
    onAwakenWifi: () -> Unit = {},
    onGrantPermissions: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onClearHistory: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    onTitleClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {}
) {
    var vibeText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    
    var localFocusedId by remember(externalFocusedId) { mutableStateOf(externalFocusedId) }

    val group = remember(groupId, state.session.groups) {
        state.session.groups.find { it.id == groupId }
    }

    var userToBlock by remember { mutableStateOf<MessagePayload?>(null) }

    val vibesData = remember(state.session.messages, groupId, localDeviceId, localFocusedId) {
        if (groupId == null) {
            Triple(emptyList<MessagePayload>(), emptyMap<String, Int>(), false)
        } else {
            val baseVibes = state.session.messages.filter { it.groupId == groupId }.distinctBy { it.messageId }
            val counts = baseVibes.groupBy { it.senderId }.mapValues { it.value.size }
            val filtered = if (localFocusedId != null) {
                baseVibes.filter { it.senderId == localFocusedId }
            } else {
                baseVibes.groupBy { it.senderId }.map { entry -> entry.value.maxBy { msg -> msg.timestamp } }
            }
            val sorted = filtered.sortedBy { it.timestamp }
            Triple(sorted, counts, localFocusedId != null)
        }
    }

    val (chatVibes, vibeCounts, isVibeFocused) = vibesData

    val energyList = remember(state.crowd.scannedDevices, chatVibes, localDeviceId) {
        val devices = state.crowd.scannedDevices
        val deviceMap = devices.associateBy { it.persistentId ?: it.id }
        chatVibes.map { msg ->
            val device = if (msg.senderId == localDeviceId) {
                P2PDevice(id = localDeviceId, name = "YOU", emoji = localEmoji, medium = P2PDevice.ConnectionMedium.BLUETOOTH)
            } else {
                deviceMap[msg.senderId] ?: P2PDevice(id = msg.senderId, name = msg.senderName, emoji = msg.senderEmoji ?: "👤", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
            }
            device to msg
        }.sortedByDescending { it.second.timestamp }
    }

    if (userToBlock != null) {
        BlukitAlert(
            title = "BLOCK USER?",
            text = "YOU WILL NO LONGER RECEIVE VIBES FROM ${userToBlock?.senderName?.uppercase()}.",
            confirmLabel = "BLOCK",
            onConfirm = { userToBlock?.let { onBlockUser(it.senderId) }; userToBlock = null },
            onDismiss = { userToBlock = null }
        )
    }

    val memberSet = remember(group, localDeviceId) { (group?.memberIds ?: emptySet()) - localDeviceId }
    val isLocal = group?.scope == VibeGroup.SCOPE_LOCAL
    val isPrivate = group?.scope == VibeGroup.SCOPE_PRIVATE

    var showNoteEditor by remember { mutableStateOf(false) }
    var activeNote by remember { mutableStateOf<MessagePayload?>(null) }
    var vibeGhostData by remember { mutableStateOf<GhostVibeData?>(null) }
    val activeVibeId = LocalActiveVibeId.current

    var showTip by remember { mutableStateOf(true) }

    BlukitFieldScaffold(
        state = state,
        currentRoute = cc.thevar.blukit.ui.navigation.Route.VibeDetail(groupId ?: ""),
        title = group?.name ?: "TIE",
        icon = if (isPrivate) Icons.Rounded.Hearing else Icons.Rounded.CellTower,
        breadcrumbTrail = breadcrumbTrail,
        onCrumbClick = onCrumbClick,
        userNickname = localNickname,
        userEmoji = localEmoji,
        onUserNicknameChange = { },
        onResetProfile = { },
        userFocusRequester = null,
        isBluetoothOff = isBluetoothOff,
        isLocationOff = isLocationOff,
        isWifiOff = isWifiOff,
        isPermissionMissing = isPermissionMissing,
        isPermanentlyDenied = isPermanentlyDenied,
        userCount = userCount,
        isStealthMode = isStealthMode,
        lowPowerMode = lowPowerMode,
        airIsStill = false,
        activeAirs = state.session.groups,
        onToggleStealth = onToggleStealth,
        onToggleLowPower = onToggleLowPower,
        onAwakenBluetooth = onAwakenBluetooth,
        onAwakenLocation = onAwakenLocation,
        onAwakenWifi = onAwakenWifi,
        onGrantPermissions = onGrantPermissions,
        onOpenSettings = onOpenSettings,
        onClearHistory = onClearHistory,
        onShowPrivacy = onShowPrivacy,
        onBack = onBack,
        onTitleClick = onTitleClick,
        onProfileClick = onProfileClick,
        floatingContent = {
            if (showTip && chatVibes.isEmpty()) {
                BlukitTip(
                    text = "THIS IS A SECURE TIE. WHISPER FREELY OR CREATE A STUDY NOTE.",
                    onDismiss = { showTip = false }
                )
            }
        },
        fieldContent = {
            RipplesField(
                state = state,
                localDeviceId = localDeviceId,
                localNickname = localNickname,
                localEmoji = localEmoji,
                activeBubbles = emptyList(), 
                selectedDevices = emptySet(),
                vibedPeers = if (localFocusedId != null) setOf(localFocusedId!!) else memberSet,
                onlyTies = isPrivate || isLocal,
                isFilterMode = isVibeFocused || localFocusedId != null,
                subjectId = localFocusedId,
                vibeGhostData = vibeGhostData,
                onDismissGhost = { vibeGhostData = null; activeVibeId.value = null },
                onDeviceClick = { 
                    val id = it.persistentId ?: it.id
                    localFocusedId = if (localFocusedId == id) null else id
                    onFocusChange(localFocusedId)
                    onToggleFocus(it) 
                },
                onDeviceLongClick = { device -> 
                    val menuId = device.persistentId ?: device.id
                    activeVibeId.value = menuId
                    vibeGhostData = GhostVibeData(
                        emoji = device.emoji,
                        title = device.name ?: "USER",
                        subtitle = "AIR PERSONA",
                        themeColor = if(isPrivate) StealthRose else StealthPrimary,
                        sourceId = menuId,
                        actions = mutableListOf<GhostAction>().apply {
                            add(GhostAction(Icons.Rounded.Hearing, "WHISPER", StealthPrimary) { onStartWhisper(menuId) })
                            add(GhostAction(Icons.Rounded.Radar, "IDENTIFY", Color.White) { 
                                localFocusedId = menuId
                                onFocusChange(menuId)
                            })
                            if (menuId in state.crowd.blockedUsers) add(GhostAction(Icons.Rounded.LockOpen, "UNBLOCK", StealthPrimary) { })
                            else add(GhostAction(Icons.Rounded.Block, "BLOCK", Color.Red) { onBlockUser(menuId) })
                            add(GhostAction(Icons.Rounded.PersonRemove, "REMOVE", StealthRose) { onRemoveMember(groupId ?: "", menuId) })
                        }
                    )
                },
                onStartScan = { },
                drawBackground = true
            )

            // COLLABORATIVE AIR CANVAS (PINNED VIBES & NOTES)
            val notes = remember(state.session.messages, groupId) {
                state.session.messages.filter { it.groupId == groupId && it.type == MessagePayload.TYPE_NOTE_UPDATE }
                    .sortedByDescending { it.timestamp }
            }

            if (notes.isNotEmpty()) {
                Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = "STUDY NOTES", fontSize = 7.sp, fontWeight = FontWeight.Black, color = StealthRose.copy(alpha = 0.6f), letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(notes) { note ->
                                Surface(
                                    onClick = { activeNote = note; showNoteEditor = true },
                                    color = StealthRose.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.5.dp, StealthRose.copy(alpha = 0.3f)),
                                    modifier = Modifier.widthIn(max = 140.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(text = "V${note.noteVersion}", fontSize = 6.sp, fontWeight = FontWeight.Black, color = StealthRose)
                                        Text(text = note.content, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        tickerContent = {
            VibingVibesTicker(
                state = state,
                energyList = energyList,
                vibeCounts = vibeCounts,
                localDeviceId = localDeviceId,
                vibedPeers = memberSet,
                isGrouped = !isVibeFocused,
                onVibeClick = { messageId ->
                    if (!isVibeFocused) {
                        val msg = state.session.messages.find { it.messageId == messageId }
                        if (msg != null) {
                            localFocusedId = msg.senderId
                            onFocusChange(localFocusedId)
                        }
                    }
                },
                onDeviceLongClick = { },
                onToggleSelection = { },
                onDeleteVibe = { },
                onManageTie = { gid ->
                    val senderId = energyList.find { it.second.groupId == gid || it.first.id == gid }?.first?.id
                    if (senderId != null) {
                        localFocusedId = senderId
                        onFocusChange(senderId)
                    }
                },
                onFocusChange = { 
                    localFocusedId = it
                    onFocusChange(it)
                },
                modifier = Modifier.fillMaxSize()
            )
        },
        inputContent = {
            BlukitInput(
                airIsStill = false,
                isPrivate = isPrivate,
                targetName = group?.name,
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
                onNote = { showNoteEditor = true; activeNote = null },
                onManage = onShowManagement,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )

    if (showNoteEditor && group != null) {
        NoteEditor(
            note = activeNote,
            onSave = { content ->
                onUpdateNote(group.id, content, activeNote?.messageId, (activeNote?.noteVersion ?: 0) + 1)
                showNoteEditor = false
                activeNote = null
            },
            onDismiss = { showNoteEditor = false; activeNote = null }
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
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("ATMOSPHERIC VAULT", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.weight(1f))
                        Switch(
                            checked = group.isVaulted,
                            onCheckedChange = { onVaultGroup(group.id, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = StealthPrimary, checkedTrackColor = StealthPrimary.copy(alpha = 0.3f))
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = onDismissManagement) { Text("DONE", color = StealthPrimary, fontWeight = FontWeight.Black) } }
        )
    }
}

@Composable
fun NoteEditor(
    note: MessagePayload?,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(note?.content ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Black,
        title = { Text(if (note == null) "NEW NOTE" else "EDIT NOTE", fontWeight = FontWeight.Black, color = StealthRose) },
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
                Text("RESONATE", color = StealthRose, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("DISCARD", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}

package cc.thevar.blukit.ui.screens

import androidx.compose.animation.*
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
    onRemoveMember: (String, String) -> Unit = { _, _ -> },
    onVaultGroup: (String, Boolean) -> Unit = { _, _ -> },
    onSeniorVaultGroup: (String, Boolean) -> Unit = { _, _ -> },
    onUpdateNote: (String, String, String?, Int) -> Unit = { _, _, _, _ -> },
    onPushRitual: (String, cc.thevar.blukit.domain.model.AirSchedule) -> Unit = { _, _ -> },
    showMemberManagement: Boolean = false,
    onShowManagement: () -> Unit = {},
    onDismissManagement: () -> Unit = {},
    externalFocusedId: String? = null,
    // Hub Callbacks
    messageText: String = "",
    onMessageChange: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onSearchToggle: (() -> Unit)? = null,
    onAcceptLink: (P2PDevice) -> Unit = {},
    onDenyLink: (P2PDevice) -> Unit = {},
    onStartSideVibe: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onShowPrivacy: () -> Unit = {},
    onNavigateToGroup: (String) -> Unit = {},
    onNavigateToVibe: (String) -> Unit = {}
) {
    var showTip by remember { mutableStateOf(true) }
    var localFocusedId by remember(externalFocusedId) { mutableStateOf(externalFocusedId) }

    val group = remember(groupId, state.session.groups) {
        state.session.groups.find { it.id == groupId }
    }

    val childTies = remember(state.session.groups, groupId) {
        state.session.groups.filter { it.parentId == groupId && it.scope != VibeGroup.SCOPE_PUBLIC }
    }

    val vibesData = remember(state.session.messages, groupId, localDeviceId, localFocusedId) {
        if (groupId == null) {
            Triple(emptyList<MessagePayload>(), emptyMap<String, Int>(), false)
        } else {
            val baseVibes = state.session.messages.filter { it.groupId == groupId && it.parentMessageId == null }
            val counts = baseVibes.groupBy { it.senderId }.mapValues { it.value.size }
            val sorted = baseVibes.sortedBy { it.timestamp }
            Triple(sorted, counts, localFocusedId != null)
        }
    }

    val (chatVibes, vibeCounts, _) = vibesData
    val memberSet = remember(group, localDeviceId) { (group?.memberIds ?: emptySet()) - localDeviceId }
    val isPrivate = group?.scope == VibeGroup.SCOPE_PRIVATE
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    var showNoteEditor by remember { mutableStateOf(false) }
    var activeNote by remember { mutableStateOf<MessagePayload?>(null) }
    val activeVibeId = LocalActiveVibeId.current

    BlukitFieldScaffold(
        themeColor = if(isPrivate) StealthRose else StealthPrimary,
        glowIntensityTarget = 0.8f,
        floatingContent = {
            AnimatedVisibility(
                visible = showTip && chatVibes.isEmpty() && childTies.isEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                BlukitTip(
                    text = "THIS TIE IS SILENT. WHISPER OR PIN A VIBE TO THE CANVAS.",
                    themeColor = if(isPrivate) StealthRose else StealthPrimary,
                    onDismiss = { showTip = false }
                )
            }
        },
        fieldContent = {
            Column(modifier = Modifier.fillMaxSize().padding(top = 80.dp)) {
                // Meta Sections
                if (childTies.isNotEmpty()) {
                    Text(
                        text = "NESTED TIES", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = if(isPrivate) StealthRose else StealthPrimary, 
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    
                    LazyColumn(modifier = Modifier.weight(0.3f)) {
                        items(childTies) { tie ->
                            MetaVibeItem(
                                title = tie.name,
                                subtitle = "SECURE SUB-TIE",
                                icon = Icons.Rounded.Hearing,
                                themeColor = if(isPrivate) StealthRose else StealthPrimary,
                                count = tie.memberIds.size,
                                lastUpdate = sdf.format(Date(tie.lastVibeTimestamp)),
                                onClick = { onNavigateToGroup(tie.id) }
                            )
                        }
                    }
                }

                // Vibe Units / Metas
                Text(
                    text = "WHISPERS", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = if(isPrivate) StealthRose else StealthPrimary, 
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                LazyColumn(modifier = Modifier.weight(0.7f)) {
                    items(chatVibes) { vibe ->
                        if (vibe.isMeta) {
                            MetaVibeItem(
                                title = vibe.content.take(20),
                                subtitle = "VIBE META",
                                icon = Icons.Rounded.BubbleChart,
                                themeColor = if(isPrivate) StealthRose else StealthPrimary,
                                count = state.session.messages.count { it.parentMessageId == vibe.messageId },
                                lastUpdate = sdf.format(Date(vibe.timestamp)),
                                onClick = { onNavigateToVibe(vibe.messageId) }
                            )
                        } else {
                            AnimatedVibeItem(
                                msg = vibe,
                                isSelected = false,
                                senderDevice = null,
                                vibeCount = 0,
                                isVibed = false,
                                isMe = vibe.senderId == localDeviceId,
                                isGrouped = false,
                                isMutual = false,
                                vibeGroup = group,
                                rowId = vibe.messageId,
                                onVibeClick = { /* Handle Unit Click */ },
                                onDeviceLongClick = { },
                                onDelete = { }
                            )
                        }
                    }
                }
            }
            
            // Canvas for Background
            RipplesField(
                state = state,
                localDeviceId = localDeviceId,
                localNickname = localNickname,
                localEmoji = localEmoji,
                activeBubbles = emptyList(),
                vibedPeers = emptySet(),
                drawBackground = false,
                drawNodes = false,
                onDeviceClick = { },
                onStartScan = { }
            )
        },
        tickerContent = {
            VibingVibesTicker(
                state = state,
                energyList = chatVibes.map { msg -> 
                    val dev = P2PDevice(id = msg.senderId, name = msg.senderName, emoji = msg.senderEmoji ?: "👤", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
                    dev to msg 
                },
                vibeCounts = vibeCounts,
                localDeviceId = localDeviceId,
                vibedPeers = memberSet,
                isGrouped = false,
                onVibeClick = { onNavigateToVibe(it) },
                onDeviceLongClick = { },
                onDeleteVibe = { },
                modifier = Modifier.fillMaxSize()
            )
        },
        inputContent = {
            BlukitVibeHub(
                currentRoute = cc.thevar.blukit.ui.navigation.Route.GroupField(groupId ?: ""),
                messageText = messageText,
                onMessageChange = onMessageChange,
                onSend = onSend,
                vibeCount = state.session.messages.filter { it.groupId == groupId }.size,
                airIsStill = false,
                incomingLinkRequests = state.crowd.incomingLinkRequests,
                selectedDevices = state.crowd.selectedDevices,
                vibedPeers = memberSet,
                groups = state.session.groups,
                onAcceptLink = onAcceptLink,
                onDenyLink = onDenyLink,
                onStartSideVibe = onStartSideVibe,
                onStartTie = { }, // Inside a Tie, this is nested Tie creation if we wanted
                onClearSelection = onClearSelection,
                onAttachFile = { },
                onSearchToggle = onSearchToggle,
                onManage = onShowManagement,
                onNote = { showNoteEditor = true; activeNote = null },
                onShowPrivacy = onShowPrivacy,
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
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SENIOR VAULT", style = MaterialTheme.typography.labelSmall, color = StealthRose, fontWeight = FontWeight.Black)
                            Text("EXEMPT FROM ALL DECAY", fontSize = 7.sp, color = Color.White.copy(alpha = 0.3f))
                        }
                        Switch(
                            checked = group.isSeniorVault,
                            onCheckedChange = { onSeniorVaultGroup(group.id, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = StealthRose, checkedTrackColor = StealthRose.copy(alpha = 0.3f))
                        )
                    }
                    if (group.schedules.isNotEmpty()) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("SHARE RITUALS", style = MaterialTheme.typography.labelSmall, color = StealthPrimary.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
                            IconButton(onClick = { group.schedules.firstOrNull()?.let { onPushRitual(group.id, it) } }) {
                                Icon(Icons.Rounded.IosShare, contentDescription = "Push", tint = StealthPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
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

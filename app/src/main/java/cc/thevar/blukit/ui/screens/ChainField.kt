/**
 * BLUKIT CHAIN FIELD
 *
 * The private, secure interaction layer for encrypted peer groups.
 * Optimized for tactical whispers and persistent shared notes.
 */
package cc.thevar.blukit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BubbleChart
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.Resonance
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
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
 * CHAIN FIELD: The deepest level of resonance. Focus on whispers and notes.
 */
/**
 * CHAIN FIELD: The deepest layer of resonance.
 * 
 * Architectural Pattern:
 * - Header: Harmony Top Bar with Stealth Rose theme.
 * - Entries: Private Radar, Secure Ties, Whispers Ticker, Hub with Note and Management actions.
 */
@Composable
fun ChainField(
    state: BluetoothUiState,
    localDeviceId: String,
    groupId: String?,
    onDisconnect: () -> Unit,
    onSendMessage: (String, String) -> Unit,
    onRemoveMember: (String, String) -> Unit = { _, _ -> },
    onVaultGroup: (String, Boolean) -> Unit = { _, _ -> },
    onSeniorVaultGroup: (String, Boolean) -> Unit = { _, _ -> },
    onAssignRole: (String, String, String) -> Unit = { _, _, _ -> },
    onUpdateNote: (String, String, String?, Int) -> Unit = { _, _, _, _ -> },
    onPushRitual: (String, cc.thevar.blukit.domain.model.CrowdSchedule) -> Unit = { _, _ -> },
    showMemberManagement: Boolean = false,
    onShowManagement: () -> Unit = {},
    onDismissManagement: () -> Unit = {},
    externalFocusedId: String? = null,
    // Hub Callbacks
    messageText: String = "",
    onMessageChange: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onSearchToggle: (() -> Unit)? = null,
    onAcceptRadio: (P2PDevice) -> Unit = {},
    onDenyRadio: (P2PDevice) -> Unit = {},
    onStartSidePulse: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    crowdIsStill: Boolean = false,
    onShowPrivacy: () -> Unit = {},
    onNavigateToGroup: (String) -> Unit = {},
    onNavigateToPulse: (String) -> Unit = {},
    isInputFocused: Boolean = false,
    onInputFocusChange: (Boolean) -> Unit = {},
    // Humanity Stage Props
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    userNickname: String = "",
    userEmoji: String = "",
    onUserNicknameChange: (String) -> Unit = {},
    activeCrowds: List<Resonance> = emptyList(),
    onShowTimeline: () -> Unit = {},
    onResetProfile: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    header: @Composable () -> Unit
) {
    var showTip by remember { mutableStateOf(true) }
    var localFocusedId by remember(externalFocusedId) { mutableStateOf(externalFocusedId) }

    val group = remember(groupId, state.session.groups) {
        state.session.groups.find { it.id == groupId }
    }

    val childTies = remember(state.session.groups, groupId) {
        state.session.groups.filter { it.parentId == groupId && it.scope != Resonance.SCOPE_PUBLIC }
    }

    val pulsesData = remember(state.session.messages, groupId, localDeviceId, localFocusedId) {
        if (groupId == null) {
            Triple(emptyList<MessagePayload>(), emptyMap<String, Int>(), false)
        } else {
            val basePulses = state.session.messages.filter { it.groupId == groupId && it.parentMessageId == null }
            val counts = basePulses.groupBy { it.senderId }.mapValues { it.value.size }
            val sorted = basePulses.sortedBy { it.timestamp }
            Triple(sorted, counts, localFocusedId != null)
        }
    }

    val (chatPulses, pulseCounts, _) = pulsesData
    val memberSet = remember(group, localDeviceId) { (group?.memberIds ?: emptySet()) - localDeviceId }
    val isPrivate = group?.scope == Resonance.SCOPE_PRIVATE
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    var showNoteEditor by remember { mutableStateOf(false) }
    var activeNote by remember { mutableStateOf<MessagePayload?>(null) }
    val activePulseId = LocalActivePulseId.current

    BlukitFieldScaffold(
        themeColor = if(isPrivate) StealthRose else StealthPrimary,
        glowIntensityTarget = 0.8f,
        header = header,
        entries = {
            // MODULE 1: BASE CONTENT (Radar + Lists)
            Column(modifier = Modifier.fillMaxSize()) {
                val chainName = group?.name ?: "CHAIN"
                RipplesField(
                    state = state,
                    localDeviceId = localDeviceId,
                    activeBubbles = emptyList(),
                    pulsedPeers = emptySet(),
                    drawBackground = false,
                    drawNodes = false,
                    onDeviceClick = { },
                    // Humanity Stage
                    title = chainName,
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    userNickname = userNickname,
                    userEmoji = userEmoji,
                    activeCrowds = activeCrowds,
                    onShowTimeline = onShowTimeline,
                    onResetProfile = onResetProfile,
                    onTitleClick = onTitleClick,
                    onBack = onBack,
                    onNicknameChange = onUserNicknameChange,
                    isDimmed = isInputFocused || state.crowd.selectedDevices.isNotEmpty(),
                    themeColor = if(isPrivate) StealthRose else StealthPrimary,
                    modifier = Modifier.fillMaxWidth().height(320.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    // Ties Section (formerly Nested Ties)
                    if (childTies.isNotEmpty()) {
                        Text(
                            text = "TIES", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = if(isPrivate) StealthRose else StealthPrimary, 
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        
                        LazyColumn(modifier = Modifier.weight(0.3f)) {
                            items(childTies) { tie ->
                                ResonanceSummary(
                                    title = tie.name,
                                    subtitle = "SECURE SUB-CHAIN",
                                    icon = Icons.Rounded.Hearing,
                                    themeColor = if(isPrivate) StealthRose else StealthPrimary,
                                    count = tie.memberIds.size,
                                    lastUpdate = sdf.format(Date(tie.lastPulseTimestamp)),
                                    onClick = { onNavigateToGroup(tie.id) },
                                    showJoin = true
                                )
                            }
                        }
                    }

                    // Pulses Section
                    Text(
                        text = "WHISPERS", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = if(isPrivate) StealthRose else StealthPrimary, 
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    LazyColumn(modifier = Modifier.weight(0.7f)) {
                        items(chatPulses) { pulse ->
                            if (pulse.isMeta) {
                                ResonanceSummary(
                                    title = pulse.content.take(20),
                                    subtitle = "RESONANCE",
                                    icon = Icons.Rounded.BubbleChart,
                                    themeColor = if(isPrivate) StealthRose else StealthPrimary,
                                    count = state.session.messages.count { it.parentMessageId == pulse.messageId },
                                    lastUpdate = sdf.format(Date(pulse.timestamp)),
                                    onClick = { onNavigateToPulse(pulse.messageId) }
                                )
                            } else {
                                AnimatedPulseItem(
                                    msg = pulse,
                                    isSelected = false,
                                    senderDevice = null,
                                    pulseCount = 0,
                                    isPulsed = false,
                                    isMe = pulse.senderId == localDeviceId,
                                    isGrouped = false,
                                    isMutual = false,
                                    resonance = group,
                                    rowId = pulse.messageId,
                                    onPulseClick = { /* Handle Unit Click */ },
                                    onDeviceLongClick = { },
                                    onDelete = { }
                                )
                            }
                        }
                    }
                }
                
                // Bottom padding to avoid occlusion by the floating ticker and hub
                Spacer(modifier = Modifier.height(140.dp))
            }

            // MODULE 2: TICKER (Floating Overlay)
            PulsingResonanceTicker(
                state = state,
                energyList = chatPulses.map { msg -> 
                    val dev = P2PDevice(id = msg.senderId, name = msg.senderName, emoji = msg.senderEmoji ?: "👤", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
                    dev to msg 
                },
                pulseCounts = pulseCounts,
                localDeviceId = localDeviceId,
                pulsedPeers = memberSet,
                isGrouped = false,
                onPulseClick = { onNavigateToPulse(it) },
                onDeviceLongClick = { },
                onDeletePulse = { },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(bottom = 100.dp) // Room for Hub
            )

            // MODULE 3: PULSE HUB (Bottom Overlay)
            BlukitPulseHub(
                currentRoute = cc.thevar.blukit.ui.navigation.Route.GroupField(groupId ?: ""),
                messageText = messageText,
                onMessageChange = onMessageChange,
                onSend = onSend,
                pulseCount = state.session.messages.filter { it.groupId == groupId }.size,
                crowdIsStill = crowdIsStill,
                incomingRadioRequests = state.crowd.incomingRadioRequests,
                selectedDevices = state.crowd.selectedDevices,
                pulsedPeers = memberSet,
                resonances = state.session.groups,
                onAcceptRadio = onAcceptRadio,
                onDenyRadio = onDenyRadio,
                onStartSidePulse = onStartSidePulse,
                onStartChain = { }, 
                onClearSelection = onClearSelection,
                onAttachFile = { },
                onSearchToggle = onSearchToggle,
                onManage = onShowManagement,
                onNote = { showNoteEditor = true; activeNote = null },
                onShowPrivacy = onShowPrivacy,
                onFocusChange = onInputFocusChange,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )

            // MODULE 4: FLOATING TIPS
            AnimatedVisibility(
                visible = showTip && chatPulses.isEmpty() && childTies.isEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                BlukitTip(
                    text = "THIS CHAIN IS SILENT. WHISPER OR PIN A PULSE TO THE CANVAS.",
                    themeColor = if(isPrivate) StealthRose else StealthPrimary,
                    onDismiss = { showTip = false }
                )
            }
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
            title = { Text("MANAGE CHAIN", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("MEMBERS", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    group.memberIds.forEach { memberId ->
                        val member = state.crowd.scannedDevices.find { it.id == memberId || it.persistentId == memberId }
                        val currentRole = group.userRoles[memberId]
                        
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
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
                            
                            // Role Assignment
                            val template = cc.thevar.blukit.domain.model.CrowdTemplates.ALL.find { it.id == group.templateId }
                            if (template != null && template.roles.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(start = 24.dp)) {
                                    items(template.roles) { role ->
                                        val isAssigned = currentRole == role
                                        Surface(
                                            onClick = { onAssignRole(group.id, memberId, role) }, 
                                            color = if (isAssigned) StealthPrimary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(0.5.dp, if (isAssigned) StealthPrimary else Color.White.copy(alpha = 0.1f))
                                        ) {
                                            Text(text = role.uppercase(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 6.sp, fontWeight = FontWeight.Black, color = if (isAssigned) StealthPrimary else Color.White.copy(alpha = 0.4f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("CROWD VAULT", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.weight(1f))
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

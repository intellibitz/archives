/**
 * CHAIN FIELD: The deepest layer of resonance.
 * 
 * Architectural Pattern:
 * - Header: Harmony Top Bar with Stealth Rose theme.
 * - Entries: Private Radar, Secure Ties, Whispers Ticker, Hub with Note and Management actions.
 */
package cc.thevar.blukit.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.Resonance
import cc.thevar.blukit.ui.components.AssignmentCreator
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChainField(
    state: cc.thevar.blukit.ui.viewmodels.BluetoothUiState,
    localDeviceId: String,
    groupId: String?,
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
    isSearchActive: Boolean = false,
    onSearchToggle: (() -> Unit)? = null,
    onAcceptRadio: (P2PDevice) -> Unit = {},
    onDenyRadio: (P2PDevice) -> Unit = {},
    onStartSidePulse: () -> Unit = {},
    onSendMessage: (String, String?) -> Unit = { _, _ -> },
    onClearSelection: () -> Unit = {},
    onNavigateToGroup: (String) -> Unit = {},
    onNavigateToPulse: (String) -> Unit = {},
    onInputFocusChange: (Boolean) -> Unit = {},
    // Humanity Stage Props
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    userNickname: String = "",
    activeCrowds: List<Resonance> = emptyList(),
    onShowTimeline: () -> Unit = {},
    onResetProfile: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    header: @Composable () -> Unit,
) {
    var showTip by remember { mutableStateOf(value = true) }
    
    val group = remember(groupId, state.session.groups) {
        state.session.groups.find { it.id == groupId }
    }

    val childTies = remember(state.session.groups, groupId) {
        state.session.groups.filter { (it.parentId == groupId) && (it.scope != Resonance.SCOPE_PUBLIC) }
    }

    val pulsesData = remember(state.session.messages, groupId, localDeviceId) {
        if (groupId == null) {
            Triple(emptyList(), emptyMap(), false)
        } else {
            val basePulses = state.session.messages.filter { it.groupId == groupId && it.parentMessageId == null }
            val counts = basePulses.groupBy { it.senderId }.mapValues { it.value.size }
            val sorted = basePulses.sortedBy { it.timestamp }
            Triple(sorted, counts, false)
        }
    }

    val (chatPulses, pulseCounts, _) = pulsesData
    val memberSet = remember(group, localDeviceId) { (group?.memberIds ?: emptySet()) - localDeviceId }
    val isPrivate = group?.scope == Resonance.SCOPE_PRIVATE
    val themeColor = if (isPrivate) StealthRose else StealthPrimary

    var showNoteEditor by remember { mutableStateOf(value = false) }
    var activeNote by remember { mutableStateOf<MessagePayload?>(null) }
    var showAssignmentCreator by remember { mutableStateOf(false) }

    BlukitFieldScaffold(
        themeColor = themeColor,
        glowIntensityTarget = 0.8f,
        header = header,
        entries = {
            // MODULE 1: BASE CONTENT (Humanity Stage + Unified Ticker/Radar)
            Column(modifier = Modifier.fillMaxSize()) {
                // Humanity Stage (Breadcrumbs)
                BlukitHumanityStage(
                    title = group?.name ?: "CHAIN",
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    activeCrowds = activeCrowds,
                    onShowTimeline = onShowTimeline,
                    onResetProfile = onResetProfile,
                    onTitleClick = onTitleClick,
                    onBack = onBack,
                    themeColor = themeColor,
                    userCount = group?.memberIds?.size ?: 0,
                    isVaulted = group?.isVaulted == true,
                    isSeniorVault = group?.isSeniorVault == true,
                    trailingContent = {
                        // Tactical Radar Toggles
                        if (onSearchToggle != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = onSearchToggle,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSearchActive) Icons.Rounded.WifiTethering else Icons.Rounded.Radar,
                                        contentDescription = "Toggle Search",
                                        tint = if (isSearchActive) StealthAmber else themeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = if (isSearchActive) "SCAN" else "RADAR",
                                    fontSize = 5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = (if (isSearchActive) StealthAmber else themeColor).copy(alpha = 0.5f),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                )

                // CHILD TIES ROW
                if (childTies.isNotEmpty()) {
                    TickerSectionHeader(title = "SECURE TIES", color = themeColor)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(childTies) { tie ->
                            ResonanceSummary(
                                title = tie.name,
                                subtitle = "SUB-CHAIN",
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

                // MODULE 2: UNIFIED RESONANCE TICKER (With integrated Radar)
                PulsingResonanceTicker(
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

            // MODULE 3: PULSE HUB (Bottom Overlay)
            BlukitPulseHub(
                currentRoute = cc.thevar.blukit.ui.navigation.Route.GroupField(groupId ?: ""),
                messageText = messageText,
                onMessageChange = onMessageChange,
                onSend = onSend,
                pulseCount = state.session.messages.filter { it.groupId == groupId }.size,
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
                    text = "THIS CHAIN IS SILENCE. WHISPER OR PIN A PULSE TO COLLABORATE.",
                    themeColor = themeColor,
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
            onDismiss = {
                showNoteEditor = false
                activeNote = null
            }
        )
    }

    if (showAssignmentCreator && group != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable { showAssignmentCreator = false },
            contentAlignment = Alignment.Center
        ) {
            AssignmentCreator(
                onAssignmentCreated = { content, _ ->
                    onSendMessage(content, group.id)
                },
                themeColor = themeColor,
                onDismiss = { showAssignmentCreator = false }
            )
        }
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
                                Text(text = (member?.name ?: if (memberId == localDeviceId) "YOU" else "UNKNOWN").uppercase(), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
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

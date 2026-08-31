/**
 * BLUKIT PRIVATE GROUP FIELD
 *
 * Private/Secure Group view for families and focused groups.
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
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Group
import cc.thevar.blukit.domain.model.GroupEvent
import cc.thevar.blukit.ui.components.MessageRecordCreator
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.components.BlukitToolbar
import cc.thevar.blukit.ui.components.BlukitFieldScaffold
import cc.thevar.blukit.ui.components.MessageHub
import java.text.SimpleDateFormat
import java.util.*

import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey

@Composable
fun PrivateGroupField(
    state: cc.thevar.blukit.ui.viewmodels.ConnectionUiState,
    localDeviceId: String,
    groupId: String?,
    pagedMessages: LazyPagingItems<Message>,
    onRemoveMember: (String, String) -> Unit = { _, _ -> },
    onVaultGroup: (String, Boolean) -> Unit = { _, _ -> },
    onSeniorVaultGroup: (String, Boolean) -> Unit = { _, _ -> },
    onAssignRole: (String, String, String) -> Unit = { _, _, _ -> },
    onUpdateRecord: (String, String, String?, Int) -> Unit = { _, _, _, _ -> },
    onPushRitual: (String, GroupEvent) -> Unit = { _, _ -> },
    showMemberManagement: Boolean = false,
    onShowManagement: () -> Unit = {},
    onDismissManagement: () -> Unit = {},
    onSend: (String) -> Unit = {},
    isSearchActive: Boolean = false,
    onSearchToggle: (() -> Unit)? = null,
    onAcceptRadio: (Source) -> Unit = {},
    onDenyRadio: (Source) -> Unit = {},
    onStartWhisper: () -> Unit = {},
    onStartSubGroup: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onNavigateToGroup: (String) -> Unit = {},
    onNavigateToMessage: (String) -> Unit = {},
    onSourceLongClick: (Source) -> Unit = {},
    onNavigateToLiveFeed: () -> Unit = {},
    onInputFocusChange: (Boolean) -> Unit = {},
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    userNickname: String = "",
    activeGroups: List<Group> = emptyList(),
    onShowTimeline: () -> Unit = {},
    onResetProfile: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    trend: String? = null,
    header: @Composable () -> Unit,
) {
    var showTip by remember { mutableStateOf(value = true) }
    
    val group = remember(groupId, state.session.groups) {
        state.session.groups.find { it.id == groupId }
    }

    val childGroups = remember(state.session.groups, groupId) {
        state.session.groups.filter { (it.parentId == groupId) && (it.scope != Group.SCOPE_PUBLIC) }
    }

    val memberSet = remember(group, localDeviceId) { (group?.memberIds ?: emptySet()) - localDeviceId }
    val isPrivate = group?.scope == Group.SCOPE_PRIVATE
    val themeColor = if (isPrivate) StealthRose else StealthPrimary

    var showRecordEditor by remember { mutableStateOf(value = false) }
    var activeRecord by remember { mutableStateOf<Message?>(null) }
    var showRecordCreator by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }

    BlukitFieldScaffold(
        themeColor = themeColor,
        glowIntensityTarget = 0.8f,
        header = header,
        entries = {
            Column(modifier = Modifier.fillMaxSize()) {
                if (childGroups.isNotEmpty()) {
                    TickerSectionHeader(title = "PRIVATE GROUPS", color = themeColor)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(childGroups) { tie ->
                            GroupSummary(
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

                PagedConnectionTicker(
                    state = state,
                    pagedMessages = pagedMessages,
                    messageCounts = emptyMap(),
                    localDeviceId = localDeviceId,
                    localNickname = userNickname,
                    pulsedPeers = memberSet,
                    isGrouped = false,
                    reverseLayout = true,
                    onMessageClick = { onNavigateToMessage(it) },
                    onSourceClick = { dev -> onNavigateToMessage(dev.id) },
                    onSourceLongClick = onSourceLongClick,
                    modifier = Modifier.weight(1f),
                    themeColor = themeColor,
                    trend = trend
                )

                MessageHub(
                    currentRoute = Route.GroupField(groupId ?: ""),
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSend = { 
                        onSend(messageText)
                        messageText = ""
                    },
                    messageCount = pagedMessages.itemCount,
                    incomingRadioRequests = state.crowd.incomingRadioRequests,
                    selectedDevices = state.crowd.selectedDevices,
                    onAcceptRadio = onAcceptRadio,
                    onDenyRadio = onDenyRadio,
                    onStartWhisper = onStartWhisper,
                    onStartSubGroup = onStartSubGroup, 
                    onClearSelection = onClearSelection,
                    onAttachFile = { },
                    isSearchMode = isSearchActive,
                    onSearchToggle = onSearchToggle,
                    onManage = onShowManagement,
                    onNote = { showRecordEditor = true; activeRecord = null },
                    onTask = { showRecordCreator = true },
                    onFocusChange = onInputFocusChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = showTip && pagedMessages.itemCount == 0 && childGroups.isEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                BlukitTip(
                    text = "THIS GROUP IS SILENT. SEND A MESSAGE TO START.",
                    themeColor = themeColor,
                    onDismiss = { showTip = false }
                )
            }
        }
    )

    if (showRecordEditor && group != null) {
        RecordEditor(
            record = activeRecord,
            onSave = { content ->
                onUpdateRecord(group.id, content, activeRecord?.messageId, (activeRecord?.noteVersion ?: 0) + 1)
                showRecordEditor = false
                activeRecord = null
            },
            onDismiss = {
                showRecordEditor = false
                activeRecord = null
            }
        )
    }

    if (showRecordCreator && group != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StealthBlack.copy(alpha = 0.8f))
                .clickable { showRecordCreator = false },
            contentAlignment = Alignment.Center
        ) {
            MessageRecordCreator(
                onRecordCreated = { content, _ ->
                    onSend(content)
                },
                themeColor = themeColor,
                onDismiss = { showRecordCreator = false }
            )
        }
    }

    if (showMemberManagement && group != null) {
        AlertDialog(
            onDismissRequest = onDismissManagement,
            containerColor = StealthBlack,
            titleContentColor = StealthPrimary,
            textContentColor = Color.White,
            title = { Text("MANAGE GROUP", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("PEOPLE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    group.memberIds.forEach { memberId ->
                        val member = state.crowd.scannedDevices.find { it.id == memberId || it.persistentId == memberId }
                        val currentRole = group.userRoles[memberId]
                        
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
                                    IconButton(onClick = { onRemoveMember(group.id, memberId) }) {
                                        Icon(Icons.Rounded.RemoveCircleOutline, contentDescription = "Remove", tint = StealthError.copy(alpha = 0.6f))
                                    }
                                }
                            }
                            
                            val template = cc.thevar.blukit.domain.model.RoomTemplates.ALL.find { it.id == group.templateId }
                            if (template != null && template.roles.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(start = 24.dp)) {
                                    items(template.roles) { role ->
                                        val isAssigned = currentRole == role
                                        Surface(
                                            onClick = { onAssignRole(group.id, memberId, role) }, 
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
                        Text("GROUP ARCHIVE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
                        Switch(
                            checked = group.isVaulted,
                            onCheckedChange = { onVaultGroup(group.id, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = StealthPrimary, checkedTrackColor = StealthPrimary.copy(alpha = 0.3f))
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SENIOR ARCHIVE", style = MaterialTheme.typography.labelSmall, color = StealthRose, fontWeight = FontWeight.Black)
                            Text("EXEMPT FROM ALL DECAY", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
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
                            Text("SHARE EVENTS", style = MaterialTheme.typography.labelSmall, color = StealthPrimary.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
                            IconButton(onClick = { group.schedules.firstOrNull()?.let { onPushRitual(group.id, it) } }) {
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
fun RecordEditor(
    record: Message?,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(record?.content ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StealthBlack,
        title = { Text(if (record == null) "NEW RECORD" else "EDIT RECORD", style = MaterialTheme.typography.titleMedium, color = StealthRose) },
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

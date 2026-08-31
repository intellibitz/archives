/**
 * BLUKIT PRIVATE SPHERE FIELD
 *
 * Private/Secure Sphere view for families and focused groups.
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
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Sphere
import cc.thevar.blukit.domain.model.SphereEvent
import cc.thevar.blukit.ui.components.EchoRecordCreator
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.navigation.Route
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PrivateSphereField(
    state: cc.thevar.blukit.ui.viewmodels.BluetoothUiState,
    localDeviceId: String,
    sphereId: String?,
    onRemoveMember: (String, String) -> Unit = { _, _ -> },
    onVaultSphere: (String, Boolean) -> Unit = { _, _ -> },
    onSeniorVaultSphere: (String, Boolean) -> Unit = { _, _ -> },
    onAssignRole: (String, String, String) -> Unit = { _, _, _ -> },
    onUpdateRecord: (String, String, String?, Int) -> Unit = { _, _, _, _ -> },
    onPushRitual: (String, SphereEvent) -> Unit = { _, _ -> },
    showMemberManagement: Boolean = false,
    onShowManagement: () -> Unit = {},
    onDismissManagement: () -> Unit = {},
    onSend: (String) -> Unit = {},
    isSearchActive: Boolean = false,
    onSearchToggle: (() -> Unit)? = null,
    onAcceptRadio: (Source) -> Unit = {},
    onDenyRadio: (Source) -> Unit = {},
    onStartSidePulse: () -> Unit = {},
    onStartChain: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onNavigateToSphere: (String) -> Unit = {},
    onNavigateToPulse: (String) -> Unit = {},
    onSourceLongClick: (Source) -> Unit = {},
    onNavigateToLiveFeed: () -> Unit = {},
    onInputFocusChange: (Boolean) -> Unit = {},
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    userNickname: String = "",
    activeSpheres: List<Sphere> = emptyList(),
    onShowTimeline: () -> Unit = {},
    onResetProfile: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    isStealthMode: Boolean = false,
    lowPowerMode: Boolean = false,
    onToggleStealth: (Boolean) -> Unit = {},
    onToggleLowPower: (Boolean) -> Unit = {},
    trend: String? = null,
    header: @Composable () -> Unit,
) {
    var showTip by remember { mutableStateOf(value = true) }
    
    val sphere = remember(sphereId, state.session.groups) {
        state.session.groups.find { it.id == sphereId }
    }

    val childSpheres = remember(state.session.groups, sphereId) {
        state.session.groups.filter { (it.parentId == sphereId) && (it.scope != Sphere.SCOPE_PUBLIC) }
    }

    val echoesData = remember(state.session.messages, sphereId, localDeviceId) {
        if (sphereId == null) {
            Triple(emptyList(), emptyMap(), false)
        } else {
            val baseEchoes = state.session.messages.filter { it.groupId == sphereId && it.parentMessageId == null }
            val counts = baseEchoes.groupBy { it.senderId }.mapValues { it.value.size }
            val sorted = baseEchoes.sortedBy { it.timestamp }
            Triple(sorted, counts, false)
        }
    }

    val (chatEchoes, echoCounts, _) = echoesData
    val memberSet = remember(sphere, localDeviceId) { (sphere?.memberIds ?: emptySet()) - localDeviceId }
    val isPrivate = sphere?.scope == Sphere.SCOPE_PRIVATE
    val themeColor = if (isPrivate) StealthRose else StealthPrimary

    var showRecordEditor by remember { mutableStateOf(value = false) }
    var activeRecord by remember { mutableStateOf<Echo?>(null) }
    var showRecordCreator by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }

    BlukitFieldScaffold(
        themeColor = themeColor,
        glowIntensityTarget = 0.8f,
        header = header,
        entries = {
            Column(modifier = Modifier.fillMaxSize()) {
                IdentityStage(
                    title = sphere?.name ?: "SPHERE",
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    activeRooms = activeSpheres,
                    onShowTimeline = onShowTimeline,
                    onResetProfile = onResetProfile,
                    onBack = onBack,
                    themeColor = themeColor,
                    userCount = sphere?.memberIds?.size ?: 0,
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
                                    text = if (isSearchActive) "SEARCH" else "SOURCES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = (if (isSearchActive) StealthAmber else themeColor).copy(alpha = StealthAlphaHigh),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                )

                if (childSpheres.isNotEmpty()) {
                    TickerSectionHeader(title = "PRIVATE SPHERES", color = themeColor)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(childSpheres) { tie ->
                            SphereSummary(
                                title = tie.name,
                                subtitle = "SUB-SPHERE",
                                icon = Icons.Rounded.Hearing,
                                themeColor = StealthRose,
                                count = tie.memberIds.size,
                                lastUpdate = "ACTIVE",
                                onClick = { onNavigateToSphere(tie.id) },
                                modifier = Modifier.width(280.dp)
                            )
                        }
                    }
                }

                ResonanceTicker(
                    state = state,
                    resonanceList = chatEchoes.map { msg -> 
                        val source = Source(id = msg.senderId, name = msg.senderName, emoji = msg.senderEmoji ?: "👤", medium = Source.ResonanceMedium.BLUETOOTH)
                        source to msg 
                    },
                    echoCounts = echoCounts,
                    localDeviceId = localDeviceId,
                    localNickname = userNickname,
                    pulsedPeers = memberSet,
                    isGrouped = false,
                    reverseLayout = true,
                    onEchoClick = { onNavigateToPulse(it) },
                    onSourceClick = { dev -> onNavigateToPulse(dev.id) },
                    onSourceLongClick = onSourceLongClick,
                    modifier = Modifier.weight(1f),
                    themeColor = themeColor,
                    trend = trend
                )
            }

            EchoHub(
                currentRoute = Route.SphereField(sphereId ?: ""),
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSend = { 
                    onSend(messageText)
                    messageText = ""
                },
                messageCount = state.session.messages.filter { it.groupId == sphereId }.size,
                incomingRadioRequests = state.crowd.incomingRadioRequests,
                selectedDevices = state.crowd.selectedDevices,
                onAcceptRadio = onAcceptRadio,
                onDenyRadio = onDenyRadio,
                onStartSidePulse = onStartSidePulse,
                onStartChain = onStartChain, 
                onClearSelection = onClearSelection,
                onAttachFile = { },
                isSearchMode = isSearchActive,
                onSearchToggle = onSearchToggle,
                onManage = onShowManagement,
                onNote = { showRecordEditor = true; activeRecord = null },
                onTask = { showRecordCreator = true },
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
                visible = showTip && chatEchoes.isEmpty() && childSpheres.isEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                BlukitTip(
                    text = "THIS SPHERE IS SILENT. RESONATE TO START THE LEDGER.",
                    themeColor = themeColor,
                    onDismiss = { showTip = false }
                )
            }
        }
    )

    if (showRecordEditor && sphere != null) {
        RecordEditor(
            record = activeRecord,
            onSave = { content ->
                onUpdateRecord(sphere.id, content, activeRecord?.messageId, (activeRecord?.noteVersion ?: 0) + 1)
                showRecordEditor = false
                activeRecord = null
            },
            onDismiss = {
                showRecordEditor = false
                activeRecord = null
            }
        )
    }

    if (showRecordCreator && sphere != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StealthBlack.copy(alpha = 0.8f))
                .clickable { showRecordCreator = false },
            contentAlignment = Alignment.Center
        ) {
            EchoRecordCreator(
                onRecordCreated = { content, _ ->
                    onSend(content)
                },
                themeColor = themeColor,
                onDismiss = { showRecordCreator = false }
            )
        }
    }

    if (showMemberManagement && sphere != null) {
        AlertDialog(
            onDismissRequest = onDismissManagement,
            containerColor = StealthBlack,
            titleContentColor = StealthPrimary,
            textContentColor = Color.White,
            title = { Text("MANAGE SPHERE", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("SOURCES", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    sphere.memberIds.forEach { memberId ->
                        val member = state.crowd.scannedDevices.find { it.id == memberId || it.persistentId == memberId }
                        val currentRole = sphere.userRoles[memberId]
                        
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
                                    IconButton(onClick = { onRemoveMember(sphere.id, memberId) }) {
                                        Icon(Icons.Rounded.RemoveCircleOutline, contentDescription = "Remove", tint = StealthError.copy(alpha = 0.6f))
                                    }
                                }
                            }
                            
                            val template = cc.thevar.blukit.domain.model.RoomTemplates.ALL.find { it.id == sphere.templateId }
                            if (template != null && template.roles.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(start = 24.dp)) {
                                    items(template.roles) { role ->
                                        val isAssigned = currentRole == role
                                        Surface(
                                            onClick = { onAssignRole(sphere.id, memberId, role) }, 
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
                        Text("SPHERE ARCHIVE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
                        Switch(
                            checked = sphere.isVaulted,
                            onCheckedChange = { onVaultSphere(sphere.id, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = StealthPrimary, checkedTrackColor = StealthPrimary.copy(alpha = 0.3f))
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SENIOR ARCHIVE", style = MaterialTheme.typography.labelSmall, color = StealthRose, fontWeight = FontWeight.Black)
                            Text("EXEMPT FROM ALL DECAY", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = sphere.isSeniorVault,
                            onCheckedChange = { onSeniorVaultSphere(sphere.id, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = StealthRose, checkedTrackColor = StealthRose.copy(alpha = 0.3f))
                        )
                    }
                    if (sphere.schedules.isNotEmpty()) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("SHARE EVENTS", style = MaterialTheme.typography.labelSmall, color = StealthPrimary.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
                            IconButton(onClick = { sphere.schedules.firstOrNull()?.let { onPushRitual(sphere.id, it) } }) {
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
    record: Echo?,
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

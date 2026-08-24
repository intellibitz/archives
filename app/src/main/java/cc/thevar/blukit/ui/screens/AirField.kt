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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.VibeGroup
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AIR FIELD: Public frequency view. 
 * Lists Ties and Shouts within a specific Air container.
 * Features collaborative Air Canvas for pinned vibes and spectral tips.
 */
@Composable
fun AirField(
    state: BluetoothUiState,
    localDeviceId: String,
    localNickname: String,
    localEmoji: String,
    airId: String?,
    onDisconnect: () -> Unit,
    onSendMessage: (String, String) -> Unit,
    onEnterTie: (String) -> Unit,
    onToggleFocus: (P2PDevice) -> Unit = {},
    onDeviceLongClick: (P2PDevice) -> Unit = {},
    onBlockUser: (String) -> Unit,
    onAddMember: (String, String) -> Unit = { _, _ -> },
    onRemoveMember: (String, String) -> Unit = { _, _ -> },
    onUpdateScope: (String, Int) -> Unit = { _, _ -> },
    onVaultGroup: (String, Boolean) -> Unit = { _, _ -> },
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
    
    var localFocusedId by remember(externalFocusedId) { mutableStateOf(externalFocusedId) }

    val air = remember(airId, state.session.groups) {
        state.session.groups.find { it.id == airId }
    }

    val childTies = remember(state.session.groups, airId) {
        state.session.groups.filter { it.parentAirId == airId }
    }

    val vibesData = remember(state.session.messages, airId, localDeviceId, localFocusedId) {
        if (airId == null) {
            Triple(emptyList<MessagePayload>(), emptyMap<String, Int>(), false)
        } else {
            val baseVibes = state.session.messages.filter { it.groupId == airId }.distinctBy { it.messageId }
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

    val energyList = remember(state.crowd.scannedDevices, chatVibes, childTies, localDeviceId) {
        val devices = state.crowd.scannedDevices
        val deviceMap = devices.associateBy { it.persistentId ?: it.id }
        
        val vibePairs = chatVibes.map { msg ->
            val device = if (msg.senderId == localDeviceId) {
                P2PDevice(id = localDeviceId, name = "YOU", emoji = localEmoji, medium = P2PDevice.ConnectionMedium.BLUETOOTH)
            } else {
                deviceMap[msg.senderId] ?: P2PDevice(id = msg.senderId, name = msg.senderName, emoji = msg.senderEmoji ?: "👤", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
            }
            device to msg
        }

        val tiePairs = childTies.map { tie ->
            val tieDevice = P2PDevice(id = tie.id, name = tie.name, emoji = "🔒", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
            tieDevice to null as MessagePayload?
        }

        (vibePairs + tiePairs).sortedByDescending { it.second?.timestamp ?: 0L }
    }

    var vibeGhostData by remember { mutableStateOf<GhostVibeData?>(null) }
    val activeVibeId = LocalActiveVibeId.current

    var showTip by remember { mutableStateOf(true) }

    BlukitFieldScaffold(
        state = state,
        currentRoute = cc.thevar.blukit.ui.navigation.Route.VibeDetail(airId ?: ""),
        title = air?.name ?: "AIR",
        icon = Icons.Rounded.Grain,
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
                    text = "YOU ARE IN ${air?.name?.uppercase() ?: "AN AIR"}. SPREAD A VIBE HERE.",
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
                vibedPeers = if (localFocusedId != null) setOf(localFocusedId!!) else emptySet(),
                onlyTies = false,
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
                        themeColor = StealthPrimary,
                        sourceId = menuId,
                        actions = mutableListOf<GhostAction>().apply {
                            add(GhostAction(Icons.Rounded.Hearing, "WHISPER", StealthRose) { /* TODO */ })
                            add(GhostAction(Icons.Rounded.Radar, "IDENTIFY", Color.White) { 
                                localFocusedId = menuId
                                onFocusChange(menuId)
                            })
                        }
                    )
                },
                onStartScan = { },
                drawBackground = true
            )

            // AIR CANVAS (PINNED VIBES)
            val pinnedVibes = remember(air?.pinnedVibeIds, state.session.messages) {
                state.session.messages.filter { it.messageId in (air?.pinnedVibeIds ?: emptySet()) }
            }

            if (pinnedVibes.isNotEmpty()) {
                Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = "AIR CANVAS", fontSize = 7.sp, fontWeight = FontWeight.Black, color = StealthPrimary.copy(alpha = 0.6f), letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(pinnedVibes) { vibe ->
                                Surface(
                                    color = StealthPrimary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.5.dp, StealthPrimary.copy(alpha = 0.3f)),
                                    modifier = Modifier.widthIn(max = 140.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(text = vibe.senderName.uppercase(), fontSize = 6.sp, fontWeight = FontWeight.Black, color = StealthPrimary)
                                        Text(text = vibe.content.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
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
                vibedPeers = emptySet(),
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
                    val isTie = state.session.groups.find { it.id == gid }?.scope == VibeGroup.SCOPE_PRIVATE
                    if (isTie) onEnterTie(gid) else {
                        localFocusedId = gid
                        onFocusChange(gid)
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
                isPrivate = false,
                targetName = air?.name,
                value = vibeText,
                onValueChange = { vibeText = it },
                onSend = {
                    if (vibeText.isNotBlank() && airId != null) {
                        onSendMessage(vibeText, airId)
                        vibeText = ""
                        focusManager.clearFocus()
                    }
                },
                onAttachFile = onAttachFile,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

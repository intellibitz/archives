package cc.thevar.blukit.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.painterResource
import cc.thevar.blukit.R
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.Resonance
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date

data class PersonaConnectionPoints(
    val uph: Offset? = null,
    val field: Offset? = null,
    val ticker: Offset? = null,
    val pulse: Offset? = null
)

val LocalPersonaCoordinates = staticCompositionLocalOf { mutableStateMapOf<String, PersonaConnectionPoints>() }
val LocalActivePulseId = staticCompositionLocalOf { mutableStateOf<String?>(null) }

@Composable
fun MixedStatusBranding(
    isBluetoothOff: Boolean,
    isWifiOff: Boolean,
    isLocationOff: Boolean,
    isPermissionMissing: Boolean,
    isLocationMandatory: Boolean = false,
    onAwakenBluetooth: () -> Unit,
    onAwakenWifi: () -> Unit,
    onAwakenLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        StatusIcon(icon = Icons.Rounded.Bluetooth, isOn = !isBluetoothOff, isWeak = false, isPermissionMissing = isPermissionMissing, onClick = onAwakenBluetooth)
        StatusIcon(icon = Icons.Rounded.Wifi, isOn = !isWifiOff, isWeak = false, isPermissionMissing = false, onClick = onAwakenWifi)
        if (isLocationMandatory) {
            StatusIcon(icon = Icons.Rounded.LocationOn, isOn = !isLocationOff, isWeak = false, isPermissionMissing = isPermissionMissing, onClick = onAwakenLocation)
        }
    }
}

@Composable
fun CrowdTicker(title: String, resonances: List<Resonance> = emptyList(), modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "CrowdTicker")
    val alpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 0.7f, animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse), label = "Alpha")
    
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title.uppercase(), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 3.sp, color = Color.White))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(4.dp).background(StealthPrimary.copy(alpha = alpha), CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = if (resonances.isEmpty()) "UNIVERSAL FREQUENCY" else "${resonances.size} ACTIVE LINKS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, fontWeight = FontWeight.Black, color = StealthPrimary.copy(alpha = 0.6f), letterSpacing = 1.sp))
            }
        }
    }
}

@Composable
fun BreadcrumbHub(
    trail: List<String>,
    onCrumbClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.Center
    ) {
        trail.forEachIndexed { index, crumb ->
            Text(
                text = crumb.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (index == trail.size - 1) FontWeight.Black else FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (index == trail.size - 1) Color.White else Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp
                ),
                modifier = Modifier.clickable { onCrumbClick(index) }
            )
            if (index < trail.size - 1) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight, 
                    contentDescription = null, 
                    tint = Color.White.copy(alpha = 0.2f), 
                    modifier = Modifier.size(12.dp).padding(horizontal = 2.dp)
                )
            }
        }
    }
}

@Composable
fun BlukitHarmonyTopBar(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    currentRoute: Route,
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    onNavigate: (Route) -> Unit,
    userNickname: String,
    userEmoji: String,
    onUserNicknameChange: (String) -> Unit,
    onResetProfile: () -> Unit,
    userFocusRequester: FocusRequester?,
    isBluetoothOff: Boolean,
    isLocationOff: Boolean,
    isWifiOff: Boolean,
    isPermissionMissing: Boolean,
    isPermanentlyDenied: Boolean,
    userCount: Int,
    isStealthMode: Boolean,
    lowPowerMode: Boolean,
    crowdIsStill: Boolean,
    isLocationMandatory: Boolean = false,
    activeCrowds: List<Resonance> = emptyList(),
    onToggleStealth: (Boolean) -> Unit,
    onToggleLowPower: (Boolean) -> Unit,
    onAwakenBluetooth: () -> Unit,
    onAwakenLocation: () -> Unit,
    onAwakenWifi: () -> Unit,
    onGrantPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onClearHistory: () -> Unit,
    onShowPrivacy: () -> Unit = {},
    onShowTimeline: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    onTitleClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val pulseAlpha by rememberInfiniteTransition(label = "AlertPulse").animateFloat(initialValue = 0.6f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse), label = "Alpha")
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetProfileDialog by remember { mutableStateOf(false) }
    val isPrivate = currentRoute is Route.Resonance || currentRoute is Route.GroupField
    val themeColor = if (isPrivate) StealthRose else StealthPrimary
    val isWeak = userCount == 0 && !isBluetoothOff && !isLocationOff
    val isStill = isBluetoothOff || isLocationOff
    val barColor = when { isStill -> Color.Red.copy(alpha = 0.15f); isPermissionMissing -> Color(0xFFF4511E).copy(alpha = 0.15f); else -> themeColor.copy(alpha = 0.05f) }

    val isUnknown = userNickname == "?" || userNickname == "SET NAME" || userNickname.isEmpty()
    Box(modifier = modifier.fillMaxWidth().statusBarsPadding(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .background(barColor, RoundedCornerShape(20.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ROW 0: GLOBAL COMMAND BAR (Host & Branding)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black, RoundedCornerShape(12.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT: [DARK] [ECO]
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    EnvironmentToggle(label = "DARK", checked = isStealthMode, onCheckedChange = onToggleStealth, themeColor = themeColor)
                    EnvironmentToggle(label = "ECO", checked = lowPowerMode, onCheckedChange = onToggleLowPower, themeColor = themeColor)
                }

                // CENTER: [BLUKIT PRIVACY]
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalArrangement = Arrangement.Center, 
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onShowPrivacy() }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_blukit_logo), 
                        contentDescription = null, 
                        tint = themeColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "BLUKIT", 
                        fontSize = 7.sp, 
                        fontWeight = FontWeight.Black, 
                        color = Color.White.copy(alpha = 0.7f), 
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PRIVACY", 
                        fontSize = 7.sp, 
                        fontWeight = FontWeight.Black, 
                        color = themeColor.copy(alpha = 0.8f), 
                        letterSpacing = 1.sp
                    )
                }
                
                // RIGHT: [STATUS LABEL] [RADIOS]
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.weight(1f)) {
                    if (isPermissionMissing || isStill || isWeak) {
                        Surface(
                            color = Color.White.copy(alpha = 0.05f), 
                            shape = RoundedCornerShape(8.dp), 
                            modifier = Modifier.graphicsLayer { alpha = pulseAlpha }
                        ) {
                            Text(
                                text = when { isPermissionMissing -> if (isPermanentlyDenied) "SETTINGS" else "ALLOW"; isStill -> "AWAKEN"; else -> "SEARCHING" }, 
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, fontWeight = FontWeight.Black, color = if(isStill || isPermissionMissing) Color.Red else Color.Yellow), 
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp).clickable { 
                                    if (isPermissionMissing) { if (isPermanentlyDenied) onOpenSettings() else onGrantPermissions() }
                                    else if (isStill) onAwakenBluetooth()
                                }
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    
                    MixedStatusBranding(
                        isBluetoothOff = isBluetoothOff,
                        isWifiOff = isWifiOff,
                        isLocationOff = isLocationOff,
                        isPermissionMissing = isPermissionMissing,
                        isLocationMandatory = isLocationMandatory,
                        onAwakenBluetooth = onAwakenBluetooth,
                        onAwakenWifi = onAwakenWifi,
                        onAwakenLocation = onAwakenLocation
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ROW 1: HUMANITY Stage (Back, Crowd, Identity)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // LEFT: Navigation Exit or Logo
                Box(modifier = Modifier.width(56.dp), contentAlignment = Alignment.CenterStart) {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = themeColor, modifier = Modifier.size(20.dp))
                        }
                    } else {
                        // Branding on left if no back button
                        Icon(
                            painter = painterResource(id = R.drawable.ic_blukit_logo), 
                            contentDescription = null, 
                            tint = themeColor,
                            modifier = Modifier.padding(start = 12.dp).size(20.dp)
                        )
                    }
                }

                // CENTER: Crowd Pulse
                val isLanding = title == "THE CROWD" || title == "PUBLIC PULSES" || title == "BLUKIT" || title == "EVENT"
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    modifier = Modifier.weight(1f).then(if (onTitleClick != null) Modifier.clickable { onTitleClick() } else Modifier),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (breadcrumbTrail.isNotEmpty()) {
                        BreadcrumbHub(trail = breadcrumbTrail, onCrumbClick = onCrumbClick)
                    } else {
                        CrowdTicker(title = title, resonances = activeCrowds)
                    }
                    if (isLanding && onTitleClick != null) {
                        Icon(imageVector = Icons.Rounded.Edit, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(start = 4.dp).size(8.dp))
                    }
                }

                // RIGHT: PROFILE Persona
                Box(modifier = Modifier.width(100.dp), contentAlignment = Alignment.CenterEnd) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 4.dp)) {
                        IconButton(onClick = onShowTimeline, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Rounded.Timeline, contentDescription = "Timeline", tint = themeColor, modifier = Modifier.size(18.dp))
                        }
                        
                        if (userFocusRequester != null) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .combinedClickable(
                                        onClick = { if (isUnknown) onProfileClick?.invoke() ?: userFocusRequester.requestFocus() else userFocusRequester.requestFocus() },
                                        onLongClick = { showResetProfileDialog = true }
                                    ), 
                                contentAlignment = Alignment.Center
                            ) {
                                UserPersona(nickname = userNickname, emoji = userEmoji, onNicknameChange = onUserNicknameChange, focusRequester = userFocusRequester)
                            }
                        }
                    }
                }
            }
        }
    }
    if (showClearHistoryDialog) { BlukitAlert(title = "CLEAR PULSES?", text = "THIS WILL PERMANENTLY REMOVE YOUR SHARED HISTORY.", confirmLabel = "CLEAR", onConfirm = { onClearHistory(); showClearHistoryDialog = false }, onDismiss = { showClearHistoryDialog = false }) }
    if (showResetProfileDialog) { BlukitAlert(title = "RESET PROFILE?", text = "THIS WILL CLEAR YOUR NAME BUT KEEP YOUR PULSES.", confirmLabel = "RESET", onConfirm = { onResetProfile(); showResetProfileDialog = false }, onDismiss = { showResetProfileDialog = false }) }
}

@Composable
fun BlukitPulseHub(
    currentRoute: Route,
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    pulseCount: Int,
    crowdIsStill: Boolean,
    incomingRadioRequests: Set<P2PDevice>,
    selectedDevices: Set<String>,
    pulsedPeers: Set<String>,
    resonances: List<Resonance> = emptyList(),
    onAcceptRadio: (P2PDevice) -> Unit,
    onDenyRadio: (P2PDevice) -> Unit,
    onStartSidePulse: () -> Unit,
    onStartChain: () -> Unit,
    onClearSelection: () -> Unit,
    onAttachFile: () -> Unit = {},
    onSearchToggle: (() -> Unit)? = null,
    onManage: (() -> Unit)? = null,
    onNote: (() -> Unit)? = null,
    onCreatePublicResonance: ((String, String?) -> Unit)? = null,
    isSearchMode: Boolean = false,
    onShowPrivacy: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isPrivate = currentRoute is Route.Resonance || currentRoute is Route.GroupField
    val targetName = if (currentRoute is Route.GroupField) resonances.find { it.id == currentRoute.groupId }?.name?.uppercase() else null
    val themeColor = if (isPrivate) StealthRose else StealthPrimary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(10f), 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Selection Actions Bar
        AnimatedVisibility(
            visible = selectedDevices.isNotEmpty(), 
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom), 
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Row(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                    .border(1.dp, themeColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onStartSidePulse, 
                    colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black), 
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) { 
                    Text("WHISPER", fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 1.sp) 
                }
                Button(
                    onClick = onStartChain, 
                    colors = ButtonDefaults.buttonColors(containerColor = StealthRose, contentColor = Color.White), 
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) { 
                    Text("START CHAIN", fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 1.sp) 
                }
                IconButton(
                    onClick = onClearSelection, 
                    modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) { 
                    Icon(Icons.Rounded.Close, tint = Color.White, contentDescription = "Cancel", modifier = Modifier.size(16.dp)) 
                }
            }
        }
        
        // Contextual Creation Banner
        val showAirBanner = isSearchMode && messageText.isNotBlank() && onCreatePublicResonance != null
        AnimatedVisibility(
            visible = showAirBanner, 
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }), 
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Button(
                onClick = { onCreatePublicResonance?.invoke(messageText, null) },
                colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(Icons.Rounded.Grain, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("CREATE RESONANCE: ${messageText.uppercase()}", fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 0.5.sp)
            }
        }

        // The Main Input Hub
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.Black.copy(alpha = 0.95f), 
                    RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .border(
                    width = 1.dp, 
                    color = Color.White.copy(alpha = 0.05f), 
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                ), 
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                BlukitInput(
                    crowdIsStill = crowdIsStill, 
                    isReadOnly = false, 
                    isFilterActive = pulsedPeers.isNotEmpty(), 
                    isPrivate = isPrivate, 
                    targetName = targetName, 
                    value = messageText, 
                    onValueChange = onMessageChange, 
                    onSend = onSend, 
                    onAttachFile = onAttachFile, 
                    onManage = onManage,
                    onNote = onNote,
                    pulseCount = pulseCount, 
                    isSearchActive = isSearchMode,
                    onSearchToggle = onSearchToggle,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Incoming Requests Row
                if (incomingRadioRequests.isNotEmpty()) {
                    val request = incomingRadioRequests.first()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(StealthPrimary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .testTag("IncomingRequestRow"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = request.emoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "INCOMING RADIO REQUEST", 
                                style = MaterialTheme.typography.labelSmall, 
                                color = StealthPrimary,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = (request.name ?: "UNKNOWN").uppercase(), 
                                style = MaterialTheme.typography.bodySmall, 
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row {
                            IconButton(
                                onClick = { onDenyRadio(request) },
                                modifier = Modifier.testTag("DenyRequestButton")
                            ) { 
                                Icon(Icons.Rounded.Close, contentDescription = "Deny", tint = Color.Red.copy(alpha = 0.6f)) 
                            }
                            IconButton(
                                onClick = { onAcceptRadio(request) },
                                modifier = Modifier.testTag("AcceptRequestButton")
                            ) { 
                                Icon(Icons.Rounded.Check, contentDescription = "Accept", tint = StealthPrimary) 
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PulsingResonanceTicker(
    state: BluetoothUiState,
    energyList: List<Pair<P2PDevice, MessagePayload?>>,
    pulseCounts: Map<String, Int>,
    localDeviceId: String,
    pulsedPeers: Set<String>,
    isGrouped: Boolean = true,
    onPulseClick: (String) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit,
    onDeletePulse: (String) -> Unit,
    onIdentifyUser: (P2PDevice) -> Unit = {},
    reverseLayout: Boolean = true,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val sdf = remember { SimpleDateFormat("HH:mm", java.util.Locale.getDefault()) }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        reverseLayout = reverseLayout,
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(energyList, key = { it.second?.messageId ?: it.first.id }) { (device, msg) ->
            val id = device.persistentId ?: device.id
            val resonance = state.session.groups.find { it.id == (msg?.groupId ?: device.id) }
            
            if (msg == null && resonance != null) {
                // HEADER: High-level Resonance Summary
                ResonanceSummary(
                    title = resonance.name,
                    subtitle = if (resonance.scope == Resonance.SCOPE_PUBLIC) "CROWD" else "PRIVATE CHAIN",
                    icon = if (resonance.scope == Resonance.SCOPE_PUBLIC) Icons.Rounded.Grain else Icons.Rounded.Hearing,
                    themeColor = if (resonance.scope == Resonance.SCOPE_PUBLIC) StealthPrimary else StealthRose,
                    count = pulseCounts[id] ?: 0,
                    lastUpdate = sdf.format(Date(resonance.lastPulseTimestamp)),
                    onClick = { onPulseClick(resonance.id) },
                    showJoin = resonance.id != Resonance.ID_CROWD,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            } else {
                // ENTRY: Atomic Pulse Item
                val count = if (isGrouped) pulseCounts[id] ?: 0 else state.session.messages.count { it.parentMessageId == msg?.messageId }
                
                AnimatedPulseItem(
                    msg = msg,
                    isSelected = device.id in state.crowd.selectedDevices,
                    senderDevice = device,
                    pulseCount = count,
                    isPulsed = id in pulsedPeers,
                    isMe = msg?.senderId == localDeviceId || device.id == localDeviceId,
                    isGrouped = isGrouped,
                    isMutual = device.id in state.session.connectedRadios,
                    resonance = resonance,
                    rowId = id,
                    onPulseClick = { msg?.messageId?.let { onPulseClick(it) } ?: onDeviceLongClick(device) },
                    onDeviceLongClick = { onDeviceLongClick(device) },
                    onDelete = { msg?.messageId?.let { onDeletePulse(it) } },
                    onIdentify = { onIdentifyUser(device) }
                )
            }
        }
    }
}

@Composable
fun RadioRequestTickerItem(device: P2PDevice, onAccept: (P2PDevice) -> Unit, onDeny: (P2PDevice) -> Unit) {
    Surface(color = StealthPrimary.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, StealthPrimary.copy(alpha = 0.3f)), modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) { Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Text(text = device.emoji, fontSize = 18.sp); Spacer(modifier = Modifier.width(12.dp)); Column(modifier = Modifier.weight(1f)) { Text(text = "RADIO REQUEST", style = MaterialTheme.typography.labelSmall, color = StealthPrimary, fontWeight = FontWeight.Black); Text(text = (device.name ?: "USER").uppercase(), style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold) }; Row { IconButton(onClick = { onDeny(device) }) { Icon(Icons.Rounded.Close, contentDescription = "Deny", tint = Color.Red.copy(alpha = 0.6f)) }; IconButton(onClick = { onAccept(device) }) { Icon(Icons.Rounded.Check, contentDescription = "Accept", tint = StealthPrimary) } } } }
}

@Composable
fun OutgoingRadioRequestTickerItem(device: P2PDevice, onCancel: (P2PDevice) -> Unit) {
    Surface(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)), modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) { Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Text(text = device.emoji, fontSize = 18.sp); Spacer(modifier = Modifier.width(12.dp)); Column(modifier = Modifier.weight(1f)) { Text(text = "REQUESTING RADIO", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Black); Text(text = (device.name ?: "USER").uppercase(), style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold) }; IconButton(onClick = { onCancel(device) }) { Icon(Icons.Rounded.Close, contentDescription = "Cancel", tint = Color.White.copy(alpha = 0.2f)) } } }
}

@Composable
fun AnimatedPulseItem(
    msg: MessagePayload?,
    isSelected: Boolean,
    senderDevice: P2PDevice?,
    pulseCount: Int,
    isPulsed: Boolean,
    isMe: Boolean,
    isGrouped: Boolean,
    isMutual: Boolean,
    resonance: Resonance?,
    rowId: String,
    onPulseClick: () -> Unit,
    onDeviceLongClick: () -> Unit,
    onDelete: () -> Unit,
    onIdentify: (() -> Unit)? = null
) {
    val coordinates = LocalPersonaCoordinates.current
    val timestamp = msg?.let { SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(Date(it.timestamp)) } ?: ""
    val themeColor = if (isMutual) StealthRose else if (isPulsed) StealthPrimary else Color.White
    
    val signatureDevice = senderDevice ?: P2PDevice(id = msg?.senderId ?: "", name = msg?.senderName ?: "USER", emoji = msg?.senderEmoji ?: "👤", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
    val isPlural = msg?.isMeta == true
    val isEntry = isGrouped && msg != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(onClick = onPulseClick, onLongClick = onDeviceLongClick)
            .background(if (isSelected) Color.White.copy(alpha = 0.05f) else Color.Transparent, RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ticker Connection Point & Entry Indicator
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .width(20.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            if (isEntry) {
                // Vertical connecting line for entries
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(themeColor.copy(alpha = 0.2f))
                )
                // Small dot for the entry
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(themeColor.copy(alpha = 0.4f), CircleShape)
                )
            }
            
            Box(
                modifier = Modifier
                    .onGloballyPositioned { 
                        val center = Offset(it.size.width / 2f, it.size.height / 2f)
                        val current = coordinates[rowId] ?: PersonaConnectionPoints()
                        coordinates[rowId] = current.copy(ticker = it.positionInRoot() + center) 
                    }
                    .size(1.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (isEntry) 4.dp else 12.dp)
        ) {
            if (isGrouped && !isEntry) {
                // Header-style
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    val tieEmoji = when (resonance?.scope) {
                        Resonance.SCOPE_LOCAL -> "📱"
                        Resonance.SCOPE_PRIVATE -> "🔒"
                        else -> "🌬️"
                    }
                    Text(text = tieEmoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val groupLabel = (resonance?.name ?: "THE CROWD").uppercase()
                        Text(
                            text = groupLabel, 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Black, 
                            color = themeColor,
                            letterSpacing = 1.sp
                        )
                    }
                }
            } else {
                if (msg != null) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isPlural) {
                                Icon(Icons.Rounded.BubbleChart, contentDescription = null, tint = themeColor, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = msg.content.uppercase(), 
                                fontSize = 11.sp, 
                                color = if (isEntry) Color.White.copy(alpha = 0.9f) else Color.White, 
                                fontWeight = if (isEntry) FontWeight.Bold else FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))

                        val realSender = if (isMe) "YOU" else (msg.senderName.uppercase())
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = realSender,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White.copy(alpha = 0.3f),
                                letterSpacing = 0.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (pulseCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• $pulseCount UNITS",
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColor.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        val realSender = if (isMe) "YOU" else (msg?.senderName ?: senderDevice?.name ?: "?")
                        Text(
                            text = realSender.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.3f),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "::", fontSize = 8.sp, color = Color.White.copy(alpha = 0.1f), fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = (msg?.content ?: "...").uppercase(), 
                            fontSize = 10.sp, 
                            color = Color.White.copy(alpha = if (msg != null) 0.9f else 0.2f), 
                            maxLines = 1, 
                            fontWeight = FontWeight.ExtraBold,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (timestamp.isNotEmpty()) {
            Text(text = timestamp, fontSize = 7.sp, color = Color.White.copy(alpha = 0.15f), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
        }

        if (isMutual) { Icon(imageVector = Icons.Rounded.Flare, contentDescription = null, tint = StealthRose.copy(alpha = 0.3f), modifier = Modifier.size(8.dp)) }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Right side Persona
        Box(
            modifier = Modifier
                .onGloballyPositioned { 
                    val center = Offset(it.size.width / 2f, it.size.height / 2f)
                    val current = coordinates[rowId] ?: PersonaConnectionPoints()
                    coordinates[rowId] = current.copy(uph = it.positionInRoot() + center) 
                }
                .size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(16.dp), 
                shape = CircleShape, 
                color = when { isSelected -> Color.White.copy(alpha = 0.2f); isMutual -> StealthRose.copy(alpha = 0.2f); isPulsed -> StealthPrimary.copy(alpha = 0.2f); else -> Color.White.copy(alpha = 0.05f) }, 
                border = BorderStroke(0.5.dp, when { isSelected -> Color.White; isMutual -> StealthRose; isPulsed -> StealthPrimary; else -> Color.White.copy(alpha = 0.1f) })
            ) { 
                Box(contentAlignment = Alignment.Center) { Text(text = signatureDevice.emoji, fontSize = 8.sp) } 
            }
        }
    }
}

@Composable
fun EnvironmentToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, themeColor: Color = StealthPrimary) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 2.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, fontWeight = FontWeight.Black, color = if (checked) themeColor else Color.White.copy(alpha = 0.2f), letterSpacing = 1.sp))
        Checkbox(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = themeColor.copy(alpha = 0.2f), 
                uncheckedColor = Color.White.copy(alpha = 0.1f), 
                checkmarkColor = themeColor
            ),
            modifier = Modifier.scale(0.6f)
        )
    }
}

/**
 * A proactive nudge that surfaces when a new Crowd frequency is discovered.
 * Features a high-intensity pulse and spectral entry.
 */
@Composable
fun CrowdNudge(
    resonance: Resonance,
    onJoin: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NudgePulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f, 
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Pulse"
    )

    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .graphicsLayer { 
                scaleX = pulse
                scaleY = pulse
            },
        color = Color.Black,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, StealthPrimary.copy(alpha = 0.3f)),
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(StealthPrimary.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, StealthPrimary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Grain, 
                    contentDescription = null, 
                    tint = StealthPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CROWD DISCOVERED", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = StealthPrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = resonance.name.uppercase(), 
                    style = MaterialTheme.typography.titleMedium, 
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${resonance.memberIds.size + 1} PERSONAS NEARBY", 
                    fontSize = 8.sp, 
                    color = Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = "Dismiss", tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(18.dp))
                }
                Button(
                    onClick = onJoin,
                    colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("AWAKEN", fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 0.5.sp)
                }
            }
        }
    }
}

@Composable
fun ConfirmationDialog(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    BlukitAlert(title = title, text = text, onConfirm = onConfirm, onDismiss = onDismiss)
}

@Composable
fun BlukitAlert(
    title: String,
    text: String,
    confirmLabel: String = "OK",
    dismissLabel: String = "CANCEL",
    themeColor: Color = StealthPrimary,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AlertGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "PulseScale"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A0C14),
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 8.dp,
        modifier = Modifier.border(1.dp, themeColor.copy(alpha = 0.1f), RoundedCornerShape(32.dp)),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = themeColor.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp * pulseScale)
                    ) {}
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive, 
                        contentDescription = null, 
                        tint = themeColor, 
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title.uppercase(), 
                    fontWeight = FontWeight.Black, 
                    color = Color.White, 
                    fontSize = 18.sp, 
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                text = text.uppercase(),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColor,
                    contentColor = if (themeColor == StealthRose) Color.White else Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = confirmLabel, 
                    fontWeight = FontWeight.Black, 
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = dismissLabel, 
                    color = Color.White.copy(alpha = 0.4f), 
                    fontWeight = FontWeight.Black, 
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    )
}

/**
 * Spectral tips to guide users through the mesh experience.
 * Displays a glowing spectral card with a tip text and a lightbulb icon.
 * Features a subtle "breathing" border glow.
 */
@Composable
fun BlukitTip(
    text: String,
    themeColor: Color = StealthAmber,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TipGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, 
        targetValue = 0.5f, 
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "GlowAlpha"
    )

    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .graphicsLayer { alpha = 0.95f },
        color = Color.Black,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, themeColor.copy(alpha = glowAlpha))
    ) {
        Row(
            modifier = Modifier.padding(16.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(themeColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.TipsAndUpdates, 
                    contentDescription = null, 
                    tint = themeColor, 
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text.uppercase(),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                lineHeight = 14.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Close, 
                    contentDescription = "Dismiss", 
                    tint = Color.White.copy(alpha = 0.2f), 
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun PulseActionMenu(pulse: MessagePayload, isMe: Boolean, onInvite: () -> Unit, onDelete: () -> Unit, onDismiss: () -> Unit, onBroadcast: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A0C14),
        shape = RoundedCornerShape(28.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(text = pulse.senderEmoji ?: "💬", fontSize = 32.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = pulse.senderName.uppercase(), fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp, letterSpacing = 1.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isMe && pulse.pulseType == MessagePayload.PULSE_SILENCE) {
                    MenuActionItem(Icons.Rounded.Grain, "BROADCAST TO CROWD", StealthPrimary, onBroadcast)
                } else if (pulse.pulseType == MessagePayload.PULSE_SHOUT) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = StealthPrimary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "ALREADY BROADCASTED", color = StealthPrimary.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }
                
                MenuActionItem(Icons.Rounded.Handshake, "INVITE TO PRIVATE", StealthRose, onInvite)
                MenuActionItem(Icons.Rounded.Delete, "DELETE PULSE", Color.Red, onDelete)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("BACK", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 9.sp)
            }
        }
    )
}

@Composable
fun StatusIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, isOn: Boolean, isWeak: Boolean, isPermissionMissing: Boolean, size: Dp = 24.dp, forceWarning: Boolean = false, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "StatusAnim")
    val alpha by infiniteTransition.animateFloat(initialValue = 0.4f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse), label = "Alpha")
    IconButton(onClick = onClick, modifier = Modifier.size(size)) {
        Icon(imageVector = icon, contentDescription = null, tint = when { isPermissionMissing || !isOn && !forceWarning -> Color.Red; forceWarning || isWeak -> Color.Yellow; else -> StealthPrimary }.copy(alpha = if (!isOn || isWeak || forceWarning) alpha else 1f), modifier = Modifier.size(size * 0.65f))
    }
}

/**
 * RESONANCE SUMMARY: A Summary-First visual paradigm component.
 * Represents high-level containers (Crowds, Resonances, or Meta-Pulses).
 */
@Composable
fun ResonanceSummary(
    title: String,
    subtitle: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    themeColor: Color,
    count: Int,
    lastUpdate: String,
    onClick: () -> Unit,
    showJoin: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PluralGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f, 
        targetValue = 0.15f, 
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse),
        label = "Glow"
    )

    Surface(
        onClick = onClick,
        color = Color.Black,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, themeColor.copy(alpha = 0.3f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        tonalElevation = 4.dp
    ) {
        Box(modifier = Modifier.background(themeColor.copy(alpha = glowAlpha))) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(themeColor.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, themeColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = themeColor, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title.uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColor.copy(alpha = 0.6f),
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showJoin) {
                        Surface(
                            onClick = onClick,
                            color = themeColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, themeColor.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = "JOIN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = themeColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        if (count > 0) {
                            Surface(
                                color = themeColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, themeColor.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = count.toString(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = themeColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = lastUpdate,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonaOptionsMenu(
    device: P2PDevice,
    isTied: Boolean,
    isBlocked: Boolean,
    isSelected: Boolean,
    isRequesting: Boolean,
    activeGroupId: String? = null,
    isAlreadyInActiveGroup: Boolean = false,
    onPulse: () -> Unit,
    onAccept: () -> Unit,
    onDeny: () -> Unit,
    onDisconnect: () -> Unit,
    onSelect: () -> Unit,
    onIdentify: () -> Unit,
    onBlock: () -> Unit,
    onUnblock: () -> Unit,
    onSync: () -> Unit = {},
    onAddToGroup: (String) -> Unit = {},
    onRemoveFromGroup: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A0C14),
        shape = RoundedCornerShape(28.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(text = device.emoji, fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = (device.name ?: "USER").uppercase(), fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 2.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isRequesting) {
                    MenuActionItem(Icons.Rounded.Handshake, "ACCEPT RADIO", StealthPrimary, onAccept)
                    MenuActionItem(Icons.Rounded.Close, "DENY RADIO", Color.Red, onDeny)
                } else if (activeGroupId != null) {
                    if (isAlreadyInActiveGroup) {
                        MenuActionItem(Icons.Rounded.PersonRemove, "REMOVE FROM CHAIN", StealthRose, { onRemoveFromGroup(activeGroupId) })
                    } else {
                        MenuActionItem(Icons.Rounded.PersonAdd, "ADD TO THIS CHAIN", StealthPrimary, { onAddToGroup(activeGroupId) })
                    }
                } else if (isTied) {
                    MenuActionItem(Icons.Rounded.Sync, "PULSE SYNC", StealthAmber, onSync)
                    MenuActionItem(Icons.Rounded.SettingsInputAntenna, "DISCONNECT", StealthRose, onDisconnect)
                } else {
                    MenuActionItem(Icons.Rounded.Hearing, "WHISPER", StealthPrimary, onPulse)
                    MenuActionItem(Icons.Rounded.SettingsInputAntenna, "SECURE RADIO", StealthRose, onSelect)
                }
                
                MenuActionItem(Icons.Rounded.Radar, "IDENTIFY", Color.White, onIdentify)
                if (isBlocked) MenuActionItem(Icons.Rounded.LockOpen, "UNBLOCK USER", StealthPrimary, onUnblock) 
                else MenuActionItem(Icons.Rounded.Block, "BLOCK USER", Color.Red, onBlock)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("BACK", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 9.sp)
            }
        }
    )
}

@Composable
fun MenuActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) { Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(12.dp)); Text(text = label, fontWeight = FontWeight.ExtraBold, color = color, fontSize = 11.sp, letterSpacing = 1.sp) } }
}

@Composable
fun UserPersona(nickname: String, emoji: String, onNicknameChange: (String) -> Unit, focusRequester: FocusRequester) {
    var localNickname by remember(nickname) { mutableStateOf(if (nickname == "?") "" else nickname) }
    val isUnknown = nickname == "?" || nickname == "SET NAME" || nickname.isEmpty()
    val infiniteTransition = rememberInfiniteTransition(label = "PersonaAnim")
    
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 1.0f, 
        targetValue = 1.4f, 
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), 
        label = "HaloPulse"
    )
    
    val themeColor = if (isUnknown) StealthAmber else StealthPrimary

    val coordinates = LocalPersonaCoordinates.current
    Column(
        verticalArrangement = Arrangement.Center, 
        horizontalAlignment = Alignment.CenterHorizontally, 
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 4.dp)
            .onGloballyPositioned { 
                val center = Offset(it.size.width / 2f, it.size.height / 2f)
                val current = coordinates["YOU"] ?: PersonaConnectionPoints()
                coordinates["YOU"] = current.copy(uph = it.positionInRoot() + center)
            }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp)) {
            // Spectral Halo
            Surface(
                shape = CircleShape,
                color = themeColor.copy(alpha = 0.1f),
                modifier = Modifier.size(32.dp * haloScale)
            ) {}
            
            Surface(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape),
                color = Color(0xFF0D1017),
                border = BorderStroke(1.5.dp, themeColor.copy(alpha = 0.6f)),
                tonalElevation = 4.dp
            ) { 
                Box(contentAlignment = Alignment.Center) {
                    Text(text = emoji, fontSize = 14.sp) 
                }
            }
            
            Icon(
                painter = painterResource(id = R.drawable.ic_blukit_logo), 
                contentDescription = null, 
                tint = themeColor, 
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .background(Color.Black, CircleShape)
                    .padding(1.dp)
            )
        }
        
        // Inline Nickname entry (minimalist)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                if (localNickname.isEmpty()) { 
                    Text(text = "SET NAME", fontSize = 6.sp, fontWeight = FontWeight.Black, color = StealthAmber.copy(alpha = 0.6f), letterSpacing = 0.5.sp) 
                }
                BasicTextField(
                    value = localNickname, 
                    onValueChange = { if (it.length <= 8) { localNickname = it; onNicknameChange(it) } }, 
                    modifier = Modifier
                        .widthIn(min = 40.dp)
                        .width(IntrinsicSize.Min)
                        .focusRequester(focusRequester)
                        .testTag("IdentityPulseInput"), 
                    textStyle = MaterialTheme.typography.labelSmall.copy(
                        color = if(isUnknown) StealthAmber else Color.White, 
                        fontWeight = FontWeight.Black, 
                        fontSize = 7.sp, 
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    ), 
                    cursorBrush = SolidColor(themeColor), 
                    singleLine = true
                )
            }
        }
    }
}

data class GhostAction(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val color: Color,
    val onClick: () -> Unit
)

data class GhostPulseData(
    val emoji: String,
    val title: String,
    val subtitle: String? = null,
    val actions: List<GhostAction>,
    val themeColor: Color,
    val sourceId: String? = null
)

@Composable
fun PulseGhost(
    data: GhostPulseData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GhostPulseAnim")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Glow"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Pulse"
    )

    val coordinates = LocalPersonaCoordinates.current

    DisposableEffect(Unit) {
        onDispose {
            coordinates.remove("GHOST_PULSE")
            coordinates.remove("GHOST_SOURCE_ID")
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() }
            .background(Color.Black.copy(alpha = 0.6f))
    ) {
        // Source connection point
        Box(
            modifier = Modifier
                .onGloballyPositioned { 
                    val center = Offset(it.size.width / 2f, it.size.height / 2f)
                    coordinates["GHOST_PULSE"] = PersonaConnectionPoints(field = it.positionInRoot() + center)
                }
                .size(1.dp)
        )

        // The Ghost Core
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer { 
                scaleX = pulseScale
                scaleY = pulseScale
            }
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black,
                border = BorderStroke(2.dp, data.themeColor.copy(alpha = glowAlpha)),
                modifier = Modifier.size(120.dp),
                tonalElevation = 12.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = data.emoji, fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = data.title.uppercase(), 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Black, 
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            if (data.subtitle != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = data.themeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, data.themeColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = data.subtitle.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = data.themeColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Action Orbit
        Box(modifier = Modifier.size(340.dp), contentAlignment = Alignment.Center) {
            data.actions.forEachIndexed { index, action ->
                val angle = (index * (360f / data.actions.size)) - 90f
                val radius = 130.dp
                val x = (Math.cos(Math.toRadians(angle.toDouble())) * radius.value).dp
                val y = (Math.sin(Math.toRadians(angle.toDouble())) * radius.value).dp

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset(x = x, y = y)
                        .clickable { action.onClick(); onDismiss() }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black,
                        border = BorderStroke(1.5.dp, action.color.copy(alpha = 0.7f)),
                        modifier = Modifier.size(64.dp),
                        tonalElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = action.icon, 
                                contentDescription = null, 
                                tint = action.color, 
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = action.label.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingGhost(
    nickname: String,
    emoji: String,
    onNicknameChange: (String) -> Unit,
    onDone: () -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val infiniteTransition = rememberInfiniteTransition(label = "GhostAnim")
    val glowAlphaState = infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Glow"
    )
    val pulseScaleState = infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Pulse"
    )
    val glowAlpha = glowAlphaState.value
    val pulseScale = pulseScaleState.value

    val coordinates = LocalPersonaCoordinates.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .navigationBarsPadding()
            .imePadding()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        // Close button at top right
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.TopEnd) {
            IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.4f))
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .onGloballyPositioned { 
                    val center = Offset(it.size.width / 2f, it.size.height / 2f)
                    val current = coordinates["ONBOARDING"] ?: PersonaConnectionPoints()
                    coordinates["ONBOARDING"] = current.copy(field = it.positionInRoot() + center)
                }
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                Surface(
                    shape = CircleShape,
                    color = StealthAmber.copy(alpha = 0.15f * glowAlpha),
                    border = BorderStroke(2.dp, StealthAmber.copy(alpha = 0.5f * glowAlpha)),
                    modifier = Modifier.fillMaxSize()
                ) {}
                
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color(0xFF0D1017),
                    border = BorderStroke(2.dp, StealthAmber),
                    tonalElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = emoji, fontSize = 36.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Surface(
                color = Color.Black,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, StealthAmber.copy(alpha = 0.4f)),
                modifier = Modifier.padding(horizontal = 40.dp).clickable(enabled = false) {}
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(text = "IDENTITY RITUAL", style = MaterialTheme.typography.labelSmall, color = StealthAmber, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    BasicTextField(
                        value = nickname,
                        onValueChange = onNicknameChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(color = Color.White, textAlign = TextAlign.Center, fontWeight = FontWeight.Black),
                        cursorBrush = SolidColor(StealthAmber),
                        decorationBox = { innerTextField ->
                            if (nickname.isEmpty()) {
                                Text("SET NICKNAME", style = MaterialTheme.typography.headlineSmall.copy(color = Color.White.copy(alpha = 0.2f), textAlign = TextAlign.Center, fontWeight = FontWeight.Black))
                            }
                            innerTextField()
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("BACK", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Black)
                        }
                        
                        Button(
                            onClick = onDone,
                            enabled = nickname.isNotBlank() && nickname != "SET NAME",
                            colors = ButtonDefaults.buttonColors(containerColor = StealthAmber, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.5f).height(48.dp)
                        ) {
                            Text("AWAKEN", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CrowdRitualGhost(
    onNameChange: (String) -> Unit,
    onDone: (String?) -> Unit, // Returns templateId if selected
    onDismiss: () -> Unit,
    nearbyAirs: List<Resonance> = emptyList(),
    onJoinAir: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var airName by remember { mutableStateOf("") }
    var selectedTemplateId by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val infiniteTransition = rememberInfiniteTransition(label = "RitualAnim")
    val glowAlphaState = infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Glow"
    )
    val pulseScaleState = infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Pulse"
    )
    val glowAlpha = glowAlphaState.value
    val pulseScale = pulseScaleState.value

    val coordinates = LocalPersonaCoordinates.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            coordinates.remove("CROWD_RITUAL")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .navigationBarsPadding()
            .imePadding()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .onGloballyPositioned { 
                    val center = Offset(it.size.width / 2f, it.size.height / 2f)
                    val current = coordinates["CROWD_RITUAL"] ?: PersonaConnectionPoints()
                    coordinates["CROWD_RITUAL"] = current.copy(field = it.positionInRoot() + center)
                }
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                Surface(
                    shape = CircleShape,
                    color = StealthPrimary.copy(alpha = 0.15f * glowAlpha),
                    border = BorderStroke(2.dp, StealthPrimary.copy(alpha = 0.5f * glowAlpha)),
                    modifier = Modifier.fillMaxSize()
                ) {}
                
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color(0xFF0D1017),
                    border = BorderStroke(2.dp, StealthPrimary),
                    tonalElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val iconEmoji = cc.thevar.blukit.domain.model.CrowdTemplates.ALL.find { it.id == selectedTemplateId }?.iconEmoji ?: "🌬️"
                        if (selectedTemplateId != null) {
                            Text(text = iconEmoji, fontSize = 32.sp)
                        } else {
                            Icon(Icons.Rounded.Grain, contentDescription = null, tint = StealthPrimary, modifier = Modifier.size(40.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Surface(
                color = Color.Black,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, StealthPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.padding(horizontal = 40.dp).clickable(enabled = false) {}
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(text = "CROWD RITUAL", style = MaterialTheme.typography.labelSmall, color = StealthPrimary, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    BasicTextField(
                        value = airName,
                        onValueChange = { airName = it; onNameChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(color = Color.White, textAlign = TextAlign.Center, fontWeight = FontWeight.Black),
                        cursorBrush = SolidColor(StealthPrimary),
                        decorationBox = { innerTextField ->
                            if (airName.isEmpty()) {
                                Text("NAME THE CROWD", style = MaterialTheme.typography.headlineSmall.copy(color = Color.White.copy(alpha = 0.2f), textAlign = TextAlign.Center, fontWeight = FontWeight.Black))
                            }
                            innerTextField()
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "SELECT TEMPLATE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        cc.thevar.blukit.domain.model.CrowdTemplates.ALL.forEach { template ->
                            val isSelected = selectedTemplateId == template.id
                            Surface(
                                onClick = { selectedTemplateId = if (isSelected) null else template.id },
                                color = if (isSelected) StealthPrimary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSelected) StealthPrimary else Color.White.copy(alpha = 0.1f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = template.iconEmoji, fontSize = 16.sp)
                                    Text(text = template.name.uppercase(), fontSize = 6.sp, fontWeight = FontWeight.Black, color = if (isSelected) StealthPrimary else Color.White)
                                }
                            }
                        }
                    }

                    if (nearbyAirs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "NEARBY FREQUENCIES", fontSize = 7.sp, color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            nearbyAirs.take(3).forEach { air ->
                                Surface(
                                    onClick = { onJoinAir(air.id) },
                                    color = StealthPrimary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.5.dp, StealthPrimary.copy(alpha = 0.2f))
                                ) {
                                    Text(text = air.name.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 8.sp, fontWeight = FontWeight.Black, color = StealthPrimary)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { onDone(selectedTemplateId) },
                        enabled = airName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("AWAKEN", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun PulseNode(
    device: P2PDevice, 
    isPulsed: Boolean,
    isSelected: Boolean,
    isPeerPulsed: Boolean,
    onlyTies: Boolean,
    projectionEmoji: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val coordinates = LocalPersonaCoordinates.current
    val activePulseId = LocalActivePulseId.current.value
    val key = device.persistentId ?: device.id
    val isPulsing = activePulseId == key

    PulsePersonaSignature(
        device = device,
        isPulsed = isPulsed,
        isSelected = isSelected,
        isPeerPulsed = isPeerPulsed,
        onlyTies = onlyTies,
        projectionEmoji = projectionEmoji,
        modifier = modifier
            .testTag("PersonaNode_$key")
            .onGloballyPositioned {
                val center = Offset(it.size.width / 2f, it.size.height / 2f)
                val current = coordinates[key] ?: PersonaConnectionPoints()
                coordinates[key] = current.copy(
                    field = it.positionInRoot() + center,
                    pulse = if (isPulsing) it.positionInRoot() + center else null
                )
            },
        onClick = onClick,
        onLongClick = onLongClick
    )
}

@Composable
fun PulseDot(device: P2PDevice, modifier: Modifier = Modifier, onClick: () -> Unit = {}, onLongClick: () -> Unit = {}) {
    Box(modifier = modifier.size(8.dp).clip(CircleShape).background(StealthPrimary.copy(alpha = 0.4f)).combinedClickable(onClick = onClick, onLongClick = onLongClick))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PulsePersonaSignature(
    device: P2PDevice, 
    isPulsed: Boolean, 
    isSelected: Boolean, 
    isPeerPulsed: Boolean, 
    onlyTies: Boolean, 
    size: Dp = 52.dp, 
    isStatic: Boolean = false, 
    isHighlighted: Boolean = false, 
    projectionEmoji: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NodeAnim")
    val isProjected = projectionEmoji != null
    val basePulse = if (isProjected) 1.6f else 1.15f
    val pulseScale by if (isStatic) { 
        remember { mutableStateOf(1.0f) } 
    } else { 
        val targetPulse = if (isHighlighted) 1.5f else if ((isPulsed || isPeerPulsed)) 1.25f else basePulse
        infiniteTransition.animateFloat(
            initialValue = 1.0f, 
            targetValue = targetPulse + (device.proximityFactor * 0.1f), 
            animationSpec = infiniteRepeatable(tween(if (isHighlighted) 500 else 2000 + (device.proximityFactor * 1000).toInt(), easing = FastOutSlowInEasing), RepeatMode.Reverse), 
            label = "Pulse"
        ) 
    }
    
    val themeColor = when {
        isHighlighted -> StealthAmber
        isProjected -> StealthRose
        isSelected -> Color.White
        isPulsed -> StealthRose
        isPeerPulsed -> StealthAmber
        else -> StealthPrimary
    }

    val proximityGlow = if (isStatic) 0f else (device.proximityFactor * 0.2f).coerceAtLeast(0f)
    val bloomBoost = if (isStatic) 0f else if (isHighlighted) 0.3f else if ((isPulsed || isPeerPulsed || isProjected)) 0.12f else 0f
    
    val id = device.persistentId ?: device.id
    Box(modifier = modifier.size(size * 2.2f).combinedClickable(onClick = onClick, onLongClick = onLongClick), contentAlignment = Alignment.Center) {
        val haloAlpha = (if (isHighlighted) 0.25f else 0.08f + proximityGlow + bloomBoost) * pulseScale
        Surface(
            shape = CircleShape, 
            color = themeColor.copy(alpha = haloAlpha.coerceAtMost(0.45f)), 
            modifier = Modifier.size(size * pulseScale * (if (isProjected) 1.8f else 1.4f) + (proximityGlow + bloomBoost).dp)
        ) {}
        Surface(
            modifier = Modifier.size(size).clip(CircleShape), 
            color = when { isSelected -> Color.White.copy(alpha = 0.2f); isProjected || isPulsed -> StealthRose.copy(alpha = 0.15f); isPeerPulsed -> StealthAmber.copy(alpha = 0.15f); else -> Color(0xFF0D1017) }, 
            border = BorderStroke(if (isSelected || isPulsed || isPeerPulsed || isProjected) (size.value / 24).dp.coerceAtLeast(1.dp) else (size.value / 48).dp.coerceAtLeast(0.5.dp), when { isSelected -> Color.White; isProjected || isPulsed -> StealthRose; isPeerPulsed -> StealthAmber; else -> Color.White.copy(alpha = 0.15f) }), 
            shape = CircleShape, 
            tonalElevation = 4.dp
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                if (projectionEmoji != null) {
                    Text(text = projectionEmoji, fontSize = (size.value / 2).sp)
                } else {
                    val mediumIcon = when (device.medium) { P2PDevice.ConnectionMedium.BLUETOOTH -> Icons.Rounded.Bluetooth; P2PDevice.ConnectionMedium.WIFI -> Icons.Rounded.Wifi; P2PDevice.ConnectionMedium.LOCATION -> Icons.Rounded.LocationOn }
                    val iconSize = (size.value / 2.5f).dp
                    Icon(imageVector = if (device.isConnecting || device.isLinkPending) Icons.Rounded.Sync else if (isSelected) Icons.Rounded.CheckCircle else mediumIcon, contentDescription = null, tint = when { isSelected -> Color.White; isPulsed -> StealthRose; isPeerPulsed -> StealthAmber; else -> Color.White.copy(alpha = 0.7f) }, modifier = Modifier.size(iconSize))
                }
                
                if (size > 32.dp) {
                    Text(text = (device.name ?: "?").uppercase().take(1), fontSize = 10.sp, fontWeight = FontWeight.Black, color = themeColor.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun PulseCrowdSignature(device: P2PDevice, pulseCount: Int, isPulsed: Boolean, size: Dp = 64.dp, modifier: Modifier = Modifier) {
    val themeColor = if (isPulsed) StealthRose else StealthPrimary
    val infiniteTransition = rememberInfiniteTransition(label = "CrowdPulse")
    val pulse by infiniteTransition.animateFloat(initialValue = 1.0f, targetValue = 1.2f, animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "Pulse")
    
    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size * 1.5f)) {
        Surface(shape = CircleShape, color = themeColor.copy(alpha = 0.05f * pulse), modifier = Modifier.size(size * pulse)) {}
        Surface(modifier = Modifier.size(size), shape = CircleShape, color = Color(0xFF0A0C14), border = BorderStroke(1.5.dp, themeColor.copy(alpha = 0.4f)), tonalElevation = 4.dp) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(text = device.emoji, fontSize = (size.value / 3).sp); if (pulseCount > 0) { Text(text = pulseCount.toString(), fontSize = 8.sp, fontWeight = FontWeight.Black, color = themeColor) } } }
    }
}

data class FieldScene(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val fieldContent: @Composable (BoxScope.() -> Unit),
    val tickerContent: @Composable (BoxScope.() -> Unit),
    val inputContent: @Composable (BoxScope.() -> Unit),
    val floatingContent: @Composable (BoxScope.() -> Unit) = {}
)

@Composable
fun BlukitFieldScaffold(
    // Field Specifics
    fieldContent: @Composable BoxScope.() -> Unit,
    tickerContent: @Composable BoxScope.() -> Unit,
    inputContent: @Composable BoxScope.() -> Unit,
    floatingContent: @Composable BoxScope.() -> Unit = {},
    themeColor: Color = StealthPrimary,
    glowIntensityTarget: Float = 0.4f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SpectralGlow")
    val wanderX by infiniteTransition.animateFloat(
        initialValue = 0f, 
        targetValue = 2000f, 
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse), 
        label = "WanderX"
    )
    val wanderY by infiniteTransition.animateFloat(
        initialValue = 0f, 
        targetValue = 2000f, 
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Reverse), 
        label = "WanderY"
    )

    // Background glow that shifts based on depth
    val glowIntensity by animateFloatAsState(
        targetValue = glowIntensityTarget,
        animationSpec = tween(1500),
        label = "GlowIntensity"
    )

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        // Deep Background Glow (Spectral)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = glowIntensity * 0.25f }
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        0.0f to themeColor.copy(alpha = 0.6f),
                        0.5f to themeColor.copy(alpha = 0.15f),
                        1.0f to Color.Transparent,
                        center = Offset(wanderX, wanderY)
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar is now handled by the parent (BlukitApp) to avoid duplications
            // across navigation transitions. Screens provide their own content logic.

            Box(modifier = Modifier.weight(1f)) {
                // Main Field Content (Radar, Bubbles, etc)
                fieldContent()
                
                // Ticker - Floating with spectral fade
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp)
                        .graphicsLayer { 
                            alpha = 0.99f 
                        }
                ) {
                    tickerContent()
                }

                // Overlay for Alerts/Nudges/Tips - Centered floatingly
                // ON TOP of ticker, but only takes needed space to allow ticker interaction
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp)
                ) {
                    floatingContent()
                }
            }

            // Input interaction hub at the very bottom
            Box(modifier = Modifier.fillMaxWidth()) {
                inputContent()
            }
        }
    }
}

/**
 * The primary interaction point for spreading pulses.
 * Features a "breathing" nudge animation when empty and an "aura" glow when focused.
 * The separator line features a moving spectral gradient.
 */
@Composable
fun BlukitInput(
    crowdIsStill: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachFile: () -> Unit = {},
    onManage: (() -> Unit)? = null,
    onNote: (() -> Unit)? = null,
    pulseCount: Int = 0,
    isReadOnly: Boolean = false,
    isFilterActive: Boolean = false,
    isPrivate: Boolean = false,
    targetName: String? = null,
    placeholder: String? = null,
    isSearchActive: Boolean = false,
    onSearchToggle: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val themeColor = if (isPrivate) StealthRose else StealthPrimary
    val focusRequester = remember { FocusRequester() }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    
    var isFocused by remember { mutableStateOf(false) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "InputNudge")
    
    // Improved "Breathing" Nudge
    val nudgeScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (value.isEmpty() && !isFocused) 1.02f else 1.0f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "NudgeScale"
    )
    
    val nudgeGlow by infiniteTransition.animateFloat(
        initialValue = 0.05f, 
        targetValue = if (value.isEmpty() && !isFocused) 0.3f else 0.05f, 
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "NudgeGlow"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.8f else nudgeGlow,
        animationSpec = tween(500),
        label = "InputGlow"
    )

    val actualPlaceholder = when { 
        isSearchActive -> "SEARCH PULSES: TYPE NICKNAME OR PULSE..."
        placeholder != null -> placeholder 
        isReadOnly -> "FILTERED" 
        isPrivate && targetName != null -> "PRIVATE PULSE TO $targetName..."
        isPrivate -> "TYPE A SECURE PULSE..."
        else -> "SPREAD A PULSE TO ${targetName ?: "THE CROWD"}..." 
    }
    
    Column(modifier = modifier.graphicsLayer { scaleX = nudgeScale; scaleY = nudgeScale }) {
        // Glowing Dynamic Separator with Flowing Gradient
        val animOffset by infiniteTransition.animateFloat(
            initialValue = -1f, 
            targetValue = 2f, 
            animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), 
            label = "SeparatorAnim"
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        0.0f to Color.Transparent,
                        animOffset to themeColor.copy(alpha = glowAlpha),
                        animOffset + 0.2f to themeColor.copy(alpha = glowAlpha * 1.5f),
                        animOffset + 0.4f to Color.Transparent,
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.98f))
                .padding(bottom = 12.dp, top = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .background(Color.White.copy(alpha = 0.03f + (if(isFocused) 0.05f else 0f)), RoundedCornerShape(28.dp))
                    .border(
                        width = 1.dp, 
                        color = themeColor.copy(alpha = glowAlpha * 0.6f), 
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Actions Group
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onAttachFile, enabled = !isReadOnly, modifier = Modifier.size(40.dp)) { 
                        Icon(
                            imageVector = Icons.Rounded.Add, 
                            contentDescription = "Attach", 
                            tint = themeColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        ) 
                    }
                    if (onSearchToggle != null) {
                        IconButton(onClick = onSearchToggle, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.Radar, 
                                contentDescription = "Search", 
                                tint = if (isSearchActive) themeColor else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp), 
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = value, 
                        onValueChange = onValueChange, 
                        enabled = !isReadOnly, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("SendPulseInput")
                            .focusRequester(focusRequester)
                            .onFocusChanged { isFocused = it.isFocused },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White, 
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp
                        ), 
                        cursorBrush = SolidColor(if (isSearchActive) Color.White else themeColor), 
                        decorationBox = { innerTextField -> 
                            if (value.isEmpty()) { 
                                Text(
                                    text = actualPlaceholder, 
                                    color = Color.White.copy(alpha = 0.25f), 
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 10.sp, 
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                ) 
                            }; 
                            innerTextField() 
                        }
                    )
                }
                
                // End Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isSearchActive) {
                        if (pulseCount > 0) { 
                            Surface(
                                color = themeColor.copy(alpha = 0.15f), 
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text(
                                    text = pulseCount.toString(), 
                                    fontSize = 8.sp, 
                                    fontWeight = FontWeight.Black, 
                                    color = themeColor, 
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) 
                            }
                        }
                        
                        if (onNote != null) {
                            IconButton(onClick = onNote, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.EditNote, 
                                    contentDescription = "Note", 
                                    tint = StealthRose.copy(alpha = 0.8f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        if (isPrivate && !isReadOnly && onManage != null) {
                            IconButton(onClick = onManage, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.Groups, 
                                    contentDescription = "Manage", 
                                    tint = themeColor.copy(alpha = 0.6f), 
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        
                        IconButton(
                            onClick = { onSend(); focusManager.clearFocus() }, 
                            enabled = value.isNotBlank() && !isReadOnly, 
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("SendPulseButton")
                        ) { 
                            val sendColor = if (value.isNotBlank()) themeColor else Color.White.copy(alpha = 0.1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .background(sendColor.copy(alpha = 0.1f), CircleShape)
                                    .border(1.dp, sendColor.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Send, 
                                    contentDescription = "Send", 
                                    tint = sendColor,
                                    modifier = Modifier.size(20.dp)
                                ) 
                            }
                        }
                    } else {
                        if (value.isNotBlank()) {
                            IconButton(onClick = { onValueChange("") }, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.Close, 
                                    contentDescription = "Clear", 
                                    tint = Color.White.copy(alpha = 0.3f), 
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

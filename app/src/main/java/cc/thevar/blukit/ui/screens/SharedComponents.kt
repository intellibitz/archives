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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cc.thevar.blukit.R
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.VibeGroup
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.*
import androidx.compose.ui.res.painterResource

data class PersonaConnectionPoints(
    val uph: Offset? = null,
    val field: Offset? = null,
    val ticker: Offset? = null,
    val vibe: Offset? = null
)

val LocalPersonaCoordinates = staticCompositionLocalOf { mutableStateMapOf<String, PersonaConnectionPoints>() }
val LocalActiveVibeId = staticCompositionLocalOf { mutableStateOf<String?>(null) }

@Composable
fun MixedStatusBranding(
    isBluetoothOff: Boolean,
    isWifiOff: Boolean,
    isLocationOff: Boolean,
    isWeak: Boolean,
    isPermissionMissing: Boolean,
    isLocationMandatory: Boolean = false,
    onAwakenBluetooth: () -> Unit,
    onAwakenWifi: () -> Unit,
    onAwakenLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        StatusIcon(icon = Icons.Rounded.Wifi, isOn = !isWifiOff, isWeak = isWeak, isPermissionMissing = false, size = 18.dp, onClick = onAwakenWifi)
        StatusIcon(icon = Icons.Rounded.Bluetooth, isOn = !isBluetoothOff, isWeak = isWeak, isPermissionMissing = isPermissionMissing, size = 18.dp, onClick = onAwakenBluetooth)
        StatusIcon(icon = Icons.Rounded.LocationOn, isOn = !isLocationOff, isWeak = isWeak && !isLocationOff, isPermissionMissing = isLocationMandatory && isLocationOff, size = 18.dp, onClick = onAwakenLocation, forceWarning = !isLocationMandatory && isLocationOff)
    }
}

@Composable
fun CondensedVibeBar(connectedPeers: List<P2PDevice>, onVibeClick: () -> Unit, modifier: Modifier = Modifier) {
    if (connectedPeers.isEmpty()) return
    Surface(onClick = onVibeClick, color = Color.Black.copy(alpha = 0.9f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, StealthRose.copy(alpha = 0.4f)), modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) { connectedPeers.take(3).forEachIndexed { index, peer -> Text(text = peer.emoji, fontSize = 14.sp, modifier = Modifier.offset(x = (index * 12).dp)) } }
                val nameLabel = if (connectedPeers.size == 1) (connectedPeers.first().name ?: "USER").uppercase() else "${connectedPeers.size} PERSONAS"
                Spacer(modifier = Modifier.width(if (connectedPeers.size > 1) (connectedPeers.size * 12 + 8).dp else 12.dp))
                Text(text = "VIBING WITH $nameLabel", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = StealthRose, letterSpacing = 1.sp, fontSize = 9.sp)
            }
            Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = StealthRose.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun AirTicker(
    title: String,
    groups: List<VibeGroup>,
    modifier: Modifier = Modifier
) {
    val displayNames = remember(groups, title) {
        val names = groups.filter { it.scope == VibeGroup.SCOPE_PUBLIC || it.scope == VibeGroup.SCOPE_LOCAL }.map { it.name.uppercase() }.toMutableList()
        if (title.uppercase() !in names) names.add(0, title.uppercase())
        names.distinct()
    }

    if (displayNames.size <= 1) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = Color.White,
                fontSize = 9.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
        )
    } else {
        var index by remember { mutableIntStateOf(0) }
        LaunchedEffect(displayNames) {
            while (true) {
                delay(3000)
                index = (index + 1) % displayNames.size
            }
        }
        AnimatedContent(
            targetState = displayNames[index],
            transitionSpec = {
                (slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
            },
            label = "AirTicker",
            modifier = modifier
        ) { name ->
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.White,
                    fontSize = 9.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BlukitHarmonyTopBar(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    currentRoute: Route,
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
    airIsStill: Boolean,
    isLocationMandatory: Boolean = false,
    activeAirs: List<VibeGroup> = emptyList(),
    onToggleStealth: (Boolean) -> Unit,
    onToggleLowPower: (Boolean) -> Unit,
    onAwakenBluetooth: () -> Unit,
    onAwakenLocation: () -> Unit,
    onAwakenWifi: () -> Unit,
    onGrantPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onClearHistory: () -> Unit,
    onShowPrivacy: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    onTitleClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val pulseAlpha by rememberInfiniteTransition(label = "AlertPulse").animateFloat(initialValue = 0.6f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse), label = "Alpha")
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetProfileDialog by remember { mutableStateOf(false) }
    val isPrivate = currentRoute is Route.Vibes || currentRoute is Route.VibeDetail
    val themeColor = if (isPrivate) StealthRose else StealthPrimary
    val isWeak = userCount == 0 && !isBluetoothOff && !isLocationOff
    val isStill = isBluetoothOff || isLocationOff
    val barColor = when { isStill -> Color.Red.copy(alpha = 0.15f); isPermissionMissing -> Color(0xFFF4511E).copy(alpha = 0.15f); else -> themeColor.copy(alpha = 0.05f) }

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
            // ROW 0: GLOBAL COMMAND BAR (Air & Branding)
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {
                    if (title == "PUBLIC VIBES" || title == "PRIVATE VIBES" || title == "BLUKIT") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "BLUKIT", fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.2f), letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PRIVACY", 
                                fontSize = 7.sp, 
                                fontWeight = FontWeight.Black, 
                                color = themeColor.copy(alpha = 0.4f), 
                                letterSpacing = 1.sp,
                                modifier = Modifier.clickable { onShowPrivacy() }
                            )
                        }
                    }
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
                        isWeak = isWeak,
                        isPermissionMissing = isPermissionMissing,
                        isLocationMandatory = isLocationMandatory,
                        onAwakenBluetooth = onAwakenBluetooth,
                        onAwakenWifi = onAwakenWifi,
                        onAwakenLocation = onAwakenLocation
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ROW 1: HUMANITY Stage (Back, Air, Identity)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // LEFT: Navigation Exit
                Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.CenterStart) {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = themeColor, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // CENTER: Air Vibe
                val isLanding = title == "THE AIR" || title == "PUBLIC VIBES" || title == "BLUKIT"
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    modifier = Modifier.weight(1f).then(if (onTitleClick != null) Modifier.clickable { onTitleClick() } else Modifier),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = themeColor.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    AirTicker(title = title, groups = activeAirs)
                    if (isLanding && onTitleClick != null) {
                        Icon(imageVector = Icons.Rounded.Edit, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(start = 4.dp).size(8.dp))
                    }
                }

                // RIGHT: PROFILE Persona
                Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.CenterEnd) {
                    if (userFocusRequester != null) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .combinedClickable(
                                    onClick = { if (userNickname == "?" || userNickname == "SET NAME" || userNickname.isEmpty()) onProfileClick?.invoke() ?: userFocusRequester.requestFocus() else userFocusRequester.requestFocus() },
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
    if (showClearHistoryDialog) { ConfirmationDialog(title = "CLEAR VIBES?", text = "THIS WILL PERMANENTLY REMOVE YOUR SHARED HISTORY.", onConfirm = { onClearHistory(); showClearHistoryDialog = false }, onDismiss = { showClearHistoryDialog = false }) }
    if (showResetProfileDialog) { ConfirmationDialog(title = "RESET PROFILE?", text = "THIS WILL CLEAR YOUR NAME BUT KEEP YOUR VIBES.", onConfirm = { onResetProfile(); showResetProfileDialog = false }, onDismiss = { showResetProfileDialog = false }) }
}

@Composable
fun BlukitVibeHub(
    currentRoute: Route,
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    vibeCount: Int,
    airIsStill: Boolean,
    incomingLinkRequests: Set<P2PDevice>,
    selectedDevices: Set<String>,
    vibedPeers: Set<String>,
    groups: List<VibeGroup> = emptyList(),
    onAcceptLink: (P2PDevice) -> Unit,
    onDenyLink: (P2PDevice) -> Unit,
    onStartSideVibe: () -> Unit,
    onStartTie: () -> Unit,
    onClearSelection: () -> Unit,
    onAttachFile: () -> Unit = {},
    onSearchToggle: (() -> Unit)? = null,
    onCreatePublicTie: ((String) -> Unit)? = null,
    isSearchMode: Boolean = false,
    onShowPrivacy: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isPrivate = currentRoute is Route.Vibes || currentRoute is Route.VibeDetail
    val targetName = if (currentRoute is Route.VibeDetail) groups.find { it.id == currentRoute.groupId }?.name?.uppercase() else null
    val themeColor = if (isPrivate) StealthRose else StealthPrimary

    Column(modifier = modifier.fillMaxWidth().zIndex(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedVisibility(visible = selectedDevices.isNotEmpty(), enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Row(modifier = Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onStartSideVibe, colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black), shape = RoundedCornerShape(12.dp)) { Text("WHISPER", fontWeight = FontWeight.Black, fontSize = 10.sp) }
                Button(onClick = onStartTie, colors = ButtonDefaults.buttonColors(containerColor = StealthRose, contentColor = Color.White), shape = RoundedCornerShape(12.dp)) { Text("START TIE", fontWeight = FontWeight.Black, fontSize = 10.sp) }
                IconButton(onClick = onClearSelection, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)) { Icon(Icons.Rounded.Close, tint = Color.White, contentDescription = "Cancel") }
            }
        }
        
        val showAirBanner = isSearchMode && messageText.isNotBlank() && onCreatePublicTie != null
        AnimatedVisibility(visible = showAirBanner, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Button(
                onClick = { onCreatePublicTie?.invoke(messageText) },
                colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Rounded.Grain, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("CREATE PUBLIC TIE: ${messageText.uppercase()}", fontWeight = FontWeight.Black, fontSize = 10.sp)
            }
        }
        Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.96f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)).border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)), contentAlignment = Alignment.BottomCenter) {
            Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp).navigationBarsPadding().imePadding()) {
                BlukitInput(
                    airIsStill = airIsStill, 
                    isReadOnly = false, 
                    isFilterActive = vibedPeers.isNotEmpty(), 
                    isPrivate = isPrivate, 
                    targetName = targetName, 
                    value = messageText, 
                    onValueChange = onMessageChange, 
                    onSend = onSend, 
                    onAttachFile = onAttachFile, 
                    vibeCount = vibeCount, 
                    isSearchActive = isSearchMode,
                    onSearchToggle = onSearchToggle,
                    modifier = Modifier.fillMaxWidth()
                )
                if (incomingLinkRequests.isNotEmpty()) {
                    val request = incomingLinkRequests.first()
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp).background(StealthPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).border(0.5.dp, StealthPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "${request.emoji} REQUEST FROM ${(request.name ?: "?").uppercase()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = StealthPrimary, fontSize = 7.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "DENY", modifier = Modifier.clickable { onDenyLink(request) }, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 8.sp)
                            Text(text = "JOIN", modifier = Modifier.clickable { onAcceptLink(request) }, color = StealthPrimary, fontWeight = FontWeight.Black, fontSize = 8.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VibingVibesTicker(
    state: BluetoothUiState,
    energyList: List<Pair<P2PDevice, cc.thevar.blukit.domain.model.MessagePayload?>>,
    vibeCounts: Map<String, Int>,
    localDeviceId: String,
    vibedPeers: Set<String>,
    isGrouped: Boolean,
    onVibeClick: (String) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit,
    onToggleSelection: (String) -> Unit,
    onDeleteVibe: (String) -> Unit,
    onManageTie: (String) -> Unit = {},
    onStartWhisper: (P2PDevice) -> Unit = {},
    onFocusChange: (String?) -> Unit = {},
    onAcceptLink: (P2PDevice) -> Unit = {},
    onDenyLink: (P2PDevice) -> Unit = {},
    reverseLayout: Boolean = true,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Box(modifier = modifier) {
        if (energyList.isEmpty() && state.crowd.incomingLinkRequests.isEmpty() && state.crowd.outgoingLinkRequests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "NO VIBES IN THE AIR", color = Color.White.copy(alpha = 0.2f), fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
        LazyColumn(state = listState, reverseLayout = reverseLayout, modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp), contentPadding = PaddingValues(top = 40.dp, bottom = 8.dp)) {
            val itemsToShow = if (reverseLayout) energyList.asReversed() else energyList
            items(itemsToShow, key = { it.second?.messageId ?: it.first.id }) { (device, msg) ->
                val id = device.persistentId ?: device.id
                val count = vibeCounts[id] ?: 1
                val group = state.session.groups.find { it.id == msg?.groupId || it.id == id }
                AnimatedVibeItem(
                    msg = msg, 
                    senderDevice = device, 
                    isMe = (msg?.senderId ?: id) == localDeviceId, 
                    vibeCount = count, 
                    isVibed = id in vibedPeers, 
                    isMutual = id in state.session.connectedLinks, 
                    isSelected = device.id in state.crowd.selectedDevices, 
                    isGrouped = isGrouped, 
                    group = group,
                    timestamp = if (msg != null) timeFormatter.format(Date(msg.timestamp)) else "", 
                    onClick = { 
                        if (state.crowd.selectedDevices.isNotEmpty()) {
                            onToggleSelection(device.id) 
                        } else if (isGrouped && count > 1) {
                            onManageTie(msg?.groupId ?: cc.thevar.blukit.domain.model.VibeGroup.ID_AIR)
                        } else if (msg != null) {
                            onVibeClick(msg.messageId)
                        }
                    }, 
                    onLongClick = { onDeviceLongClick(device) }, 
                    onDelete = { msg?.let { onDeleteVibe(it.messageId) } },
                    onManage = { 
                        val gid = msg?.groupId
                        if (gid != null) onManageTie(gid) 
                        else onStartWhisper(device)
                    }
                )
            }
            if (state.crowd.incomingLinkRequests.isNotEmpty() || state.crowd.outgoingLinkRequests.isNotEmpty()) {
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
            items(state.crowd.incomingLinkRequests.toList(), key = { "in_${it.id}" }) { VibeRequestTickerItem(it, onAcceptLink, onDenyLink) }
            items(state.crowd.outgoingLinkRequests.toList(), key = { "out_${it.id}" }) { OutgoingVibeRequestTickerItem(it, onDenyLink) }
        }
    }
}

@Composable
fun VibeRequestTickerItem(device: P2PDevice, onAccept: (P2PDevice) -> Unit, onDeny: (P2PDevice) -> Unit) {
    val coordinates = LocalPersonaCoordinates.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).onGloballyPositioned { val center = Offset(it.size.width - with(density) { 4.dp.toPx() }, it.size.height / 2f); val current = coordinates[device.persistentId ?: device.id] ?: PersonaConnectionPoints(); coordinates[device.persistentId ?: device.id] = current.copy(ticker = it.positionInRoot() + center) }.background(StealthPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).border(0.5.dp, StealthPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 6.dp)) {
        Text(text = device.emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = (device.name ?: "?").uppercase(), fontWeight = FontWeight.Black, color = Color.White, fontSize = 9.sp)
            Text(text = "REQUESTING LINK", fontSize = 6.sp, color = StealthPrimary, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "DENY", modifier = Modifier.clickable { onDeny(device) }, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold, fontSize = 8.sp)
            Text(text = "JOIN", modifier = Modifier.clickable { onAccept(device) }, color = StealthPrimary, fontWeight = FontWeight.Black, fontSize = 8.sp)
        }
    }
}

@Composable
fun OutgoingVibeRequestTickerItem(device: P2PDevice, onCancel: (P2PDevice) -> Unit) {
    val coordinates = LocalPersonaCoordinates.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).onGloballyPositioned { val center = Offset(it.size.width - with(density) { 4.dp.toPx() }, it.size.height / 2f); val current = coordinates[device.persistentId ?: device.id] ?: PersonaConnectionPoints(); coordinates[device.persistentId ?: device.id] = current.copy(ticker = it.positionInRoot() + center) }.background(StealthRose.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).border(0.5.dp, StealthRose.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 6.dp)) {
        Text(text = device.emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = (device.name ?: "?").uppercase(), fontWeight = FontWeight.Black, color = Color.White, fontSize = 9.sp)
            Text(text = "LINKING...", fontSize = 6.sp, color = StealthRose, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
        Text(text = "CANCEL", modifier = Modifier.clickable { onCancel(device) }, color = StealthRose.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 8.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnimatedVibeItem(
    msg: cc.thevar.blukit.domain.model.MessagePayload?, 
    isMe: Boolean, 
    senderDevice: P2PDevice?, 
    vibeCount: Int, 
    isVibed: Boolean, 
    isMutual: Boolean, 
    isSelected: Boolean = false, 
    isGrouped: Boolean, 
    group: VibeGroup? = null,
    timestamp: String, 
    onClick: () -> Unit, 
    onLongClick: () -> Unit, 
    onDelete: () -> Unit, 
    onManage: (() -> Unit)? = null
) {
    val coordinates = LocalPersonaCoordinates.current
    val rowId = if (isMe) "YOU" else (senderDevice?.persistentId ?: senderDevice?.id ?: msg?.senderId ?: "UNKNOWN")
    val density = androidx.compose.ui.platform.LocalDensity.current
    
    val signatureDevice = senderDevice ?: cc.thevar.blukit.domain.model.P2PDevice(id = "YOU", name = "YOU", emoji = "👤", medium = cc.thevar.blukit.domain.model.P2PDevice.ConnectionMedium.BLUETOOTH)
    
    val activeVibeId = LocalActiveVibeId.current.value
    val isVibing = activeVibeId == rowId
    
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .onGloballyPositioned { 
                val center = Offset(with(density) { 24.dp.toPx() }, it.size.height / 2f)
                val current = coordinates[rowId] ?: PersonaConnectionPoints()
                coordinates[rowId] = current.copy(
                    ticker = it.positionInRoot() + center,
                    vibe = if (isVibing) it.positionInRoot() + center else null
                ) 
            }
            .animateContentSize()
            .background(
                if (isMe) StealthPrimary.copy(alpha = 0.08f) 
                else if (isSelected) Color.White.copy(alpha = 0.1f) 
                else if (isMutual) StealthRose.copy(alpha = 0.04f) 
                else if (isVibed) StealthPrimary.copy(alpha = 0.02f) 
                else Color.White.copy(alpha = 0.01f), 
                RoundedCornerShape(4.dp)
            )
            .border(
                if (isMe) 1.dp else if (isSelected) 1.dp else 0.dp, 
                if (isMe) StealthPrimary.copy(alpha = 0.2f) else if (isSelected) Color.White else Color.Transparent, 
                RoundedCornerShape(4.dp)
            )
            .combinedClickable(onClick = onClick, onLongClick = if (isMe) onDelete else onLongClick)
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        VibePersonaSignature(device = signatureDevice, isVibed = isMutual, isSelected = isSelected, isPeerVibed = isVibed, onlyTies = false, size = 18.dp, isStatic = true, modifier = Modifier.padding(end = 8.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                val primaryTitle = if (isGrouped) {
                    (group?.name ?: msg?.groupName ?: "THE AIR").uppercase()
                } else {
                    (if (isMe) "YOU" else (senderDevice?.name ?: msg?.senderName ?: "?")).uppercase()
                }

                Text(
                    text = primaryTitle, 
                    fontSize = 9.sp, 
                    fontWeight = FontWeight.Black, 
                    color = if (isMe && !isGrouped) StealthPrimary else Color.White, 
                    letterSpacing = 0.5.sp
                )
                
                val divider = @Composable {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "•", fontSize = 5.sp, color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.width(4.dp))
                }

                divider()
                val contextLabel = if (isGrouped && (group?.scope ?: msg?.vibeType ?: 0) == cc.thevar.blukit.domain.model.VibeGroup.SCOPE_PUBLIC) "TIE" else "VIBE"
                Text(text = contextLabel, fontSize = 5.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.4f))
                
                divider()
                val (scopeLabel, scopeColor) = when (msg?.vibeType) {
                    cc.thevar.blukit.domain.model.MessagePayload.VIBE_SHOUT -> "AIR" to StealthPrimary
                    cc.thevar.blukit.domain.model.MessagePayload.VIBE_SILENCE -> "LOCAL" to Color.White.copy(alpha = 0.4f)
                    else -> "TIE" to StealthRose
                }
                Text(text = scopeLabel, fontSize = 5.sp, fontWeight = FontWeight.Black, color = scopeColor.copy(alpha = 0.6f))
                
                if (!isGrouped) {
                    divider()
                    Text(text = (group?.name ?: msg?.groupName ?: "THE AIR").uppercase(), fontSize = 5.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.4f))
                }
                
                val socialLabel = if (group != null && group.scope == VibeGroup.SCOPE_PRIVATE) {
                    if (group.memberIds.size <= 2) "1-1" else "GROUP"
                } else null
                
                if (socialLabel != null) {
                    divider()
                    Text(text = socialLabel, fontSize = 5.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.4f))
                }

                if (isGrouped && vibeCount > 1) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+${vibeCount - 1} MORE", 
                        fontSize = 5.sp, 
                        fontWeight = FontWeight.Black, 
                        color = (if (isMe) StealthPrimary else Color.White).copy(alpha = 0.4f),
                        modifier = Modifier.background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                
                if (onManage != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Rounded.Settings, 
                        contentDescription = "Manage", 
                        tint = (if (msg?.vibeType == cc.thevar.blukit.domain.model.MessagePayload.VIBE_WHISPER) StealthRose else Color.White).copy(alpha = 0.4f), 
                        modifier = Modifier.size(10.dp).clickable { onManage() }
                    )
                }
            }
            if (msg?.type == cc.thevar.blukit.domain.model.MessagePayload.TYPE_IDENTITY_UPDATE) {
                Text(
                    text = "${msg.content} IS NOW KNOWN AS ${msg.senderName}",
                    fontSize = 7.sp,
                    color = StealthAmber.copy(alpha = 0.6f),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            } else if (msg?.type == cc.thevar.blukit.domain.model.MessagePayload.TYPE_IMAGE) {
                AsyncImage(
                    model = msg.content,
                    contentDescription = "Image",
                    modifier = Modifier.padding(vertical = 2.dp).size(80.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.05f))
                )
            } else {
                if (isGrouped && msg != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        if (vibeCount > 1) {
                            Text(
                                text = "$vibeCount VIBES",
                                fontSize = 6.sp,
                                fontWeight = FontWeight.Black,
                                color = StealthPrimary,
                                modifier = Modifier
                                    .background(StealthPrimary.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = msg.content.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "::", 
                            fontSize = 8.sp, 
                            color = Color.White.copy(alpha = 0.1f), 
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        val realSender = if (isMe) "YOU" else (msg.senderName.uppercase())
                        Text(
                            text = realSender,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.3f),
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
        
        // Right side Persona for Connection Points
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
                color = when { isSelected -> Color.White.copy(alpha = 0.2f); isMutual -> StealthRose.copy(alpha = 0.2f); isVibed -> StealthPrimary.copy(alpha = 0.2f); else -> Color.White.copy(alpha = 0.05f) }, 
                border = BorderStroke(0.5.dp, when { isSelected -> Color.White; isMutual -> StealthRose; isVibed -> StealthPrimary; else -> Color.White.copy(alpha = 0.1f) })
            ) { 
                Box(contentAlignment = Alignment.Center) { Text(text = signatureDevice.emoji, fontSize = 8.sp) } 
            }
        }
    }
}

@Composable
fun EnvironmentToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, themeColor: Color = StealthPrimary) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 2.dp)) {
        Text(text = label, fontSize = 7.sp, fontWeight = FontWeight.Black, color = if(checked) themeColor else Color.White.copy(alpha = 0.4f), letterSpacing = 0.5.sp)
        Box(modifier = Modifier.size(width = 32.dp, height = 20.dp), contentAlignment = Alignment.Center) {
            Switch(
                checked = checked, 
                onCheckedChange = onCheckedChange, 
                colors = SwitchDefaults.colors(
                    checkedThumbColor = themeColor, 
                    checkedTrackColor = themeColor.copy(alpha = 0.3f), 
                    uncheckedThumbColor = Color.Gray, 
                    uncheckedTrackColor = Color.DarkGray
                ), 
                modifier = Modifier.scale(0.45f)
            )
        }
    }
}

@Composable
fun AirNudge(
    group: VibeGroup,
    onJoin: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        color = Color.Black,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, StealthPrimary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(StealthPrimary.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, StealthPrimary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Grain,
                    contentDescription = null,
                    tint = StealthPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NEW AIR",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = StealthPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = group.name.uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${group.memberIds.size} PERSONAS VIBING",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Dismiss", tint = Color.White.copy(alpha = 0.3f))
                }
                Button(
                    onClick = onJoin,
                    colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("JOIN", fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun ConfirmationDialog(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, containerColor = Color.Black, titleContentColor = StealthPrimary, textContentColor = Color.White, title = { Text(title, fontWeight = FontWeight.Black) }, text = { Text(text) }, confirmButton = { TextButton(onClick = onConfirm) { Text("CONFIRM", color = Color.Red, fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } })
}

@Composable
fun VibeActionMenu(
    message: cc.thevar.blukit.domain.model.MessagePayload,
    isBroadcasted: Boolean,
    onBroadcast: () -> Unit,
    onInvite: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A0C14),
        shape = RoundedCornerShape(28.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(text = message.senderEmoji ?: "💬", fontSize = 32.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "VIBE PULSE", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp, letterSpacing = 1.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = message.content,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
                
                if (!isBroadcasted) {
                    MenuActionItem(Icons.Rounded.Grain, "BROADCAST PUBLICLY", StealthPrimary, onBroadcast)
                } else {
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
                MenuActionItem(Icons.Rounded.Delete, "DELETE VIBE", Color.Red, onDelete)
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

@Composable
fun PersonaOptionsMenu(
    device: P2PDevice,
    isTied: Boolean,
    isBlocked: Boolean,
    isSelected: Boolean,
    isRequesting: Boolean,
    activeGroupId: String? = null,
    isAlreadyInActiveGroup: Boolean = false,
    onVibe: () -> Unit,
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
                    MenuActionItem(Icons.Rounded.Handshake, "ACCEPT REQUEST", StealthPrimary, onAccept)
                    MenuActionItem(Icons.Rounded.Close, "DENY REQUEST", Color.Red, onDeny)
                } else if (activeGroupId != null) {
                    if (isAlreadyInActiveGroup) {
                        MenuActionItem(Icons.Rounded.PersonRemove, "REMOVE FROM TIE", StealthRose, { onRemoveFromGroup(activeGroupId) })
                    } else {
                        MenuActionItem(Icons.Rounded.PersonAdd, "ADD TO THIS TIE", StealthPrimary, { onAddToGroup(activeGroupId) })
                    }
                } else if (isTied) {
                    MenuActionItem(Icons.Rounded.Sync, "VIBE SYNC", StealthAmber, onSync)
                    MenuActionItem(Icons.Rounded.LinkOff, "DISCONNECT", StealthRose, onDisconnect)
                } else {
                    MenuActionItem(Icons.Rounded.Hearing, "WHISPER", StealthPrimary, onVibe)
                    MenuActionItem(Icons.Rounded.Link, "SECURE LINK", StealthRose, onSelect)
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
    val isUnknown = nickname == "?"
    val infiniteTransition = rememberInfiniteTransition(label = "NodeAnim")
    val pulseScaleState = infiniteTransition.animateFloat(
        initialValue = 1.0f, 
        targetValue = 1.1f, 
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), 
        label = "Pulse"
    )
    val pulseScale = pulseScaleState.value
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
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(30.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
                    .background(themeColor.copy(alpha = 0.1f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFF0A0C14), CircleShape)
                    .border(BorderStroke(1.dp, themeColor.copy(alpha = 0.4f)), CircleShape), 
                contentAlignment = Alignment.Center
            ) { 
                Text(text = emoji, fontSize = 11.sp) 
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_blukit_logo), 
                contentDescription = null, 
                tint = themeColor, 
                modifier = Modifier
                    .size(9.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .background(Color.Black, CircleShape)
                    .padding(1.dp)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                if (localNickname.isEmpty()) { 
                    Text(text = "SET NAME", fontSize = 7.sp, fontWeight = FontWeight.Black, color = StealthAmber.copy(alpha = 0.6f), letterSpacing = 0.5.sp) 
                }
                BasicTextField(
                    value = localNickname, 
                    onValueChange = { if (it.length <= 8) { localNickname = it; onNicknameChange(it) } }, 
                    modifier = Modifier.widthIn(min = 40.dp).width(IntrinsicSize.Min).focusRequester(focusRequester), 
                    textStyle = MaterialTheme.typography.labelSmall.copy(color = if(isUnknown) StealthAmber else Color.White, fontWeight = FontWeight.Black, fontSize = 7.sp, textAlign = TextAlign.Center), 
                    cursorBrush = SolidColor(StealthPrimary), 
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

data class GhostVibeData(
    val emoji: String,
    val title: String,
    val subtitle: String? = null,
    val actions: List<GhostAction>,
    val themeColor: Color,
    val sourceId: String? = null
)

@Composable
fun VibeGhost(
    data: GhostVibeData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GhostVibeAnim")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Glow"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Pulse"
    )

    val coordinates = LocalPersonaCoordinates.current

    DisposableEffect(Unit) {
        onDispose {
            coordinates.remove("GHOST_VIBE")
            coordinates.remove("GHOST_SOURCE_ID")
        }
    }

    // Radial layout for actions
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() }
    ) {
        // Active Actions Circle
        Box(modifier = Modifier.size(280.dp), contentAlignment = Alignment.Center) {
            data.actions.forEachIndexed { index, action ->
                val angle = (index * (360f / data.actions.size)) - 90f
                val radius = 110.dp
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
                        color = Color.Black.copy(alpha = 0.9f),
                        border = BorderStroke(1.5.dp, action.color.copy(alpha = 0.6f)),
                        modifier = Modifier.size(54.dp),
                        tonalElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = action.icon, contentDescription = null, tint = action.color, modifier = Modifier.size(26.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = action.label.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .onGloballyPositioned { 
                    val center = Offset(it.size.width / 2f, it.size.height / 2f)
                    val current = coordinates["GHOST_VIBE"] ?: PersonaConnectionPoints()
                    coordinates["GHOST_VIBE"] = current.copy(field = it.positionInRoot() + center)
                    // Passing sourceId via a dummy coordinate entry with ID in ticker.x (just a hack for now)
                    if (data.sourceId != null) {
                        coordinates["GHOST_SOURCE_ID"] = PersonaConnectionPoints(ticker = Offset(1f, 1f)) // Just to mark it exists
                        // In a real app, I'd pass this as a parameter to the canvas, but we're constrained by the existing architecture.
                    }
                }
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                // Glowing Air Halo
                Surface(
                    shape = CircleShape,
                    color = data.themeColor.copy(alpha = 0.15f * glowAlpha),
                    border = BorderStroke(2.dp, data.themeColor.copy(alpha = 0.5f * glowAlpha)),
                    modifier = Modifier.fillMaxSize()
                ) {}
                
                // Core Air Persona
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = Color(0xFF0D1017),
                    border = BorderStroke(2.dp, data.themeColor),
                    tonalElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = data.emoji, fontSize = 32.sp)
                        // If it's a promotion vibe, show a subtle "Spread" icon overlay?
                        if (data.sourceId != null) {
                            Icon(
                                imageVector = Icons.Rounded.Grain,
                                contentDescription = null,
                                tint = data.themeColor.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp).align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-4).dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, data.themeColor.copy(alpha = 0.4f)),
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = data.title.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    if (data.subtitle != null) {
                        Text(
                            text = data.subtitle.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = data.themeColor.copy(alpha = 0.8f),
                            letterSpacing = 0.5.sp
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

    DisposableEffect(Unit) {
        onDispose {
            coordinates.remove("ONBOARDING")
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
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
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
            // Glowing Halo
            Surface(
                shape = CircleShape,
                color = StealthAmber.copy(alpha = 0.1f * glowAlpha),
                border = BorderStroke(1.dp, StealthAmber.copy(alpha = 0.4f * glowAlpha)),
                modifier = Modifier.fillMaxSize()
            ) {}
            
            // Core Persona
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = Color(0xFF0D1017),
                border = BorderStroke(2.dp, StealthAmber),
                tonalElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = emoji, fontSize = 24.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, StealthAmber.copy(alpha = 0.3f)),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "WHO ARE YOU?",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                    color = StealthAmber,
                    letterSpacing = 1.sp
                )
                BasicTextField(
                    value = if (nickname == "?" || nickname == "SET NAME") "" else nickname,
                    onValueChange = { if (it.length <= 8) onNicknameChange(it) },
                    modifier = Modifier
                        .widthIn(min = 60.dp)
                        .width(IntrinsicSize.Min)
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(StealthAmber),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = { onDone() }
                    )
                )
            }
        }
    }
}

@Composable
fun AirRitualGhost(
    onNameChange: (String) -> Unit,
    onDone: () -> Unit,
    onDismiss: () -> Unit,
    nearbyAirs: List<VibeGroup> = emptyList(),
    onJoinAir: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val infiniteTransition = rememberInfiniteTransition(label = "AirGhostAnim")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Glow"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Pulse"
    )

    val coordinates = LocalPersonaCoordinates.current
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    DisposableEffect(Unit) { onDispose { coordinates.remove("AIR_RITUAL") } }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .onGloballyPositioned { 
                val center = Offset(it.size.width / 2f, it.size.height / 2f)
                val current = coordinates["AIR_RITUAL"] ?: PersonaConnectionPoints()
                coordinates["AIR_RITUAL"] = current.copy(field = it.positionInRoot() + center)
            }
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
            Surface(
                shape = CircleShape,
                color = StealthPrimary.copy(alpha = 0.15f * glowAlpha),
                border = BorderStroke(2.dp, StealthPrimary.copy(alpha = 0.5f * glowAlpha)),
                modifier = Modifier.fillMaxSize()
            ) {}
            
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = Color(0xFF0D1017),
                border = BorderStroke(2.dp, StealthPrimary),
                tonalElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Rounded.Grain, contentDescription = null, tint = StealthPrimary, modifier = Modifier.size(32.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Surface(
            color = Color.Black.copy(alpha = 0.8f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, StealthPrimary.copy(alpha = 0.4f)),
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "NAME THE AIR",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        color = StealthPrimary,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(16.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Dismiss", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(10.dp))
                    }
                }
                BasicTextField(
                    value = name,
                    onValueChange = { if (it.length <= 16) { name = it; onNameChange(it) } },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .widthIn(min = 120.dp)
                        .width(IntrinsicSize.Min)
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(StealthPrimary),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (name.isEmpty()) {
                            Text("e.g. GATE 7, CONCERT", color = Color.White.copy(alpha = 0.2f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                        innerTextField()
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = { if (name.isNotBlank()) onDone() }
                    )
                )
                if (name.isNotBlank()) {
                    Button(
                        onClick = onDone, 
                        colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 4.dp).height(24.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text("VIBE", fontWeight = FontWeight.Black, fontSize = 8.sp)
                    }
                }
                
                // NEARBY VIBRATIONS
                val publicAirs = nearbyAirs.filter { it.scope == VibeGroup.SCOPE_PUBLIC && it.id != VibeGroup.ID_AIR && it.id != VibeGroup.ID_SILENCE }
                if (publicAirs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NEARBY VIBRATIONS",
                        fontSize = 6.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.4f),
                        letterSpacing = 1.sp
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        publicAirs.take(3).forEach { air ->
                            Surface(
                                color = StealthPrimary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.5.dp, StealthPrimary.copy(alpha = 0.3f)),
                                modifier = Modifier.clickable { onJoinAir(air.id) }
                            ) {
                                Text(
                                    text = air.name.uppercase(),
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Black,
                                    color = StealthPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VibeNode(device: P2PDevice, isVibed: Boolean, isSelected: Boolean, isPeerVibed: Boolean, onlyTies: Boolean, subjectId: String? = null, modifier: Modifier = Modifier, onClick: () -> Unit = {}, onLongClick: () -> Unit = {}) {
    val isHighlighted = device.id == subjectId || device.persistentId == subjectId
    VibePersonaSignature(device = device, isVibed = isVibed, isSelected = isSelected, isPeerVibed = isPeerVibed, onlyTies = onlyTies, size = if (device.proximityFactor > 0.8f) 52.dp else 44.dp, isHighlighted = isHighlighted, modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick))
}

@Composable
fun VibeDot(device: P2PDevice, modifier: Modifier = Modifier, onClick: () -> Unit = {}, onLongClick: () -> Unit = {}) {
    Box(modifier = modifier.size(8.dp).clip(CircleShape).background(StealthPrimary.copy(alpha = 0.4f)).combinedClickable(onClick = onClick, onLongClick = onLongClick))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VibePersonaSignature(
    device: P2PDevice, 
    isVibed: Boolean, 
    isSelected: Boolean, 
    isPeerVibed: Boolean, 
    onlyTies: Boolean, 
    size: Dp = 52.dp, 
    isStatic: Boolean = false, 
    isHighlighted: Boolean = false, 
    projectionEmoji: String? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NodeAnim")
    val isProjected = projectionEmoji != null
    val basePulse = if (isProjected) 1.6f else 1.15f
    val pulseScale by if (isStatic) { 
        remember { mutableStateOf(1.0f) } 
    } else { 
        val targetPulse = if (isHighlighted) 1.5f else if ((isVibed || isPeerVibed)) 1.25f else basePulse
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
        isVibed -> StealthRose
        isPeerVibed -> StealthAmber
        else -> StealthPrimary
    }

    val proximityGlow = if (isStatic) 0f else (device.proximityFactor * 0.2f).coerceAtLeast(0f)
    val bloomBoost = if (isStatic) 0f else if (isHighlighted) 0.3f else if ((isVibed || isPeerVibed || isProjected)) 0.12f else 0f
    
    Box(modifier = modifier.size(size * 2.2f), contentAlignment = Alignment.Center) {
        val haloAlpha = (if (isHighlighted) 0.25f else 0.08f + proximityGlow + bloomBoost) * pulseScale
        Surface(
            shape = CircleShape, 
            color = themeColor.copy(alpha = haloAlpha.coerceAtMost(0.45f)), 
            modifier = Modifier.size(size * pulseScale * (if (isProjected) 1.8f else 1.4f) + (proximityGlow + bloomBoost).dp)
        ) {}
        Surface(
            modifier = Modifier.size(size).clip(CircleShape), 
            color = when { isSelected -> Color.White.copy(alpha = 0.2f); isProjected || isVibed -> StealthRose.copy(alpha = 0.15f); isPeerVibed -> StealthAmber.copy(alpha = 0.15f); else -> Color(0xFF0D1017) }, 
            border = BorderStroke(if (isSelected || isVibed || isPeerVibed || isProjected) (size.value / 24).dp.coerceAtLeast(1.dp) else (size.value / 48).dp.coerceAtLeast(0.5.dp), when { isSelected -> Color.White; isProjected || isVibed -> StealthRose; isPeerVibed -> StealthAmber; else -> Color.White.copy(alpha = 0.15f) }), 
            shape = CircleShape, 
            tonalElevation = 4.dp
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                if (projectionEmoji != null) {
                    Text(text = projectionEmoji, fontSize = (size.value / 2).sp)
                } else {
                    val mediumIcon = when (device.medium) { P2PDevice.ConnectionMedium.BLUETOOTH -> Icons.Rounded.Bluetooth; P2PDevice.ConnectionMedium.WIFI -> Icons.Rounded.Wifi; P2PDevice.ConnectionMedium.LOCATION -> Icons.Rounded.LocationOn }
                    val iconSize = (size.value / 2.5f).dp
                    Icon(imageVector = if (device.isConnecting || device.isLinkPending) Icons.Rounded.Sync else if (isSelected) Icons.Rounded.CheckCircle else mediumIcon, contentDescription = null, tint = when { isSelected -> Color.White; isVibed -> StealthRose; isPeerVibed -> StealthAmber; else -> Color.White.copy(alpha = 0.7f) }, modifier = Modifier.size(iconSize))
                }
                
                if (size > 32.dp) {
                    val displayName = (device.name ?: "?").take(7).uppercase()
                    Text(text = if (!onlyTies && isVibed) "$displayName+" else displayName, fontSize = (size.value / 6.5).sp, color = when { isSelected -> Color.White; isProjected || isVibed -> StealthRose; isPeerVibed -> StealthAmber; else -> Color.White.copy(alpha = 0.6f) }, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                }
            }
        }
    }
}

@Composable
fun VibeAirSignature(
    device: P2PDevice,
    memberCount: Int,
    isVibed: Boolean,
    size: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AirAnim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Pulse"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "Glow"
    )

    val themeColor = if (device.id == cc.thevar.blukit.domain.model.VibeGroup.ID_SILENCE) Color.White.copy(alpha = 0.4f) else if (isVibed) StealthRose else StealthPrimary

    Box(modifier = modifier.size(size * 1.8f), contentAlignment = Alignment.Center) {
        // Outer Air Halo
        Surface(
            shape = CircleShape,
            color = themeColor.copy(alpha = 0.05f * glowAlpha),
            border = BorderStroke(1.dp, themeColor.copy(alpha = 0.2f * glowAlpha)),
            modifier = Modifier.size(size * pulseScale * 1.5f)
        ) {}

        // Core Air Node
        Surface(
            modifier = Modifier.size(size),
            shape = CircleShape,
            color = Color(0xFF0D1017),
            border = BorderStroke(2.dp, themeColor.copy(alpha = 0.6f)),
            tonalElevation = 6.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = device.emoji,
                    fontSize = (size.value / 3.5).sp
                )
                Text(
                    text = (device.name ?: "THE AIR").take(8).uppercase(),
                    fontSize = (size.value / 10).sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Persona Satellites (Around the outer ring)
        val satellites = min(memberCount, 8)
        if (satellites > 0) {
            val radius = (size.value * 0.75f).dp
            for (i in 0 until satellites) {
                val angle = (i.toDouble() / satellites) * 2 * PI
                val x = (radius.value * cos(angle)).toFloat().dp
                val y = (radius.value * sin(angle)).toFloat().dp
                
                Box(
                    modifier = Modifier
                        .offset(x, y)
                        .size(6.dp)
                        .background(themeColor.copy(alpha = 0.8f), CircleShape)
                        .border(0.5.dp, Color.Black.copy(alpha = 0.4f), CircleShape)
                )
            }
        }
    }
}

@Composable
fun BlukitInput(
    airIsStill: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachFile: () -> Unit = {},
    onManage: (() -> Unit)? = null,
    vibeCount: Int = 0,
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
    val borderColor = if (airIsStill) Color.Red.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)
    val actualPlaceholder = when { 
        isSearchActive -> "SEARCH VIBES: TYPE NICKNAME OR VIBE..."
        placeholder != null -> placeholder 
        isReadOnly -> "FILTERED" 
        isPrivate && targetName != null -> "PRIVATE VIBE TO $targetName..."
        isPrivate -> "TYPE A SECURE VIBE..."
        else -> "SPREAD A VIBE TO ${targetName ?: "THE AIR"}..." 
    }
    
    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)).border(1.dp, borderColor, RoundedCornerShape(24.dp)).padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onAttachFile, enabled = !isReadOnly, modifier = Modifier.size(32.dp)) { 
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Attach", tint = themeColor.copy(alpha = 0.6f)) 
                }
                if (onSearchToggle != null) {
                    IconButton(onClick = onSearchToggle, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Radar, 
                            contentDescription = "Search", 
                            tint = if (isSearchActive) themeColor else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            BasicTextField(
                value = value, 
                onValueChange = onValueChange, 
                enabled = !isReadOnly, 
                modifier = Modifier.weight(1f), 
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White), 
                cursorBrush = SolidColor(if (isSearchActive) Color.White else themeColor), 
                decorationBox = { innerTextField -> 
                    if (value.isEmpty()) { 
                        Text(text = actualPlaceholder, color = Color.White.copy(alpha = 0.3f), style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)) 
                    }; 
                    innerTextField() 
                }
            )
            
            if (!isSearchActive) {
                if (vibeCount > 0) { Text(text = vibeCount.toString(), fontSize = 8.sp, fontWeight = FontWeight.Black, color = themeColor, modifier = Modifier.padding(horizontal = 8.dp)) }
                
                // TACTICAL EXPANSION: Quick manage for private ties
                if (isPrivate && !isReadOnly && onManage != null) {
                    IconButton(onClick = onManage, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Rounded.PersonAdd, contentDescription = "Manage", tint = themeColor.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                    }
                }
                
                IconButton(onClick = onSend, enabled = value.isNotBlank() && !isReadOnly, modifier = Modifier.size(32.dp)) { Icon(imageVector = Icons.AutoMirrored.Rounded.Send, contentDescription = "Send", tint = if (value.isNotBlank()) themeColor else Color.White.copy(alpha = 0.2f)) }
            } else {
                if (value.isNotBlank()) {
                    IconButton(onClick = { onValueChange("") }, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

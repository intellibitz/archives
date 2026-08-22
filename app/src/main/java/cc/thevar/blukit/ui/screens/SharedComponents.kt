package cc.thevar.blukit.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose

data class PersonaConnectionPoints(
    val uph: Offset? = null,
    val field: Offset? = null,
    val ticker: Offset? = null
)

val LocalPersonaCoordinates = staticCompositionLocalOf { mutableStateMapOf<String, PersonaConnectionPoints>() }


@Composable
fun MixedStatusBranding(
    isBluetoothOff: Boolean,
    isWifiOff: Boolean,
    isLocationOff: Boolean,
    isWeak: Boolean,
    isPermissionMissing: Boolean,
    onAwakenBluetooth: () -> Unit,
    onAwakenWifi: () -> Unit,
    onAwakenLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        StatusIcon(icon = Icons.Rounded.Wifi, isOn = !isWifiOff, isWeak = isWeak, isPermissionMissing = isPermissionMissing, size = 18.dp, onClick = onAwakenWifi)
        StatusIcon(icon = Icons.Rounded.Bluetooth, isOn = !isBluetoothOff, isWeak = isWeak, isPermissionMissing = isPermissionMissing, size = 18.dp, onClick = onAwakenBluetooth)
        StatusIcon(icon = Icons.Rounded.LocationOn, isOn = !isLocationOff, isWeak = isWeak, isPermissionMissing = isPermissionMissing, size = 18.dp, onClick = onAwakenLocation)
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
    onToggleStealth: (Boolean) -> Unit,
    onToggleLowPower: (Boolean) -> Unit,
    onAwakenBluetooth: () -> Unit,
    onAwakenLocation: () -> Unit,
    onAwakenWifi: () -> Unit,
    onGrantPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onClearHistory: () -> Unit,
    onBack: (() -> Unit)? = null,
    onManage: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val pulseAlpha by rememberInfiniteTransition(label = "AlertPulse").animateFloat(initialValue = 0.6f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse), label = "Alpha")
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
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
            // ROW 0: GLOBAL COMMAND BAR (Atmosphere & Branding)
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

                // CENTER: [STADIUM VIBES]
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {
                    if (title == "PUBLIC VIBES" || title == "PRIVATE VIBES" || title == "BLUKIT") {
                        Text(text = "STADIUM VIBES", fontSize = 7.sp, fontWeight = FontWeight.Black, color = themeColor.copy(alpha = 0.8f), letterSpacing = 1.sp)
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
                        onAwakenBluetooth = onAwakenBluetooth,
                        onAwakenWifi = onAwakenWifi,
                        onAwakenLocation = onAwakenLocation
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ROW 1: HUMANITY & TACTICAL (Search, Manage, Identity)
            Row(modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                // SEARCH Discovery (Left)
                if (onSearch != null) {
                    IconButton(onClick = onSearch, modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.04f)).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))) { 
                        Icon(Icons.Rounded.Radar, contentDescription = "Search", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) 
                    }
                } else {
                    Spacer(modifier = Modifier.width(36.dp))
                }

                // MANAGE Group Management (Center)
                if (onManage != null) {
                    IconButton(onClick = onManage, modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.04f)).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))) { 
                        Icon(Icons.Rounded.People, contentDescription = "Manage", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) 
                    }
                } else {
                    Spacer(modifier = Modifier.width(36.dp))
                }

                // PROFILE Identity (Right - Aligned with Ticker UPH)
                if (userFocusRequester != null) {
                    Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.04f)).clickable { userFocusRequester.requestFocus() }, contentAlignment = Alignment.Center) {
                            UserPersona(nickname = userNickname, emoji = userEmoji, airIsStill = airIsStill, onNicknameChange = onUserNicknameChange, focusRequester = userFocusRequester)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // ROW 2: PROTOCOLS & NAVIGATION (Privacy, Context, Exit)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                // PRIVACY Portal (Left)
                Text(
                    text = "PRIVACY", 
                    fontSize = 7.sp, 
                    fontWeight = FontWeight.Black, 
                    color = StealthPrimary.copy(alpha = 0.6f), 
                    letterSpacing = 0.5.sp, 
                    modifier = Modifier.clickable { showPrivacyDialog = true }
                )

                // Context Title (Center)
                if (title != "PUBLIC VIBES" && title != "PRIVATE VIBES" && title != "BLUKIT") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = icon, contentDescription = null, tint = themeColor.copy(alpha = 0.8f), modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = title.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp, color = themeColor.copy(alpha = 0.8f), fontSize = 7.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // BACK Navigation Exit (Right)
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.size(24.dp)) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp)) }
                } else {
                    Spacer(modifier = Modifier.width(24.dp))
                }
            }
        }
    }
    if (showClearHistoryDialog) { ConfirmationDialog(title = "CLEAR VIBES?", text = "THIS WILL PERMANENTLY REMOVE YOUR SHARED HISTORY.", onConfirm = { onClearHistory(); showClearHistoryDialog = false }, onDismiss = { showClearHistoryDialog = false }) }
    if (showPrivacyDialog) { AlertDialog(onDismissRequest = { showPrivacyDialog = false }, containerColor = Color.Black, titleContentColor = StealthPrimary, textContentColor = Color.White.copy(alpha = 0.7f), title = { Text("PRIVACY PROTOCOL", fontWeight = FontWeight.Black, fontSize = 14.sp) }, text = { Text("BLUKIT IS ANONYMOUS-FIRST. ALL VIBES ARE EPHEMERAL AND STORED LOCALLY FOR 12 HOURS.", fontSize = 11.sp) }, confirmButton = { TextButton(onClick = { showPrivacyDialog = false }) { Text("UNDERSTOOD", color = StealthPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp) } }) }
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
    modifier: Modifier = Modifier
) {
    val isPrivate = currentRoute is Route.Vibes || currentRoute is Route.VibeDetail
    val targetName = if (currentRoute is Route.VibeDetail) groups.find { it.id == currentRoute.groupId }?.name?.uppercase() else null

    Column(modifier = modifier.fillMaxWidth().zIndex(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedVisibility(visible = selectedDevices.isNotEmpty(), enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Row(modifier = Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onStartSideVibe, colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black), shape = RoundedCornerShape(12.dp)) { Text("WHISPER", fontWeight = FontWeight.Black, fontSize = 10.sp) }
                Button(onClick = onStartTie, colors = ButtonDefaults.buttonColors(containerColor = StealthRose, contentColor = Color.White), shape = RoundedCornerShape(12.dp)) { Text("VIBE REQUEST", fontWeight = FontWeight.Black, fontSize = 10.sp) }
                IconButton(onClick = onClearSelection, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)) { Icon(Icons.Rounded.Close, tint = Color.White, contentDescription = "Cancel") }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.96f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)).border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)), contentAlignment = Alignment.BottomCenter) {
            Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp).navigationBarsPadding().imePadding()) {
                BlukitInput(airIsStill = airIsStill, isReadOnly = false, isFilterActive = vibedPeers.isNotEmpty(), isPrivate = isPrivate, targetName = targetName, value = messageText, onValueChange = onMessageChange, onSend = onSend, vibeCount = vibeCount, modifier = Modifier.fillMaxWidth())
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
                Text(text = "VIBE RESONANCE", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp, letterSpacing = 1.sp)
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
                    MenuActionItem(Icons.Rounded.Podcasts, "BROADCAST PUBLICLY", StealthPrimary, onBroadcast)
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
        confirmButton = {}
    )
}

@Composable
fun StatusIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, isOn: Boolean, isWeak: Boolean, isPermissionMissing: Boolean, size: Dp = 24.dp, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "StatusAnim")
    val alpha by infiniteTransition.animateFloat(initialValue = 0.4f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse), label = "Alpha")
    IconButton(onClick = onClick, modifier = Modifier.size(size)) {
        Icon(imageVector = icon, contentDescription = null, tint = when { isPermissionMissing || !isOn -> Color.Red; isWeak -> Color.Yellow; else -> StealthPrimary }.copy(alpha = if (!isOn || isWeak) alpha else 1f), modifier = Modifier.size(size * 0.65f))
    }
}

@Composable
fun PersonaOptionsMenu(device: P2PDevice, isTied: Boolean, isBlocked: Boolean, isSelected: Boolean, isRequesting: Boolean, onVibe: () -> Unit, onAccept: () -> Unit, onDeny: () -> Unit, onDisconnect: () -> Unit, onSelect: () -> Unit, onIdentify: () -> Unit, onBlock: () -> Unit, onUnblock: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, containerColor = Color(0xFF0A0C14), shape = RoundedCornerShape(28.dp), title = { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) { Text(text = device.emoji, fontSize = 40.sp); Spacer(modifier = Modifier.height(8.dp)); Text(text = (device.name ?: "USER").uppercase(), fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 2.sp) } }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { if (isRequesting) { MenuActionItem(Icons.Rounded.Handshake, "ACCEPT REQUEST", StealthPrimary, onAccept); MenuActionItem(Icons.Rounded.Close, "DENY REQUEST", Color.Red, onDeny) } else if (isTied) { MenuActionItem(Icons.Rounded.LinkOff, "DISCONNECT", StealthRose, onDisconnect) } else { MenuActionItem(Icons.Rounded.Hearing, "WHISPER", StealthPrimary, onVibe); MenuActionItem(Icons.Rounded.Link, "SECURE LINK", StealthRose, onSelect) }; MenuActionItem(Icons.Rounded.Radar, "IDENTIFY", Color.White, onIdentify); if (isBlocked) MenuActionItem(Icons.Rounded.LockOpen, "UNBLOCK USER", StealthPrimary, onUnblock) else MenuActionItem(Icons.Rounded.Block, "BLOCK USER", Color.Red, onBlock) } }, confirmButton = {})
}

@Composable
fun MenuActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) { Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(12.dp)); Text(text = label, fontWeight = FontWeight.ExtraBold, color = color, fontSize = 11.sp, letterSpacing = 1.sp) } }
}

@Composable
fun UserPersona(nickname: String, emoji: String, airIsStill: Boolean, onNicknameChange: (String) -> Unit, focusRequester: FocusRequester) {
    var localNickname by remember(nickname) { mutableStateOf(if (nickname == "?") "" else nickname) }
    val isUnknown = nickname == "?"
    val pulseScale by rememberInfiniteTransition(label = "NodeAnim").animateFloat(initialValue = 1.0f, targetValue = 1.1f, animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "Pulse")
    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxHeight().padding(horizontal = 4.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(30.dp)) {
            Box(modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }.background((if (isUnknown) StealthAmber else StealthPrimary).copy(alpha = 0.1f), CircleShape))
            Box(modifier = Modifier.size(24.dp).background(Color(0xFF0A0C14), CircleShape).border(1.dp, if (isUnknown) StealthAmber.copy(alpha = 0.4f) else StealthPrimary.copy(alpha = 0.4f), CircleShape), contentAlignment = Alignment.Center) { Text(text = emoji, fontSize = 11.sp) }
            Icon(painter = painterResource(id = R.drawable.ic_blukit_logo), contentDescription = null, tint = if (isUnknown) StealthAmber else StealthPrimary, modifier = Modifier.size(9.dp).align(Alignment.BottomEnd).offset(x = 1.dp, y = 1.dp).background(Color.Black, CircleShape).padding(1.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                if (localNickname.isEmpty()) { Text(text = "SET NAME", fontSize = 7.sp, fontWeight = FontWeight.Black, color = StealthAmber.copy(alpha = 0.6f), letterSpacing = 0.5.sp) }
                BasicTextField(value = localNickname, onValueChange = { if (it.length <= 8) { localNickname = it; onNicknameChange(it) } }, modifier = Modifier.widthIn(min = 40.dp).width(IntrinsicSize.Min).focusRequester(focusRequester), textStyle = MaterialTheme.typography.labelSmall.copy(color = if(isUnknown) StealthAmber else Color.White, fontWeight = FontWeight.Black, fontSize = 7.sp, textAlign = TextAlign.Center), cursorBrush = SolidColor(StealthPrimary), singleLine = true)
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
fun VibePersonaSignature(device: P2PDevice, isVibed: Boolean, isSelected: Boolean, isPeerVibed: Boolean, onlyTies: Boolean, size: Dp = 52.dp, isStatic: Boolean = false, isHighlighted: Boolean = false, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "NodeAnim")
    val pulseScale by if (isStatic) { remember { mutableStateOf(1.0f) } } else { val targetPulse = if (isHighlighted) 1.5f else if ((isVibed || isPeerVibed)) 1.25f else 1.15f; infiniteTransition.animateFloat(initialValue = 1.0f, targetValue = targetPulse + (device.proximityFactor * 0.1f), animationSpec = infiniteRepeatable(tween(if (isHighlighted) 500 else 2000 + (device.proximityFactor * 1000).toInt(), easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "Pulse") }
    val proximityGlow = if (isStatic) 0f else (device.proximityFactor * 0.2f).coerceAtLeast(0f)
    val bloomBoost = if (isStatic) 0f else if (isHighlighted) 0.3f else if ((isVibed || isPeerVibed)) 0.12f else 0f
    Box(modifier = modifier.size(size * 2.2f), contentAlignment = Alignment.Center) {
        val haloAlpha = (if (isHighlighted) 0.25f else 0.08f + proximityGlow + bloomBoost) * pulseScale
        Surface(shape = CircleShape, color = (if (isHighlighted) StealthAmber else if (isSelected) Color.White else if (isVibed) StealthRose else if (isPeerVibed) StealthAmber else StealthPrimary).copy(alpha = haloAlpha.coerceAtMost(0.45f)), modifier = Modifier.size(size * pulseScale * (1.4f + proximityGlow + bloomBoost))) {}
        Surface(modifier = Modifier.size(size).clip(CircleShape), color = when { isSelected -> Color.White.copy(alpha = 0.2f); isVibed -> StealthRose.copy(alpha = 0.15f); isPeerVibed -> StealthAmber.copy(alpha = 0.15f); else -> Color(0xFF0D1017) }, border = BorderStroke(if (isSelected || isVibed || isPeerVibed) (size.value / 24).dp.coerceAtLeast(1.dp) else (size.value / 48).dp.coerceAtLeast(0.5.dp), when { isSelected -> Color.White; isVibed -> StealthRose; isPeerVibed -> StealthAmber; else -> Color.White.copy(alpha = 0.15f) }), shape = CircleShape, tonalElevation = 4.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                val mediumIcon = when (device.medium) { P2PDevice.ConnectionMedium.BLUETOOTH -> Icons.Rounded.Bluetooth; P2PDevice.ConnectionMedium.WIFI -> Icons.Rounded.Wifi; P2PDevice.ConnectionMedium.LOCATION -> Icons.Rounded.LocationOn }
                val iconSize = (size.value / 2.5f).dp
                Icon(imageVector = if (device.isConnecting || device.isLinkPending) Icons.Rounded.Sync else if (isSelected) Icons.Rounded.CheckCircle else mediumIcon, contentDescription = null, tint = when { isSelected -> Color.White; isVibed -> StealthRose; isPeerVibed -> StealthAmber; else -> Color.White.copy(alpha = 0.7f) }, modifier = Modifier.size(iconSize))
                if (size > 32.dp) {
                    val displayName = (device.name ?: "?").take(7).uppercase()
                    Text(text = if (!onlyTies && isVibed) "$displayName+" else displayName, fontSize = (size.value / 6.5).sp, color = when { isSelected -> Color.White; isVibed -> StealthRose; isPeerVibed -> StealthAmber; else -> Color.White.copy(alpha = 0.6f) }, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                }
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
    vibeCount: Int = 0,
    isReadOnly: Boolean = false,
    isFilterActive: Boolean = false,
    isPrivate: Boolean = false,
    targetName: String? = null,
    decoratorText: String? = null,
    placeholder: String? = null,
    modifier: Modifier = Modifier
) {
    val themeColor = if (isPrivate) StealthRose else StealthPrimary
    val borderColor = if (airIsStill) Color.Red.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)
    val actualDecoratorText = when { decoratorText != null -> decoratorText; isReadOnly -> "FOCUS VIBES: READ ONLY"; isPrivate && targetName != null -> "PRIVATE VIBE TO $targetName"; else -> "SPREAD A VIBE TO THE STADIUM" }
    val actualPlaceholder = when { placeholder != null -> placeholder; isReadOnly -> "FILTERED"; isPrivate -> "TYPE A SECURE VIBE..."; else -> "TYPE A VIBE..." }
    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)).border(1.dp, borderColor, RoundedCornerShape(24.dp)).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(value = value, onValueChange = onValueChange, enabled = !isReadOnly, modifier = Modifier.weight(1f), textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White), cursorBrush = SolidColor(themeColor), decorationBox = { innerTextField -> if (value.isEmpty()) { Text(text = actualPlaceholder, color = Color.White.copy(alpha = 0.3f), style = MaterialTheme.typography.bodyMedium) }; innerTextField() })
            if (vibeCount > 0) { Text(text = vibeCount.toString(), fontSize = 8.sp, fontWeight = FontWeight.Black, color = themeColor, modifier = Modifier.padding(horizontal = 8.dp)) }
            IconButton(onClick = onSend, enabled = value.isNotBlank() && !isReadOnly, modifier = Modifier.size(32.dp)) { Icon(imageVector = Icons.AutoMirrored.Rounded.Send, contentDescription = "Send", tint = if (value.isNotBlank()) themeColor else Color.White.copy(alpha = 0.2f)) }
        }
        Surface(color = Color.Black, modifier = Modifier.offset(y = (-1).dp)) { Text(text = actualDecoratorText, fontSize = 6.sp, fontWeight = FontWeight.Black, color = (if (airIsStill) Color.Red else themeColor).copy(alpha = 0.6f), letterSpacing = 1.sp, modifier = Modifier.padding(horizontal = 6.dp)) }
    }
}

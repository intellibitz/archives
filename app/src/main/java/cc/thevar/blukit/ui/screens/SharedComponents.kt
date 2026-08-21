package cc.thevar.blukit.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.R
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose

@Composable
fun RadioB(
    modifier: Modifier = Modifier,
    color: Color = StealthPrimary
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        
        drawLine(
            color = color,
            start = Offset(centerX - 4.dp.toPx(), centerY - 8.dp.toPx()),
            end = Offset(centerX - 4.dp.toPx(), centerY + 8.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(centerX - 4.dp.toPx(), centerY - 8.dp.toPx()),
            size = Size(8.dp.toPx(), 8.dp.toPx()),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(centerX - 4.dp.toPx(), centerY),
            size = Size(10.dp.toPx(), 8.dp.toPx()),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        drawCircle(
            color = color,
            radius = 1.dp.toPx(),
            center = Offset(centerX + 6.dp.toPx(), centerY - 4.dp.toPx())
        )
        drawCircle(
            color = color,
            radius = 1.dp.toPx(),
            center = Offset(centerX + 8.dp.toPx(), centerY + 4.dp.toPx())
        )
    }
}

@Composable
fun TopHubTabs(
    currentRoute: Route,
    onNavigate: (Route) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TopHubTab(
            label = "ALL",
            icon = Icons.Rounded.Groups,
            isSelected = currentRoute is Route.Blukit,
            onClick = { onNavigate(Route.Blukit) }
        )
        Box(modifier = Modifier.width(1.dp).height(12.dp).background(Color.White.copy(alpha = 0.08f)))
        TopHubTab(
            label = "MINE",
            icon = Icons.Rounded.Flare,
            isSelected = currentRoute is Route.Mine,
            onClick = { onNavigate(Route.Mine) }
        )
    }
}

@Composable
private fun TopHubTab(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) (if (label == "MINE") StealthRose else StealthPrimary) else Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(10.dp)
        )
        Text(
            text = label,
            fontSize = 7.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f),
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun BlukitHarmonyTopBar(
    currentRoute: Route,
    onNavigate: (Route) -> Unit,
    isNoiseFilterActive: Boolean,
    onToggleNoiseFilter: (Boolean) -> Unit,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isBluetoothOff: Boolean,
    isLocationOff: Boolean,
    isWifiOff: Boolean,
    isPermissionMissing: Boolean,
    isPermanentlyDenied: Boolean,
    userCount: Int,
    isStealthMode: Boolean,
    lowPowerMode: Boolean,
    vibeCount: Int,
    onToggleStealth: (Boolean) -> Unit,
    onToggleLowPower: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onResetProfile: () -> Unit,
    onAwakenBluetooth: () -> Unit,
    onAwakenLocation: () -> Unit,
    onAwakenWifi: () -> Unit,
    onGrantPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onBack: (() -> Unit)? = null,
    onManage: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val pulseAlpha by rememberInfiniteTransition(label = "AlertPulse").animateFloat(
        initialValue = 0.6f, targetValue = 1f, 
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse), 
        label = "Alpha"
    )
    
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val isWeak = userCount == 0 && !isBluetoothOff && !isLocationOff
    val isStill = isBluetoothOff || isLocationOff
    val barColor = when { 
        isStill -> Color.Red.copy(alpha = 0.15f)
        isPermissionMissing -> Color(0xFFF4511E).copy(alpha = 0.15f)
        else -> Color.White.copy(alpha = 0.05f) 
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .background(barColor, RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ROW 1: Alerts (Left) | Branding (Center)
        Box(modifier = Modifier.fillMaxWidth().heightIn(min = 20.dp), contentAlignment = Alignment.Center) {
            // LEFT: Status Text / Permission required
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                if (isPermissionMissing) {
                    Surface(
                        onClick = { if (isPermanentlyDenied) onOpenSettings() else onGrantPermissions() },
                        color = Color.White,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.graphicsLayer { alpha = pulseAlpha }
                    ) {
                        Text(
                            text = if (isPermanentlyDenied) "SETTINGS" else "ALLOW",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp, fontWeight = FontWeight.Black, color = Color.Red),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                } else if (isStill || isWeak) {
                    Text(
                        text = if (isStill) "AIR IS STILL" else "SEARCHING",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, fontWeight = FontWeight.Black, color = if(isStill) Color.Red else Color.Yellow),
                        modifier = Modifier.graphicsLayer { alpha = pulseAlpha }
                    )
                }
            }
            
            // CENTER: Branding
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "V I", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp))
                Spacer(modifier = Modifier.width(6.dp))
                RadioB(modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "E S", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp))
            }
        }

        // ROW 2: Radios | Delete/Reset
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            // LEFT: Back + Radios
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    }
                }
                StatusIcon(icon = Icons.Rounded.Bluetooth, isOn = !isBluetoothOff, isWeak = isWeak, isPermissionMissing = isPermissionMissing, onClick = onAwakenBluetooth)
                StatusIcon(icon = Icons.Rounded.Wifi, isOn = !isWifiOff, isWeak = isWeak, isPermissionMissing = isPermissionMissing, onClick = onAwakenWifi)
                StatusIcon(icon = Icons.Rounded.LocationOn, isOn = !isLocationOff, isWeak = isWeak, isPermissionMissing = isPermissionMissing, onClick = onAwakenLocation)
            }

            // RIGHT: DELETE, RESET
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { showClearHistoryDialog = true }, 
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (vibeCount > 0) {
                            Text(text = vibeCount.toString(), fontSize = 8.sp, fontWeight = FontWeight.Black, color = StealthPrimary)
                        }
                        Icon(Icons.Rounded.DeleteSweep, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }
                }

                IconButton(
                    onClick = { showLogoutDialog = true }, 
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Rounded.RestartAlt, contentDescription = "Reset", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                }
            }
        }

        // ROW 3: Environment (Left) | Context (Right)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            // LEFT: DARK, ECO (Moved to Row 3)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                EnvironmentToggle(label = "DARK", checked = isStealthMode, onCheckedChange = onToggleStealth)
                EnvironmentToggle(label = "ECO", checked = lowPowerMode, onCheckedChange = onToggleLowPower)
            }

            // RIGHT: Screen Title (ALL, MINE, etc.) OR Tabs
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (currentRoute is Route.Blukit) {
                    IconButton(
                        onClick = { onToggleNoiseFilter(!isNoiseFilterActive) }, 
                        modifier = Modifier
                            .size(28.dp)
                            .background(if (isNoiseFilterActive) StealthPrimary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f), CircleShape)
                            .border(1.dp, if (isNoiseFilterActive) StealthPrimary else Color.Transparent, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isNoiseFilterActive) Icons.Rounded.FilterCenterFocus else Icons.Rounded.Tune, 
                            contentDescription = "Filter", 
                            tint = if (isNoiseFilterActive) StealthPrimary else Color.White.copy(alpha = 0.4f), 
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (currentRoute is Route.Blukit || currentRoute is Route.Mine) {
                    TopHubTabs(currentRoute = currentRoute, onNavigate = onNavigate)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = (if (title == "MINE") StealthRose else StealthPrimary).copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = title.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = (if (title == "MINE") StealthRose else StealthPrimary).copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                        )
                        if (onManage != null) {
                            IconButton(onClick = onManage, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Rounded.People, contentDescription = "Manage", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }

        if (showClearHistoryDialog) { ConfirmationDialog(title = "CLEAR VIBES?", text = "THIS WILL PERMANENTLY REMOVE YOUR SHARED HISTORY.", onConfirm = { onClearHistory(); showClearHistoryDialog = false }, onDismiss = { showClearHistoryDialog = false }) }
        if (showLogoutDialog) { ConfirmationDialog(title = "RESET PROFILE?", text = "THIS WILL DELETE YOUR LOCAL BLUKIT IDENTITY.", onConfirm = { onResetProfile(); showLogoutDialog = false }, onDismiss = { showLogoutDialog = false }) }
    }
}

@Composable
fun EnvironmentToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) { 
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) { 
        Text(text = label, fontSize = 7.sp, fontWeight = FontWeight.Black, color = if(checked) StealthPrimary else Color.White.copy(alpha = 0.4f), letterSpacing = 0.5.sp)
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange, 
            modifier = Modifier.scale(0.6f).height(24.dp), 
            colors = SwitchDefaults.colors(checkedThumbColor = StealthPrimary, checkedTrackColor = StealthPrimary.copy(alpha = 0.5f))
        ) 
    } 
}

@Composable
fun ConfirmationDialog(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) { 
    AlertDialog(
        onDismissRequest = onDismiss, 
        containerColor = Color.Black, 
        titleContentColor = StealthPrimary, 
        textContentColor = Color.White.copy(alpha = 0.7f), 
        title = { Text(title, fontWeight = FontWeight.Black, fontSize = 14.sp) }, 
        text = { Text(text, fontSize = 11.sp) }, 
        confirmButton = { TextButton(onClick = onConfirm) { Text("PROCEED", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp) } }, 
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp) } }
    ) 
}

@Composable
private fun StatusIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, isOn: Boolean, isWeak: Boolean, isPermissionMissing: Boolean, onClick: () -> Unit) { 
    val tint = when { 
        !isOn -> Color.Red
        isPermissionMissing -> Color(0xFFF4511E)
        isWeak -> Color.Yellow
        else -> Color.Green 
    }
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon, 
            contentDescription = null, 
            tint = tint.copy(alpha = 0.8f), 
            modifier = Modifier.size(18.dp)
        ) 
    }
}

@Composable
fun PersonaOptionsMenu(
    device: P2PDevice,
    isVibed: Boolean,
    isTied: Boolean,
    isBlocked: Boolean,
    onFocus: () -> Unit,
    onVibe: () -> Unit,
    onWhisper: () -> Unit,
    onBlock: () -> Unit,
    onUnblock: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Black.copy(alpha = 0.95f),
        titleContentColor = StealthPrimary,
        textContentColor = Color.White,
        tonalElevation = 12.dp,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = device.emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = (device.name ?: "?").uppercase(), fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onFocus,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isVibed) Color.White.copy(alpha = 0.1f) else StealthPrimary,
                        contentColor = if (isVibed) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.FilterCenterFocus, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isVibed) "UNFOCUS" else "FOCUS", fontWeight = FontWeight.Black)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onVibe,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTied) StealthRose.copy(alpha = 0.2f) else StealthRose,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = if (isTied) "TIED" else "VIBE", fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                    
                    Button(
                        onClick = onWhisper,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StealthPrimary.copy(alpha = 0.15f),
                            contentColor = StealthPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, StealthPrimary.copy(alpha = 0.3f))
                    ) {
                        Icon(imageVector = Icons.Rounded.Hearing, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "WHISPER", fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isBlocked) {
                        TextButton(onClick = onUnblock, modifier = Modifier.weight(1f)) {
                            Text("UNBLOCK", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                        }
                    } else {
                        TextButton(onClick = onBlock, modifier = Modifier.weight(1f)) {
                            Text("BLOCK", color = Color.Red.copy(alpha = 0.6f), fontSize = 10.sp)
                        }
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("CLOSE", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun BlukitInput(
    airIsStill: Boolean,
    isReadOnly: Boolean = false,
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    vibeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderAlpha by animateFloatAsState(if (isFocused) 0.6f else 0.1f, label = "BorderGlow")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(28.dp))
            .border(1.dp, Color.White.copy(alpha = borderAlpha), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(2.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(26.dp))
                    .background(if (isReadOnly) StealthPrimary.copy(alpha = 0.02f) else Color.White.copy(alpha = 0.03f))
                    .padding(end = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).padding(start = 16.dp), contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) { 
                            Text(
                                text = if (isReadOnly) "FILTER MODE: READ ONLY" else "TYPE TO SPREAD VIBES", 
                                fontSize = 8.sp, 
                                fontWeight = FontWeight.Black, 
                                letterSpacing = 1.sp, 
                                color = if (isReadOnly) StealthPrimary.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.4f)
                            ) 
                        }
                        BasicTextField(
                            value = if (isReadOnly) "" else value, 
                            onValueChange = { if (!isReadOnly) onValueChange(it) }, 
                            modifier = Modifier.fillMaxWidth().testTag("SendVibeInput"), 
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontSize = 13.sp), 
                            interactionSource = interactionSource, 
                            cursorBrush = SolidColor(StealthPrimary), 
                            singleLine = true,
                            enabled = !isReadOnly
                        )
                    }
                    if (vibeCount > 0 && !isReadOnly) { 
                        Box(modifier = Modifier.padding(horizontal = 8.dp).background(StealthPrimary.copy(alpha = 0.15f), CircleShape).border(0.5.dp, StealthPrimary.copy(alpha = 0.3f), CircleShape).padding(horizontal = 6.dp, vertical = 2.dp)) { 
                            Text(text = vibeCount.toString(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black, color = StealthPrimary)) 
                        } 
                    }
                    Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(if (value.isNotBlank() && !isReadOnly) Brush.linearGradient(listOf(StealthPrimary, StealthAmber)) else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.02f)))).clickable(enabled = value.isNotBlank() && !isReadOnly) { onSend() }.testTag("SendVibeButton"), contentAlignment = Alignment.Center) { 
                        Icon(
                            imageVector = if (isReadOnly) Icons.Rounded.Lock else Icons.AutoMirrored.Rounded.Send, 
                            contentDescription = if (isReadOnly) "Locked" else "Send", 
                            tint = if (value.isNotBlank() && !isReadOnly) Color.Black else Color.White.copy(alpha = 0.2f), 
                            modifier = Modifier.size(20.dp)
                        ) 
                    }
                }
            }
        }
    }
}

@Composable
fun UserPersona(
    nickname: String, emoji: String, airIsStill: Boolean, onNicknameChange: (String) -> Unit, focusRequester: FocusRequester
) {
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
                if (localNickname.isEmpty()) {
                    Text(
                        text = "SET NAME",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        color = StealthAmber.copy(alpha = 0.6f),
                        letterSpacing = 0.5.sp
                    )
                }
                BasicTextField(
                    value = localNickname, 
                    onValueChange = { localNickname = it; onNicknameChange(it.ifBlank { "?" }) }, 
                    modifier = Modifier.widthIn(min = 10.dp, max = 60.dp).focusRequester(focusRequester).testTag("IdentityVibeInput"), 
                    textStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (airIsStill) Color.Red else if (isUnknown) StealthAmber else StealthPrimary, textAlign = TextAlign.Center, letterSpacing = 0.5.sp), 
                    singleLine = true, 
                    cursorBrush = SolidColor(StealthPrimary)
                )
            }
            Text(text = " (YOU)", fontSize = 5.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.15f), letterSpacing = 0.5.sp)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VibeNode(
    device: P2PDevice, isVibed: Boolean, isSelected: Boolean, isPeerVibed: Boolean, onlyTies: Boolean, activeBubble: String? = null, modifier: Modifier = Modifier, onClick: () -> Unit, onLongClick: () -> Unit
) {
    val nodeSize = if (device.proximityFactor > 0.8f) 60.dp else 48.dp
    Box(modifier = modifier.size(nodeSize * 1.5f), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.size(nodeSize).background(when { isSelected -> Color.White.copy(alpha = 0.1f); isVibed -> StealthRose.copy(alpha = 0.1f); isPeerVibed -> StealthAmber.copy(alpha = 0.1f); else -> Color(0xFF0D1017) }, CircleShape).border(if (isSelected || isVibed || isPeerVibed) 1.5.dp else 1.dp, when { isSelected -> Color.White; isVibed -> StealthRose; isPeerVibed -> StealthAmber; else -> Color.White.copy(alpha = 0.1f) }, CircleShape).clip(CircleShape).combinedClickable(onClick = onClick, onLongClick = onLongClick).testTag("PersonaNode_${device.id}"),
            contentAlignment = Alignment.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = device.emoji, fontSize = (nodeSize.value / 2.5f).sp)
                val displayName = (device.name ?: "?").take(7).uppercase()
                Text(text = if (!onlyTies && isVibed) "$displayName+" else displayName, fontSize = 9.sp, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Black, letterSpacing = 0.5.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp).testTag("PersonaNodeName"))
                Icon(painter = painterResource(id = R.drawable.ic_blukit_logo), contentDescription = null, tint = if (isVibed) StealthRose else if (isPeerVibed) StealthAmber else StealthPrimary, modifier = Modifier.size(10.dp).align(Alignment.BottomEnd).offset(x = (-2).dp, y = (-2).dp).background(Color.Black, CircleShape).padding(1.dp))
            }
        }
        activeBubble?.let { Box(modifier = Modifier.offset(y = (-40).dp)) { Box(modifier = Modifier.background(StealthPrimary.copy(alpha = 0.9f), RoundedCornerShape(12.dp, 12.dp, 12.dp, 2.dp)).padding(horizontal = 10.dp, vertical = 6.dp).widthIn(max = 130.dp)) { Text(text = it, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, maxLines = 2, overflow = TextOverflow.Ellipsis) } } }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VibeDot(
    device: P2PDevice, modifier: Modifier = Modifier, onClick: () -> Unit, onLongClick: () -> Unit = {}
) {
    val dotAlpha by rememberInfiniteTransition(label = "DotAnim").animateFloat(initialValue = 0.3f, targetValue = 0.7f, animationSpec = infiniteRepeatable(tween(2000 + (device.proximityFactor * 1000).toInt(), easing = LinearEasing), RepeatMode.Reverse), label = "Alpha")
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick).padding(2.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(16.dp)) {
            Surface(modifier = Modifier.size(12.dp), shape = CircleShape, color = StealthPrimary.copy(alpha = 0.1f * dotAlpha), border = BorderStroke(0.5.dp, StealthPrimary.copy(alpha = dotAlpha))) {
                Box(contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Rounded.Person, contentDescription = null, tint = StealthPrimary.copy(alpha = dotAlpha), modifier = Modifier.size(8.dp)) }
            }
            Icon(painter = painterResource(id = R.drawable.ic_blukit_logo), contentDescription = null, tint = StealthPrimary.copy(alpha = dotAlpha), modifier = Modifier.size(7.dp).align(Alignment.BottomEnd).offset(x = 1.dp, y = 1.dp).background(Color.Black, CircleShape).padding(0.5.dp))
        }
        Text(text = (device.name ?: "?").take(5).uppercase(), fontSize = 6.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.3f), letterSpacing = 0.5.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UnifiedPersonaCloud(
    devices: List<P2PDevice>,
    vibedPeers: Set<String>,
    connectedLinks: Set<String>,
    activeBubbles: List<BubbleData>,
    onDeviceClick: (P2PDevice) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit = {},
    isVertical: Boolean = false,
    userNickname: String = "",
    userEmoji: String = "",
    onUserNicknameChange: (String) -> Unit = {},
    userFocusRequester: FocusRequester? = null,
    airIsStill: Boolean = false
) {
    val activeSenders = remember(activeBubbles) { activeBubbles.map { it.senderId }.toSet() }
    val sortedDevices = remember(devices, activeSenders, vibedPeers, connectedLinks) {
        devices.sortedWith(
            compareByDescending<P2PDevice> { 
                it.id in activeSenders || it.persistentId in activeSenders || 
                it.id in connectedLinks || it.persistentId in vibedPeers || it.id in vibedPeers
            }.thenByDescending { it.proximityFactor }
        ).take(if (isVertical) 16 else 24)
    }

    Box(
        modifier = (if (isVertical) Modifier.fillMaxHeight().width(64.dp) else Modifier.fillMaxWidth())
            .padding(horizontal = 2.dp)
            .background(Color.White.copy(alpha = 0.01f), RoundedCornerShape(12.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
    ) {
        if (isVertical) {
            FlowColumn(
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .fillMaxHeight()
                    .testTag("PersonaCloudColumn"),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
                maxItemsInEachColumn = 11 // Adjusted for user persona
            ) {
                // ANCHOR: User Persona
                if (userFocusRequester != null) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .clickable { userFocusRequester.requestFocus() },
                        contentAlignment = Alignment.Center
                    ) {
                        UserPersona(
                            nickname = userNickname,
                            emoji = userEmoji,
                            airIsStill = airIsStill,
                            onNicknameChange = onUserNicknameChange,
                            focusRequester = userFocusRequester
                        )
                    }
                }

                sortedDevices.forEach { device ->
                    PersonaCloudItem(
                        device = device,
                        activeSenders = activeSenders,
                        connectedLinks = connectedLinks,
                        vibedPeers = vibedPeers,
                        onDeviceClick = onDeviceClick,
                        onDeviceLongClick = onDeviceLongClick,
                        isVertical = true
                    )
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "TAP TO FOCUS • LONG PRESS TO WHISPER",
                    fontSize = 5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.15f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                FlowRow(
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .fillMaxWidth()
                        .testTag("PersonaCloudRow"),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Center,
                    maxItemsInEachRow = 10 // Adjusted for user persona
                ) {
                    // ANCHOR: User Persona
                    if (userFocusRequester != null) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .clickable { userFocusRequester.requestFocus() },
                            contentAlignment = Alignment.Center
                        ) {
                            UserPersona(
                                nickname = userNickname,
                                emoji = userEmoji,
                                airIsStill = airIsStill,
                                onNicknameChange = onUserNicknameChange,
                                focusRequester = userFocusRequester
                            )
                        }
                    }

                    sortedDevices.forEach { device ->
                        PersonaCloudItem(
                            device = device,
                            activeSenders = activeSenders,
                            connectedLinks = connectedLinks,
                            vibedPeers = vibedPeers,
                            onDeviceClick = onDeviceClick,
                            onDeviceLongClick = onDeviceLongClick,
                            isVertical = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonaCloudItem(
    device: P2PDevice,
    activeSenders: Set<String>,
    connectedLinks: Set<String>,
    vibedPeers: Set<String>,
    onDeviceClick: (P2PDevice) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit,
    isVertical: Boolean
) {
    val isActive = device.id in activeSenders || device.persistentId in activeSenders
    val isTied = device.id in connectedLinks
    val isVibed = device.persistentId in vibedPeers || device.id in vibedPeers
    
    if (isActive || isVibed || isTied) {
        VibeNode(
            device = device,
            isVibed = isTied,
            isSelected = false,
            isPeerVibed = isVibed,
            onlyTies = false,
            modifier = Modifier.size(if (isVertical) 38.dp else 42.dp).padding(2.dp),
            onClick = { onDeviceClick(device) },
            onLongClick = { onDeviceLongClick(device) }
        )
    } else {
        VibeDot(
            device = device,
            modifier = Modifier.padding(2.dp),
            onClick = { onDeviceClick(device) },
            onLongClick = { onDeviceLongClick(device) }
        )
    }
}

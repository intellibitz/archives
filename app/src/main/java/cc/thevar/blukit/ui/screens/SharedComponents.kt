package cc.thevar.blukit.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.drawscope.rotate as drawRotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
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

data class PersonaConnectionPoints(
    val uph: Offset? = null,
    val field: Offset? = null,
    val ticker: Offset? = null
)

val LocalPersonaCoordinates = compositionLocalOf { mutableStateMapOf<String, PersonaConnectionPoints>() }

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
fun BlukitHarmonyTopBar(
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
    onToggleStealth: (Boolean) -> Unit,
    onToggleLowPower: (Boolean) -> Unit,
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

    val localContext = LocalContext.current

    val isWeak = userCount == 0 && !isBluetoothOff && !isLocationOff
    val isStill = isBluetoothOff || isLocationOff
    val barColor = when { 
        isStill -> Color.Red.copy(alpha = 0.15f)
        isPermissionMissing -> Color(0xFFF4511E).copy(alpha = 0.15f)
        else -> Color.White.copy(alpha = 0.05f) 
    }

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .background(barColor, RoundedCornerShape(20.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
        // ROW 1: Environment (Left) | Branding (Center) | Radios (Right)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            // LEFT: DARK, ECO
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Start) {
                EnvironmentToggle(label = "DARK", checked = isStealthMode, onCheckedChange = onToggleStealth)
                EnvironmentToggle(label = "ECO", checked = lowPowerMode, onCheckedChange = onToggleLowPower)
            }

            // CENTER: Branding + Privacy
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "V I", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp))
                    Spacer(modifier = Modifier.width(6.dp))
                    RadioB(modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "E S", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable { 
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/intellibitz/blukit/blob/main/PRIVACY_POLICY.md"))
                            localContext.startActivity(intent)
                        }
                        .padding(horizontal = 6.dp, vertical = 1.dp), 
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PRIVACY", 
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 6.sp, 
                            fontWeight = FontWeight.Black, 
                            color = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }
            }

            // RIGHT: Radios + Alerts Below
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                        }
                    }
                    StatusIcon(icon = Icons.Rounded.Bluetooth, isOn = !isBluetoothOff, isWeak = isWeak, isPermissionMissing = isPermissionMissing, onClick = onAwakenBluetooth)
                    StatusIcon(icon = Icons.Rounded.Wifi, isOn = !isWifiOff, isWeak = isWeak, isPermissionMissing = isPermissionMissing, onClick = onAwakenWifi)
                    StatusIcon(icon = Icons.Rounded.LocationOn, isOn = !isLocationOff, isWeak = isWeak, isPermissionMissing = isPermissionMissing, onClick = onAwakenLocation)
                }
                
                // Alerts moved below radio signals
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
        }

        // ROW 2: Screen Title (Only if not main UPH anchors)
        if (title != "ALL" && title != "VIBES") {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = StealthPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = StealthPrimary.copy(alpha = 0.8f),
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
        
        // Border Decorator
        Surface(
            color = Color.Black,
            modifier = Modifier.offset(y = 1.dp)
        ) {
            Text(
                text = "HARMONY",
                fontSize = 6.sp,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.2f),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
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
    isSelected: Boolean,
    isRequesting: Boolean = false,
    onFocus: () -> Unit,
    onVibe: () -> Unit,
    onWhisper: () -> Unit,
    onSelect: () -> Unit,
    onAccept: () -> Unit = {},
    onDeny: () -> Unit = {},
    onDisconnect: () -> Unit = {},
    onBlock: () -> Unit,
    onUnblock: () -> Unit,
    onIdentify: () -> Unit = {},
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A0C14),
        titleContentColor = StealthPrimary,
        textContentColor = Color.White,
        tonalElevation = 8.dp,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = device.emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = (device.name ?: "?").uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isRequesting) {
                    MenuActionItem(icon = Icons.Rounded.CheckCircle, label = "ACCEPT VIBE REQUEST", color = StealthPrimary, onClick = { onAccept(); onDismiss() })
                    MenuActionItem(icon = Icons.Rounded.Cancel, label = "DENY VIBE REQUEST", color = Color.White.copy(alpha = 0.4f), onClick = { onDeny(); onDismiss() })
                }
                
                if (isTied) {
                    MenuActionItem(icon = Icons.Rounded.LinkOff, label = "DISCONNECT VIBE", color = StealthRose, onClick = { onDisconnect(); onDismiss() })
                } else if (!isRequesting) {
                    MenuActionItem(icon = Icons.Rounded.Flare, label = "DIRECT VIBE REQUEST", color = StealthRose, onClick = { onVibe(); onDismiss() })
                }

                MenuActionItem(icon = Icons.Rounded.FilterCenterFocus, label = if (isVibed) "UNFOCUS PERSONA" else "FOCUS PERSONA", color = StealthPrimary, onClick = { onFocus(); onDismiss() })
                MenuActionItem(icon = if (isSelected) Icons.Rounded.RemoveCircleOutline else Icons.Rounded.AddCircleOutline, label = if (isSelected) "REMOVE FROM SELECTION" else "ADD TO VIBE SELECTION", color = if (isSelected) Color.White.copy(alpha = 0.6f) else StealthPrimary, onClick = { onSelect(); onDismiss() })
                MenuActionItem(icon = Icons.Rounded.ChatBubbleOutline, label = "SECURE WHISPER", color = StealthPrimary, onClick = { onWhisper(); onDismiss() })
                MenuActionItem(icon = Icons.Rounded.Radar, label = "SEARCH / IDENTIFY", color = StealthPrimary.copy(alpha = 0.8f), onClick = { onIdentify(); onDismiss() })
                
                if (isBlocked) {
                    MenuActionItem(icon = Icons.Rounded.Block, label = "UNBLOCK USER", color = Color.Gray, onClick = { onUnblock(); onDismiss() })
                } else {
                    MenuActionItem(icon = Icons.Rounded.Block, label = "BLOCK USER", color = Color.Red, onClick = { onBlock(); onDismiss() })
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("CLOSE", color = Color.White.copy(alpha = 0.4f)) } }
    )
}

@Composable
private fun MenuActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
        }
    }
}

@Composable
fun UserPersona(
    nickname: String,
    emoji: String,
    airIsStill: Boolean,
    onNicknameChange: (String) -> Unit,
    focusRequester: FocusRequester
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
    selectedDevices: Set<String> = emptySet(),
    activeBubbles: List<BubbleData>,
    onDeviceClick: (P2PDevice) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit = {},
    isVertical: Boolean = false,
    userNickname: String = "",
    userEmoji: String = "",
    onUserNicknameChange: (String) -> Unit = {},
    userFocusRequester: FocusRequester? = null,
    airIsStill: Boolean = false,
    isNoiseFilterActive: Boolean = false,
    vibedPeersCount: Int = 0,
    onClearHistory: () -> Unit = {},
    showFilter: Boolean = false,
    currentRoute: Route = Route.Blukit,
    subjectId: String? = null,
    onNavigate: (Route) -> Unit = {}
) {
    val coordinates = LocalPersonaCoordinates.current
    val activeSenders = remember(activeBubbles) { activeBubbles.map { it.senderId }.toSet() }
    
    var showClearHistoryDialog by remember { mutableStateOf(false) }
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
            .border(0.5.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp)),
        contentAlignment = if (isVertical) Alignment.CenterStart else Alignment.BottomCenter
    ) {
        if (isVertical) {
            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ANCHOR 1: ALL Toggle (Top)
                val isAllSelected = currentRoute is Route.Blukit
                val isFocusAll = vibedPeersCount == 0 && subjectId == null
                val isGlowingAll = isAllSelected && isFocusAll
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (isAllSelected && isFocusAll) {
                        Text(text = "ALL VIBES", fontSize = 6.sp, fontWeight = FontWeight.Black, color = StealthPrimary, modifier = Modifier.padding(end = 4.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { onNavigate(Route.Blukit) },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isGlowingAll) StealthPrimary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f))
                                .border(1.dp, if (isGlowingAll) StealthPrimary else Color.Transparent, RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Groups,
                                contentDescription = "All",
                                tint = if (isGlowingAll) StealthPrimary else Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(text = "ALL", fontSize = 5.sp, fontWeight = FontWeight.Black, color = if (isGlowingAll) StealthPrimary else Color.White.copy(alpha = 0.2f), letterSpacing = 0.5.sp)
                    }
                }

                // ANCHOR 2: VIBES Toggle
                Spacer(modifier = Modifier.height(8.dp))
                val isVibesSelected = currentRoute is Route.Vibes || currentRoute is Route.VibeDetail
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (isVibesSelected) {
                        val vibesStatus = if (currentRoute is Route.VibeDetail) "SECURE VIBE" else "VIBES"
                        Text(text = vibesStatus, fontSize = 6.sp, fontWeight = FontWeight.Black, color = StealthRose, modifier = Modifier.padding(end = 4.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { onNavigate(if (isVibesSelected) Route.Blukit else Route.Vibes) }, 
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isVibesSelected) StealthRose.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f))
                                .border(1.dp, if (isVibesSelected) StealthRose else Color.Transparent, RoundedCornerShape(12.dp))
                                .combinedClickable(
                                    onClick = { onNavigate(if (isVibesSelected) Route.Blukit else Route.Vibes) },
                                    onLongClick = { showClearHistoryDialog = true }
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Flare, 
                                contentDescription = "Vibes", 
                                tint = if (isVibesSelected) StealthRose else Color.White.copy(alpha = 0.4f), 
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(text = "VIBES", fontSize = 5.sp, fontWeight = FontWeight.Black, color = if (isVibesSelected) StealthRose else Color.White.copy(alpha = 0.2f), letterSpacing = 0.5.sp)
                    }
                }

                // ANCHOR 3: RESET (User Persona)
                if (userFocusRequester != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .clickable { userFocusRequester.requestFocus() }
                                    .onGloballyPositioned { 
                                        val current = coordinates["YOU"] ?: PersonaConnectionPoints()
                                        coordinates["YOU"] = current.copy(uph = it.positionInRoot())
                                    },
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
                            Text(text = "RESET", fontSize = 5.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.2f), letterSpacing = 0.5.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Others (Scrollable)
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("PersonaCloudColumn"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top)
                ) {
                    items(sortedDevices.size) { index ->
                        val device = sortedDevices[index]
                        val isSelected = device.id in selectedDevices
                        val isFocused = device.persistentId in vibedPeers || device.id in vibedPeers || device.id in connectedLinks || device.id == subjectId || device.persistentId == subjectId
                        val isSelectiveFocus = vibedPeers.isNotEmpty() || subjectId != null
                        val dimAlpha = if (isSelectiveFocus && !isFocused) 0.1f else 1f
                        
                        PersonaCloudItem(
                            device = device,
                            activeSenders = activeSenders,
                            connectedLinks = connectedLinks,
                            vibedPeers = vibedPeers,
                            isSelected = isSelected,
                            onDeviceClick = onDeviceClick,
                            onDeviceLongClick = onDeviceLongClick,
                            isVertical = true,
                            modifier = Modifier
                                .graphicsLayer { alpha = dimAlpha }
                                .onGloballyPositioned { 
                                    val current = coordinates[device.id] ?: PersonaConnectionPoints()
                                    coordinates[device.id] = current.copy(uph = it.positionInRoot())
                                }
                        )
                    }
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
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                        .fillMaxWidth()
                        .testTag("PersonaCloudRow"),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 8
                ) {
                    sortedDevices.forEach { device ->
                        val isSelected = device.id in selectedDevices
                        val isFocused = device.persistentId in vibedPeers || device.id in vibedPeers || device.id in connectedLinks || device.id == subjectId || device.persistentId == subjectId
                        val isBroadFocus = isNoiseFilterActive && vibedPeers.isEmpty() && subjectId == null
                        val dimAlpha = if (isNoiseFilterActive && !isFocused && !isBroadFocus) 0.1f else 1f
                        
                        PersonaCloudItem(
                            device = device,
                            activeSenders = activeSenders,
                            connectedLinks = connectedLinks,
                            vibedPeers = vibedPeers,
                            isSelected = isSelected,
                            onDeviceClick = { onDeviceClick(device) },
                            onDeviceLongClick = { onDeviceLongClick(device) },
                            isVertical = false,
                            modifier = Modifier
                                .graphicsLayer { alpha = dimAlpha }
                                .onGloballyPositioned { 
                                    val current = coordinates[device.id] ?: PersonaConnectionPoints()
                                    coordinates[device.id] = current.copy(uph = it.positionInRoot())
                                }
                        )
                    }
                }
            }
        }
        
        // Border Decorator
        Surface(
            color = Color.Black,
            modifier = Modifier.then(if (isVertical) Modifier.rotate(-90f).offset(y = (-32).dp) else Modifier.offset(y = 1.dp))
        ) {
            Text(
                text = if (isVertical) "PERSONA HUB" else "CROWD HUB",
                fontSize = 6.sp,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.2f),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
        
        if (showClearHistoryDialog) { ConfirmationDialog(title = "CLEAR VIBES?", text = "THIS WILL PERMANENTLY REMOVE YOUR SHARED HISTORY.", onConfirm = { onClearHistory(); showClearHistoryDialog = false }, onDismiss = { showClearHistoryDialog = false }) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PersonaCloudItem(
    device: P2PDevice,
    activeSenders: Set<String>,
    connectedLinks: Set<String>,
    vibedPeers: Set<String>,
    isSelected: Boolean = false,
    onDeviceClick: (P2PDevice) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit,
    isVertical: Boolean,
    modifier: Modifier = Modifier
) {
    val isVibed = device.persistentId in vibedPeers || device.id in vibedPeers
    val isTied = device.id in connectedLinks
    val isActive = device.id in activeSenders || device.persistentId in activeSenders
    
    val pulseAlpha by rememberInfiniteTransition(label = "ActivePulse").animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse),
        label = "Alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(if (isVertical) 52.dp else 44.dp)
            .combinedClickable(
                onClick = { onDeviceClick(device) },
                onLongClick = { onDeviceLongClick(device) }
            )
            .padding(2.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(if (isVertical) 32.dp else 24.dp)) {
            if (isActive) {
                Box(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = pulseAlpha }.background(StealthPrimary.copy(alpha = 0.2f), CircleShape))
            }
            Surface(
                modifier = Modifier.size(if (isVertical) 28.dp else 20.dp),
                shape = CircleShape,
                color = when {
                    isSelected -> Color.White.copy(alpha = 0.2f)
                    isTied -> StealthRose.copy(alpha = 0.2f)
                    isVibed -> StealthPrimary.copy(alpha = 0.2f)
                    else -> Color.White.copy(alpha = 0.05f)
                },
                border = BorderStroke(
                    if (isSelected || isTied || isVibed) 1.dp else 0.5.dp,
                    when {
                        isSelected -> Color.White
                        isTied -> StealthRose
                        isVibed -> StealthPrimary
                        else -> Color.White.copy(alpha = 0.1f)
                    }
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = device.emoji, fontSize = if (isVertical) 14.sp else 10.sp)
                }
            }
        }
        Text(
            text = (device.name ?: "?").take(5).uppercase(),
            fontSize = if (isVertical) 7.sp else 6.sp,
            fontWeight = FontWeight.Black,
            color = if (isTied) StealthRose else if (isVibed) StealthPrimary else Color.White.copy(alpha = 0.3f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
    modifier: Modifier = Modifier
) {
    val borderColor = if (airIsStill) Color.Red.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)
    val decoratorText = if (isReadOnly) "FOCUS VIBES: READ ONLY" else if (isFilterActive) "VIBE TO ALL" else "SPREAD A VIBE"
    
    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = !isReadOnly,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                cursorBrush = SolidColor(StealthPrimary),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = if (isReadOnly) "FILTERED" else if (isFilterActive) "TYPE TO ALL..." else "TYPE A VIBE...",
                            color = Color.White.copy(alpha = 0.3f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    innerTextField()
                }
            )
            if (vibeCount > 0) {
                Text(
                    text = vibeCount.toString(),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = StealthPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            IconButton(
                onClick = onSend,
                enabled = value.isNotBlank() && !isReadOnly,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send",
                    tint = if (value.isNotBlank()) StealthPrimary else Color.White.copy(alpha = 0.2f)
                )
            }
        }
        
        // Border Decorator
        Surface(
            color = Color.Black,
            modifier = Modifier
                .offset(y = 1.dp)
                .padding(bottom = 0.dp)
        ) {
            Text(
                text = decoratorText,
                fontSize = 6.sp,
                fontWeight = FontWeight.Black,
                color = (if (airIsStill) Color.Red else StealthPrimary).copy(alpha = 0.6f),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
    }
}

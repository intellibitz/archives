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
fun BlukitTopTitle(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SPREAD",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            RadioB(modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = "VIBES",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = StealthPrimary.copy(alpha = 0.6f),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = StealthPrimary.copy(alpha = 0.7f)
                )
            )
        }
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
    isVertical: Boolean = false
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
                maxItemsInEachColumn = 10
            ) {
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
                    maxItemsInEachRow = 9
                ) {
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

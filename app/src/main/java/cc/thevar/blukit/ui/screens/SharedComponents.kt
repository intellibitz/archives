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
        
        // Draw the vertical stem of 'B'
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(centerX - 4.dp.toPx(), centerY - 8.dp.toPx()),
            end = androidx.compose.ui.geometry.Offset(centerX - 4.dp.toPx(), centerY + 8.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        
        // Draw top signal arc (upper loop of B)
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(centerX - 4.dp.toPx(), centerY - 8.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(8.dp.toPx(), 8.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        
        // Draw bottom signal arc (lower loop of B)
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(centerX - 4.dp.toPx(), centerY),
            size = androidx.compose.ui.geometry.Size(10.dp.toPx(), 8.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        
        // Add tiny 'signal' dots or extra lines to emphasize the radio theme
        drawCircle(
            color = color,
            radius = 1.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(centerX + 6.dp.toPx(), centerY - 4.dp.toPx())
        )
        drawCircle(
            color = color,
            radius = 1.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(centerX + 8.dp.toPx(), centerY + 4.dp.toPx())
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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "DISTANCE: ${device.proximityLabel.uppercase()}", 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f)
                )
                
                Button(
                    onClick = onFocus,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isVibed) Color.White.copy(alpha = 0.1f) else StealthPrimary,
                        contentColor = if (isVibed) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = if (isVibed) "UNFOCUS" else "FOCUS", fontWeight = FontWeight.Black)
                }

                Button(
                    onClick = onVibe,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTied) StealthRose.copy(alpha = 0.2f) else StealthRose,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = if (isTied) "ALREADY VIBED" else "VIBE", fontWeight = FontWeight.Black)
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                if (isBlocked) {
                    Button(
                        onClick = onUnblock,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.3f), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "UNBLOCK USER", fontWeight = FontWeight.Black)
                    }
                } else {
                    Button(
                        onClick = onBlock,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.6f), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "BLOCK USER", fontWeight = FontWeight.Black)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = Color.White.copy(alpha = 0.4f))
            }
        }
    )
}

@Composable
fun BlukitInput(
    nickname: String,
    emoji: String,
    airIsStill: Boolean,
    onNicknameChange: (String) -> Unit,
    personaFocusRequester: FocusRequester,
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    vibeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    // Lightweight border animation
    val borderAlpha by animateFloatAsState(
        if (isFocused) 0.6f else 0.1f,
        label = "BorderGlow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(32.dp))
            .border(1.dp, Color.White.copy(alpha = borderAlpha), RoundedCornerShape(32.dp))
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // SEGMENT 1: IDENTITY (Persona) - Unified Column
            Box(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(30.dp, 4.dp, 4.dp, 30.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { personaFocusRequester.requestFocus() },
                contentAlignment = Alignment.Center
            ) {
                UserPersona(
                    nickname = nickname,
                    emoji = emoji,
                    airIsStill = airIsStill,
                    onNicknameChange = onNicknameChange,
                    focusRequester = personaFocusRequester
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // SEGMENT 2: VIBE ACTION (Custom BasicTextField)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp, 30.dp, 30.dp, 4.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = "TYPE TO SPREAD VIBES",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth().testTag("SendVibeInput"),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontSize = 13.sp
                        ),
                        interactionSource = interactionSource,
                        cursorBrush = SolidColor(StealthPrimary),
                        singleLine = true
                    )
                }

                if (vibeCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .background(StealthPrimary.copy(alpha = 0.15f), CircleShape)
                            .border(0.5.dp, StealthPrimary.copy(alpha = 0.3f), CircleShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = vibeCount.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = StealthPrimary
                            )
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (value.isNotBlank()) 
                                Brush.linearGradient(listOf(StealthPrimary, StealthAmber))
                            else 
                                Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.02f)))
                        )
                        .clickable(enabled = value.isNotBlank()) { onSend() }
                        .testTag("SendVibeButton"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send",
                        tint = if (value.isNotBlank()) Color.Black else Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UserPersona(
    nickname: String,
    emoji: String,
    airIsStill: Boolean,
    onNicknameChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    var localNickname by remember(nickname) { mutableStateOf(nickname) }
    val isUnknown = nickname == "?"

    val infiniteTransition = rememberInfiniteTransition(label = "NodeAnim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.1f, // Reduced for battery
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Pulse"
    )

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight().padding(horizontal = 4.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(30.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .background((if (isUnknown) StealthAmber else StealthPrimary).copy(alpha = 0.1f), CircleShape)
            )
            
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFF0A0C14), CircleShape)
                    .border(1.dp, if (isUnknown) StealthAmber.copy(alpha = 0.4f) else StealthPrimary.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 11.sp)
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_blukit_logo),
                contentDescription = null,
                tint = if (isUnknown) StealthAmber else StealthPrimary,
                modifier = Modifier
                    .size(9.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .background(Color.Black, CircleShape)
                    .padding(1.dp)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = localNickname,
                onValueChange = { newName ->
                    localNickname = newName
                    onNicknameChange(newName.ifBlank { "?" })
                },
                modifier = Modifier
                    .widthIn(max = 60.dp)
                    .focusRequester(focusRequester)
                    .testTag("IdentityVibeInput"),
                textStyle = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = if (airIsStill) Color.Red else if (isUnknown) Color.White.copy(alpha = 0.4f) else StealthPrimary,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(StealthPrimary)
            )
            Text(
                text = " (YOU)",
                fontSize = 5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.15f),
                letterSpacing = 0.5.sp
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VibeNode(
    device: P2PDevice, 
    isVibed: Boolean,
    isSelected: Boolean,
    isPeerVibed: Boolean,
    onlyTies: Boolean,
    activeBubble: String? = null, 
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val nodeSize = if (device.proximityFactor > 0.8f) 60.dp else 48.dp
    
    Box(modifier = modifier.size(nodeSize * 1.5f), contentAlignment = Alignment.Center) {
        // Optimized background
        Box(
            modifier = Modifier
                .size(nodeSize)
                .background(
                    when {
                        isSelected -> Color.White.copy(alpha = 0.1f)
                        isVibed -> StealthRose.copy(alpha = 0.1f)
                        isPeerVibed -> StealthAmber.copy(alpha = 0.1f)
                        else -> Color(0xFF0D1017)
                    },
                    CircleShape
                )
                .border(
                    if (isSelected || isVibed || isPeerVibed) 1.5.dp else 1.dp,
                    when {
                        isSelected -> Color.White
                        isVibed -> StealthRose
                        isPeerVibed -> StealthAmber
                        else -> Color.White.copy(alpha = 0.1f)
                    },
                    CircleShape
                )
                .clip(CircleShape)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .testTag("PersonaNode_${device.id}"),
            contentAlignment = Alignment.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                val mediumIcon = when (device.medium) {
                    P2PDevice.ConnectionMedium.BLUETOOTH -> Icons.Rounded.Bluetooth
                    P2PDevice.ConnectionMedium.WIFI -> Icons.Rounded.Wifi
                    P2PDevice.ConnectionMedium.LOCATION -> Icons.Rounded.LocationOn
                }

                Icon(
                    imageVector = if (device.isConnecting || device.isLinkPending) Icons.Rounded.Sync else if (isSelected) Icons.Rounded.CheckCircle else mediumIcon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier.size((nodeSize.value / 1.4f).dp)
                )
                
                val displayName = (device.name ?: "?").take(4).uppercase()
                Text(
                    text = if (!onlyTies && isVibed) "$displayName+" else displayName,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.testTag("PersonaNodeName")
                )
            }
        }

        activeBubble?.let {
            Box(modifier = Modifier.offset(y = (-40).dp)) {
                Box(
                    modifier = Modifier
                        .background(StealthPrimary.copy(alpha = 0.9f), RoundedCornerShape(12.dp, 12.dp, 12.dp, 2.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .widthIn(max = 130.dp)
                ) {
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VibeDot(
    device: P2PDevice,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "DotAnim")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(2000 + (device.proximityFactor * 1000).toInt(), easing = LinearEasing), RepeatMode.Reverse),
        label = "Alpha"
    )

    Box(
        modifier = modifier
            .size(32.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(12.dp),
            shape = CircleShape,
            color = StealthPrimary.copy(alpha = 0.1f * dotAlpha),
            border = BorderStroke(0.5.dp, StealthPrimary.copy(alpha = dotAlpha))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = StealthPrimary.copy(alpha = dotAlpha),
                    modifier = Modifier.size(8.dp)
                )
            }
        }
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
    onDeviceLongClick: (P2PDevice) -> Unit = {}
) {
    val activeSenders = remember(activeBubbles) { activeBubbles.map { it.senderId }.toSet() }
    val sortedDevices = remember(devices, activeSenders) {
        devices.sortedWith(
            compareByDescending<P2PDevice> { it.id in activeSenders || it.persistentId in activeSenders }
                .thenByDescending { it.proximityFactor }
        ).take(18)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .background(Color.White.copy(alpha = 0.01f), RoundedCornerShape(12.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "TAP TO FOCUS • LONG PRESS TO VIBE",
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
                    val isActive = device.id in activeSenders || device.persistentId in activeSenders
                    val isTied = device.id in connectedLinks
                    val isVibed = device.persistentId in vibedPeers || device.id in vibedPeers
                    
                    if (isActive) {
                        VibeNode(
                            device = device,
                            isVibed = isTied,
                            isSelected = false,
                            isPeerVibed = isVibed,
                            onlyTies = false,
                            modifier = Modifier.size(42.dp).padding(2.dp),
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
            }
        }
    }
}

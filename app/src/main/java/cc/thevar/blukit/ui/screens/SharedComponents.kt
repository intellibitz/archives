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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_blukit_logo),
                contentDescription = null,
                tint = StealthPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SPREAD VIBES",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = StealthPrimary
                )
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
fun StudentOptionsMenu(
    device: P2PDevice,
    isVibed: Boolean,
    isTied: Boolean,
    onFocus: () -> Unit,
    onVibe: () -> Unit,
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
                    Text(text = if (isVibed) "REMOVE FOCUS" else "FOCUS (BLOSSOM)", fontWeight = FontWeight.Black)
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
                    Text(text = if (isTied) "ALREADY VIBED" else "VIBE (SECURE LINK)", fontWeight = FontWeight.Black)
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
fun BlukitHeartbeat(
    energy: Float,
    rotation: Float,
    lowPowerMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Heartbeat")
    
    val vibeScale by infiniteTransition.animateFloat(
        initialValue = 0.9f + (energy * 0.1f),
        targetValue = if (lowPowerMode) 1.05f else 1.25f + (energy * 0.3f), 
        animationSpec = infiniteRepeatable(
            animation = tween(if (lowPowerMode) 3000 else 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = if (lowPowerMode) 0.1f else 0.2f,
        targetValue = if (lowPowerMode) 0.3f else 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (lowPowerMode) 4000 else 2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(52.dp)) {
        Canvas(modifier = Modifier.fillMaxSize().blur(12.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(StealthPrimary.copy(alpha = auraAlpha), Color.Transparent)
                ),
                radius = size.minDimension / 1.2f * vibeScale
            )
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            rotate(rotation) {
                val scanBrush = Brush.sweepGradient(
                    0.0f to StealthPrimary.copy(alpha = 0.8f), 
                    0.1f to StealthPrimary.copy(alpha = 0.1f), 
                    0.25f to Color.Transparent, 
                    center = center
                )
                drawCircle(brush = scanBrush, radius = size.minDimension / 2.2f)
            }
        }
        Box(
            modifier = Modifier
                .size(24.dp * vibeScale)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(StealthPrimary, StealthRose)))
                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
        )
    }
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
    placeholder: String = "SPREAD VIBES...",
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    val borderGlow by animateColorAsState(
        if (isFocused) StealthPrimary.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.1f),
        label = "BorderGlow"
    )

    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp), // Slightly taller for segmented feel
            color = Color.Black.copy(alpha = 0.85f),
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(1.dp, borderGlow),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SEGMENT 1: IDENTITY (Persona)
                Row(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(30.dp, 4.dp, 4.dp, 30.dp))
                        .background(Color.White.copy(alpha = 0.03f)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
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

                // SEGMENT 2: VIBE ACTION (Input + Send)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp, 30.dp, 30.dp, 4.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .padding(end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.weight(1f).testTag("SendVibeInput"),
                        placeholder = { 
                            Text(
                                text = "TYPE TO SPREAD VIBES", 
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Visible,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            ) 
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        interactionSource = interactionSource,
                        singleLine = true,
                        maxLines = 1
                    )

                    if (vibeCount > 0) {
                        Surface(
                            color = StealthPrimary.copy(alpha = 0.15f),
                            shape = CircleShape,
                            border = BorderStroke(0.5.dp, StealthPrimary.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = vibeCount.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = StealthPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    Surface(
                        onClick = { if (value.isNotBlank()) onSend() },
                        shape = CircleShape,
                        color = Color.Transparent,
                        modifier = Modifier.testTag("SendVibeButton")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (value.isNotBlank()) 
                                        Brush.linearGradient(listOf(StealthPrimary, StealthAmber))
                                    else 
                                        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.02f)))
                                ),
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
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Pulse"
    )

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 4.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
            Surface(
                shape = CircleShape,
                color = (if (isUnknown) StealthAmber else StealthPrimary).copy(alpha = 0.1f),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
            ) {}
            
            Surface(
                shape = CircleShape,
                color = Color(0xFF12141A),
                modifier = Modifier.size(24.dp),
                border = BorderStroke(1.dp, if (isUnknown) StealthAmber.copy(alpha = 0.5f) else StealthPrimary.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = emoji, fontSize = 12.sp)
                }
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_blukit_logo),
                contentDescription = null,
                tint = if (isUnknown) StealthAmber else StealthPrimary,
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .background(Color.Black, CircleShape)
                    .padding(1.dp)
            )
        }

        BasicTextField(
            value = localNickname,
            onValueChange = { newName ->
                localNickname = newName
                onNicknameChange(newName.ifBlank { "?" })
            },
            modifier = Modifier
                .widthIn(max = 80.dp)
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
    val infiniteTransition = rememberInfiniteTransition(label = "NodeAnim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f + (device.proximityFactor * 0.1f),
        animationSpec = infiniteRepeatable(tween(2000 + (device.proximityFactor * 1000).toInt(), easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Pulse"
    )

    val nodeSize = if (device.proximityFactor > 0.8f) 64.dp else 52.dp
    val proximityGlow = (device.proximityFactor * 0.4f).coerceAtLeast(0f)

    Box(modifier = modifier.size(nodeSize * 2f), contentAlignment = Alignment.Center) {
        Surface(
            shape = CircleShape,
            color = (if (isSelected) Color.White else if (isVibed) StealthRose else if (isPeerVibed) StealthAmber else StealthPrimary).copy(alpha = (0.05f + proximityGlow) * pulseScale),
            modifier = Modifier.size(nodeSize * pulseScale * (1.6f + proximityGlow))
        ) {}

        Surface(
            modifier = Modifier.size(nodeSize).clip(CircleShape).combinedClickable(onClick = onClick, onLongClick = onLongClick),
            color = when {
                isSelected -> Color.White.copy(alpha = 0.2f)
                isVibed -> StealthRose.copy(alpha = 0.15f)
                isPeerVibed -> StealthAmber.copy(alpha = 0.15f)
                else -> Color(0xFF12141A)
            },
            border = BorderStroke(
                if (isSelected || isVibed || isPeerVibed) 2.dp else 1.dp,
                when {
                    isSelected -> Color.White
                    isVibed -> StealthRose
                    isPeerVibed -> StealthAmber
                    else -> Color.White.copy(alpha = 0.15f)
                }
            ),
            shape = CircleShape,
            tonalElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) { // NAME CENTERED OVER ICON
                val mediumIcon = when (device.medium) {
                    P2PDevice.ConnectionMedium.BLUETOOTH -> Icons.Rounded.Bluetooth
                    P2PDevice.ConnectionMedium.WIFI -> Icons.Rounded.Wifi
                    P2PDevice.ConnectionMedium.LOCATION -> Icons.Rounded.LocationOn
                    else -> Icons.Rounded.LocationOn
                }

                Icon(
                    imageVector = if (device.isConnecting || device.isLinkPending) Icons.Rounded.Sync else if (isSelected) Icons.Rounded.CheckCircle else mediumIcon,
                    contentDescription = null,
                    tint = when {
                        isSelected -> Color.White.copy(alpha = 0.1f)
                        isVibed -> StealthRose.copy(alpha = 0.1f)
                        isPeerVibed -> StealthAmber.copy(alpha = 0.1f)
                        else -> Color.White.copy(alpha = 0.05f)
                    },
                    modifier = Modifier.size((nodeSize.value / 1.5f).dp)
                )
                
                val displayName = (device.name ?: "?").take(4).uppercase()
                Text(
                    text = if (!onlyTies && isVibed) "$displayName+" else displayName,
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.graphicsLayer { alpha = 0.9f }
                )
            }
        }

        activeBubble?.let {
            Box(modifier = Modifier.offset(y = (-48).dp)) {
                Surface(
                    color = StealthPrimary.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 2.dp),
                    modifier = Modifier.widthIn(max = 140.dp)
                ) {
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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

    FlowRow(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .fillMaxWidth(),
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

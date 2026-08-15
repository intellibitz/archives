package cc.thevar.blukit.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import cc.thevar.blukit.R
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthSecondary
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthRose
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

data class BubbleData(
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val messageId: String,
    val isPrivate: Boolean = false
)

data class RelayEvent(
    val id: String,
    val start: Offset,
    val end: Offset,
    val startTime: Long
)

data class VibeRipple(
    val id: String,
    val center: Offset,
    val startTime: Long,
    val color: Color
)

/**
 * BLUKIT: ENERGY PROXIMITY.
 */
@Composable
fun RipplesField(
    state: BluetoothUiState,
    localDeviceId: String,
    localEmoji: String,
    activeBubbles: List<BubbleData>,
    externalEnergy: Float = 0f,
    onlyTies: Boolean = false,
    onDeviceClick: (P2PDevice) -> Unit,
    onStartScan: () -> Unit,
    onVibeSurge: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // 1. Relay, Vibe Ripples & Collective Energy State
    val relayEvents = remember { mutableStateListOf<RelayEvent>() }
    val vibeRipples = remember { mutableStateListOf<VibeRipple>() }
    val processedRelayIds = remember { mutableSetOf<String>() }
    var collectiveEnergy by remember { mutableStateOf(0f) }

    // 2. Logic: Trigger Relay Lines & Vibe Ripples when a new bubble appears
    LaunchedEffect(activeBubbles.size) {
        if (activeBubbles.isNotEmpty()) {
            val last = activeBubbles.last()
            if (last.messageId !in processedRelayIds) {
                processedRelayIds.add(last.messageId)
                
                // Surge Collective Energy
                collectiveEnergy = (collectiveEnergy + 0.4f).coerceAtMost(1.0f)
                
                // Calculate Target Position
                val deviceIndex = state.scannedDevices.indexOfFirst { it.id == last.senderId }
                val proximity = if (deviceIndex != -1) state.scannedDevices[deviceIndex].proximityFactor else 1.0f
                onVibeSurge(proximity)

                val targetOffset = if (deviceIndex != -1) {
                    val device = state.scannedDevices[deviceIndex]
                    val maxRadiusPx = with(density) { 140.dp.toPx() }
                    val radiusValue = (1f - device.proximityFactor) * maxRadiusPx + with(density) { 60.dp.toPx() }
                    val angle = (deviceIndex.toDouble() / state.scannedDevices.size) * 2 * PI
                    Offset((radiusValue * cos(angle)).toFloat(), (radiusValue * sin(angle)).toFloat())
                } else Offset.Zero

                val startOffset = Offset((Random.nextFloat() - 0.5f) * 1000f, (Random.nextFloat() - 0.5f) * 1600f)
                relayEvents.add(RelayEvent(last.messageId, startOffset, targetOffset, System.currentTimeMillis()))
                
                // Add a Ripple at the target location
                val rippleColor = if (last.isPrivate) StealthRose else StealthAmber
                vibeRipples.add(VibeRipple(last.messageId, targetOffset, System.currentTimeMillis(), rippleColor))
            }
        }
    }

    // Cleanup & Decay logic
    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            relayEvents.removeAll { now - it.startTime > 800 }
            vibeRipples.removeAll { now - it.startTime > 2000 }
            
            // Decay Energy
            if (collectiveEnergy > 0f) {
                collectiveEnergy = (collectiveEnergy - 0.05f).coerceAtLeast(0f)
            }
            delay(100)
        }
    }

    val finalEnergy = (collectiveEnergy + externalEnergy).coerceAtMost(1.0f)

    Column(
        modifier = modifier.fillMaxSize().background(Color.Transparent) 
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            StadiumBackground(energy = finalEnergy)
            RelayLayer(relayEvents)
            // Filter devices if only ties are requested
            val displayDevices = if (onlyTies) {
                state.scannedDevices.filter { it.id in state.connectedLinks }
            } else {
                state.scannedDevices
            }

            VibeRippleLayer(vibeRipples)
            VibesConnectivity(displayDevices)
            
            // Center Node (Removed - moved to badge)
            Box(contentAlignment = Alignment.Center) {
                val myBubble = activeBubbles.findLast { it.senderId == localDeviceId }
                val myBubbleColor = if (myBubble?.isPrivate == true) StealthRose else Color.White
                BubbleWrapper(activeBubble = myBubble, color = myBubbleColor)
            }

            // Vibe nodes and bubbles
            if (displayDevices.isNotEmpty()) {
                VibeNodes(
                    devices = displayDevices,
                    connectedLinks = state.connectedLinks,
                    activeBubbles = activeBubbles,
                    onDeviceClick = onDeviceClick
                )
            }
        }
    }
}

@Composable
private fun BubbleWrapper(activeBubble: BubbleData?, color: Color) {
    val bubbleScale by animateFloatAsState(
        targetValue = if (activeBubble != null) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "Scale"
    )
    val bubbleAlpha by animateFloatAsState(targetValue = if (activeBubble != null) 1f else 0f, animationSpec = tween(400), label = "Alpha")
    val bubbleBlur by animateFloatAsState(targetValue = if (activeBubble != null) 0f else 12f, animationSpec = tween(600), label = "Blur")

    if (bubbleAlpha > 0.01f) {
        Box(
            modifier = Modifier
                .offset(y = (-72).dp)
                .graphicsLayer {
                    scaleX = bubbleScale
                    scaleY = bubbleScale
                    alpha = bubbleAlpha
                }
                .blur(bubbleBlur.dp)
        ) {
            activeBubble?.let {
                Bubble(content = it.content, color = color)
            }
        }
    }
}

@Composable
private fun Bubble(content: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.95f),
        shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
        modifier = Modifier.widthIn(max = 160.dp),
        shadowElevation = 8.dp
    ) {
        Text(
            text = content,
            fontSize = 12.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StadiumBackground(energy: Float) {
    val dotsCount = 1200 // Refined: Increased for High-Fidelity Stadium feel
    val points = remember {
        List(dotsCount) { 
            Triple(
                Offset(Random.nextFloat(), Random.nextFloat()), 
                0.2f + Random.nextFloat() * 1.2f, 
                Random.nextFloat()
            ) 
        }
    }
    
    val rippleCount = 40 // Background atmospheric ripples
    val ripples = remember {
        List(rippleCount) {
            Triple(
                Offset(Random.nextFloat(), Random.nextFloat()),
                Random.nextFloat(), // scale offset
                Random.nextFloat()  // speed multiplier
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Stadium")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, 
        targetValue = 2 * PI.toFloat(), 
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)), 
        label = "T"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // 0. Collective Glow: Screen-Wide Bloom when energy is high
        if (energy > 0.5f) {
            val bloomAlpha = ((energy - 0.5f) * 0.15f).coerceIn(0f, 0.08f)
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to StealthAmber.copy(alpha = bloomAlpha),
                    1.0f to Color.Transparent,
                    center = center
                ),
                radius = size.maxDimension * 0.8f,
                center = center
            )
        }

        // 1. Atmosphere: Distant souls as points
        points.forEach { (offset, dotSize, colorShift) ->
            val movementScale = 15f + energy * 25f
            val dx = sin(time + offset.x * 20) * movementScale
            val dy = cos(time + offset.y * 20) * movementScale
            
            val color = if (colorShift > 0.95f) StealthRose else StealthAmber
            val alpha = (0.02f + 0.08f * abs(sin(time * 0.5f + colorShift * 10)) + energy * 0.15f).coerceIn(0.01f, 0.3f)
            
            val currentPos = Offset(offset.x * size.width + dx, offset.y * size.height + dy)

            // Motion Blur Effect: Draw slight tails behind the points when energy > 0.6
            if (energy > 0.6f) {
                val tailCount = 3
                for (i in 1..tailCount) {
                    val tailAlpha = alpha * (1f - i.toFloat() / (tailCount + 1))
                    val tailOffset = 1.2f * i 
                    // Approximate previous position by reversing a bit of the oscillation
                    val tPrev = time - 0.05f * i
                    val pdx = sin(tPrev + offset.x * 20) * movementScale
                    val pdy = cos(tPrev + offset.y * 20) * movementScale
                    val prevPos = Offset(offset.x * size.width + pdx, offset.y * size.height + pdy)
                    
                    drawCircle(
                        color = color.copy(alpha = tailAlpha),
                        radius = dotSize.dp.toPx() * (1f + energy * 0.4f) * (1f - i * 0.2f),
                        center = prevPos
                    )
                }
            }

            drawCircle(
                color = color.copy(alpha = alpha), 
                radius = dotSize.dp.toPx() * (1f + energy * 0.5f), 
                center = currentPos
            )
        }

        // 2. Atmosphere: Collective ripples
        ripples.forEach { (offset, scaleOffset, speed) ->
            val progress = (time * (0.2f + speed * 0.3f + energy * 0.5f) + scaleOffset) % 1f
            val radius = progress * (200.dp.toPx() + energy * 100.dp.toPx())
            val alpha = (1f - progress) * (0.05f + energy * 0.1f)
            
            drawCircle(
                color = StealthAmber.copy(alpha = alpha),
                radius = radius,
                center = Offset(offset.x * size.width, offset.y * size.height),
                style = Stroke(width = (0.5.dp.toPx() + energy.dp.toPx()))
            )
        }
    }
}

@Composable
private fun VibeRippleLayer(ripples: List<VibeRipple>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val now = System.currentTimeMillis()
        ripples.forEach { ripple ->
            val progress = ((now - ripple.startTime) / 2000f).coerceIn(0f, 1f)
            val alpha = 1f - progress
            val radius = progress * 300.dp.toPx()
            
            drawCircle(
                color = ripple.color.copy(alpha = alpha * 0.3f),
                radius = radius,
                center = center + ripple.center,
                style = Stroke(width = 2.dp.toPx())
            )
            
            drawCircle(
                color = ripple.color.copy(alpha = alpha * 0.1f),
                radius = radius * 0.7f,
                center = center + ripple.center,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}


@Composable
private fun RelayLayer(events: List<RelayEvent>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val now = System.currentTimeMillis()
        events.forEach { event ->
            val progress = ((now - event.startTime) / 600f).coerceIn(0f, 1f)
            if (progress < 1f) {
                val startPos = center + event.start
                val endPos = center + event.end
                
                // Refined: Particle Trail instead of just a line and dot
                val trailParticles = 6
                for (i in 0 until trailParticles) {
                    val p = (progress - i * 0.04f).coerceIn(0f, 1f)
                    val particlePos = Offset(
                        lerp(startPos.x, endPos.x, p),
                        lerp(startPos.y, endPos.y, p)
                    )
                    val pAlpha = (1f - progress) * (1f - i.toFloat() / trailParticles)
                    drawCircle(
                        color = StealthPrimary.copy(alpha = pAlpha),
                        radius = (2.dp.toPx() * (1f - i.toFloat() / trailParticles)).coerceAtLeast(0.5.dp.toPx()),
                        center = particlePos
                    )
                }

                val currentHead = Offset(lerp(startPos.x, endPos.x, progress), lerp(startPos.y, endPos.y, progress))
                drawCircle(color = Color.White.copy(alpha = 0.8f * (1f - progress)), radius = 2.dp.toPx(), center = currentHead)
            }
        }
    }
}

@Composable
private fun VibesConnectivity(devices: List<P2PDevice>) {
    val connectedDevices = devices.filter { it.isConnected }
    if (connectedDevices.isEmpty()) return
    val infiniteTransition = rememberInfiniteTransition(label = "TheVibes")
    val flow by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "F")

    Canvas(modifier = Modifier.fillMaxSize()) {
        connectedDevices.forEach { device ->
            val radiusPx = (1f - device.proximityFactor) * 140.dp.toPx() + 60.dp.toPx()
            val angle = (devices.indexOf(device).toDouble() / devices.size) * 2 * PI
            val target = Offset(center.x + (radiusPx * cos(angle)).toFloat(), center.y + (radiusPx * sin(angle)).toFloat())
            drawLine(color = StealthPrimary.copy(alpha = 0.05f + 0.2f * flow), start = center, end = target, strokeWidth = (1f + flow).dp.toPx())
        }
    }
}

@Composable
private fun VibeNodes(
    devices: List<P2PDevice>, 
    connectedLinks: Set<String>,
    activeBubbles: List<BubbleData>, 
    onDeviceClick: (P2PDevice) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        devices.forEachIndexed { index, device ->
            val radiusValue = (1f - device.proximityFactor) * 140f + 60f
            val angle = (index.toDouble() / devices.size) * 2 * PI
            val activeBubble = activeBubbles.findLast { it.senderId == device.id }
            val bubbleColor = if (activeBubble?.isPrivate == true) StealthRose else StealthAmber
            val isVibed = device.id in connectedLinks
            
            VibeNode(
                device = device, 
                isVibed = isVibed,
                xOffset = (radiusValue * cos(angle)).toFloat().dp, 
                yOffset = (radiusValue * sin(angle)).toFloat().dp, 
                activeBubble = activeBubble,
                bubbleColor = bubbleColor,
                onClick = { onDeviceClick(device) }
            )
        }
    }
}

@Composable
private fun VibeNode(
    device: P2PDevice, 
    isVibed: Boolean,
    xOffset: Dp, 
    yOffset: Dp, 
    activeBubble: BubbleData?, 
    bubbleColor: Color,
    onClick: () -> Unit
) {
    val vibeDuration = (3000 - (device.proximityFactor * 2200)).toInt().coerceIn(500, 3000)
    val infiniteTransition = rememberInfiniteTransition(label = "Node")
    val vibeScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.1f + (device.proximityFactor * 0.15f),
        animationSpec = infiniteRepeatable(
            animation = tween(vibeDuration),
            repeatMode = RepeatMode.Reverse
        ),
        label = "S"
    )
    val nodeSize = if (device.proximityFactor > 0.7f) 56.dp else 44.dp

    val glowAlpha by animateFloatAsState(
        targetValue = if (isVibed) 0.8f else 0.2f,
        animationSpec = tween(1000),
        label = "GlowAlpha"
    )

    Box(modifier = Modifier.offset(xOffset, yOffset).size(nodeSize * 3.5f), contentAlignment = Alignment.Center) {
        BubbleWrapper(activeBubble = activeBubble, color = bubbleColor)
        
        // Dynamic Halo
        Box(
            modifier = Modifier
                .size(nodeSize * vibeScale * 1.5f)
                .background(
                    Brush.radialGradient(
                        listOf(
                            (if (isVibed) StealthAmber else colorForProximity(device.proximityLabel)).copy(alpha = 0.2f * glowAlpha),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        // Main Node Body
        Box(
            modifier = Modifier
                .size(nodeSize)
                .clip(CircleShape)
                .background(
                    if (isVibed) {
                        Brush.linearGradient(listOf(StealthAmber, StealthRose))
                    } else {
                        Brush.linearGradient(listOf(Color(0xFF1A1D26), Color(0xFF0A0C14)))
                    }
                )
                .border(
                    if (isVibed) 2.dp else 1.dp,
                    if (isVibed) Color.White else Color.White.copy(alpha = 0.15f),
                    CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (device.isConnecting || device.isLinkPending) Icons.Rounded.Refresh else Icons.Rounded.Person,
                    contentDescription = null,
                    tint = if (isVibed) Color.Black else if (device.isLinkPending) StealthAmber else Color.White,
                    modifier = Modifier.size((nodeSize.value / 2.2f).dp).graphicsLayer {
                        if (isVibed || device.isLinkPending) {
                            scaleX = vibeScale
                            scaleY = vibeScale
                        }
                    }
                )
                Text(
                    text = (device.name ?: stringResource(R.string.anonymous)).take(6).uppercase(),
                    fontSize = 8.sp,
                    color = if (device.isConnected) Color.Black else Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun colorForProximity(group: String): Color {
    return when (group) {
        "Very Close" -> Color(0xFF00FF88)
        "Close" -> Color(0xFF00FFCC)
        "Moderate" -> Color(0xFFFFE500)
        else -> Color(0xFFFF1744)
    }
}

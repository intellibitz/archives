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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

data class BubbleData(
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val messageId: String
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
 * The Air: A high-fidelity vibe visualization for projected displays.
 * Animates vibe relays, atmospheric ripples, and peer-specific vibes.
 */
@OptIn(ExperimentalPermissionsApi::class)
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
                vibeRipples.add(VibeRipple(last.messageId, targetOffset, System.currentTimeMillis(), StealthAmber))
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
            RadarBackground()
            RelayLayer(relayEvents)
            // Filter devices if only ties are requested
            val displayDevices = if (onlyTies) {
                state.scannedDevices.filter { it.id in state.connectedLinks }
            } else {
                state.scannedDevices
            }

            VibeRippleLayer(vibeRipples)
            VibesConnectivity(displayDevices)
            
            // Center "Me" Node + Own Bubble
            Box(contentAlignment = Alignment.Center) {
                CenterNode(localEmoji)
                val myBubble = activeBubbles.findLast { it.senderId == localDeviceId }
                BubbleWrapper(activeBubble = myBubble, color = Color.White)
            }

            // Vibe nodes and bubbles
            if (displayDevices.isNotEmpty()) {
                VibeNodes(
                    devices = displayDevices,
                    activeBubbles = activeBubbles,
                    onDeviceClick = onDeviceClick
                )
            } else if (!state.isConnecting) {
                EmptyRadarHint(onlyTies)
            } else {
                LoadingRadarHint(state.isConnecting)
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
    val dotsCount = 800 // High density for stadium feel
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
        // 1. Atmosphere: Distant souls as points
        points.forEach { (offset, dotSize, colorShift) ->
            val dx = sin(time + offset.x * 20) * (15f + energy * 20f)
            val dy = cos(time + offset.y * 20) * (15f + energy * 20f)
            val color = if (colorShift > 0.95f) StealthRose else StealthAmber
            val alpha = (0.02f + 0.08f * abs(sin(time * 0.5f + colorShift * 10)) + energy * 0.15f).coerceIn(0.01f, 0.3f)
            
            drawCircle(
                color = color.copy(alpha = alpha), 
                radius = dotSize.dp.toPx() * (1f + energy * 0.5f), 
                center = Offset(offset.x * size.width + dx, offset.y * size.height + dy)
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
private fun RadarBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "Sweep")
    val rotation by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)), label = "R")
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = center
        val maxRadius = size.minDimension / 1.7f
        for (i in 1..4) {
            drawCircle(color = StealthPrimary.copy(alpha = 0.03f), radius = maxRadius * (i / 4f), style = Stroke(width = 1.dp.toPx()))
        }
        rotate(rotation) {
            val sonarBrush = Brush.sweepGradient(0.0f to StealthPrimary.copy(alpha = 0.6f), 0.2f to StealthPrimary.copy(alpha = 0.1f), 0.5f to Color.Transparent, center = center)
            drawCircle(brush = sonarBrush, radius = maxRadius)
            drawLine(color = StealthPrimary.copy(alpha = 0.4f), start = center, end = Offset(center.x + maxRadius, center.y), strokeWidth = 2.dp.toPx())
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
                val currentHead = Offset(lerp(startPos.x, endPos.x, progress), lerp(startPos.y, endPos.y, progress))
                val currentTail = Offset(lerp(startPos.x, endPos.x, (progress - 0.15f).coerceAtLeast(0f)), lerp(startPos.y, endPos.y, (progress - 0.15f).coerceAtLeast(0f)))
                drawLine(color = StealthPrimary.copy(alpha = 1f - progress), start = currentTail, end = currentHead, strokeWidth = 2.5.dp.toPx())
                drawCircle(color = Color.White.copy(alpha = 0.7f * (1f - progress)), radius = 1.5.dp.toPx(), center = currentHead)
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
private fun CenterNode(emoji: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "Me")
    val vibeScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f, // Even more intense
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "VibeScale"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, // Brighter base
        targetValue = 0.7f, // Brighter peak
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraAlpha"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
        // High-Fidelity Aura
        Canvas(modifier = Modifier.fillMaxSize().blur(24.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(StealthAmber.copy(alpha = auraAlpha), Color.Transparent)
                ),
                radius = size.minDimension / 1.3f * vibeScale
            )
        }
        
        // Inner Vibe Body
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.4f),
            border = BorderStroke(2.dp, Brush.radialGradient(listOf(StealthAmber, StealthRose))),
            modifier = Modifier.size(64.dp * vibeScale)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Person, 
                    contentDescription = null,
                    tint = StealthAmber,
                    modifier = Modifier.size(32.dp).graphicsLayer {
                        scaleX = vibeScale
                        scaleY = vibeScale
                    }
                )
            }
        }
    }
}

@Composable
private fun VibeNodes(devices: List<P2PDevice>, activeBubbles: List<BubbleData>, onDeviceClick: (P2PDevice) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        devices.forEachIndexed { index, device ->
            val radiusValue = (1f - device.proximityFactor) * 140f + 60f
            val angle = (index.toDouble() / devices.size) * 2 * PI
            VibeNode(device = device, xOffset = (radiusValue * cos(angle)).toFloat().dp, yOffset = (radiusValue * sin(angle)).toFloat().dp, activeBubble = activeBubbles.findLast { it.senderId == device.id }, onClick = { onDeviceClick(device) })
        }
    }
}

@Composable
private fun VibeNode(device: P2PDevice, xOffset: Dp, yOffset: Dp, activeBubble: BubbleData?, onClick: () -> Unit) {
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
        targetValue = if (device.isConnected) 0.8f else 0.2f,
        animationSpec = tween(1000),
        label = "GlowAlpha"
    )

    Box(modifier = Modifier.offset(xOffset, yOffset).size(nodeSize * 3.5f), contentAlignment = Alignment.Center) {
        BubbleWrapper(activeBubble = activeBubble, color = StealthAmber)
        
        // Dynamic Halo
        Box(
            modifier = Modifier
                .size(nodeSize * vibeScale * 1.5f)
                .background(
                    Brush.radialGradient(
                        listOf(
                            (if (device.isConnected) StealthAmber else colorForProximity(device.proximityLabel)).copy(alpha = 0.2f * glowAlpha),
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
                    if (device.isConnected) {
                        Brush.linearGradient(listOf(StealthAmber, StealthRose))
                    } else {
                        Brush.linearGradient(listOf(Color(0xFF1A1D26), Color(0xFF0A0C14)))
                    }
                )
                .border(
                    if (device.isConnected) 2.dp else 1.dp,
                    if (device.isConnected) Color.White else Color.White.copy(alpha = 0.15f),
                    CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (device.isConnecting) Icons.Rounded.HourglassEmpty else Icons.Rounded.Person,
                    contentDescription = null,
                    tint = if (device.isConnected) Color.Black else Color.White,
                    modifier = Modifier.size((nodeSize.value / 2.2f).dp).graphicsLayer {
                        if (device.isConnected) {
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

@Composable
private fun EmptyRadarHint(onlyTies: Boolean) {
    Column(
        modifier = Modifier.padding(top = 280.dp), // Positioned well below the central vibe
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.Diversity3, 
            contentDescription = null,
            tint = StealthAmber.copy(alpha = 0.3f),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = (if (onlyTies) "NO TIES FOUND…" else stringResource(R.string.shout_empty_radar)).uppercase(), 
            style = MaterialTheme.typography.labelSmall,
            color = StealthAmber.copy(alpha = 0.4f), 
            fontWeight = FontWeight.Black, 
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun LoadingRadarHint(isConnecting: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp), color = StealthAmber, strokeWidth = 2.dp)
        Text(text = (if (isConnecting) "BRIDGING…" else "SYNCING THE VIBES…"), style = MaterialTheme.typography.labelSmall, color = StealthAmber, modifier = Modifier.padding(top = 12.dp), letterSpacing = 2.sp)
    }
}

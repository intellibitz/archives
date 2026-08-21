package cc.thevar.blukit.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
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
import androidx.compose.ui.zIndex
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
 * BLUKIT: THE ATMOSPHERIC FIELD.
 */
@Composable
fun RipplesField(
    state: BluetoothUiState,
    localDeviceId: String,
    localEmoji: String,
    activeBubbles: List<BubbleData>,
    selectedDevices: Set<String> = emptySet(),
    vibedPeers: Set<String> = emptySet(),
    externalEnergy: Float = 0f,
    onlyTies: Boolean = false,
    isFilterMode: Boolean = false,
    lowPowerMode: Boolean = false,
    highlightedUserId: String? = null,
    onDeviceClick: (P2PDevice) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit = {},
    onStartScan: () -> Unit,
    onVibeSurge: (Float) -> Unit = {},
    drawBackground: Boolean = true,
    drawNodes: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val density = LocalDensity.current

    val relayEvents = remember { mutableStateListOf<RelayEvent>() }
    val vibeRipples = remember { mutableStateListOf<VibeRipple>() }
    val processedRelayIds = remember { mutableSetOf<String>() }
    var collectiveEnergy by remember { mutableStateOf(0f) }

    LaunchedEffect(activeBubbles.size) {
        if (activeBubbles.isNotEmpty()) {
            val last = activeBubbles.last()
            if (last.messageId !in processedRelayIds) {
                processedRelayIds.add(last.messageId)
                collectiveEnergy = (collectiveEnergy + 0.35f).coerceAtMost(1.0f)
                
                val deviceIndex = state.crowd.scannedDevices.indexOfFirst { it.id == last.senderId }
                val proximity = if (deviceIndex != -1) state.crowd.scannedDevices[deviceIndex].proximityFactor else 0.5f
                onVibeSurge(proximity)

                val targetOffset = if (deviceIndex != -1) {
                    val device = state.crowd.scannedDevices[deviceIndex]
                    val maxRadiusPx = with(density) { 140.dp.toPx() }
                    val radiusValue = (1f - device.proximityFactor) * maxRadiusPx + with(density) { 60.dp.toPx() }
                    val angle = (deviceIndex.toDouble() / state.crowd.scannedDevices.size) * 2 * PI
                    Offset((radiusValue * cos(angle)).toFloat(), (radiusValue * sin(angle)).toFloat())
                } else Offset.Zero

                val startOffset = Offset((Random.nextFloat() - 0.5f) * 1200f, (Random.nextFloat() - 0.5f) * 1800f)
                relayEvents.add(RelayEvent(last.messageId, startOffset, targetOffset, System.currentTimeMillis()))
                
                val rippleColor = if (last.isPrivate) StealthRose else StealthPrimary
                vibeRipples.add(VibeRipple(last.messageId, targetOffset, System.currentTimeMillis(), rippleColor))
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            relayEvents.removeAll { now - it.startTime > 800 }
            vibeRipples.removeAll { now - it.startTime > 2000 }
            if (collectiveEnergy > 0f) collectiveEnergy = (collectiveEnergy - 0.04f).coerceAtLeast(0f)
            delay(100)
        }
    }

        val finalEnergy = (collectiveEnergy + externalEnergy).coerceAtMost(1.0f)

    val coordinates = LocalPersonaCoordinates.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .onGloballyPositioned { 
                val centerPos = it.positionInRoot() + Offset(it.size.width / 2f, it.size.height / 2f)
                val current = coordinates["YOU"] ?: PersonaConnectionPoints()
                // Adjust so that +32dp offset in BlukitApp Canvas reaches exact center
                val fieldAnchor = centerPos - Offset(with(density) { 32.dp.toPx() }, with(density) { 32.dp.toPx() })
                coordinates["YOU"] = current.copy(field = fieldAnchor)
            }, 
        contentAlignment = Alignment.BottomCenter
    ) {
        if (drawBackground) {
            StadiumBackground(energy = finalEnergy, lowPowerMode = lowPowerMode)
        }
        
        RelayLayer(relayEvents)
        
        val displayDevices = if (onlyTies) {
            state.crowd.scannedDevices.filter { it.id in state.session.connectedLinks }
        } else {
            state.crowd.scannedDevices
        }

        if (drawNodes) {
            ResonanceArcs(devices = displayDevices, energy = finalEnergy)
            VibeNodes(
                devices = displayDevices,
                connectedLinks = state.session.connectedLinks,
                selectedDevices = selectedDevices,
                vibedPeers = vibedPeers,
                activeBubbles = activeBubbles,
                onlyTies = onlyTies,
                isFilterMode = isFilterMode,
                highlightedUserId = highlightedUserId,
                onDeviceClick = onDeviceClick,
                onDeviceLongClick = onDeviceLongClick
            )
        }

        VibesConnectivity(devices = state.crowd.scannedDevices)

        // LAYER 2: Overlay Content (Vibes Ticker - Bottom Layer)
        Box(modifier = Modifier.fillMaxSize().zIndex(0.5f)) {
            content()
        }
    }
}

@Composable
private fun ResonanceArcs(devices: List<P2PDevice>, energy: Float) {
    if (devices.size < 2 || energy < 0.2f) return
    
    val infiniteTransition = rememberInfiniteTransition(label = "Resonance")
    val flow by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "Flow"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val nodeOffsets = devices.mapIndexed { index, device ->
            val radiusPx = (1f - device.proximityFactor) * 140.dp.toPx() + 60.dp.toPx()
            val angle = (index.toDouble() / devices.size) * 2 * PI
            center + Offset((radiusPx * cos(angle)).toFloat(), (radiusPx * sin(angle)).toFloat())
        }

        for (i in devices.indices) {
            for (j in i + 1 until devices.indices.last + 1) {
                val d1 = devices[i]
                val d2 = devices[j]
                val dist = (nodeOffsets[i] - nodeOffsets[j]).getDistance()
                
                // Only resonate if they are "close" in the field and energy is high
                if (dist < 300.dp.toPx()) {
                    val alpha = ((1f - dist / 300.dp.toPx()) * energy * 0.3f).coerceIn(0f, 0.2f)
                    val color = if (d1.isConnected && d2.isConnected) StealthRose else StealthPrimary
                    
                    drawLine(
                        color = color.copy(alpha = alpha),
                        start = nodeOffsets[i],
                        end = nodeOffsets[j],
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(10f, 20f),
                            flow * 30f
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun StadiumBackground(energy: Float, lowPowerMode: Boolean) {
    val dotsCount = if (lowPowerMode) 200 else 800
    val points = remember {
        List(dotsCount) { 
            Triple(Offset(Random.nextFloat(), Random.nextFloat()), 0.5f + Random.nextFloat() * 1.5f, Random.nextFloat()) 
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "Atmosphere")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f, 
        animationSpec = infiniteRepeatable(tween(30000, easing = LinearEasing)), 
        label = "Time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // High-Fidelity Energy Bloom
        if (energy > 0.4f) {
            val bloomAlpha = ((energy - 0.4f) * 0.12f).coerceIn(0f, 0.1f)
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to StealthPrimary.copy(alpha = bloomAlpha),
                    1.0f to Color.Transparent,
                    center = center
                ),
                radius = size.maxDimension * 0.7f,
                center = center
            )
        }

        points.forEach { (offset, dotSize, seed) ->
            val movementProgress = (time + seed) % 1f
            val speedFactor = 1f + energy * 2f
            val driftX = sin(movementProgress * 2 * PI.toFloat() * speedFactor) * 30f * energy
            val driftY = cos(movementProgress * 2 * PI.toFloat() * speedFactor) * 30f * energy
            
            val alpha = (0.05f + 0.15f * abs(sin(movementProgress * PI.toFloat() * 4)) + energy * 0.3f).coerceIn(0.02f, 0.5f)
            val currentPos = Offset(offset.x * size.width + driftX, offset.y * size.height + driftY)
            
            val color = if (seed > 0.8f) StealthRose else StealthPrimary
            drawCircle(color = color.copy(alpha = alpha), radius = dotSize.dp.toPx() * (1f + energy * 0.8f), center = currentPos)
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
            val radius = progress * 400.dp.toPx()
            
            drawCircle(
                color = ripple.color.copy(alpha = alpha * 0.2f),
                radius = radius,
                center = center + ripple.center,
                style = Stroke(width = 1.5.dp.toPx())
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
                
                val trailParticles = 8
                for (i in 0 until trailParticles) {
                    val p = (progress - i * 0.03f).coerceIn(0f, 1f)
                    val particlePos = Offset(lerp(startPos.x, endPos.x, p), lerp(startPos.y, endPos.y, p))
                    val pAlpha = (1f - progress) * (1f - i.toFloat() / trailParticles)
                    drawCircle(
                        color = StealthPrimary.copy(alpha = pAlpha * 0.8f),
                        radius = (3.dp.toPx() * (1f - i.toFloat() / trailParticles)).coerceAtLeast(1.dp.toPx()),
                        center = particlePos
                    )
                }
            }
        }
    }
}

@Composable
private fun VibesConnectivity(devices: List<P2PDevice>) {
    val connectedDevices = devices.filter { it.isConnected }
    if (connectedDevices.isEmpty()) return
    val flow by rememberInfiniteTransition(label = "EnergyFlow").animateFloat(
        initialValue = 0f, targetValue = 1f, 
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse), 
        label = "F"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        connectedDevices.forEach { device ->
            val radiusPx = (1f - device.proximityFactor) * 140.dp.toPx() + 60.dp.toPx()
            val angle = (devices.indexOf(device).toDouble() / devices.size) * 2 * PI
            val target = Offset(center.x + (radiusPx * cos(angle)).toFloat(), center.y + (radiusPx * sin(angle)).toFloat())
            
            drawLine(
                brush = Brush.linearGradient(listOf(StealthPrimary.copy(alpha = 0.02f), StealthPrimary.copy(alpha = 0.1f + 0.1f * flow))),
                start = center, end = target, strokeWidth = (1f + flow * 2f).dp.toPx()
            )
        }
    }
}

@Composable
private fun VibeNodes(
    devices: List<P2PDevice>, 
    connectedLinks: Set<String>,
    selectedDevices: Set<String>,
    vibedPeers: Set<String>,
    activeBubbles: List<BubbleData>, 
    onlyTies: Boolean,
    isFilterMode: Boolean,
    highlightedUserId: String? = null,
    onDeviceClick: (P2PDevice) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        devices.forEachIndexed { index, device ->
            val radiusValue = (1f - device.proximityFactor) * 140f + 60f
            val angle = (index.toDouble() / devices.size) * 2 * PI
            val activeBubble = activeBubbles.findLast { it.senderId == device.id }
            val isTied = device.id in connectedLinks
            val isVibed = device.persistentId in vibedPeers || device.id in vibedPeers
            val isSelected = device.id in selectedDevices
            
            val xOffset = (radiusValue * cos(angle)).toFloat().dp
            val yOffset = (radiusValue * sin(angle)).toFloat().dp

            val isFocused = isVibed || isTied || isSelected
            val isBroadFocus = isFilterMode && vibedPeers.isEmpty()
            val noiseDimAlpha = if (isFilterMode && !isFocused && !isBroadFocus) 0.05f else 1f

            Box(modifier = Modifier.graphicsLayer { alpha = noiseDimAlpha }) {
                // Better Filter Visuals: Glimmers (Dots) vs Blossoms (Nodes)
                // Show as Dot if in Blukit tab and not vibed/tied
                if (!isFilterMode && !onlyTies && !isVibed && !isTied) {
                    VibeDot(
                    device = device,
                    xOffset = xOffset,
                    yOffset = yOffset,
                    onClick = { onDeviceClick(device) },
                    isHighlighted = device.id == highlightedUserId
                )
                } else {
                    VibeNode(
                        device = device, 
                        isVibed = isTied, // If it's a Tie, it uses Rose
                        isSelected = isSelected,
                        isPeerVibed = isVibed, // Filter highlighting
                        onlyTies = onlyTies,
                        xOffset = xOffset, 
                        yOffset = yOffset, 
                        activeBubble = activeBubble,
                        onClick = { onDeviceClick(device) },
                        onLongClick = { onDeviceLongClick(device) },
                        isFilterActive = isFilterMode,
                        isHighlighted = device.id == highlightedUserId
                    )
                }
            }
        }
    }
}

@Composable
private fun VibeDot(
    device: P2PDevice,
    xOffset: Dp,
    yOffset: Dp,
    onClick: () -> Unit,
    isHighlighted: Boolean = false
) {
    val coordinates = LocalPersonaCoordinates.current
    val infiniteTransition = rememberInfiniteTransition(label = "DotAnim")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = if (isHighlighted) 0.8f else 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(if (isHighlighted) 500 else 2000 + (device.proximityFactor * 1000).toInt(), easing = LinearEasing), RepeatMode.Reverse),
        label = "Alpha"
    )

    Box(
        modifier = Modifier
            .offset(xOffset, yOffset)
            .onGloballyPositioned { 
                val current = coordinates[device.id] ?: PersonaConnectionPoints()
                coordinates[device.id] = current.copy(field = it.positionInRoot())
            }
            .size(32.dp) // Large touch area
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(if (isHighlighted) 16.dp else 12.dp), 
            shape = CircleShape,
            color = (if (isHighlighted) StealthAmber else StealthPrimary).copy(alpha = 0.1f * dotAlpha),
            border = BorderStroke(if (isHighlighted) 1.5.dp else 0.5.dp, (if (isHighlighted) StealthAmber else StealthPrimary).copy(alpha = dotAlpha))
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

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun VibeNode(
    device: P2PDevice, 
    isVibed: Boolean,
    isSelected: Boolean,
    isPeerVibed: Boolean,
    onlyTies: Boolean,
    xOffset: Dp, 
    yOffset: Dp, 
    activeBubble: BubbleData?, 
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isFilterActive: Boolean = false,
    isHighlighted: Boolean = false
) {
    val coordinates = LocalPersonaCoordinates.current
    val infiniteTransition = rememberInfiniteTransition(label = "NodeAnim")
    
    val targetPulse = if (isHighlighted) 1.5f else if (isFilterActive && (isVibed || isPeerVibed)) 1.25f else 1.15f
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = targetPulse + (device.proximityFactor * 0.1f),
        animationSpec = infiniteRepeatable(tween(if (isHighlighted) 500 else 2000 + (device.proximityFactor * 1000).toInt(), easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Pulse"
    )

    val nodeSize = if (device.proximityFactor > 0.8f) 64.dp else 52.dp
    val proximityGlow = (device.proximityFactor * 0.4f).coerceAtLeast(0f)
    val bloomBoost = if (isHighlighted) 0.4f else if (isFilterActive && (isVibed || isPeerVibed)) 0.2f else 0f

    Box(
        modifier = Modifier
            .offset(xOffset, yOffset)
            .onGloballyPositioned { 
                val current = coordinates[device.id] ?: PersonaConnectionPoints()
                coordinates[device.id] = current.copy(field = it.positionInRoot())
            }
            .size(nodeSize * 2f), 
        contentAlignment = Alignment.Center
    ) {
        // High-Fidelity Halo
        Surface(
            shape = CircleShape,
            color = (if (isHighlighted) StealthAmber else if (isSelected) Color.White else if (isVibed) StealthRose else if (isPeerVibed) StealthAmber else StealthPrimary).copy(alpha = (if (isHighlighted) 0.3f else 0.05f + proximityGlow + bloomBoost) * pulseScale),
            modifier = Modifier.size(nodeSize * pulseScale * (1.6f + proximityGlow + bloomBoost))
        ) {}

        // Main Body
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
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                val mediumIcon = when (device.medium) {
                    P2PDevice.ConnectionMedium.BLUETOOTH -> Icons.Rounded.Bluetooth
                    P2PDevice.ConnectionMedium.WIFI -> Icons.Rounded.Wifi
                    P2PDevice.ConnectionMedium.LOCATION -> Icons.Rounded.LocationOn
                }

                Icon(
                    imageVector = if (device.isConnecting || device.isLinkPending) Icons.Rounded.Sync else if (isSelected) Icons.Rounded.CheckCircle else mediumIcon,
                    contentDescription = null,
                    tint = when {
                        isSelected -> Color.White
                        isVibed -> StealthRose
                        isPeerVibed -> StealthAmber
                        else -> Color.White.copy(alpha = 0.7f)
                    },
                    modifier = Modifier.size((nodeSize.value / 2.5f).dp)
                )
                val displayName = (device.name ?: "?").take(7).uppercase()
                Text(
                    text = if (!onlyTies && isVibed) "$displayName+" else displayName,
                    fontSize = 8.sp,
                    color = when {
                        isSelected -> Color.White
                        isVibed -> StealthRose
                        isPeerVibed -> StealthAmber
                        else -> Color.White.copy(alpha = 0.6f)
                    },
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Active Bubble Overlay
        activeBubble?.let {
            Box(modifier = Modifier.offset(y = (-48).dp)) {
                Surface(
                    color = (if (it.isPrivate) StealthRose else StealthPrimary).copy(alpha = 0.95f),
                    shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 2.dp),
                    modifier = Modifier.widthIn(max = 140.dp)
                ) {
                    Text(
                        text = it.content,
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

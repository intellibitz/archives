package cc.thevar.blukit.ui.screens

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
 * BLUKIT: THE AIR FIELD.
 */
@Composable
fun RipplesField(
    state: BluetoothUiState,
    localDeviceId: String,
    localNickname: String,
    localEmoji: String,
    activeBubbles: List<BubbleData>,
    selectedDevices: Set<String> = emptySet(),
    vibedPeers: Set<String> = emptySet(),
    externalEnergy: Float = 0f,
    onlyTies: Boolean = false,
    isFilterMode: Boolean = false,
    lowPowerMode: Boolean = false,
    highlightedUserId: String? = null,
    subjectId: String? = null,
    showGhostOnboarding: Boolean = false,
    vibeGhostData: GhostVibeData? = null,
    onNicknameChange: (String) -> Unit = {},
    onOnboardingDone: () -> Unit = {},
    onDismissGhost: () -> Unit = {},
    onDeviceClick: (P2PDevice) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit = {},
    onStartScan: () -> Unit,
    onVibeSurge: (Float) -> Unit = {},
    drawBackground: Boolean = true,
    drawNodes: Boolean = true,
    airList: List<Pair<P2PDevice, Int>> = emptyList(),
    showAirGhost: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
    airRitualGhost: @Composable () -> Unit = {}
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
                coordinates["YOU"] = current.copy(field = centerPos)
            }, 
        contentAlignment = Alignment.BottomCenter
    ) {
        if (drawBackground) {
            AirBackground(energy = finalEnergy, lowPowerMode = lowPowerMode, onlyTies = onlyTies)
        }
        
        RelayLayer(relayEvents, onlyTies = onlyTies)
        
        val bubbleSenders = remember(activeBubbles) { activeBubbles.map { it.senderId }.toSet() }
        val displayDevices = if (onlyTies) {
            state.crowd.scannedDevices.filter { 
                it.id in state.session.connectedLinks || 
                it.id in bubbleSenders || 
                it.persistentId in bubbleSenders ||
                state.crowd.incomingLinkRequests.any { req -> req.id == it.id } ||
                state.crowd.outgoingLinkRequests.any { req -> req.id == it.id }
            }
        } else {
            state.crowd.scannedDevices
        }

        if (drawNodes) {
            Box(modifier = Modifier.fillMaxSize().zIndex(2f)) {
                if (airList.isNotEmpty()) {
                    AirNodes(
                        airList = airList,
                        vibedPeers = vibedPeers,
                        onAirClick = onDeviceClick,
                        onAirLongClick = onDeviceLongClick
                    )
                } else {
                    VibeArcs(devices = displayDevices, energy = finalEnergy, onlyTies = onlyTies)
                    VibeNodes(
                        state = state,
                        devices = displayDevices,
                        connectedLinks = state.session.connectedLinks,
                        selectedDevices = selectedDevices,
                        vibedPeers = vibedPeers,
                        activeBubbles = activeBubbles,
                        onlyTies = onlyTies,
                        isFilterMode = isFilterMode,
                        highlightedUserId = highlightedUserId,
                        subjectId = subjectId,
                        onDeviceClick = onDeviceClick,
                        onDeviceLongClick = onDeviceLongClick
                    )
                }
            }
        }

        // DIM OVERLAY
        val isGhostVisible = showGhostOnboarding || vibeGhostData != null || showAirGhost
        AnimatedVisibility(
            visible = isGhostVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize().zIndex(15f)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(enabled = false) {})
        }

        if (showGhostOnboarding) {
            Box(modifier = Modifier.fillMaxSize().zIndex(20f), contentAlignment = Alignment.Center) {
                OnboardingGhost(
                    nickname = localNickname,
                    emoji = localEmoji,
                    onNicknameChange = onNicknameChange,
                    onDone = onOnboardingDone
                )
            }
        }
        
        Box(modifier = Modifier.fillMaxSize().zIndex(25f), contentAlignment = Alignment.Center) {
            airRitualGhost()
        }

        if (vibeGhostData != null) {
            Box(modifier = Modifier.fillMaxSize().zIndex(30f)) {
                VibeGhost(
                    data = vibeGhostData,
                    onDismiss = onDismissGhost
                )
            }
        }

        if (drawNodes) {
            VibesConnectivity(devices = state.crowd.scannedDevices, onlyTies = onlyTies)
        }

        // LAYER 2: Overlay Content (Vibes Ticker - Bottom Layer)
        Box(modifier = Modifier.fillMaxSize().zIndex(0.5f)) {
            content()
        }
    }
}

@Composable
private fun VibeArcs(devices: List<P2PDevice>, energy: Float, onlyTies: Boolean = false) {
    if (devices.size < 2 || energy < 0.2f) return
    
    val infiniteTransition = rememberInfiniteTransition(label = "Vibration")
    val flow by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "Flow"
    )

    val arcColor = if (onlyTies) StealthRose else StealthPrimary

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
                
                // Only vibe if they are "close" in the field and energy is high
                if (dist < 300.dp.toPx()) {
                    val alpha = ((1f - dist / 300.dp.toPx()) * energy * 0.3f).coerceIn(0f, 0.2f)
                    val color = if (d1.isConnected && d2.isConnected) StealthRose else arcColor
                    
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
private fun AirBackground(energy: Float, lowPowerMode: Boolean, onlyTies: Boolean = false) {
    val dotsCount = if (lowPowerMode) 200 else 800
    val points = remember {
        List(dotsCount) { 
            Triple(Offset(Random.nextFloat(), Random.nextFloat()), 0.5f + Random.nextFloat() * 1.5f, Random.nextFloat()) 
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "Air")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f, 
        animationSpec = infiniteRepeatable(tween(30000, easing = LinearEasing)), 
        label = "Time"
    )

    val airColor = if (onlyTies) StealthRose else StealthPrimary

    Canvas(modifier = Modifier.fillMaxSize()) {
        // High-Fidelity Energy Bloom
        if (energy > 0.4f) {
            val bloomAlpha = ((energy - 0.4f) * 0.12f).coerceIn(0f, 0.1f)
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to airColor.copy(alpha = bloomAlpha),
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
            
            val color = if (seed > 0.8f) StealthRose else airColor
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
private fun RelayLayer(events: List<RelayEvent>, onlyTies: Boolean = false) {
    val relayColor = if (onlyTies) StealthRose else StealthPrimary
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
                        color = relayColor.copy(alpha = pAlpha * 0.8f),
                        radius = (3.dp.toPx() * (1f - i.toFloat() / trailParticles)).coerceAtLeast(1.dp.toPx()),
                        center = particlePos
                    )
                }
            }
        }
    }
}

@Composable
private fun VibesConnectivity(devices: List<P2PDevice>, onlyTies: Boolean = false) {
    val connectedDevices = devices.filter { it.isConnected }
    if (connectedDevices.isEmpty()) return
    val flow by rememberInfiniteTransition(label = "EnergyFlow").animateFloat(
        initialValue = 0f, targetValue = 1f, 
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse), 
        label = "F"
    )

    val connectionColor = if (onlyTies) StealthRose else StealthPrimary

    Canvas(modifier = Modifier.fillMaxSize()) {
        connectedDevices.forEach { device ->
            val radiusPx = (1f - device.proximityFactor) * 140.dp.toPx() + 60.dp.toPx()
            val angle = (devices.indexOf(device).toDouble() / devices.size) * 2 * PI
            val target = Offset(center.x + (radiusPx * cos(angle)).toFloat(), center.y + (radiusPx * sin(angle)).toFloat())
            
            drawLine(
                brush = Brush.linearGradient(listOf(connectionColor.copy(alpha = 0.02f), connectionColor.copy(alpha = 0.1f + 0.1f * flow))),
                start = center, end = target, strokeWidth = (1f + flow * 2f).dp.toPx()
            )
        }
    }
}

@Composable
private fun VibeNodes(
    state: BluetoothUiState,
    devices: List<P2PDevice>, 
    connectedLinks: Set<String>,
    selectedDevices: Set<String>,
    vibedPeers: Set<String>,
    activeBubbles: List<BubbleData>, 
    onlyTies: Boolean,
    isFilterMode: Boolean,
    highlightedUserId: String? = null,
    subjectId: String? = null,
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

            val isFocused = isVibed || isTied || isSelected || device.id == subjectId || device.persistentId == subjectId
            val isBroadFocus = isFilterMode && vibedPeers.isEmpty() && subjectId == null
            val noiseDimAlpha = if (isFilterMode && !isFocused && !isBroadFocus) 0.15f else 1f

            Box(modifier = Modifier.graphicsLayer { alpha = noiseDimAlpha }) {
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
                    isHighlighted = device.id == highlightedUserId,
                    projectionEmoji = state.session.groups.find { it.id == device.persistentId || it.id == device.id }?.projectionEmoji
                )
            }
        }
    }
}

@Composable
private fun AirNodes(
    airList: List<Pair<P2PDevice, Int>>, 
    vibedPeers: Set<String>,
    onAirClick: (P2PDevice) -> Unit,
    onAirLongClick: (P2PDevice) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        airList.forEachIndexed { index, (device, count) ->
            val radiusValue = if (airList.size == 1) 0f else 90f
            val angle = (index.toDouble() / airList.size) * 2 * PI
            
            val xOffset = (radiusValue * cos(angle)).toFloat().dp
            val yOffset = (radiusValue * sin(angle)).toFloat().dp
            
            val isVibed = device.id in vibedPeers

            Box(modifier = Modifier.offset(xOffset, yOffset)) {
                VibeAirSignature(
                    device = device,
                    memberCount = count,
                    isVibed = isVibed,
                    size = if (airList.size == 1) 84.dp else 60.dp,
                    modifier = Modifier.combinedClickable(
                        onClick = { onAirClick(device) },
                        onLongClick = { onAirLongClick(device) }
                    )
                )
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
    val activeVibeId = LocalActiveVibeId.current.value
    val key = device.persistentId ?: device.id
    val isVibing = activeVibeId == key
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
                val center = Offset(it.size.width / 2f, it.size.height / 2f)
                val current = coordinates[key] ?: PersonaConnectionPoints()
                coordinates[key] = current.copy(
                    field = it.positionInRoot() + center,
                    vibe = if (isVibing) it.positionInRoot() + center else null
                )
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
    isHighlighted: Boolean = false,
    projectionEmoji: String? = null
) {
    val coordinates = LocalPersonaCoordinates.current
    val activeVibeId = LocalActiveVibeId.current.value
    val key = device.persistentId ?: device.id
    val isVibing = activeVibeId == key
    val nodeSize = if (device.proximityFactor > 0.8f) 64.dp else 52.dp

    Box(
        modifier = Modifier
            .offset(xOffset, yOffset)
            .onGloballyPositioned { 
                val center = Offset(it.size.width / 2f, it.size.height / 2f)
                val current = coordinates[key] ?: PersonaConnectionPoints()
                coordinates[key] = current.copy(
                    field = it.positionInRoot() + center,
                    vibe = if (isVibing) it.positionInRoot() + center else null
                )
            }
            .size(nodeSize * 2.2f) // Expanded hit area
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center
    ) {
        VibePersonaSignature(
            device = device,
            isVibed = isVibed,
            isSelected = isSelected,
            isPeerVibed = isPeerVibed,
            onlyTies = onlyTies,
            size = nodeSize,
            isHighlighted = isHighlighted,
            projectionEmoji = projectionEmoji
        )

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

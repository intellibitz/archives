package cc.thevar.blukit.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.VibeGroup
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import cc.thevar.blukit.ui.theme.StealthAmber
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

data class BubbleData(
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val messageId: String,
    val isPrivate: Boolean
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
 * BLUKIT: THE CROWDS FIELD.
 */
@Composable
fun RipplesField(
    state: BluetoothUiState,
    localDeviceId: String,
    activeBubbles: List<BubbleData>,
    selectedDevices: Set<String> = emptySet(),
    vibedPeers: Set<String> = emptySet(),
    externalEnergy: Float = 0f,
    onlyTies: Boolean = false,
    isFilterMode: Boolean = false,
    lowPowerMode: Boolean = false,
    highlightedUserId: String? = null,
    subjectId: String? = null,
    vibeGhostData: GhostVibeData? = null,
    onDismissGhost: () -> Unit = {},
    onDeviceClick: (P2PDevice) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit = {},
    onStartScan: () -> Unit,
    onVibeSurge: (Float) -> Unit = {},
    drawBackground: Boolean = true,
    drawNodes: Boolean = true,
    crowdList: List<Pair<P2PDevice, Int>> = emptyList(),
    showCrowdGhost: Boolean = false,
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
            AtmosphericHeatmap(intensity = state.activity.energyIntensity)
        }
        
        RelayLayer(relayEvents, onlyTies = onlyTies)
        
        val bubbleSenders = remember(activeBubbles) { activeBubbles.map { it.senderId }.toSet() }
        val displayDevices = if (onlyTies) {
            state.crowd.scannedDevices.filter { 
                it.id in state.session.connectedRadios || 
                it.id in bubbleSenders || 
                it.persistentId in bubbleSenders ||
                state.crowd.incomingRadioRequests.any { req -> req.id == it.id } ||
                state.crowd.outgoingRadioRequests.any { req -> req.id == it.id }
            }
        } else {
            state.crowd.scannedDevices
        }

        if (drawNodes) {
            Box(modifier = Modifier.fillMaxSize().zIndex(2f)) {
                if (crowdList.isNotEmpty()) {
                    CrowdNodes(
                        crowdList = crowdList,
                        vibedPeers = vibedPeers,
                        onCrowdClick = onDeviceClick,
                        onCrowdLongClick = onDeviceLongClick
                    )
                }

                VibeNodes(
                    state = state,
                    devices = displayDevices,
                    connectedLinks = state.session.connectedRadios,
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

        VibeRippleLayer(vibeRipples)


        if (vibeGhostData != null) {
            VibeGhost(data = vibeGhostData, onDismiss = onDismissGhost)
        }
        
        airRitualGhost()
        
        content()
    }
}

@Composable
private fun AtmosphericHeatmap(intensity: Float) {
    if (intensity <= 0.05f) return
    
    val infiniteTransition = rememberInfiniteTransition(label = "HeatmapAnim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "HeatPulse"
    )

    Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = intensity * pulse * 0.4f }) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(
            brush = Brush.radialGradient(
                0.0f to StealthPrimary.copy(alpha = 0.5f),
                0.7f to StealthPrimary.copy(alpha = 0.1f),
                1.0f to Color.Transparent,
                center = center,
                radius = size.minDimension / 1.5f
            ),
            radius = size.minDimension / 1.5f,
            center = center
        )
    }
}

@Composable
private fun VibeArcs(devices: List<P2PDevice>, energy: Float, onlyTies: Boolean) {
    if (devices.isEmpty()) return
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        devices.forEachIndexed { index, device ->
            val maxRadiusPx = 140.dp.toPx()
            val radiusValue = (1f - device.proximityFactor) * maxRadiusPx + 60.dp.toPx()
            val angle = (index.toDouble() / devices.size) * 2 * PI
            val x = (radiusValue * cos(angle)).toFloat()
            val y = (radiusValue * sin(angle)).toFloat()
            
            drawCircle(
                color = StealthPrimary.copy(alpha = 0.05f * energy),
                radius = 2.dp.toPx(),
                center = center + Offset(x, y)
            )
        }
    }
}

@Composable
private fun AirBackground(energy: Float, lowPowerMode: Boolean, onlyTies: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "Background")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(60000, easing = LinearEasing)),
        label = "Rotation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        
        if (!lowPowerMode) {
            rotate(rotation) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        0.0f to StealthPrimary.copy(alpha = 0.02f * energy),
                        0.5f to Color.Transparent,
                        1.0f to StealthPrimary.copy(alpha = 0.02f * energy),
                        center = center
                    ),
                    radius = size.maxDimension,
                    center = center
                )
            }
        }

        drawCircle(
            brush = Brush.radialGradient(
                0.0f to StealthPrimary.copy(alpha = 0.1f * energy),
                0.8f to Color.Transparent,
                center = center,
                radius = size.minDimension / 2
            ),
            radius = size.minDimension / 2,
            center = center
        )
    }
}

@Composable
private fun VibeRippleLayer(ripples: List<VibeRipple>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        ripples.forEach { ripple ->
            val progress = (System.currentTimeMillis() - ripple.startTime) / 2000f
            if (progress in 0f..1f) {
                drawCircle(
                    color = ripple.color.copy(alpha = 0.4f * (1f - progress)),
                    radius = progress * 300.dp.toPx(),
                    center = center + ripple.center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun RelayLayer(relays: List<RelayEvent>, onlyTies: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        relays.forEach { relay ->
            val progress = (System.currentTimeMillis() - relay.startTime) / 800f
            if (progress in 0f..1f) {
                val currentPos = relay.start + (relay.end - relay.start) * progress
                drawCircle(
                    color = StealthPrimary.copy(alpha = 0.8f * (1f - progress)),
                    radius = 3.dp.toPx(),
                    center = center + currentPos
                )
            }
        }
    }
}

@Composable
private fun VibesConnectivity(devices: List<P2PDevice>, onlyTies: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        // Draw connection lines between devices if relevant
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
    highlightedUserId: String?,
    subjectId: String?,
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
                    isVibed = isTied,
                    isSelected = isSelected,
                    isPeerVibed = isVibed,
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
private fun CrowdNodes(
    crowdList: List<Pair<P2PDevice, Int>>, 
    vibedPeers: Set<String>,
    onCrowdClick: (P2PDevice) -> Unit,
    onCrowdLongClick: (P2PDevice) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        crowdList.forEachIndexed { index, (device, count) ->
            val radiusValue = if (crowdList.size == 1) 0f else 90f
            val angle = (index.toDouble() / crowdList.size) * 2 * PI
            
            val xOffset = (radiusValue * cos(angle)).toFloat().dp
            val yOffset = (radiusValue * sin(angle)).toFloat().dp
            
            VibeCrowdSignature(
                device = device,
                vibeCount = count,
                isVibed = device.id in vibedPeers,
                modifier = Modifier
                    .offset(xOffset, yOffset)
                    .clickable { onCrowdClick(device) }
            )
        }
    }
}


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
        modifier = Modifier.offset(xOffset, yOffset),
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
            projectionEmoji = projectionEmoji,
            modifier = Modifier
                .testTag("PersonaNode_$key")
                .onGloballyPositioned { 
                    val center = Offset(it.size.width / 2f, it.size.height / 2f)
                    val current = coordinates[key] ?: PersonaConnectionPoints()
                    coordinates[key] = current.copy(
                        field = it.positionInRoot() + center,
                        vibe = if (isVibing) it.positionInRoot() + center else null
                    )
                },
            onClick = onClick,
            onLongClick = onLongClick
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

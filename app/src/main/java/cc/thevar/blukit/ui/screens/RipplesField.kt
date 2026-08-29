/**
 * BLUKIT UI: RIPPLES FIELD (THE DISCOVERY RADAR)
 *
 * A high-fidelity spatial view of the local mesh energy.
 * Orchestrates the "Spectral Radar" where users, Crowds, and relay events are visualized.
 * 
 * Architectural Patterns:
 * - Spatial Positioning: Maps signal strength (proximityFactor) to orbital radius.
 * - Crowd Canvas: Surfaces high-resonance pulses determined by swarm consensus.
 * - Relay Animations: Visualizes pulses as traveling dots between nodes.
 * - Pulse Ripples: Context-aware circles expanding from nodes when energy is emitted.
 * - Spectral Dimming: Intelligently dims background nodes to focus user focus.
 * - Atmospheric Heatmap: Background glow intensity based on aggregate mesh activity.
 */
package cc.thevar.blukit.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.Resonance
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

/** Metadata for local pulse visualizations (Active Bubbles). */
data class BubbleData(
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val messageId: String,
    val isPrivate: Boolean
)

/** Transient state for mesh relay animations (Travel dots). */
data class RelayEvent(
    val id: String,
    val start: Offset,
    val end: Offset,
    val startTime: Long,
    val color: Color = StealthPrimary
)

/** Expanding rings signaling node energy emission. */
data class PulseRipple(
    val id: String,
    val center: Offset,
    val startTime: Long,
    val color: Color
)

/**
 * The root spatial interaction layer.
 */
@Composable
fun RipplesField(
    state: BluetoothUiState,
    activeBubbles: List<BubbleData>,
    onDeviceClick: (P2PDevice) -> Unit,
    modifier: Modifier = Modifier,
    selectedDevices: Set<String> = emptySet(),
    pulsedPeers: Set<String> = emptySet(),
    externalEnergy: Float = 0f,
    onlyTies: Boolean = false,
    isFilterMode: Boolean = false,
    lowPowerMode: Boolean = false,
    highlightedUserId: String? = null,
    subjectId: String? = null,
    pulseGhostData: GhostPulseData? = null,
    onDismissGhost: () -> Unit = {},
    onDeviceLongClick: (P2PDevice) -> Unit = {},
    onPulseSurge: (Float) -> Unit = {},
    crowdList: List<Pair<P2PDevice, Int>> = emptyList(),
    onSearchToggle: (() -> Unit)? = null,
    isSearchActive: Boolean = false,
    // --- Humanity Stage Props (Row 1) ---
    title: String = "",
    breadcrumbTrail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    userNickname: String = "",
    userEmoji: String = "",
    activeCrowds: List<Resonance> = emptyList(),
    onShowTimeline: () -> Unit = {},
    onResetProfile: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    onNicknameChange: (String) -> Unit = {},
    themeColor: Color = StealthPrimary,
    isDimmed: Boolean = false,
    isVaulted: Boolean = false,
    isSeniorVault: Boolean = false,
    content: @Composable () -> Unit = {},
    airRitualGhost: @Composable () -> Unit = {}
) {
    val density = LocalDensity.current

    // Internal animation registries
    val relayEvents = remember { mutableStateListOf<RelayEvent>() }
    val pulseRipples = remember { mutableStateListOf<PulseRipple>() }
    val processedRelayIds = remember { mutableSetOf<String>() }
    var collectiveEnergy by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }

    // TRIGGER: Signal surge on new incoming bubbles
    LaunchedEffect(activeBubbles.size) {
        if (activeBubbles.isNotEmpty()) {
            val last = activeBubbles.last()
            if (last.messageId !in processedRelayIds) {
                processedRelayIds.add(last.messageId)
                collectiveEnergy = (collectiveEnergy + 0.35f).coerceAtMost(1.0f)
                
                val deviceIndex = state.crowd.scannedDevices.indexOfFirst { it.id == last.senderId }
                val proximity = if (deviceIndex != -1) state.crowd.scannedDevices[deviceIndex].proximityFactor else 0.5f
                onPulseSurge(proximity)

                // Calculate spatial offset for the sender node
                val targetOffset = if (deviceIndex != -1) {
                    val device = state.crowd.scannedDevices[deviceIndex]
                    val maxRadiusPx = with(density) { 140.dp.toPx() }
                    val radiusValue = (1f - device.proximityFactor) * maxRadiusPx + with(density) { 60.dp.toPx() }
                    val angle = (deviceIndex.toDouble() / state.crowd.scannedDevices.size) * 2 * PI
                    Offset((radiusValue * cos(angle)).toFloat(), (radiusValue * sin(angle)).toFloat())
                } else Offset.Zero

                // Spawn travel dot from random mesh direction
                val startOffset = Offset((Random.nextFloat() - 0.5f) * 1200f, (Random.nextFloat() - 0.5f) * 1800f)
                relayEvents.add(RelayEvent(last.messageId, startOffset, targetOffset, System.currentTimeMillis()))
                
                val rippleColor = if (last.isPrivate) StealthRose else StealthPrimary
                relayEvents.add(RelayEvent(last.messageId, startOffset, targetOffset, System.currentTimeMillis(), rippleColor))
                pulseRipples.add(PulseRipple(last.messageId, targetOffset, System.currentTimeMillis(), rippleColor))
            }
        }
    }

    // Animation Cleanup Loop
    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            relayEvents.removeAll { now - it.startTime > 800 }
            pulseRipples.removeAll { now - it.startTime > 2000 }
            collectiveEnergy = (collectiveEnergy - 0.04f).coerceAtLeast(0f)
            delay(100.milliseconds)
        }
    }

    val coordinates = LocalPersonaCoordinates.current

    val dimAlpha by animateFloatAsState(
        targetValue = if (isDimmed) 0.25f else 1.0f,
        animationSpec = tween(500),
        label = "SpectralDimming"
    )

    BlukitWidget(
        themeColor = themeColor,
        header = {
            // MERGED ROW 1 & 2: HUMANITY STAGE + TACTICAL RADAR CONTROLS
            Box(modifier = Modifier.graphicsLayer { alpha = dimAlpha }) {
                BlukitHumanityStage(
                    title = title,
                    breadcrumbTrail = breadcrumbTrail,
                    onCrumbClick = onCrumbClick,
                    activeCrowds = activeCrowds,
                    onShowTimeline = onShowTimeline,
                    onResetProfile = onResetProfile,
                    onTitleClick = onTitleClick,
                    onBack = onBack,
                    themeColor = themeColor,
                    userCount = state.crowd.scannedDevices.size,
                    isVaulted = isVaulted,
                    isSeniorVault = isSeniorVault,
                    trailingContent = {
                        // Tactical Radar Toggles
                        if (onSearchToggle != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = onSearchToggle,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSearchActive) Icons.Rounded.WifiTethering else Icons.Rounded.Radar,
                                        contentDescription = "Toggle Search",
                                        tint = if (isSearchActive) StealthAmber else themeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = if (isSearchActive) "SEARCH" else "RADAR",
                                    fontSize = 5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = (if (isSearchActive) StealthAmber else themeColor).copy(alpha = 0.5f),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                )
            }
        },
        entries = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.Transparent)
                    .onGloballyPositioned { 
                        // Synchronize spatial center with the coordinate registry
                        val centerPos = it.positionInRoot() + Offset(it.size.width / 2f, it.size.height / 2f)
                        val current = coordinates["YOU"] ?: PersonaConnectionPoints()
                        coordinates["YOU"] = current.copy(field = centerPos)
                    }, 
                contentAlignment = Alignment.Center
            ) {
                AtmosphericHeatmap(energy = collectiveEnergy, themeColor = themeColor)

                Box(modifier = Modifier.graphicsLayer { alpha = dimAlpha }) {
                    RelayLayer(relayEvents)
                }
                
                // MAIN RADAR NODES: Visualizing local peers
                RadarNodesLayer(
                    devices = if (onlyTies) state.crowd.scannedDevices.filter { it.isConnected } else state.crowd.scannedDevices,
                    onDeviceClick = onDeviceClick,
                    onDeviceLongClick = onDeviceLongClick,
                    selectedDevices = selectedDevices,
                    pulsedPeers = pulsedPeers,
                    bubbleSenders = remember(activeBubbles) { activeBubbles.map { it.senderId }.toSet() },
                    themeColor = themeColor,
                    density = density
                )

                PulseRippleLayer(pulseRipples)

                if (pulseGhostData != null) {
                    PulseGhost(data = pulseGhostData, onDismiss = onDismissGhost)
                }
                
                airRitualGhost()
                content()
            }
        },
        showGlow = false, 
        modifier = modifier
    )
}


@Composable
private fun RelayLayer(relays: List<RelayEvent>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        relays.forEach { relay ->
            val progress = (System.currentTimeMillis() - relay.startTime) / 800f
            if (progress in 0f..1f) {
                val currentPos = relay.start + (relay.end - relay.start) * progress
                drawCircle(
                    color = relay.color.copy(alpha = 0.8f * (1f - progress)),
                    radius = 3.dp.toPx(),
                    center = center + currentPos
                )
            }
        }
    }
}

@Composable
private fun PulseRippleLayer(ripples: List<PulseRipple>) {
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
private fun RadarNodesLayer(
    devices: List<P2PDevice>,
    onDeviceClick: (P2PDevice) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit,
    selectedDevices: Set<String>,
    pulsedPeers: Set<String>,
    bubbleSenders: Set<String>,
    themeColor: Color,
    density: androidx.compose.ui.unit.Density
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        devices.forEachIndexed { index, device ->
            val maxRadiusPx = with(density) { 140.dp.toPx() }
            // PROXIMITY mapping: closer signal = smaller orbital radius
            val radiusValue = (1f - device.proximityFactor) * maxRadiusPx + with(density) { 60.dp.toPx() }
            val angle = (index.toDouble() / devices.size.coerceAtLeast(1)) * 2 * PI
            
            val xOffset = (radiusValue * cos(angle)).toFloat()
            val yOffset = (radiusValue * sin(angle)).toFloat()
            
            Box(modifier = Modifier.offset(with(density) { xOffset.toDp() }, with(density) { yOffset.toDp() })) {
                PulsePersonaSignature(
                    device = device,
                    isPulsed = bubbleSenders.contains(device.id) || bubbleSenders.contains(device.persistentId),
                    isSelected = selectedDevices.contains(device.id),
                    isPeerPulsed = pulsedPeers.contains(device.id),
                    size = 40.dp,
                    themeColor = themeColor,
                    onClick = { onDeviceClick(device) },
                    onLongClick = { onDeviceLongClick(device) }
                )
            }
        }
    }
}

@Composable
private fun AtmosphericHeatmap(energy: Float, themeColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "HeatmapPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.8f * pulseScale
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    themeColor.copy(alpha = 0.15f * energy),
                    themeColor.copy(alpha = 0.05f * energy),
                    Color.Transparent
                ),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
    }
}


/**
 * MINI RADAR: A lightweight spatial view for a specific crowd context.
 * Unifies the spatial radar with the ticker entries.
 */
@Composable
fun CrowdMiniRadar(
    resonance: Resonance,
    members: List<P2PDevice>,
    modifier: Modifier = Modifier,
    themeColor: Color = StealthPrimary,
    isDefaultCrowd: Boolean = false,
    onDeviceClick: (P2PDevice) -> Unit = {},
    onDeviceLongClick: (P2PDevice) -> Unit = {},
    activeBubbles: List<BubbleData> = emptyList()
) {
    val bubbleSenders = remember(activeBubbles) { activeBubbles.map { it.senderId }.toSet() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isDefaultCrowd) {
            // --- DEFAULT CROWD: Unified Anchor (Outside) + Horizontal Lineup ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // TACTICAL DIVIDER (Aligned with the row's left Persona anchor)
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(themeColor.copy(alpha = 0.2f))
                )

                // 3. REMAINING USERS LINED UP HORIZONTALLY
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy((-12).dp) // Tactical overlapping for 32dp nodes
                ) {
                    members.take(10).forEach { device ->
                        PulsePersonaSignature(
                            device = device,
                            isPulsed = bubbleSenders.contains(device.id) || bubbleSenders.contains(device.persistentId),
                            isSelected = false,
                            isPeerPulsed = false,
                            size = 32.dp,
                            isStatic = false,
                            themeColor = themeColor,
                            subLabel = "USER",
                            onClick = { onDeviceClick(device) },
                            onLongClick = { onDeviceLongClick(device) }
                        )
                    }
                    if (members.size > 10) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .border(0.5.dp, themeColor.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${members.size - 10}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = themeColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        } else {
            // --- USER-OWNED CROWD: Orbital lineup around Owner ---
            val owner = members.find { it.id == resonance.ownerId || it.persistentId == resonance.ownerId }
            val centerEmoji = owner?.emoji ?: resonance.projectionEmoji ?: "⚡"

            Box(modifier = Modifier.zIndex(2f)) {
                PulsePersonaSignature(
                    device = P2PDevice(id = "OWNER", name = owner?.name ?: resonance.name, emoji = centerEmoji),
                    isPulsed = owner?.let { bubbleSenders.contains(it.id) || bubbleSenders.contains(it.persistentId) } ?: false,
                    isSelected = false,
                    isPeerPulsed = false,
                    size = 48.dp,
                    isStatic = false,
                    themeColor = themeColor,
                    subLabel = if (owner == null) "EVENT" else "OWNER",
                    onClick = { owner?.let { onDeviceClick(it) } }
                )
            }

            val others = members.filter { it.id != resonance.ownerId && it.persistentId != resonance.ownerId }
            others.take(8).forEachIndexed { index, device ->
                val radius = 48f // Increased radius for 32dp nodes
                val angle = (index.toDouble() / others.size.coerceAtLeast(1)) * 2 * PI
                val xOffset = (radius * cos(angle)).toFloat().dp
                val yOffset = (radius * sin(angle)).toFloat().dp

                Box(modifier = Modifier.offset(xOffset, yOffset)) {
                    PulsePersonaSignature(
                        device = device,
                        isPulsed = bubbleSenders.contains(device.id) || bubbleSenders.contains(device.persistentId),
                        isSelected = false,
                        isPeerPulsed = false,
                        size = 32.dp,
                        isStatic = false,
                        themeColor = themeColor,
                        subLabel = "USER",
                        onClick = { onDeviceClick(device) },
                        onLongClick = { onDeviceLongClick(device) }
                    )
                }
            }
        }
    }
}

package cc.thevar.blukit.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
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
import cc.thevar.blukit.domain.model.Resonance
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

data class PulseRipple(
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
    pulsedPeers: Set<String> = emptySet(),
    externalEnergy: Float = 0f,
    onlyTies: Boolean = false,
    isFilterMode: Boolean = false,
    lowPowerMode: Boolean = false,
    highlightedUserId: String? = null,
    subjectId: String? = null,
    pulseGhostData: GhostPulseData? = null,
    onDismissGhost: () -> Unit = {},
    onDeviceClick: (P2PDevice) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit = {},
    onPulseSurge: (Float) -> Unit = {},
    drawBackground: Boolean = true,
    drawNodes: Boolean = true,
    crowdList: List<Pair<P2PDevice, Int>> = emptyList(),
    onSearchToggle: (() -> Unit)? = null,
    isSearchActive: Boolean = false,
    // HUMANITY STAGE PROPS
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
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
    airRitualGhost: @Composable () -> Unit = {}
) {
    val density = LocalDensity.current

    val relayEvents = remember { mutableStateListOf<RelayEvent>() }
    val pulseRipples = remember { mutableStateListOf<PulseRipple>() }
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
                onPulseSurge(proximity)

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
                pulseRipples.add(PulseRipple(last.messageId, targetOffset, System.currentTimeMillis(), rippleColor))
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            relayEvents.removeAll { now - it.startTime > 800 }
            pulseRipples.removeAll { now - it.startTime > 2000 }
            if (collectiveEnergy > 0f) collectiveEnergy = (collectiveEnergy - 0.04f).coerceAtLeast(0f)
            delay(100)
        }
    }

    val finalEnergy = (collectiveEnergy + externalEnergy).coerceAtMost(1.0f)
    val finalThemeColor = themeColor

    val coordinates = LocalPersonaCoordinates.current

    val dimAlpha by animateFloatAsState(
        targetValue = if (isDimmed) 0.25f else 1.0f,
        animationSpec = tween(500),
        label = "SpectralDimming"
    )

    BlukitWidget(
        themeColor = finalThemeColor,
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
                    themeColor = finalThemeColor,
                    userCount = state.crowd.scannedDevices.size,
                    trailingContent = {
                        // Tactical Toggles
                        if (onSearchToggle != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = onSearchToggle,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSearchActive) Icons.Rounded.WifiTethering else Icons.Rounded.Radar,
                                        contentDescription = "Toggle Search",
                                        tint = if (isSearchActive) StealthAmber else finalThemeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = if (isSearchActive) "SEARCH" else "RADAR",
                                    fontSize = 5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = (if (isSearchActive) StealthAmber else finalThemeColor).copy(alpha = 0.5f),
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
                    .height(320.dp) // Fixed height for radar field
                    .background(Color.Transparent)
                    .onGloballyPositioned { 
                        val centerPos = it.positionInRoot() + Offset(it.size.width / 2f, it.size.height / 2f)
                        val current = coordinates["YOU"] ?: PersonaConnectionPoints()
                        coordinates["YOU"] = current.copy(field = centerPos)
                    }, 
                contentAlignment = Alignment.Center
            ) {
                if (drawBackground) {
                    Box(modifier = Modifier.graphicsLayer { alpha = dimAlpha }) {
                        AirBackground(energy = finalEnergy, lowPowerMode = lowPowerMode, onlyTies = onlyTies)
                        AtmosphericHeatmap(intensity = state.activity.energyIntensity)
                    }
                }
                
                Box(modifier = Modifier.graphicsLayer { alpha = dimAlpha }) {
                    RelayLayer(relayEvents, onlyTies = onlyTies)
                }
                
                val bubbleSenders = remember(activeBubbles) { 
                    activeBubbles.asSequence().map { it.senderId }.toSet() 
                }
                val displayDevices = if (onlyTies) {
                    state.crowd.scannedDevices.filter { 
                        it.id in state.session.connectedTies || 
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
                            Box(modifier = Modifier.graphicsLayer { alpha = dimAlpha }) {
                                CrowdNodes(
                                    crowdList = crowdList,
                                    pulsedPeers = pulsedPeers,
                                    onCrowdClick = onDeviceClick,
                                    onCrowdLongClick = onDeviceLongClick
                                )
                            }
                        }

                        PulseNodes(
                            state = state,
                            devices = displayDevices,
                            connectedTies = state.session.connectedTies,
                            selectedDevices = selectedDevices,
                            pulsedPeers = pulsedPeers,
                            activeBubbles = activeBubbles,
                            onlyTies = onlyTies,
                            isFilterMode = isFilterMode,
                            highlightedUserId = highlightedUserId,
                            subjectId = subjectId,
                            userNickname = userNickname,
                            userEmoji = userEmoji,
                            title = title,
                            onDeviceClick = onDeviceClick,
                            onDeviceLongClick = onDeviceLongClick,
                            onNicknameChange = onNicknameChange,
                            isDimmed = isDimmed
                        )
                    }
                }

                PulseRippleLayer(pulseRipples)

                if (pulseGhostData != null) {
                    PulseGhost(data = pulseGhostData, onDismiss = onDismissGhost)
                }
                
                airRitualGhost()
                
                content()
            }
        },
        showGlow = false, // Background handles glow
        modifier = modifier
    )
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
private fun PulseArcs(devices: List<P2PDevice>, energy: Float, onlyTies: Boolean) {
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
private fun PulseConnectivity(devices: List<P2PDevice>, onlyTies: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        // Draw connection lines between devices if relevant
    }
}

@Composable
private fun PulseNodes(
    state: BluetoothUiState,
    devices: List<P2PDevice>,
    connectedTies: Set<String>,
    selectedDevices: Set<String>,
    pulsedPeers: Set<String>,
    activeBubbles: List<BubbleData>,
    onlyTies: Boolean,
    isFilterMode: Boolean,
    highlightedUserId: String?,
    subjectId: String?,
    userNickname: String,
    userEmoji: String,
    title: String,
    onDeviceClick: (P2PDevice) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit,
    onNicknameChange: (String) -> Unit,
    isDimmed: Boolean = false
) {
    val coordinates = LocalPersonaCoordinates.current
    
    val nodeDimAlpha by animateFloatAsState(
        targetValue = if (isDimmed) 0.4f else 1.0f,
        animationSpec = tween(500),
        label = "NodeDimming"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // 1. CENTER: RESONANCE IDENTITY
        val resonance = state.session.groups.find { it.name == title || it.id == title }
        val isRoot = title == "THE CROWD" || title == "EVENT"
        val centerCount = if (isRoot) state.crowd.scannedDevices.size else resonance?.allMemberIds?.size ?: state.crowd.scannedDevices.size
        
        // Resolve Owner Persona if exists
        val owner = state.crowd.scannedDevices.find { it.id == resonance?.ownerId || it.persistentId == resonance?.ownerId }

        val centerIcon = when {
            isRoot -> Icons.Rounded.Grain
            resonance?.scope == Resonance.SCOPE_PRIVATE -> Icons.Rounded.Hearing
            else -> null
        }
        
        val centerEmoji = when {
            owner != null -> owner.emoji
            resonance?.projectionEmoji != null -> resonance.projectionEmoji
            resonance?.templateId != null -> cc.thevar.blukit.domain.model.CrowdTemplates.ALL.find { it.id == resonance.templateId }?.iconEmoji ?: "⚡"
            else -> "⚡"
        }

        Box(modifier = Modifier.graphicsLayer { alpha = nodeDimAlpha }) {
            PulseCrowdSignature(
                device = P2PDevice(id = "CONTEXT", name = title, emoji = centerEmoji ?: "⚡"),
                pulseCount = centerCount,
                isPulsed = false,
                size = 64.dp,
                icon = centerIcon,
                title = title
            )
        }

        // 2. NEARBY: YOUR PERSONA
        val userRadius = 52f
        val userAngle = -PI / 2 // Anchored to top-ish
        val userX = (userRadius * cos(userAngle)).toFloat().dp
        val userY = (userRadius * sin(userAngle)).toFloat().dp
        
        val isIdentitySet = userNickname.isNotBlank() && userNickname != "?" && userNickname != "SET NAME"
        
        Box(
            modifier = Modifier
                .offset(userX, userY)
                .onGloballyPositioned { 
                    val center = Offset(it.size.width / 2f, it.size.height / 2f)
                    val current = coordinates["YOU"] ?: PersonaConnectionPoints()
                    coordinates["YOU"] = current.copy(field = it.positionInRoot() + center) 
                }
        ) {
            PulsePersonaSignature(
                device = P2PDevice(id = "YOU", name = if (isIdentitySet) userNickname else "YOU", emoji = userEmoji),
                isPulsed = false,
                isSelected = false,
                isPeerPulsed = false,
                onlyTies = false,
                size = 46.dp, // Slightly larger
                isStatic = false, 
                onClick = { onNicknameChange(userNickname) }
            )
        }

        // 3. OTHER USERS: Surrounding the cluster
        devices.forEachIndexed { index, device ->
            val baseRadius = (1f - device.proximityFactor) * 80f + 110f
            val angle = (index.toDouble() / devices.size) * 2 * PI
            val activeBubble = activeBubbles.findLast { it.senderId == device.id }
            val isTied = device.id in connectedTies
            val isPulsed = device.persistentId in pulsedPeers || device.id in pulsedPeers
            val isSelected = device.id in selectedDevices
            
            val xOffset = (baseRadius * cos(angle)).toFloat().dp
            val yOffset = (baseRadius * sin(angle)).toFloat().dp

            val isFocused = isPulsed || isTied || isSelected || device.id == subjectId || device.persistentId == subjectId
            val isBroadFocus = isFilterMode && pulsedPeers.isEmpty() && subjectId == null
            val noiseDimAlpha = if (isFilterMode && !isFocused && !isBroadFocus) 0.15f else if (isDimmed && !isSelected) 0.3f else 1f

            Box(modifier = Modifier.graphicsLayer { alpha = noiseDimAlpha }) {
                PulseNode(
                    device = device, 
                    isPulsed = isTied,
                    isSelected = isSelected,
                    isPeerPulsed = isPulsed,
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
    pulsedPeers: Set<String>,
    onCrowdClick: (P2PDevice) -> Unit,
    onCrowdLongClick: (P2PDevice) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        crowdList.forEachIndexed { index, (device, count) ->
            val radiusValue = if (crowdList.size == 1) 0f else 90f
            val angle = (index.toDouble() / crowdList.size) * 2 * PI
            
            val xOffset = (radiusValue * cos(angle)).toFloat().dp
            val yOffset = (radiusValue * sin(angle)).toFloat().dp
            
            PulseCrowdSignature(
                device = device,
                pulseCount = count,
                isPulsed = device.id in pulsedPeers,
                modifier = Modifier
                    .offset(xOffset, yOffset)
                    .clickable { onCrowdClick(device) }
            )
        }
    }
}


@Composable
private fun PulseNode(
    device: P2PDevice, 
    isPulsed: Boolean,
    isSelected: Boolean,
    isPeerPulsed: Boolean,
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
    val activePulseId = LocalActivePulseId.current.value
    val key = device.persistentId ?: device.id
    val isPulsing = activePulseId == key
    val nodeSize = if (device.proximityFactor > 0.8f) 64.dp else 52.dp

    Box(
        modifier = Modifier.offset(xOffset, yOffset),
        contentAlignment = Alignment.Center
    ) {
        PulsePersonaSignature(
            device = device,
            isPulsed = isPulsed,
            isSelected = isSelected,
            isPeerPulsed = isPeerPulsed,
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
                        pulse = if (isPulsing) it.positionInRoot() + center else null
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

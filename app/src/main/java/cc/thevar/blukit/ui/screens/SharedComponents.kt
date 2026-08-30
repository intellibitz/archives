/**
 * BLUKIT SHARED UI COMPONENTS
 *
 * This file acts as the primary library for reusable UI elements across the Blukit mesh.
 * It enforces the **Header + Entries** architectural pattern and utilizes the
 * **Resonance + Pulse** lexicon to define tactical interaction states.
 *
 * Design Philosophy:
 * - Summary-First visual paradigm.
 * - High-density, high-fidelity spectral aesthetics.
 * - Decoupled slots for flexible module composition.
 */
package cc.thevar.blukit.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cc.thevar.blukit.R
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.Resonance
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.components.AssignmentItem
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

/**
 * Spatial coordinates for Persona connections within the field.
 * Used by the global Canvas to draw resonance threads between hubs and nodes.
 */
data class PersonaConnectionPoints(
    val uph: Offset? = null,
    val field: Offset? = null,
    val ticker: Offset? = null,
    val pulse: Offset? = null,
)

val LocalPersonaCoordinates = staticCompositionLocalOf { mutableStateMapOf<String, PersonaConnectionPoints>() }
val LocalActivePulseId = staticCompositionLocalOf { mutableStateOf<String?>(null) }
val LocalUserEmoji = staticCompositionLocalOf { "👤" }

/** Metadata for local pulse visualizations (Active Bubbles). */
data class BubbleData(
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val messageId: String,
    val isPrivate: Boolean,
)

/** Transient state for mesh relay animations (Travel dots). */
data class RelayEvent(
    val id: String,
    val start: Offset,
    val end: Offset,
    val startTime: Long,
    val color: Color = StealthPrimary,
)

/** Expanding rings signaling node energy emission. */
data class PulseRipple(
    val id: String,
    val center: Offset,
    val startTime: Long,
    val color: Color,
)

/**
 * Provides a composite view of system radio statuses (BT, WiFi, GPS).
 * Features breathing animations for inactive or restricted radios.
 */
@Composable
fun MixedStatusBranding(
    isBluetoothOff: Boolean,
    isWifiOff: Boolean,
    onAwakenBluetooth: () -> Unit,
    onAwakenWifi: () -> Unit,
    isPermissionMissing: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        StatusIcon(icon = Icons.Rounded.Bluetooth, isOn = !isBluetoothOff, isWeak = false, isPermissionMissing = isPermissionMissing, onClick = onAwakenBluetooth, onColor = StealthAmber)
        StatusIcon(icon = Icons.Rounded.Wifi, isOn = !isWifiOff, isWeak = false, isPermissionMissing = false, onClick = onAwakenWifi, onColor = StealthAmber)
    }
}

/**
 * A minimalist real-time counter of active Ties within the current mesh context.
 * Utilizes a pulsing amber dot to signal active spectrum discovery.
 */
@Composable
fun CrowdTicker(modifier: Modifier = Modifier, title: String, resonances: List<Resonance> = emptyList()) {
    val infiniteTransition = rememberInfiniteTransition(label = "CrowdTicker")
    val alpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 0.7f, animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse), label = "Alpha")
    
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = title, 
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(StealthPrimary.copy(alpha = alpha), CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (resonances.isEmpty()) "universal frequency" else "${resonances.size} groups active", 
                    style = MaterialTheme.typography.labelSmall,
                    color = StealthPrimary.copy(alpha = StealthAlphaHigh)
                )
            }
        }
    }
}

/**
 * CROWD CANVAS: The spatial intelligence header for high-resonance pulses.
 * Features a horizontal row of high-priority energy nodes that "glow" based on consensus.
 */
@Composable
fun CrowdCanvas(
    highResonancePulses: List<MessagePayload>,
    themeColor: Color,
    onPulseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        highResonancePulses.forEach { pulse ->
            val infiniteTransition = rememberInfiniteTransition(label = "CanvasGlow")
            val glowScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "Pulse"
            )

            Surface(
                onClick = { onPulseClick(pulse.messageId) },
                color = StealthBlack.copy(alpha = StealthAlphaHigh),
                shape = CircleShape,
                border = BorderStroke(1.dp, themeColor.copy(alpha = StealthAlphaHigh)),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(36.dp)
                    .graphicsLayer {
                        scaleX = glowScale
                        scaleY = glowScale
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = pulse.senderEmoji ?: "🔥", fontSize = 16.sp)
                }
            }
        }
    }
}

/**
 * Tactical navigation landmark. Displays the nested path (e.g., EVENT > CROWD > CHAIN).
 * Supports direct crumb interactions for rapid hierarchy traversal.
 */
@Composable
fun BreadcrumbHub(
    trail: List<String>,
    onCrumbClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.Center
    ) {
        trail.forEachIndexed { index, crumb ->
            Text(
                text = crumb.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = (if (index == (trail.size - 1)) FontWeight.Black else FontWeight.Bold),
                    color = (if (index == (trail.size - 1)) Color.White else Color.White.copy(alpha = StealthAlphaHigh)),
                ),
                modifier = Modifier.clickable { onCrumbClick(index) }
            )
            if (index < (trail.size - 1)) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight, 
                    contentDescription = null, 
                    tint = Color.White.copy(alpha = 0.2f), 
                    modifier = Modifier.size(14.dp).padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
fun BlukitTacticalHeader(
    themeColor: Color,
    onAwakenBluetooth: () -> Unit,
    onAwakenWifi: () -> Unit,
    onGrantPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onShowPrivacy: () -> Unit,
    isBluetoothOff: Boolean = false,
    isWifiOff: Boolean = false,
    isPermissionMissing: Boolean = false,
    isPermanentlyDenied: Boolean = false,
    modifier: Modifier = Modifier,
    aiSummary: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "HarmonyCycle")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, 
        targetValue = 1f, 
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse), 
        label = "Alpha"
    )
    
    val scanLinePos by infiniteTransition.animateFloat(
        initialValue = -0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "ScanLine"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp)) // Standardized
            .background(StealthSurface)
            .border(1.dp, Color.White.copy(alpha = StealthAlphaBorder), RoundedCornerShape(28.dp))
    ) {
        // SCAN LINE HINT
        Box(
            modifier = Modifier
                .fillMaxWidth(0.3f)
                .matchParentSize()
                .align(Alignment.CenterStart)
                .graphicsLayer { translationX = scanLinePos * 1000f }
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        0.0f to Color.Transparent,
                        0.5f to themeColor.copy(alpha = 0.05f),
                        1.0f to Color.Transparent
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp), // Standardized padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: AI Insights
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(2f)) {
                Surface(
                    color = StealthAmber.copy(alpha = StealthAlphaLow),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, StealthAmber.copy(alpha = StealthAlphaMedium)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = StealthAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = aiSummary ?: "Scanning Mesh...", 
                            style = MaterialTheme.typography.labelSmall,
                            color = StealthAmber,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // CENTER: SETTINGS
            IconButton(onClick = onShowPrivacy, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Settings, 
                    contentDescription = "Settings", 
                    tint = Color.White.copy(alpha = StealthAlphaMedium),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // RIGHT: [STATUS LABEL] [RADIOS]
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.weight(1f)) {
                if (isPermissionMissing || isBluetoothOff) {
                    Surface(
                        color = StealthError.copy(alpha = StealthAlphaLow), 
                        shape = RoundedCornerShape(8.dp), 
                        modifier = Modifier.graphicsLayer { alpha = pulseAlpha }
                    ) {
                        Text(
                            text = when { isPermissionMissing -> if (isPermanentlyDenied) "SETUP" else "GRANT"; isBluetoothOff -> "AWAKE"; else -> "ACTIVE" }, 
                            style = MaterialTheme.typography.labelSmall.copy(color = StealthError), 
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).clickable { 
                                if (isPermissionMissing) { if (isPermanentlyDenied) onOpenSettings() else onGrantPermissions() }
                                else if (isBluetoothOff) onAwakenBluetooth()
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                MixedStatusBranding(
                    isBluetoothOff = isBluetoothOff,
                    isWifiOff = isWifiOff,
                    isPermissionMissing = isPermissionMissing,
                    onAwakenBluetooth = onAwakenBluetooth,
                    onAwakenWifi = onAwakenWifi
                )
            }
        }
    }
}

/**
 * HUMANITY STAGE: The contextual navigation and identity layer (Row 1).
 */
@Composable
fun BlukitHumanityStage(
    title: String,
    breadcrumbTrail: List<String>,
    onCrumbClick: (Int) -> Unit,
    activeCrowds: List<Resonance>,
    onShowTimeline: () -> Unit,
    onResetProfile: () -> Unit,
    onTitleClick: (() -> Unit)?,
    onBack: (() -> Unit)?,
    themeColor: Color,
    modifier: Modifier = Modifier,
    userCount: Int? = null,
    isVaulted: Boolean = false,
    isSeniorVault: Boolean = false,
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
    isDiscovery: Boolean = false,
    isLiveFeedMode: Boolean = false,
    onModeChange: (Boolean) -> Unit = {}
) {
    var showResetProfileDialog by remember { mutableStateOf(value = false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp) // Standard height
            .padding(horizontal = 16.dp), // Standardized padding
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // LEFT: Navigation & Switcher
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            modifier = Modifier.weight(1.8f),
            horizontalArrangement = Arrangement.Start
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = themeColor, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            if (isDiscovery) {
                // MODE SWITCHER: Groups vs Live Feed
                val infiniteTransition = rememberInfiniteTransition(label = "ModeGlow")
                val glowAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
                    label = "Glow"
                )

                Surface(
                    color = Color.White.copy(alpha = StealthAlphaLow),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = glowAlpha * 0.2f))
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        Surface(
                            onClick = { onModeChange(false) },
                            color = if (!isLiveFeedMode) themeColor.copy(alpha = StealthAlphaMedium * glowAlpha) else Color.Transparent,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text = "GROUPS", 
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (!isLiveFeedMode) StealthOnPrimary else Color.White.copy(alpha = StealthAlphaHigh)
                                )
                            }
                        }
                        Surface(
                            onClick = { onModeChange(true) },
                            color = if (isLiveFeedMode) themeColor.copy(alpha = StealthAlphaMedium * glowAlpha) else Color.Transparent,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text = "LIVE FEED", 
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isLiveFeedMode) StealthOnPrimary else Color.White.copy(alpha = StealthAlphaHigh)
                                )
                            }
                        }
                    }
                }
            } else if (breadcrumbTrail.isNotEmpty()) {
                BreadcrumbHub(trail = breadcrumbTrail, onCrumbClick = onCrumbClick)
            } else {
                CrowdTicker(title = title, resonances = activeCrowds)
            }

            if (userCount != null && !isDiscovery) {
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    color = themeColor.copy(alpha = StealthAlphaLow),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = StealthAlphaMedium))
                ) {
                    Text(
                        text = "$userCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = themeColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // RIGHT: TACTICAL TOOLS
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.End
        ) {
            trailingContent?.invoke(this)
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(start = 12.dp)) {
                IconButton(onClick = onShowTimeline, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.Timeline, contentDescription = "History", tint = themeColor, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = "HISTORY", 
                    style = MaterialTheme.typography.labelSmall,
                    color = themeColor.copy(alpha = StealthAlphaHigh)
                )
            }
            
            // Task 3: Live Feed Navigation
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(start = 16.dp)) {
                IconButton(
                    onClick = { onModeChange(!isLiveFeedMode) }, 
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isLiveFeedMode) Icons.Rounded.Stream else Icons.Rounded.Waves, 
                        contentDescription = "Live Feed", 
                        tint = if (isLiveFeedMode) StealthRose else themeColor, 
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "LIVE", 
                    style = MaterialTheme.typography.labelSmall,
                    color = (if (isLiveFeedMode) StealthRose else themeColor).copy(alpha = StealthAlphaHigh)
                )
            }
        }
    }

    if (showResetProfileDialog) {
        BlukitAlert(
            title = "RESET PROFILE?", 
            text = "THIS WILL CLEAR YOUR NAME BUT KEEP YOUR PULSES.", 
            confirmLabel = "RESET", 
            onConfirm = { 
                onResetProfile()
                showResetProfileDialog = false 
            }, 
            onDismiss = { showResetProfileDialog = false }
        )
    }
}

/**
 * PULSE HUB: The primary interaction point at the bottom of the field.
 * 
 * Features a contextual header for banners and
 * a row of entries for message tools (Attach, Search, Input) and system toggles.
 */
@Composable
fun BlukitPulseHub(
    currentRoute: Route,
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    pulseCount: Int,
    incomingRadioRequests: Set<P2PDevice>,
    selectedDevices: Set<String>,
    onAcceptRadio: (P2PDevice) -> Unit,
    onDenyRadio: (P2PDevice) -> Unit,
    onStartSidePulse: () -> Unit,
    onStartChain: () -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier,
    resonances: List<Resonance> = emptyList(),
    onAttachFile: () -> Unit = {},
    onSearchToggle: (() -> Unit)? = null,
    onManage: (() -> Unit)? = null,
    onNote: (() -> Unit)? = null,
    onCreatePublicResonance: ((String, String?) -> Unit)? = null,
    onTask: (() -> Unit)? = null, // NEW: For assignment creation
    isSearchMode: Boolean = false,
    onFocusChange: (Boolean) -> Unit = {},
    isStealthMode: Boolean = false,
    lowPowerMode: Boolean = false,
    onToggleStealth: (Boolean) -> Unit = {},
    onToggleLowPower: (Boolean) -> Unit = {}
) {
    val isPrivate = currentRoute is Route.Resonance || currentRoute is Route.GroupField
    val targetName = if (currentRoute is Route.GroupField) resonances.find { it.id == currentRoute.groupId }?.name?.uppercase() else null
    val themeColor = if (isPrivate) StealthRose else StealthPrimary

    BlukitWidget(
        themeColor = themeColor,
        header = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Selection Actions Bar
                AnimatedVisibility(
                    visible = selectedDevices.isNotEmpty(), 
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom), 
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .background(StealthBlack.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                            .border(1.dp, themeColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onStartSidePulse, 
                            colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black), 
                            shape = RoundedCornerShape(20.dp), // Softened
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) { 
                            Text("PRIVATE", fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp) 
                        }
                        Button(
                            onClick = onStartChain, 
                            colors = ButtonDefaults.buttonColors(containerColor = StealthRose, contentColor = Color.White), 
                            shape = RoundedCornerShape(20.dp), // Softened
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) { 
                            Text("NEW GROUP", fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp) 
                        }
                        IconButton(
                            onClick = onClearSelection, 
                            modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) { 
                            Icon(Icons.Rounded.Close, tint = Color.White, contentDescription = "Cancel", modifier = Modifier.size(16.dp)) 
                        }
                    }
                }
                
                // Contextual Creation Banner
                val showAirBanner = isSearchMode && messageText.isNotBlank() && onCreatePublicResonance != null
                AnimatedVisibility(
                    visible = showAirBanner, 
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    Button(
                        onClick = { onCreatePublicResonance?.invoke(messageText, null) },
                        colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(bottom = 12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Icon(Icons.Rounded.Grain, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("CREATE GROUP: ${messageText.uppercase()}", fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 0.5.sp)
                    }
                }

                // Incoming Requests Row
                if (incomingRadioRequests.isNotEmpty()) {
                    val request = incomingRadioRequests.first()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(StealthPrimary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .testTag("IncomingRequestRow"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = request.emoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "INCOMING RADIO REQUEST", 
                                style = MaterialTheme.typography.labelSmall, 
                                color = StealthPrimary,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = (request.name ?: "UNKNOWN").uppercase(), 
                                style = MaterialTheme.typography.bodySmall, 
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row {
                            IconButton(
                                onClick = { onDenyRadio(request) },
                                modifier = Modifier.testTag("DenyRequestButton")
                            ) { 
                                Icon(Icons.Rounded.Close, contentDescription = "Deny", tint = Color.Red.copy(alpha = 0.6f)) 
                            }
                            IconButton(
                                onClick = { onAcceptRadio(request) },
                                modifier = Modifier.testTag("AcceptRequestButton")
                            ) { 
                                Icon(Icons.Rounded.Check, contentDescription = "Accept", tint = StealthPrimary) 
                            }
                        }
                    }
                }
            }
        },
        entries = {
            val isPulseLocked = currentRoute is Route.Event
            Column(modifier = Modifier.fillMaxWidth()) {
                // SYSTEM TOGGLES (Moved from Top Bar)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EnvironmentToggle(label = "DARK", checked = isStealthMode, onCheckedChange = onToggleStealth, themeColor = themeColor)
                    Spacer(modifier = Modifier.width(16.dp))
                    EnvironmentToggle(label = "ECO", checked = lowPowerMode, onCheckedChange = onToggleLowPower, themeColor = themeColor)
                }

                BlukitInput(
                    isReadOnly = false, 
                    isPulseLocked = isPulseLocked,
                    isPrivate = isPrivate, 
                    targetName = targetName, 
                    value = messageText, 
                    onValueChange = onMessageChange, 
                    onSend = onSend, 
                    onAttachFile = onAttachFile, 
                    onManage = onManage,
                    onNote = onNote,
                    onTask = onTask, // NEW: Propagate to Input
                    pulseCount = pulseCount, 
                    isSearchActive = isSearchMode,
                    onSearchToggle = onSearchToggle,
                    onFocusChange = onFocusChange,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding()
                )
            }
        },
        modifier = modifier.zIndex(10f)
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
    // CONSTRAINT: Constrain background radar nodes to the upper 70% of the field
    // to avoid occlusion by the bottom Hub on shorter devices.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
            .padding(24.dp)
    ) {
        val maxRadiusPx = with(density) { 140.dp.toPx() }

        devices.forEachIndexed { index, device ->
            val id = device.persistentId ?: device.id
            val isPulsed = id in pulsedPeers
            val isSelected = device.id in selectedDevices
            val isBubbleSender = id in bubbleSenders
            
            // Calculate spatial position
            val proximity = device.proximityFactor
            val radiusValue = (1f - proximity) * maxRadiusPx + with(density) { 60.dp.toPx() }
            val angle = (index.toDouble() / devices.size.coerceAtLeast(1)) * 2 * Math.PI
            
            val offsetX = (radiusValue * Math.cos(angle)).toFloat()
            val offsetY = (radiusValue * Math.sin(angle)).toFloat()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = with(density) { offsetX.toDp() }, y = with(density) { offsetY.toDp() })
            ) {
                PulsePersonaSignature(
                    device = device,
                    isPulsed = isPulsed,
                    isSelected = isSelected,
                    isPeerPulsed = isBubbleSender,
                    size = 40.dp,
                    onClick = { onDeviceClick(device) },
                    onLongClick = { onDeviceLongClick(device) },
                    subLabel = device.name ?: "MEMBER"
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
            brush = androidx.compose.ui.graphics.Brush.radialGradient(
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
    val bubbleSenders = remember(activeBubbles) { activeBubbles.asSequence().map { it.senderId }.toSet() }

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
                            subLabel = "PEER",
                            onClick = { onDeviceClick(device) },
                            onLongClick = { onDeviceLongClick(device) }
                        )
                    }
                    if (members.size > 10) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(StealthBlack.copy(alpha = 0.5f))
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
                        subLabel = "PEER",
                        onClick = { onDeviceClick(device) },
                        onLongClick = { onDeviceLongClick(device) }
                    )
                }
            }
        }
    }
}

/**
 * BLUKIT WIDGET: The standardized tactical container for all mesh modules.
 * Strictly follows the Header + Entries architectural pattern.
 */
@Composable
fun BlukitWidget(
    entries: @Composable (ColumnScope.() -> Unit),
    modifier: Modifier = Modifier,
    header: @Composable (BoxScope.() -> Unit)? = null,
    themeColor: Color = StealthPrimary,
    showGlow: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WidgetGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f, 
        targetValue = 0.15f, 
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse),
        label = "Glow"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        if (header != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                header()
            }
        }
        
        Surface(
            color = StealthSurface.copy(alpha = 0.4f),
            shape = RoundedCornerShape(28.dp), // Standardized
            border = BorderStroke(1.dp, Color.White.copy(alpha = StealthAlphaBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.background(if (showGlow) themeColor.copy(alpha = glowAlpha) else Color.Transparent)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp), // Standardized
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    entries()
                }
            }
        }
    }
}

/**
 * TICKER SECTION HEADER: A tactical label for categorizing ticker contents.
 */
@Composable
fun TickerSectionHeader(
    title: String, 
    modifier: Modifier = Modifier,
    color: Color = StealthPrimary, 
    onAction: (() -> Unit)? = null,
    actionLabel: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = StealthAlphaHigh)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(color.copy(alpha = StealthAlphaLow))
            )
            
            if (onAction != null && actionLabel != null) {
                Text(
                    text = "OR",
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(color.copy(alpha = StealthAlphaLow))
                )
            }
        }

        if (onAction != null && actionLabel != null) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = actionLabel.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = color,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}

/**
 * PULSING RESONANCE TICKER: The life stream of the mesh.
 * 
 * Reorganizes flat pulse data into a structured hierarchy of Headers (Resonances)
 * and indented Entries (Pulses).
 */
@Composable
fun PulsingResonanceTicker(
    state: BluetoothUiState,
    energyList: List<Pair<P2PDevice, MessagePayload?>>,
    pulseCounts: Map<String, Int>,
    localDeviceId: String,
    pulsedPeers: Set<String>,
    onPulseClick: (String) -> Unit,
    onDeviceClick: (P2PDevice) -> Unit,
    onDeviceLongClick: (P2PDevice) -> Unit,
    modifier: Modifier = Modifier,
    localNickname: String = "?",
    activeBubbles: List<BubbleData> = emptyList(),
    isGrouped: Boolean = true,
    reverseLayout: Boolean = true,
    themeColor: Color = StealthPrimary,
    onPulseSurge: (Float) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val sdf = remember { SimpleDateFormat("HH:mm", java.util.Locale.getDefault()) }
    val density = LocalDensity.current

    // TACTICAL DEPTH: Dim background based on scroll or presence of items
    val isScrolling = listState.isScrollInProgress
    val hasContent = energyList.isNotEmpty()
    val backgroundAlpha = 0.8f

    // Internal animation registries (from RipplesField)
    val relayEvents = remember { mutableStateListOf<RelayEvent>() }
    val pulseRipples = remember { mutableStateListOf<PulseRipple>() }
    val processedRelayIds = remember { mutableSetOf<String>() }
    var collectiveEnergy by remember { mutableFloatStateOf(0f) }

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
                    val angle = (deviceIndex.toDouble() / state.crowd.scannedDevices.size.coerceAtLeast(1)) * 2 * PI
                    Offset((radiusValue * cos(angle)).toFloat(), (radiusValue * sin(angle)).toFloat())
                } else Offset.Zero

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
            kotlinx.coroutines.delay(100.milliseconds)
        }
    }

    Box(modifier = modifier) {
        // MODULE 1: CONTEXTUAL FIELD BACKGROUND (The Radar)
        Box(
            modifier = Modifier.fillMaxSize(), 
            contentAlignment = Alignment.Center
        ) {
            AtmosphericHeatmap(energy = collectiveEnergy, themeColor = themeColor)
            RelayLayer(relayEvents)
            
            RadarNodesLayer(
                devices = state.crowd.scannedDevices,
                onDeviceClick = onDeviceClick,
                onDeviceLongClick = onDeviceLongClick,
                selectedDevices = state.crowd.selectedDevices,
                pulsedPeers = pulsedPeers,
                bubbleSenders = remember(activeBubbles) { activeBubbles.asSequence().map { it.senderId }.toSet() },
                themeColor = themeColor,
                density = density
            )

            PulseRippleLayer(pulseRipples)
        }

        // TACTICAL DEPTH: Dim background based on scroll or presence of items
        val dimmingAlpha by animateFloatAsState(
            targetValue = if (isScrolling || hasContent) 0.95f else 0f,
            animationSpec = tween(500),
            label = "DimmingAlpha"
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StealthBlack.copy(alpha = dimmingAlpha))
        )

        // MODULE 2: THE TICKER
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            reverseLayout = reverseLayout,
            contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
        ) {
            items(energyList, key = { it.second?.messageId ?: it.first.id }) { (device, msg) ->
                val id = device.persistentId ?: device.id
                val resonance = state.session.groups.find { it.id == (msg?.groupId ?: device.id) }
                
                if (msg == null && resonance != null) {
                    // HEADER: High-level Resonance Summary
                    val members = if (resonance.id == Resonance.ID_GLOBAL) {
                        state.crowd.scannedDevices
                    } else {
                        state.crowd.scannedDevices.filter { it.id in resonance.allMemberIds || it.persistentId in resonance.allMemberIds }
                    }

                    val userCount = if (resonance.id == Resonance.ID_GLOBAL) {
                        state.crowd.scannedDevices.size
                    } else {
                        resonance.allMemberIds.size
                    }

                    val dynamicSubtitle = if (resonance.scope == Resonance.SCOPE_PUBLIC) "EVENT" else "PRIVATE CHAIN"
                    val userEmoji = LocalUserEmoji.current

                    ResonanceSummary(
                        title = resonance.name,
                        subtitle = dynamicSubtitle,
                        icon = if (resonance.scope == Resonance.SCOPE_PUBLIC) Icons.Rounded.Grain else Icons.Rounded.Hearing,
                        themeColor = if (resonance.scope == Resonance.SCOPE_PUBLIC) StealthPrimary else StealthRose,
                        count = userCount,
                        lastUpdate = sdf.format(Date(resonance.lastPulseTimestamp)),
                        onClick = { onPulseClick(resonance.id) },
                        showJoin = true,
                        aiTrend = device.statusLabel,
                        leftContent = if (resonance.id == Resonance.ID_GLOBAL) {
                            {
                                PulsePersonaSignature(
                                    device = P2PDevice(id = "YOU", name = localNickname, emoji = userEmoji),
                                    isPulsed = false,
                                    isSelected = false,
                                    isPeerPulsed = false,
                                    size = 44.dp,
                                    isStatic = false,
                                    themeColor = StealthPrimary,
                                    subLabel = "YOU",
                                    onClick = { onDeviceClick(P2PDevice(id = "YOU", name = localNickname, emoji = userEmoji)) }
                                )
                            }
                        } else null,
                        topContent = {
                            CrowdMiniRadar(
                                resonance = resonance,
                                members = members,
                                isDefaultCrowd = resonance.id == Resonance.ID_GLOBAL,
                                themeColor = if (resonance.scope == Resonance.SCOPE_PUBLIC) StealthPrimary else StealthRose,
                                onDeviceClick = onDeviceClick,
                                onDeviceLongClick = onDeviceLongClick,
                                activeBubbles = activeBubbles
                            )
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                } else {
                    // ENTRY: Atomic Message Item
                    val count = if (isGrouped) pulseCounts[id] ?: 0 else state.session.messages.count { it.parentMessageId == msg?.messageId }
                    
                    AnimatedPulseItem(
                        msg = msg,
                        isSelected = device.id in state.crowd.selectedDevices,
                        senderDevice = device,
                        pulseCount = count,
                        isPulsed = id in pulsedPeers,
                        isMe = msg?.senderId == localDeviceId || device.id == localDeviceId,
                        isGrouped = isGrouped,
                        isMutual = device.id in state.session.connectedTies,
                        rowId = id,
                        onPulseClick = { msg?.messageId?.let { onPulseClick(it) } ?: onDeviceLongClick(device) },
                        onDeviceLongClick = { onDeviceLongClick(device) },
                        topContent = {
                            // INTEGRATED RADAR: Each ticker entry gets a mini radar context if it's a grouped message
                            if (isGrouped && resonance != null) {
                                val members = state.crowd.scannedDevices.filter { it.id in resonance.allMemberIds || it.persistentId in resonance.allMemberIds }
                                CrowdMiniRadar(
                                    resonance = resonance,
                                    members = members,
                                    themeColor = if (resonance.scope == Resonance.SCOPE_PUBLIC) StealthPrimary else StealthRose,
                                    onDeviceClick = onDeviceClick,
                                    onDeviceLongClick = onDeviceLongClick,
                                    activeBubbles = activeBubbles,
                                    modifier = Modifier.height(60.dp).padding(vertical = 4.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RadioRequestTickerItem(device: P2PDevice, onAccept: (P2PDevice) -> Unit, onDeny: (P2PDevice) -> Unit) {
    Surface(
        color = StealthPrimary.copy(alpha = StealthAlphaLow), 
        shape = RoundedCornerShape(16.dp), 
        border = BorderStroke(1.dp, StealthPrimary.copy(alpha = StealthAlphaMedium)), 
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) { 
        Row(
            modifier = Modifier.padding(16.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) { 
            Text(text = device.emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) { 
                Text(
                    text = "RADIO REQUEST", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = StealthPrimary
                )
                Text(
                    text = device.name ?: "Peer", 
                    style = MaterialTheme.typography.titleMedium, 
                    color = Color.White
                ) 
            }
            Row { 
                IconButton(onClick = { onDeny(device) }) { 
                    Icon(Icons.Rounded.Close, contentDescription = "Deny", tint = StealthError) 
                }
                IconButton(onClick = { onAccept(device) }) { 
                    Icon(Icons.Rounded.Check, contentDescription = "Accept", tint = StealthPrimary) 
                } 
            } 
        } 
    }
}


/**
 * ANIMATED PULSE ITEM: The atomic unit of interaction in the ticker.
 * 
 * Features:
 * - Vertical thread lines for nested entries.
 * - Pulse-synced resonance dots.
 * - Context-aware theme colors (Amber/Rose).
 */
@Composable
fun AnimatedPulseItem(
    msg: MessagePayload?,
    isSelected: Boolean,
    senderDevice: P2PDevice?,
    pulseCount: Int,
    isPulsed: Boolean,
    isMe: Boolean,
    isGrouped: Boolean,
    isMutual: Boolean,
    rowId: String,
    onPulseClick: () -> Unit,
    onDeviceLongClick: () -> Unit,
    topContent: @Composable (() -> Unit)? = null
) {
    val coordinates = LocalPersonaCoordinates.current
    val timestamp = msg?.let { SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(Date(it.timestamp)) } ?: ""
    val themeColor = if (isMutual) StealthRose else if (isPulsed) StealthPrimary else Color.White
    val signatureDevice = senderDevice ?: P2PDevice(id = msg?.senderId ?: "", name = msg?.senderName ?: "MEMBER", emoji = msg?.senderEmoji ?: "👤", medium = P2PDevice.ConnectionMedium.BLUETOOTH)
    val isPlural = msg?.isMeta == true
    val isEntry = isGrouped && msg != null

    val infiniteTransition = rememberInfiniteTransition(label = "PulseEntry")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "DotAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp) // Refined rhythm
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onPulseClick, onLongClick = onDeviceLongClick)
            .background(if (isSelected) Color.White.copy(alpha = StealthAlphaLow) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp) // Standardized padding
    ) {
        if (topContent != null) {
            Box(modifier = Modifier.padding(start = 48.dp, bottom = 8.dp)) {
                topContent()
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ticker Connection Point & Entry Indicator
            Box(
                modifier = Modifier
                    .padding(start = 0.dp)
                    .width(20.dp)
                    .height(44.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isEntry) {
                    // Vertical connecting line for entries
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(themeColor.copy(alpha = 0.15f))
                    )
                    // Small dot for the entry (The Resonance Anchor)
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(themeColor.copy(alpha = 0.3f * dotAlpha), CircleShape)
                            .border(1.dp, themeColor.copy(alpha = 0.6f * dotAlpha), CircleShape)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .onGloballyPositioned { 
                            val center = Offset(it.size.width / 2f, it.size.height / 2f)
                            val current = coordinates[rowId] ?: PersonaConnectionPoints()
                            coordinates[rowId] = current.copy(ticker = it.positionInRoot() + center) 
                        }
                        .size(1.dp)
                )
            }

            // ENTRY: Timestamp on the left
            if (timestamp.isNotEmpty()) {
                Text(
                    text = timestamp, 
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f), 
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Persona Emoji
            Surface(
                modifier = Modifier.size(32.dp), // Increased size
                shape = CircleShape, 
                color = when { isSelected -> Color.White.copy(alpha = 0.2f); isMutual -> StealthRose.copy(alpha = 0.15f); isPulsed -> StealthPrimary.copy(alpha = 0.15f); else -> StealthSurfaceVariant }, 
                border = BorderStroke(1.dp, when { isSelected -> Color.White; isMutual -> StealthRose; isPulsed -> StealthPrimary; else -> Color.White.copy(alpha = 0.1f) })
            ) { 
                Box(
                    contentAlignment = Alignment.Center, 
                    modifier = Modifier.onGloballyPositioned { 
                        val center = Offset(it.size.width / 2f, it.size.height / 2f)
                        val current = coordinates[rowId] ?: PersonaConnectionPoints()
                        coordinates[rowId] = current.copy(uph = it.positionInRoot() + center) 
                    }
                ) { 
                    Text(text = signatureDevice.emoji, fontSize = 16.sp) 
                } 
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                if (msg?.type == MessagePayload.TYPE_ASSIGNMENT_TASK) {
                    AssignmentItem(
                        assignment = msg,
                        onStatusChange = { /* Propagated via PulseStore CRDT */ },
                        themeColor = if (isMutual) StealthRose else StealthPrimary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else if (msg != null) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isPlural) {
                                Icon(Icons.Rounded.BubbleChart, contentDescription = null, tint = themeColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = msg.content, 
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isEntry) Color.White.copy(alpha = 0.9f) else Color.White, 
                                maxLines = 2, // Allow wrapping
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))

                        val realSender = if (isMe) "You" else msg.senderName
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = realSender,
                                style = MaterialTheme.typography.bodySmall,
                                color = themeColor.copy(alpha = StealthAlphaHigh),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (pulseCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• $pulseCount replies",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = themeColor.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        val realSender = if (isMe) "You" else (senderDevice?.name ?: "?")
                        Text(
                            text = realSender,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "::", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "...", 
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            if (isMutual) { 
                Icon(imageVector = Icons.Rounded.Flare, contentDescription = null, tint = StealthRose.copy(alpha = 0.5f), modifier = Modifier.size(14.dp)) 
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun EnvironmentToggle(
    label: String, 
    checked: Boolean, 
    onCheckedChange: (Boolean) -> Unit, 
    themeColor: Color = StealthPrimary
) {
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (checked) 1f else 0.3f,
        animationSpec = tween(500),
        label = "IndicatorAlpha"
    )
    
    val glowAlpha by animateFloatAsState(
        targetValue = if (checked) 0.6f else 0f,
        animationSpec = tween(500),
        label = "IndicatorGlow"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically, 
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .graphicsLayer { alpha = indicatorAlpha }
    ) {
        // TACTICAL LED INDICATOR
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (checked) themeColor else Color.White.copy(alpha = 0.1f))
                .border(
                    width = 1.dp, 
                    color = if (checked) themeColor.copy(alpha = glowAlpha) else Color.Transparent, 
                    shape = CircleShape
                )
        )
        
        Spacer(modifier = Modifier.width(6.dp))
        
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp, 
                fontWeight = FontWeight.Black, 
                color = if (checked) Color.White else Color.White.copy(alpha = 0.6f), 
                letterSpacing = 1.sp
            )
        )
    }
}

@Composable
fun ConfirmationDialog(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    BlukitAlert(title = title, text = text, onConfirm = onConfirm, onDismiss = onDismiss)
}

/**
 * BLUKIT ALERT: The standard high-fidelity modal for system confirmations.
 * Features a notification-synced halo pulse animation.
 */
@Composable
fun BlukitAlert(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmLabel: String = "OK",
    dismissLabel: String = "CANCEL",
    themeColor: Color = StealthPrimary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AlertGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "PulseScale"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StealthSurface,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 8.dp,
        modifier = modifier.border(1.dp, themeColor.copy(alpha = StealthAlphaBorder), RoundedCornerShape(28.dp)),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = themeColor.copy(alpha = StealthAlphaLow),
                        modifier = Modifier.size(48.dp * pulseScale)
                    ) {}
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive, 
                        contentDescription = null, 
                        tint = themeColor, 
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title.uppercase(), 
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White, 
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                text = text, // Removed .uppercase()
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = StealthAlphaHigh),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColor,
                    contentColor = if (themeColor == StealthRose) Color.White else Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 8.dp).height(44.dp)
            ) {
                Text(
                    text = confirmLabel, 
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = dismissLabel, 
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }
    )
}

/**
 * Spectral tips to guide users through the mesh experience.
 * Displays a glowing spectral card with a tip text and a lightbulb icon.
 * Features a subtle "breathing" border glow.
 */
/**
 * SPECTRAL TIP: Glowing guidance cards for mesh onboarding and discovery.
 */
@Composable
fun RadioRequestEntry(
    device: P2PDevice,
    onAccept: () -> Unit,
    onDeny: () -> Unit,
    themeColor: Color = StealthPrimary
) {
    Surface(
        color = themeColor.copy(alpha = StealthAlphaLow),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, themeColor.copy(alpha = StealthAlphaMedium)),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = device.emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "RADIO REQUEST", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = themeColor
                )
                Text(
                    text = device.name ?: "Peer", 
                    style = MaterialTheme.typography.titleMedium, 
                    color = Color.White
                )
            }
            IconButton(onClick = onDeny) { Icon(Icons.Rounded.Close, contentDescription = "Deny", tint = StealthError) }
            IconButton(onClick = onAccept) { Icon(Icons.Rounded.Check, contentDescription = "Accept", tint = themeColor) }
        }
    }
}

@Composable
fun SunkPulseVault(
    archivedCrowds: List<Resonance>,
    onRestore: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StealthBlack,
        shape = RoundedCornerShape(28.dp),
        title = { 
            Text(
                text = "Sunk Pulse Vault", 
                style = MaterialTheme.typography.titleMedium,
                color = StealthPrimary
            ) 
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(archivedCrowds) { resonance ->
                    ResonanceSummary(
                        title = resonance.name,
                        subtitle = "Archived",
                        icon = Icons.Rounded.Unarchive,
                        themeColor = StealthGray,
                        count = -1,
                        lastUpdate = "SUNK",
                        onClick = { onRestore(resonance.id) }
                    )
                }
            }
        },
        confirmButton = { 
            TextButton(onClick = onDismiss) { 
                Text(
                    text = "CLOSE", 
                    style = MaterialTheme.typography.labelLarge,
                    color = StealthPrimary
                ) 
            } 
        }
    )
}
@Composable
fun BlukitTip(
    text: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    themeColor: Color = StealthAmber
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TipGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, 
        targetValue = 0.5f, 
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "GlowAlpha"
    )

    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .graphicsLayer { alpha = 0.98f },
        color = StealthSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, themeColor.copy(alpha = glowAlpha))
    ) {
        Row(
            modifier = Modifier.padding(16.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(themeColor.copy(alpha = StealthAlphaLow), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.TipsAndUpdates, 
                    contentDescription = null, 
                    tint = themeColor, 
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text, // Removed .uppercase()
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Close, 
                    contentDescription = "Dismiss", 
                    tint = Color.White.copy(alpha = 0.3f), 
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun PulseActionMenu(pulse: MessagePayload, isMe: Boolean, onInvite: () -> Unit, onDelete: () -> Unit, onDismiss: () -> Unit, onBroadcast: () -> Unit, onVote: (Int) -> Unit = {}) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StealthSurface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(text = pulse.senderEmoji ?: "💬", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = pulse.senderName, 
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Swarm Logic: Consensus Voting
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MenuActionItem(Icons.Rounded.ThumbUp, "Upvote", StealthPrimary, modifier = Modifier.weight(1f)) { onVote(1); onDismiss() }
                    MenuActionItem(Icons.Rounded.ThumbDown, "Downvote", StealthError.copy(alpha = 0.8f), modifier = Modifier.weight(1f)) { onVote(-1); onDismiss() }
                }

                if (isMe && pulse.pulseType == MessagePayload.PULSE_SILENCE) {
                    MenuActionItem(Icons.Rounded.Grain, "Broadcast to Crowd", StealthPrimary, onClick = onBroadcast)
                } else if (pulse.pulseType == MessagePayload.PULSE_SHOUT) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = StealthPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Already Broadcasted", 
                            style = MaterialTheme.typography.labelSmall,
                            color = StealthPrimary.copy(alpha = StealthAlphaHigh)
                        )
                    }
                }
                
                MenuActionItem(Icons.Rounded.Handshake, "Invite to Private", StealthRose, onClick = onInvite)
                MenuActionItem(Icons.Rounded.Delete, "Delete Pulse", StealthError, onClick = onDelete)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "BACK", 
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }
    )
}

@Composable
fun StatusIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, isOn: Boolean, isWeak: Boolean, isPermissionMissing: Boolean, size: Dp = 24.dp, forceWarning: Boolean = false, onColor: Color = StealthPrimary, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "StatusAnim")
    val alpha by infiniteTransition.animateFloat(initialValue = 0.4f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse), label = "Alpha")
    IconButton(onClick = onClick, modifier = Modifier.size(size)) {
        Icon(imageVector = icon, contentDescription = null, tint = when { isPermissionMissing || !isOn && !forceWarning -> Color.Red; forceWarning || isWeak -> Color.Yellow; else -> onColor }.copy(alpha = if (!isOn || isWeak || forceWarning) alpha else 1f), modifier = Modifier.size(size * 0.65f))
    }
}

@Composable
fun ResonanceSummary(
    title: String,
    subtitle: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    themeColor: Color,
    count: Int,
    lastUpdate: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showJoin: Boolean = false,
    leftContent: @Composable (() -> Unit)? = null,
    topContent: @Composable (() -> Unit)? = null,
    underIconContent: @Composable (ColumnScope.() -> Unit)? = null,
    aiTrend: String? = null // NEW: For showing synthesized trends
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PluralGlow")
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f, 
        targetValue = 0.15f, 
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse),
        label = "Glow"
    )

    Surface(
        onClick = { 
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            onClick() 
        },
        color = StealthSurface,
        shape = RoundedCornerShape(28.dp), 
        border = BorderStroke(1.dp, themeColor.copy(alpha = StealthAlphaBorder)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp), 
        tonalElevation = 8.dp
    ) {
        Box(modifier = Modifier.background(themeColor.copy(alpha = glowAlpha))) {
            Column {
                if (topContent != null) {
                    Box(modifier = Modifier.padding(top = 8.dp)) {
                        topContent()
                    }
                }
                Row(
                    modifier = Modifier.padding(16.dp), // Standardized internal padding
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT: [ICON]
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (leftContent != null) {
                            leftContent()
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(themeColor.copy(alpha = StealthAlphaLow), CircleShape)
                                    .border(1.dp, themeColor.copy(alpha = StealthAlphaMedium), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon, 
                                    contentDescription = null, 
                                    tint = themeColor, 
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        underIconContent?.invoke(this)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // CENTER: [COUNT + TITLE + SUBTITLE]
                    Column(modifier = Modifier.weight(1f)) {
                        if (count >= 0) {
                            Text(
                                text = "$count ${if (count == 1) "member" else "members"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = themeColor.copy(alpha = StealthAlphaHigh)
                            )
                        }
                        Text(
                            text = title, // Removed .uppercase()
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (subtitle != null || aiTrend != null) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                if (aiTrend != null) {
                                    Box(
                                        modifier = Modifier
                                            .background(StealthAmber.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = aiTrend.uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = StealthAmber
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                if (subtitle != null) {
                                    Text(
                                        text = subtitle, // Removed .uppercase()
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (subtitle == "SWARM REPORT" || subtitle == "AI SUMMARY") StealthAmber else themeColor.copy(alpha = StealthAlphaHigh),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    
                    // RIGHT: [ENTER + TIMESTAMP]
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (showJoin) {
                            Button(
                                onClick = { 
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    onClick() 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "JOIN",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = StealthOnPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Text(
                            text = lastUpdate,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonaOptionsMenu(
    device: P2PDevice,
    isTied: Boolean,
    isBlocked: Boolean,
    isRequesting: Boolean,
    activeGroupId: String? = null,
    isAlreadyInActiveGroup: Boolean = false,
    onPulse: () -> Unit,
    onAccept: () -> Unit,
    onDeny: () -> Unit,
    onDisconnect: () -> Unit,
    onSelect: () -> Unit,
    onIdentify: () -> Unit,
    onBlock: () -> Unit,
    onUnblock: () -> Unit,
    onSync: () -> Unit = {},
    onAddToGroup: (String) -> Unit = {},
    onRemoveFromGroup: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StealthSurface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(text = device.emoji, fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = device.name ?: "Peer", 
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isRequesting) {
                    MenuActionItem(Icons.Rounded.Handshake, "Accept Radio", StealthPrimary, onClick = onAccept)
                    MenuActionItem(Icons.Rounded.Close, "Deny Radio", StealthError, onClick = onDeny)
                } else if (activeGroupId != null) {
                    if (isAlreadyInActiveGroup) {
                        MenuActionItem(Icons.Rounded.PersonRemove, "Remove from Group", StealthRose, onClick = { onRemoveFromGroup(activeGroupId) })
                    } else {
                        MenuActionItem(Icons.Rounded.PersonAdd, "Add to this Group", StealthPrimary, onClick = { onAddToGroup(activeGroupId) })
                    }
                } else if (isTied) {
                    MenuActionItem(Icons.Rounded.Sync, "Sync Messages", StealthAmber, onClick = onSync)
                    MenuActionItem(Icons.Rounded.SettingsInputAntenna, "Disconnect", StealthRose, onClick = onDisconnect)
                } else {
                    MenuActionItem(Icons.Rounded.Hearing, "Private Message", StealthPrimary, onClick = onPulse)
                    MenuActionItem(Icons.Rounded.SettingsInputAntenna, "Connect", StealthRose, onClick = onSelect)
                }
                
                MenuActionItem(Icons.Rounded.Radar, "Identify", Color.White, onClick = onIdentify)
                if (isBlocked) MenuActionItem(Icons.Rounded.LockOpen, "Unblock User", StealthPrimary, onClick = onUnblock) 
                else MenuActionItem(Icons.Rounded.Block, "Block User", StealthError, onClick = onBlock)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "BACK", 
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }
    )
}

@Composable
fun MenuActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick, 
        color = Color.White.copy(alpha = 0.03f), 
        shape = RoundedCornerShape(12.dp), 
        modifier = modifier.fillMaxWidth()
    ) { 
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) { 
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label, 
                style = MaterialTheme.typography.labelLarge,
                color = color
            ) 
        } 
    }
}

data class GhostAction(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val color: Color,
    val onClick: () -> Unit
)

data class GhostPulseData(
    val emoji: String,
    val title: String,
    val subtitle: String? = null,
    val actions: List<GhostAction>,
    val themeColor: Color,
    val sourceId: String? = null
)

/**
 * PULSE GHOST: Orbiting action menu for tactical persona management.
 * Features a central persona core with satellite actions (Whisper, Identify, Sync).
 */
@Composable
fun PulseGhost(
    data: GhostPulseData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GhostPulseAnim")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Glow"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Pulse"
    )

    val coordinates = LocalPersonaCoordinates.current

    DisposableEffect(Unit) {
        onDispose {
            coordinates.remove("GHOST_PULSE")
            coordinates.remove("GHOST_SOURCE_ID")
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() }
            .background(StealthBlack.copy(alpha = 0.6f))
    ) {
        // Source connection point
        Box(
            modifier = Modifier
                .onGloballyPositioned { 
                    val center = Offset(it.size.width / 2f, it.size.height / 2f)
                    coordinates["GHOST_PULSE"] = PersonaConnectionPoints(field = it.positionInRoot() + center)
                }
                .size(1.dp)
        )

        // The Ghost Core
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer { 
                scaleX = pulseScale
                scaleY = pulseScale
            }
        ) {
            Surface(
                shape = CircleShape,
                color = StealthBlack,
                border = BorderStroke(2.dp, data.themeColor.copy(alpha = glowAlpha)),
                modifier = Modifier.size(120.dp),
                tonalElevation = 12.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = data.emoji, fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = data.title.uppercase(), 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Black, 
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            if (data.subtitle != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = data.themeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, data.themeColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = data.subtitle.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = data.themeColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Action Orbit
        Box(modifier = Modifier.size(340.dp), contentAlignment = Alignment.Center) {
            data.actions.forEachIndexed { index, action ->
                val angle = (index * (360f / data.actions.size)) - 90f
                val radius = 130.dp
                val x = (kotlin.math.cos(Math.toRadians(angle.toDouble())) * radius.value).dp
                val y = (kotlin.math.sin(Math.toRadians(angle.toDouble())) * radius.value).dp

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset(x = x, y = y)
                        .clickable { action.onClick(); onDismiss() }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = StealthBlack,
                        border = BorderStroke(1.5.dp, action.color.copy(alpha = 0.7f)),
                        modifier = Modifier.size(64.dp),
                        tonalElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = action.icon, 
                                contentDescription = null, 
                                tint = action.color, 
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = StealthBlack.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = action.label.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * IDENTITY RITUAL: The primary onboarding overlay for forming a tactical persona.
 */
@Composable
fun OnboardingGhost(
    nickname: String,
    emoji: String,
    onNicknameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    val infiniteTransition = rememberInfiniteTransition(label = "GhostAnim")
    val glowAlphaState = infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Glow"
    )
    val pulseScaleState = infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Pulse"
    )
    val glowAlpha = glowAlphaState.value
    val pulseScale = pulseScaleState.value

    val coordinates = LocalPersonaCoordinates.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StealthBlack.copy(alpha = 0.95f))
            .navigationBarsPadding()
            .imePadding()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        // Close button at top right
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.TopEnd) {
            IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.4f))
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .onGloballyPositioned { 
                    val center = Offset(it.size.width / 2f, it.size.height / 2f)
                    val current = coordinates["ONBOARDING"] ?: PersonaConnectionPoints()
                    coordinates["ONBOARDING"] = current.copy(field = it.positionInRoot() + center)
                }
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                Surface(
                    shape = CircleShape,
                    color = StealthAmber.copy(alpha = 0.15f * glowAlpha),
                    border = BorderStroke(2.dp, StealthAmber.copy(alpha = 0.5f * glowAlpha)),
                    modifier = Modifier.fillMaxSize()
                ) {}
                
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = StealthBlack,
                    border = BorderStroke(2.dp, StealthAmber),
                    tonalElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = emoji, fontSize = 36.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Surface(
                color = StealthBlack,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, StealthAmber.copy(alpha = 0.4f)),
                modifier = Modifier.padding(horizontal = 40.dp).clickable(enabled = false) {}
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "FORM YOUR PERSONA", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = StealthAmber
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    BasicTextField(
                        value = nickname,
                        onValueChange = onNicknameChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(color = Color.White, textAlign = TextAlign.Center, fontWeight = FontWeight.Black),
                        cursorBrush = SolidColor(StealthAmber),
                        decorationBox = { innerTextField ->
                            if (nickname.isEmpty()) {
                                Text(
                                    text = "ENTER NICKNAME", 
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White.copy(alpha = 0.2f), 
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            innerTextField()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Blukit is local and anonymous. Your persona stays on your device until you choose to emit energy into the mesh.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text(
                                text = "LURK", 
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                        
                        Button(
                            onClick = onDone,
                            enabled = nickname.isNotBlank() && nickname != "SET NAME",
                            colors = ButtonDefaults.buttonColors(containerColor = StealthAmber, contentColor = Color.Black),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1.5f).height(48.dp)
                        ) {
                            Text(
                                text = "DIVE IN", 
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * CROWD RITUAL: The creation overlay for establishing new public frequencies.
 */
@Composable
fun CrowdRitualGhost(
    onNameChange: (String) -> Unit,
    onDone: (String?) -> Unit, // Returns templateId if selected
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    nearbyAirs: List<Resonance> = emptyList(),
    onJoinAir: (String) -> Unit = {},
    title: String = "ESTABLISH FREQUENCY",
    hint: String = "TITLE YOUR FREQUENCY"
) {
    var airName by remember { mutableStateOf("") }
    var selectedTemplateId by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val infiniteTransition = rememberInfiniteTransition(label = "RitualAnim")
    val glowAlphaState = infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Glow"
    )
    val pulseScaleState = infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Pulse"
    )
    val glowAlpha = glowAlphaState.value
    val pulseScale = pulseScaleState.value

    val coordinates = LocalPersonaCoordinates.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            coordinates.remove("CROWD_RITUAL")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StealthBlack.copy(alpha = 0.85f))
            .navigationBarsPadding()
            .imePadding()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .onGloballyPositioned { 
                    val center = Offset(it.size.width / 2f, it.size.height / 2f)
                    val current = coordinates["CROWD_RITUAL"] ?: PersonaConnectionPoints()
                    coordinates["CROWD_RITUAL"] = current.copy(field = it.positionInRoot() + center)
                }
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                Surface(
                    shape = CircleShape,
                    color = StealthPrimary.copy(alpha = 0.15f * glowAlpha),
                    border = BorderStroke(2.dp, StealthPrimary.copy(alpha = 0.5f * glowAlpha)),
                    modifier = Modifier.fillMaxSize()
                ) {}
                
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = StealthBlack,
                    border = BorderStroke(2.dp, StealthPrimary),
                    tonalElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val iconEmoji = cc.thevar.blukit.domain.model.CrowdTemplates.ALL.find { it.id == selectedTemplateId }?.iconEmoji ?: "🌬️"
                        if (selectedTemplateId != null) {
                            Text(text = iconEmoji, fontSize = 32.sp)
                        } else {
                            Icon(Icons.Rounded.Grain, contentDescription = null, tint = StealthPrimary, modifier = Modifier.size(40.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Surface(
                color = Color.Black,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, StealthPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.padding(horizontal = 40.dp).clickable(enabled = false) {}
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = title, 
                        style = MaterialTheme.typography.labelSmall, 
                        color = StealthPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    BasicTextField(
                        value = airName,
                        onValueChange = { airName = it; onNameChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(color = Color.White, textAlign = TextAlign.Center),
                        cursorBrush = SolidColor(StealthPrimary),
                        decorationBox = { innerTextField ->
                            if (airName.isEmpty()) {
                                Text(
                                    text = hint, 
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White.copy(alpha = 0.2f), 
                                    textAlign = TextAlign.Center
                                )
                            }
                            innerTextField()
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Select Template", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = Color.White.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        cc.thevar.blukit.domain.model.CrowdTemplates.ALL.forEach { template ->
                            val isSelected = selectedTemplateId == template.id
                            Surface(
                                onClick = { selectedTemplateId = if (isSelected) null else template.id },
                                color = if (isSelected) StealthPrimary.copy(alpha = StealthAlphaMedium) else StealthSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isSelected) StealthPrimary else Color.White.copy(alpha = StealthAlphaBorder))
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = template.iconEmoji, fontSize = 18.sp)
                                    Text(
                                        text = template.name, 
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) StealthOnPrimary else Color.White
                                    )
                                }
                            }
                        }
                    }

                    if (nearbyAirs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Nearby Groups", 
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            nearbyAirs.take(3).forEach { air ->
                                Surface(
                                    onClick = { onJoinAir(air.id) },
                                    color = StealthPrimary.copy(alpha = StealthAlphaLow),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, StealthPrimary.copy(alpha = StealthAlphaMedium))
                                ) {
                                    Text(
                                        text = air.name, 
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StealthPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = { onDone(selectedTemplateId) },
                        enabled = airName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "CREATE", 
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
/**
 * PULSE PERSONA SIGNATURE: High-fidelity visual identity for users on the mesh.
 * Features proximity-based glow factor and connection state indicators.
 */
@Composable
fun PulsePersonaSignature(
    device: P2PDevice, 
    isPulsed: Boolean, 
    isSelected: Boolean, 
    isPeerPulsed: Boolean, 
    modifier: Modifier = Modifier,
    size: Dp = 52.dp, 
    isStatic: Boolean = false, 
    isHighlighted: Boolean = false, 
    projectionEmoji: String? = null,
    themeColor: Color? = null,
    subLabel: String? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NodeAnim")
    val isProjected = projectionEmoji != null
    val basePulse = if (isProjected) 1.6f else 1.15f
    val pulseScale by if (isStatic) { 
        remember { mutableFloatStateOf(1.0f) }
    } else { 
        val targetPulse = if (isHighlighted) 1.5f else if ((isPulsed || isPeerPulsed)) 1.25f else basePulse
        infiniteTransition.animateFloat(
            initialValue = 1.0f, 
            targetValue = targetPulse + (device.proximityFactor * 0.1f), 
            animationSpec = infiniteRepeatable(tween(if (isHighlighted) 500 else 2000 + (device.proximityFactor * 1000).toInt(), easing = FastOutSlowInEasing), RepeatMode.Reverse), 
            label = "Pulse",
        ) 
    }
    
    val personaThemeColor = themeColor ?: when {
        isHighlighted -> StealthAmber
        isProjected -> StealthRose
        isSelected -> Color.White
        isPulsed -> StealthRose
        isPeerPulsed -> StealthAmber
        else -> StealthPrimary
    }

    val proximityGlow = if (isStatic) 0f else (device.proximityFactor * 0.2f).coerceAtLeast(0f)
    val bloomBoost = if (isStatic) 0f else if (isHighlighted) 0.3f else if ((isPulsed || isPeerPulsed || isProjected)) 0.12f else 0f
    
    val isMe = device.id == "YOU"
    Box(modifier = modifier.size(size * 2.2f).combinedClickable(onClick = onClick, onLongClick = onLongClick), contentAlignment = Alignment.Center) {
        val haloAlpha = (if (isHighlighted || isMe) 0.3f else 0.08f + proximityGlow + bloomBoost) * pulseScale
        Surface(
            shape = CircleShape, 
            color = personaThemeColor.copy(alpha = haloAlpha.coerceAtMost(0.45f)),
            modifier = Modifier.size(size * pulseScale * (if (isProjected) 1.8f else 1.4f) + (proximityGlow + bloomBoost).dp)
        ) {}
        Surface(
            modifier = Modifier.size(size).clip(CircleShape), 
            color = when { isSelected -> Color.White.copy(alpha = 0.2f); isProjected || isPulsed -> StealthRose.copy(alpha = StealthAlphaLow); isPeerPulsed -> StealthAmber.copy(alpha = StealthAlphaLow); else -> StealthBlack }, 
            border = BorderStroke(if (isSelected || isPulsed || isPeerPulsed || isProjected || isMe) (size.value / 24).dp.coerceAtLeast(1.dp) else (size.value / 48).dp.coerceAtLeast(0.5.dp), when { isSelected || isMe -> Color.White; isProjected || isPulsed -> StealthRose; isPeerPulsed -> StealthAmber; else -> Color.White.copy(alpha = StealthAlphaBorder) }), 
            shape = CircleShape, 
            tonalElevation = if (isMe) 8.dp else 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (device.avatarPath != null) {
                    coil.compose.AsyncImage(
                        model = device.avatarPath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    val emojiToShow = projectionEmoji ?: device.emoji
                    if (device.avatarPath == null) {
                        if (emojiToShow.isNotBlank() && emojiToShow != "👤") {
                            Text(text = emojiToShow, fontSize = (size.value / 2).sp)
                        } else {
                            val mediumIcon = when (device.medium) { P2PDevice.ConnectionMedium.BLUETOOTH -> Icons.Rounded.Bluetooth; P2PDevice.ConnectionMedium.WIFI -> Icons.Rounded.Wifi; P2PDevice.ConnectionMedium.LOCATION -> Icons.Rounded.LocationOn }
                            val iconSize = (size.value / 2.5f).dp
                            val icon = if (isMe) Icons.Rounded.Face else if (device.isConnecting || device.isTiePending) Icons.Rounded.Sync else if (isSelected) Icons.Rounded.CheckCircle else mediumIcon
                            Icon(imageVector = icon, contentDescription = null, tint = when { isSelected || isMe -> Color.White; isPulsed -> StealthRose; isPeerPulsed -> StealthAmber; else -> Color.White.copy(alpha = StealthAlphaHigh) }, modifier = Modifier.size(iconSize))
                        }
                    }
                    
                    if (size >= 24.dp) {
                        val nameText = device.name ?: if (isMe) "YOU" else "?"
                        val displayText = if (isMe && nameText == "YOU") "YOU" else nameText.take(3).uppercase()
                        Text(
                            text = displayText, 
                            style = MaterialTheme.typography.labelSmall,
                            color = personaThemeColor.copy(alpha = StealthAlphaHigh)
                        )
                        
                        // Signal Strength
                        if (!isMe) {
                            Row(
                                modifier = Modifier.padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                val bars = (device.proximityFactor * 4).toInt().coerceAtLeast(1)
                                repeat(4) { i ->
                                    Box(
                                        modifier = Modifier
                                            .size(width = 2.dp, height = (2 + i * 2).dp)
                                            .background(if (i < bars) personaThemeColor else Color.White.copy(alpha = StealthAlphaLow))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // DECORATION: Label (YOU/MEMBER/CUSTOM)
        val finalLabel = subLabel ?: if (isMe) "YOU" else "MEMBER"
        if (size >= 48.dp) {
            Text(
                text = finalLabel,
                style = MaterialTheme.typography.labelSmall,
                color = personaThemeColor.copy(alpha = StealthAlphaHigh),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = (size.value * 0.12f).dp)
            )
        }
    }
}

@Composable
fun PulseCrowdSignature(
    device: P2PDevice, 
    pulseCount: Int, 
    isPulsed: Boolean, 
    modifier: Modifier = Modifier,
    size: Dp = 64.dp, 
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    title: String? = null
) {
    val themeColor = if (isPulsed) StealthRose else StealthPrimary
    val infiniteTransition = rememberInfiniteTransition(label = "CrowdPulse")
    val pulse by infiniteTransition.animateFloat(initialValue = 1.0f, targetValue = 1.2f, animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "Pulse")
    
    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size * 1.5f)) {
        Surface(shape = CircleShape, color = themeColor.copy(alpha = 0.05f * pulse), modifier = Modifier.size(size * pulse)) {}
        Surface(modifier = Modifier.size(size), shape = CircleShape, color = StealthBlack, border = BorderStroke(1.5.dp, themeColor.copy(alpha = 0.4f)), tonalElevation = 4.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { 
                if (icon != null) {
                    Icon(imageVector = icon, contentDescription = null, tint = themeColor, modifier = Modifier.size((size.value / 2.5f).dp))
                } else {
                    Text(text = device.emoji, fontSize = (size.value / 3).sp)
                }
                if (pulseCount > 0) { 
                    Text(
                        text = "$pulseCount ${if (pulseCount == 1) "MEMBER" else "MEMBERS"}", 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Black, 
                        color = themeColor,
                        letterSpacing = 0.5.sp
                    ) 
                } 
            } 
        }

        if (title != null && size > 40.dp) {
            Text(
                text = title.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 2.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
            )
        }
    }
}


@Composable
fun BlukitFieldScaffold(
    header: @Composable () -> Unit,
    entries: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    themeColor: Color = StealthPrimary,
    glowIntensityTarget: Float = 0.4f
) {
    Column(modifier = modifier.fillMaxSize().background(StealthBlack)) {
        header()
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            entries()
        }
    }
}

/**
 * The primary interaction point for spreading pulses.
 * Features a "breathing" nudge animation when empty and an "aura" glow when focused.
 * The separator line features a moving spectral gradient.
 */
/**
 * BLUKIT INPUT: The high-performance entry point for generating mesh energy.
 * 
 * Logic:
 * - Breathing Nudge: Encourages interaction when empty.
 * - Aura Glow: Signals active focus.
 * - Contextual Lock: Restricts pulses based on field scoping (e.g., Event landing).
 */
@Composable
fun BlukitInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    onAttachFile: () -> Unit = {},
    onManage: (() -> Unit)? = null,
    onNote: (() -> Unit)? = null,
    onTask: (() -> Unit)? = null, // NEW: For assignment creation
    pulseCount: Int = 0,
    isReadOnly: Boolean = false,
    isPulseLocked: Boolean = false,
    isPrivate: Boolean = false,
    targetName: String? = null,
    placeholder: String? = null,
    isSearchActive: Boolean = false,
    onSearchToggle: (() -> Unit)? = null,
    onFocusChange: (Boolean) -> Unit = {}
) {
    val themeColor = if (isPrivate) StealthRose else StealthPrimary
    val focusRequester = remember { FocusRequester() }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    var isFocused by remember { mutableStateOf(value = false) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "InputNudge")
    
    val nudgeGlow by infiniteTransition.animateFloat(
        initialValue = 0.05f, 
        targetValue = if (value.isEmpty() && !isFocused) 0.2f else 0.05f, 
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "NudgeGlow"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.8f else nudgeGlow,
        animationSpec = tween(500),
        label = "InputGlow"
    )

    val actualPlaceholder = when { 
        isSearchActive -> "Scan network: Type name or message..."
        isPulseLocked -> "Signal the mesh: Start a frequency..."
        placeholder != null -> placeholder 
        isReadOnly -> "INTERCEPTED" 
        isPrivate && targetName != null -> "Private message to $targetName..."
        isPrivate -> "Send a secure message..."
        else -> "Send a message to ${targetName ?: "the group"}..." 
    }
    
    Column(modifier = modifier) {
        // Glowing Dynamic Separator with Flowing Gradient
        val animOffset by infiniteTransition.animateFloat(
            initialValue = -1f, 
            targetValue = 2f, 
            animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)), 
            label = "SeparatorAnim"
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp) // Thinner
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        0.0f to Color.Transparent,
                        animOffset to themeColor.copy(alpha = glowAlpha),
                        animOffset + 0.2f to themeColor.copy(alpha = glowAlpha * 1.5f),
                        animOffset + 0.4f to Color.Transparent,
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(StealthBlack.copy(alpha = StealthAlphaOverlay))
                .padding(bottom = 12.dp, top = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) // Standardized
                    .background(Color.White.copy(alpha = 0.03f + (if(isFocused) 0.05f else 0f)), RoundedCornerShape(28.dp))
                    .border(
                        width = 1.dp, 
                        color = themeColor.copy(alpha = glowAlpha * 0.5f),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Actions Group
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onAttachFile, enabled = !isReadOnly, modifier = Modifier.size(44.dp)) { 
                        Icon(
                            imageVector = if (isPulseLocked) Icons.Rounded.Grain else Icons.Rounded.Add, 
                            contentDescription = if (isPulseLocked) "New Schedule" else "Attach", 
                            tint = if (isPulseLocked) StealthAmber else themeColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(22.dp)
                        ) 
                    }
                    if (onSearchToggle != null) {
                        IconButton(onClick = onSearchToggle, modifier = Modifier.size(44.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.Radar, 
                                contentDescription = "Scan", 
                                tint = if (isSearchActive) themeColor else Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp), 
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = value, 
                        onValueChange = onValueChange, 
                        enabled = !isReadOnly, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("SendPulseInput")
                            .focusRequester(focusRequester)
                            .onFocusChanged { 
                                isFocused = it.isFocused
                                onFocusChange(it.isFocused)
                            },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White), 
                        cursorBrush = SolidColor(if (isSearchActive) Color.White else themeColor), 
                        decorationBox = { innerTextField -> 
                            if (value.isEmpty()) { 
                                Text(
                                    text = actualPlaceholder, 
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.3f)
                                ) 
                            }
                            innerTextField() 
                        }
                    )
                }
                
                // End Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isSearchActive) {
                        if (pulseCount > 0) { 
                            Surface(
                                color = themeColor.copy(alpha = StealthAlphaLow),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text(
                                    text = pulseCount.toString(), 
                                    style = MaterialTheme.typography.labelSmall,
                                    color = themeColor, 
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) 
                            }
                        }
                        
                        if (onNote != null) {
                            IconButton(onClick = onNote, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.EditNote, 
                                    contentDescription = "Note", 
                                    tint = StealthRose.copy(alpha = 0.9f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        if (onTask != null) {
                            IconButton(onClick = onTask, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Assignment, 
                                    contentDescription = "Task", 
                                    tint = StealthAmber.copy(alpha = 0.9f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        if (isPrivate && !isReadOnly && onManage != null) {
                            IconButton(onClick = onManage, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.Groups, 
                                    contentDescription = "Manage", 
                                    tint = themeColor.copy(alpha = 0.8f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        
                        IconButton(
                            onClick = { 
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onSend()
                                focusManager.clearFocus() 
                            }, 
                            enabled = value.isNotBlank() && !isReadOnly && !isPulseLocked, 
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("SendPulseButton")
                        ) { 
                            val sendColor = if (value.isNotBlank() && !isPulseLocked) themeColor else Color.White.copy(alpha = 0.1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp)
                                    .background(sendColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Send, 
                                    contentDescription = "Send", 
                                    tint = if (value.isNotBlank()) Color.Black else Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(22.dp)
                                ) 
                            }
                        }
                    } else {
                        if (value.isNotBlank()) {
                            IconButton(onClick = { onValueChange("") }, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    imageVector = Icons.Rounded.Close, 
                                    contentDescription = "Clear", 
                                    tint = Color.White.copy(alpha = 0.3f), 
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

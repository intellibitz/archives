package cc.thevar.blukit.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Sphere
import cc.thevar.blukit.ui.theme.*

/**
 * Provides a composite view of system resonance statuses.
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

@Composable
fun StatusIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isOn: Boolean,
    isWeak: Boolean,
    isPermissionMissing: Boolean,
    onClick: () -> Unit,
    onColor: Color
) {
    IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when {
                isPermissionMissing -> StealthError.copy(alpha = StealthAlphaMedium)
                isOn -> onColor
                isWeak -> onColor.copy(alpha = StealthAlphaMedium)
                else -> Color.White.copy(alpha = StealthAlphaLow)
            },
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * A minimalist real-time counter of active Spheres within the current field context.
 */
@Composable
fun SphereTicker(title: String, modifier: Modifier = Modifier, spheres: List<Sphere> = emptyList()) {
    val infiniteTransition = rememberInfiniteTransition(label = "SphereTicker")
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
                val spheresLabel = if (spheres.isEmpty()) "nearby Sources" else "${spheres.size} Spheres active"
                Text(
                    text = spheresLabel, 
                    style = MaterialTheme.typography.labelSmall,
                    color = StealthPrimary.copy(alpha = StealthAlphaHigh)
                )
            }
        }
    }
}

/**
 * ECHO CANVAS: The spatial intelligence header for high-resonance Echoes.
 */
@Composable
fun EchoCanvas(
    highResonanceEchoes: List<Echo>,
    themeColor: Color,
    onEchoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        highResonanceEchoes.forEach { echo ->
            val infiniteTransition = rememberInfiniteTransition(label = "CanvasGlow")
            val glowScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "Echo"
            )

            Surface(
                onClick = { onEchoClick(echo.messageId) },
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
                    Text(text = echo.senderEmoji ?: "🔥", fontSize = 16.sp)
                }
            }
        }
    }
}

/**
 * Tactical navigation landmark. Displays the nested path.
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
                text = crumb, // Removed .uppercase()
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
fun ResonanceHeader(
    themeColor: Color,
    onAwakenBluetooth: () -> Unit,
    onAwakenWifi: () -> Unit,
    onGrantPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    isBluetoothOff: Boolean = false,
    isWifiOff: Boolean = false,
    isPermissionMissing: Boolean = false,
    isPermanentlyDenied: Boolean = false,
    resonanceStatus: String? = null,
    breeze: String? = null,
    highResonanceMessages: List<Echo> = emptyList(),
    trail: List<String> = emptyList(),
    onCrumbClick: (Int) -> Unit = {},
    trend: String? = null
) {
    val auraColor = when (trend) {
        "ACADEMIC RITUAL" -> AtmosphereAcademic
        "URBAN TRANSIT" -> AtmosphereTransit
        "SOCIAL SYNERGY" -> AtmosphereSocial
        "ROOM NOURISHMENT" -> AtmosphereFood
        "COLLECTIVE ACTION" -> AtmosphereAction
        else -> themeColor
    }

    val infiniteTransition = rememberInfiniteTransition(label = "HarmonyCycle")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, 
        targetValue = 1f, 
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse), 
        label = "Alpha"
    )

    val auraGlow by infiniteTransition.animateFloat(
        initialValue = 0.02f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse),
        label = "Aura"
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
            .clip(RoundedCornerShape(28.dp))
            .background(StealthSurface)
            .border(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    0.0f to Color.White.copy(alpha = StealthAlphaBorder),
                    0.5f to auraColor.copy(alpha = auraGlow * 3f),
                    1.0f to Color.White.copy(alpha = StealthAlphaBorder)
                ),
                shape = RoundedCornerShape(28.dp)
            )
    ) {
        // Aura Background
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(auraColor.copy(alpha = auraGlow), Color.Transparent),
                        radius = 400f
                    )
                )
        )
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                            text = breeze ?: resonanceStatus ?: "Resonating...", 
                            style = MaterialTheme.typography.labelSmall,
                            color = StealthAmber,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (highResonanceMessages.isNotEmpty()) {
                EchoCanvas(
                    highResonanceEchoes = highResonanceMessages,
                    themeColor = themeColor,
                    onEchoClick = { /* Handled by parent */ },
                    modifier = Modifier.weight(1f)
                )
            }

            IconButton(onClick = onLogout, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Logout, 
                    contentDescription = "Logout", 
                    tint = Color.White.copy(alpha = StealthAlphaMedium),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.weight(1f)) {
                if (isPermissionMissing || isBluetoothOff) {
                    Surface(
                        color = StealthError.copy(alpha = StealthAlphaLow), 
                        shape = RoundedCornerShape(8.dp), 
                        modifier = Modifier.graphicsLayer { alpha = pulseAlpha }
                    ) {
                        Text(
                            text = when { isPermissionMissing -> if (isPermanentlyDenied) "Setup" else "Grant"; isBluetoothOff -> "Awake"; else -> "Active" }, 
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
 * IDENTITY STAGE: The contextual navigation and identity layer (Row 1).
 */
@Composable
fun IdentityStage(
    title: String,
    breadcrumbTrail: List<String>,
    onCrumbClick: (Int) -> Unit,
    activeRooms: List<Sphere>,
    onShowTimeline: () -> Unit,
    onResetProfile: () -> Unit,
    onBack: (() -> Unit)?,
    themeColor: Color,
    modifier: Modifier = Modifier,
    userCount: Int? = null,
    isDiscovery: Boolean = false,
    isLiveFeedMode: Boolean = false,
    onModeChange: (Boolean) -> Unit = {},
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
) {
    var showResetProfileDialog by remember { mutableStateOf(value = false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
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
                                    text = "Spheres", 
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
                                    text = "Stream", 
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
                Text(
                    text = title, 
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.End, 
            modifier = Modifier.weight(1f)
        ) {
            if (trailingContent != null) {
                trailingContent()
            }

            if (userCount != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onShowTimeline() }) {
                    Icon(imageVector = Icons.Rounded.Layers, contentDescription = "Ledger", tint = themeColor, modifier = Modifier.size(20.dp))
                    Text(text = "LEDGER", style = MaterialTheme.typography.labelSmall, color = themeColor.copy(alpha = StealthAlphaHigh))
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showResetProfileDialog = true }) {
                Icon(imageVector = Icons.Rounded.AccountCircle, contentDescription = "Profile", tint = Color.White.copy(alpha = StealthAlphaHigh), modifier = Modifier.size(20.dp))
                Text(text = "PROFILE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = StealthAlphaMedium))
            }
        }
    }

    if (showResetProfileDialog) {
        AlertDialog(
            onDismissRequest = { showResetProfileDialog = false },
            containerColor = StealthSurface,
            title = { Text("Reset Tactical Persona?", style = MaterialTheme.typography.titleMedium, color = Color.White) },
            text = { Text("This will clear your nickname and emoji avatar locally. Your device anchor remains.", color = Color.White.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = { onResetProfile(); showResetProfileDialog = false }) {
                    Text("RESET", color = StealthError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetProfileDialog = false }) {
                    Text("CANCEL", color = Color.White.copy(alpha = 0.4f))
                }
            }
        )
    }
}

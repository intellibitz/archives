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
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.Group
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.screens.StatusIcon

/**
 * Provides a composite view of system connection statuses.
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
 * A minimalist real-time counter of active Groups within the current field context.
 */
@Composable
fun GroupTicker(title: String, modifier: Modifier = Modifier, groups: List<Group> = emptyList()) {
    val infiniteTransition = rememberInfiniteTransition(label = "GroupTicker")
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
                val groupsLabel = if (groups.isEmpty()) "nearby people" else "${groups.size} Groups active"
                Text(
                    text = groupsLabel, 
                    style = MaterialTheme.typography.labelSmall,
                    color = StealthPrimary.copy(alpha = StealthAlphaHigh)
                )
            }
        }
    }
}

/**
 * MESSAGE CANVAS: The spatial intelligence header for high-connection Messages.
 */
@Composable
fun MessageCanvas(
    highConnectionMessages: List<Message>,
    themeColor: Color,
    onMessageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        highConnectionMessages.forEach { message ->
            val infiniteTransition = rememberInfiniteTransition(label = "CanvasGlow")
            val glowScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "Message"
            )

            Surface(
                onClick = { onMessageClick(message.messageId) },
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
                    Text(text = message.senderEmoji ?: "🔥", fontSize = 16.sp)
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
                text = crumb, 
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlukitToolbar(
    title: String,
    onLogout: () -> Unit,
    onResetProfile: () -> Unit,
    modifier: Modifier = Modifier,
    themeColor: Color = StealthPrimary,
    onBack: (() -> Unit)? = null,
    connectionStatus: String? = null,
    trend: String? = null,
    isBluetoothOff: Boolean = false,
    isWifiOff: Boolean = false,
    onAwakenBluetooth: () -> Unit = {},
    onAwakenWifi: () -> Unit = {},
) {
    val auraColor = when (trend) {
        "ACADEMIC RITUAL" -> AssistantAcademic
        "URBAN TRANSIT" -> AssistantTransit
        "SOCIAL SYNERGY" -> AssistantSocial
        "ROOM NOURISHMENT" -> AssistantFood
        "COLLECTIVE ACTION" -> AssistantAction
        else -> themeColor
    }

    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (connectionStatus != null) {
                    Text(
                        text = connectionStatus.lowercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = auraColor.copy(alpha = 0.7f)
                    )
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        },
        actions = {
            if (isBluetoothOff || isWifiOff) {
                IconButton(onClick = { if (isBluetoothOff) onAwakenBluetooth() else onAwakenWifi() }) {
                    Icon(
                        imageVector = if (isBluetoothOff) Icons.Rounded.BluetoothDisabled else Icons.Rounded.WifiOff,
                        contentDescription = "Radio Status",
                        tint = StealthError
                    )
                }
            }
            
            var showMenu by remember { mutableStateOf(false) }
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "Options", tint = Color.White)
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(StealthSurface)
            ) {
                DropdownMenuItem(
                    text = { Text("Profile Settings", color = Color.White) },
                    onClick = { onResetProfile(); showMenu = false },
                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = Color.White) }
                )
                DropdownMenuItem(
                    text = { Text("Logout", color = StealthError) },
                    onClick = { onLogout(); showMenu = false },
                    leadingIcon = { Icon(Icons.Rounded.Logout, contentDescription = null, tint = StealthError) }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = StealthBlack,
            titleContentColor = Color.White
        ),
        modifier = modifier
    )
}

/**
 * IDENTITY STAGE: The contextual navigation and identity layer (Row 1).
 */
@Composable
fun IdentityStage(
    title: String,
    onLogout: () -> Unit,
    onResetProfile: () -> Unit,
    modifier: Modifier = Modifier,
    themeColor: Color = StealthPrimary,
    onBack: (() -> Unit)? = null,
    connectionStatus: String? = null,
    trend: String? = null,
    isBluetoothOff: Boolean = false,
    isWifiOff: Boolean = false,
    onAwakenBluetooth: () -> Unit = {},
    onAwakenWifi: () -> Unit = {},
) {
    BlukitToolbar(
        title = title,
        onLogout = onLogout,
        onResetProfile = onResetProfile,
        modifier = modifier,
        themeColor = themeColor,
        onBack = onBack,
        connectionStatus = connectionStatus,
        trend = trend,
        isBluetoothOff = isBluetoothOff,
        isWifiOff = isWifiOff,
        onAwakenBluetooth = onAwakenBluetooth,
        onAwakenWifi = onAwakenWifi
    )
}

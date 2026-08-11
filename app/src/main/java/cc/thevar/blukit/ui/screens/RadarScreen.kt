package cc.thevar.blukit.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.R
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import kotlin.math.cos
import kotlin.math.sin

/**
 * Supreme Senior Android Expert Implementation:
 * Animated Visual Radar with correct type conversions for Dp/Float and high-performance Canvas rendering.
 */
@Composable
fun RadarScreen(
    state: BluetoothUiState,
    onDeviceClick: (P2PDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        RadarBackground()

        // Me (center)
        CenterNode()

        // Discovered devices as peers
        if (state.scannedDevices.isNotEmpty()) {
            PeerNodes(
                devices = state.scannedDevices,
                onDeviceClick = onDeviceClick
            )
        } else if (!state.isConnecting) {
            EmptyRadarHint()
        } else {
            LoadingRadarHint(state.isConnecting)
        }
    }
}

@Composable
private fun CenterNode() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.me),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PeerNodes(
    devices: List<P2PDevice>,
    onDeviceClick: (P2PDevice) -> Unit
) {
    val totalDevices = devices.size
    
    devices.forEachIndexed { index, device ->
        val proximityGroup = device.proximityLabel
        val proximityFactor = device.proximityFactor
        
        val radiusValue = (1f - proximityFactor) * 140f + 60f
        val radius = radiusValue.dp

        val angle = (index.toDouble() / totalDevices) * 2 * Math.PI
        val xOff = (radiusValue * cos(angle)).toFloat()
        val yOff = (radiusValue * sin(angle)).toFloat()

        PeerNode(
            device = device,
            xOffset = xOff.dp,
            yOffset = yOff.dp,
            proximityGroup = proximityGroup,
            onClick = { onDeviceClick(device) }
        )
    }
}

@Composable
private fun PeerNode(
    device: P2PDevice,
    xOffset: Dp,
    yOffset: Dp,
    proximityGroup: String,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NodePulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val nodeSize = when (proximityGroup) {
        "Very Close" -> 64.dp
        "Close" -> 56.dp
        else -> 48.dp
    } * pulse

    Box(
        modifier = Modifier
            .offset(xOffset, yOffset)
            .size(nodeSize)
            .clip(CircleShape)
            .background(colorForProximity(proximityGroup).copy(alpha = 0.6f))
            .border(
                width = if (device.isConnecting) 3.dp else 1.dp,
                color = if (device.isConnecting) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when {
                    device.isConnecting -> "🔗"
                    device.isConnected -> "✅"
                    else -> device.emoji ?: "👤"
                },
                fontSize = (nodeSize.value / 2.5f).sp
            )
            val displayName = device.name ?: stringResource(R.string.discovery_unknown_device)
            Text(
                text = if (displayName.length > 8) "${displayName.take(8)}..." else displayName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun colorForProximity(group: String): Color {
    return when (group) {
        "Very Close" -> Color.Green.copy(alpha = 0.6f)
        "Close" -> Color.Green.copy(alpha = 0.4f)
        "Moderate" -> Color.Yellow.copy(alpha = 0.5f)
        else -> Color.Red.copy(alpha = 0.3f)
    }
}

@Composable
private fun EmptyRadarHint() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📡", fontSize = 48.sp)
            Text(
                text = "Looking for friends nearby...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "Anyone with Blukit open will show up here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun LoadingRadarHint(isConnecting: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary
            )
            val text = if (isConnecting) "Reaching out..." else "Finding friends..."
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun RadarBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")
    val radiusRatio by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarSweep"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = center
        val maxRadius = size.minDimension / 2.5f
        val strokeWidth = with(density) { 1.dp.toPx() }

        // Concentric circles
        for (i in 1..4) {
            drawCircle(
                color = primaryColor.copy(alpha = 0.15f),
                radius = maxRadius * (i / 4f),
                center = center,
                style = Stroke(width = strokeWidth)
            )
        }

        // Radial lines
        for (angle in 0 until 360 step 45) {
            val radAngle = Math.toRadians(angle.toDouble())
            val x1 = cos(radAngle).toFloat() * maxRadius + center.x
            val y1 = sin(radAngle).toFloat() * maxRadius + center.y
            
            drawLine(
                color = primaryColor.copy(alpha = 0.1f),
                start = Offset(center.x, center.y),
                end = Offset(x1, y1),
                strokeWidth = strokeWidth
            )
        }

        // Radar sweep circle
        drawCircle(
            color = primaryColor.copy(alpha = (1 - radiusRatio) * 0.4f),
            radius = maxRadius * radiusRatio,
            center = center,
            style = Stroke(width = strokeWidth * 2f)
        )
    }
}

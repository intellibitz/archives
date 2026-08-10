package cc.thevar.blukit.ui.discovery

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import cc.thevar.blukit.domain.model.P2PDevice
import kotlinx.coroutines.awaitCancellation
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarScreen(
    state: BluetoothUiState,
    onDeviceClick: (P2PDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            isVisible = true
            try {
                awaitCancellation()
            } finally {
                isVisible = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        if (isVisible) {
            RadarBackground()
        } else {
            RadarStaticBackground()
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "ME",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        state.scannedDevices.forEachIndexed { index, device ->
            PeerNode(
                device = device,
                index = index,
                total = state.scannedDevices.size,
                onClick = { onDeviceClick(device) }
            )
        }
    }
}

@Composable
fun RadarBackground() {
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

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = center
        val maxRadius = size.minDimension / 2.2f

        for (i in 1..4) {
            drawCircle(
                color = primaryColor.copy(alpha = 0.2f),
                radius = maxRadius * (i / 4f),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        drawCircle(
            color = primaryColor.copy(alpha = 1f - radiusRatio),
            radius = maxRadius * radiusRatio,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun RadarStaticBackground() {
    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = center
        val maxRadius = size.minDimension / 2.2f
        for (i in 1..4) {
            drawCircle(
                color = primaryColor.copy(alpha = 0.2f),
                radius = maxRadius * (i / 4f),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

@Composable
fun PeerNode(
    device: P2PDevice,
    index: Int,
    total: Int,
    onClick: () -> Unit
) {
    val angle = (index.toFloat() / total) * 2 * Math.PI
    val distance = 100.dp + (index * 20).dp

    Box(
        modifier = Modifier
            .offset(
                x = (distance.value * cos(angle)).dp,
                y = (distance.value * sin(angle)).dp
            )
            .size(60.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "👤",
                fontSize = 20.sp
            )
            Text(
                text = device.name ?: "Unknown",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1
            )
        }
    }
}

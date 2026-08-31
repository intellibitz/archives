package cc.thevar.blukit.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cc.thevar.blukit.ui.theme.*

/**
 * BLUKIT WIDGET: A standard surface container for field content.
 */
@Composable
fun BlukitWidget(
    themeColor: Color,
    header: @Composable () -> Unit,
    entries: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = StealthSurface,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(themeColor.copy(alpha = 0.05f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                header()
            }
            Box(modifier = Modifier.padding(16.dp)) {
                entries()
            }
        }
    }
}

/**
 * FIELD SCAFFOLD: The primary layout structure for resonance screens.
 */
@Composable
fun BlukitFieldScaffold(
    header: @Composable () -> Unit,
    entries: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    themeColor: Color = StealthPrimary,
    glowIntensityTarget: Float = 0.5f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ScaffoldGlow")
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = glowIntensityTarget,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Glow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StealthBlack)
    ) {
        // Background Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(themeColor.copy(alpha = glowIntensity * 0.1f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(500f, 1000f),
                        radius = 1200f
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            header()
            Box(modifier = Modifier.weight(1f)) {
                entries()
            }
        }
    }
}

@Composable
fun BlukitTip(
    text: String,
    themeColor: Color,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = themeColor.copy(alpha = StealthAlphaLow),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, themeColor.copy(alpha = StealthAlphaMedium)),
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = themeColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text, 
                style = MaterialTheme.typography.labelSmall,
                color = themeColor,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.Close, contentDescription = "Dismiss", tint = themeColor.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun BlukitAlert(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StealthSurface,
        title = { Text(text, style = MaterialTheme.typography.titleMedium, color = Color.White) },
        text = { Text(text, color = Color.White.copy(alpha = 0.7f)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = StealthError)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.White.copy(alpha = 0.4f))
            }
        }
    )
}

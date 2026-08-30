package cc.thevar.blukit.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.ui.theme.*

@Composable
fun MeshSearchingView(
    modifier: Modifier = Modifier,
    onSignalPresence: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Searching")
    
    val searchProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SearchProgress"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            // Animated Radar Rings
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = size.width / 4f
                
                repeat(3) { i ->
                    val progress = (searchProgress + i / 3f) % 1f
                    drawCircle(
                        color = StealthPrimary.copy(alpha = (1f - progress) * 0.3f),
                        radius = baseRadius + progress * baseRadius * 2,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = StealthBlack,
                border = BorderStroke(2.dp, StealthPrimary.copy(alpha = glowAlpha)),
                tonalElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CellTower,
                        contentDescription = null,
                        tint = StealthPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "LOOKING FOR PEOPLE...",
            style = MaterialTheme.typography.titleMedium,
            color = StealthPrimary,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "The air is silent. No one else is using Blukit nearby. Say hello to the mesh to start the first conversation.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onSignalPresence,
            colors = ButtonDefaults.buttonColors(
                containerColor = StealthPrimary,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(56.dp).fillMaxWidth(0.8f)
        ) {
            Icon(Icons.Rounded.GraphicEq, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "SAY HELLO",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

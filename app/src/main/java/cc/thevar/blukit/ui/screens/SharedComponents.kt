package cc.thevar.blukit.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.PictureInPicture
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthPrimary

@Composable
fun BlukitHeader(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 64.dp, bottom = 12.dp), // Padded for global badge
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                color = Color.White
            ),
            modifier = Modifier.weight(1f)
        )
        
        if (actions != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                content = actions
            )
        }
    }
}

@Composable
fun VibingVibesAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "VibingVibes")
    val vibeScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "VibeScale"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraAlpha"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(120.dp)) {
        // Outer Aura
        Canvas(modifier = Modifier.fillMaxSize().blur(24.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(StealthAmber.copy(alpha = auraAlpha), Color.Transparent)
                ),
                radius = size.minDimension / 1.5f * vibeScale
            )
        }
        
        // Inner Core
        Surface(
            shape = CircleShape,
            color = StealthAmber.copy(alpha = 0.15f),
            border = BorderStroke(2.dp, StealthAmber),
            modifier = Modifier.size(64.dp * vibeScale)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Heart is pure, no text or icons
            }
        }
    }
}

@Composable
fun StatusOverlay(
    isDiscovering: Boolean,
    isBluetoothEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusItem("AIR", isBluetoothEnabled)
        StatusItem("VIBE", isDiscovering)
    }
}

@Composable
private fun StatusItem(label: String, active: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(if (active) StealthAmber else Color.White.copy(alpha = 0.2f), CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label, 
            fontSize = 7.sp, 
            fontWeight = FontWeight.Black, 
            letterSpacing = 1.sp,
            color = Color.White.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun BlukitInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    placeholder: String = "SEND VIBES...",
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val glowColor by animateColorAsState(
        if (isFocused) StealthPrimary.copy(alpha = 0.4f) 
        else StealthPrimary.copy(alpha = 0.05f),
        label = "glow"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
            .imePadding()
            .shadow(
                elevation = if (isFocused) 8.dp else 0.dp,
                shape = CircleShape,
                ambientColor = StealthPrimary,
                spotColor = StealthPrimary
            ),
        color = Color(0xFF0A0C14),
        shape = CircleShape,
        border = BorderStroke(1.dp, glowColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        placeholder.uppercase(), 
                        style = MaterialTheme.typography.bodyMedium.copy(
                            letterSpacing = 1.sp,
                            color = Color.White.copy(alpha = 0.3f)
                        )
                    ) 
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                interactionSource = interactionSource,
                maxLines = 4
            )
            
            IconButton(
                onClick = onSend,
                enabled = value.isNotBlank(),
                modifier = Modifier
                    .padding(4.dp)
                    .size(44.dp)
                    .background(
                        if (value.isNotBlank()) StealthPrimary else Color.White.copy(alpha = 0.05f),
                        CircleShape
                    )
            ) {
                Text(
                    text = "🌬️",
                    fontSize = 18.sp,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = if (value.isNotBlank()) 1f else 0.4f
                        }
                        .semantics { contentDescription = "Send" }
                )
            }
        }
    }
}

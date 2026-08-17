package cc.thevar.blukit.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose

@Composable
fun BlukitHeartbeat(
    energy: Float,
    rotation: Float,
    lowPowerMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Heartbeat")
    
    val vibeScale by infiniteTransition.animateFloat(
        initialValue = 0.9f + (energy * 0.1f),
        targetValue = if (lowPowerMode) 1.05f else 1.25f + (energy * 0.3f), 
        animationSpec = infiniteRepeatable(
            animation = tween(if (lowPowerMode) 3000 else 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = if (lowPowerMode) 0.1f else 0.2f,
        targetValue = if (lowPowerMode) 0.3f else 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (lowPowerMode) 4000 else 2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(52.dp)) {
        Canvas(modifier = Modifier.fillMaxSize().blur(12.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(StealthPrimary.copy(alpha = auraAlpha), Color.Transparent)
                ),
                radius = size.minDimension / 1.2f * vibeScale
            )
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            rotate(rotation) {
                val scanBrush = Brush.sweepGradient(
                    0.0f to StealthPrimary.copy(alpha = 0.8f), 
                    0.1f to StealthPrimary.copy(alpha = 0.1f), 
                    0.25f to Color.Transparent, 
                    center = center
                )
                drawCircle(brush = scanBrush, radius = size.minDimension / 2.2f)
            }
        }
        Box(
            modifier = Modifier
                .size(24.dp * vibeScale)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(StealthPrimary, StealthRose)))
                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
        )
    }
}

@Composable
fun BlukitInput(
    nickname: String,
    emoji: String,
    airIsStill: Boolean,
    onNicknameChange: (String) -> Unit,
    personaFocusRequester: FocusRequester,
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    vibeCount: Int = 0,
    placeholder: String = "SPREAD VIBES...",
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    val borderGlow by animateColorAsState(
        if (isFocused) StealthPrimary.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.1f),
        label = "BorderGlow"
    )

    Box(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), // Slim fixed height
            color = Color.Black.copy(alpha = 0.85f),
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(1.dp, borderGlow),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserPersona(
                    nickname = nickname,
                    emoji = emoji,
                    airIsStill = airIsStill,
                    onNicknameChange = onNicknameChange,
                    focusRequester = personaFocusRequester
                )

                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f).testTag("SendVibeInput"),
                    placeholder = { 
                        Text(
                            text = placeholder.uppercase(), 
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Visible,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 7.sp, // Tiny horizontal label
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = Color.White.copy(alpha = 0.2f)
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
                    singleLine = true,
                    maxLines = 1
                )

                if (vibeCount > 0) {
                    Surface(
                        color = StealthPrimary.copy(alpha = 0.15f),
                        shape = CircleShape,
                        border = BorderStroke(0.5.dp, StealthPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = vibeCount.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = StealthPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Surface(
                    onClick = { if (value.isNotBlank()) onSend() },
                    shape = CircleShape,
                    color = Color.Transparent,
                    modifier = Modifier.testTag("SendVibeButton")
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (value.isNotBlank()) 
                                    Brush.linearGradient(listOf(StealthPrimary, StealthAmber))
                                else 
                                    Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.02f)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Flare,
                            contentDescription = "Vibe",
                            tint = if (value.isNotBlank()) Color.Black else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserPersona(
    nickname: String,
    emoji: String,
    airIsStill: Boolean,
    onNicknameChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    var localNickname by remember(nickname) { mutableStateOf(nickname) }
    val isUnknown = nickname == "?"

    val infiniteTransition = rememberInfiniteTransition(label = "NodeAnim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Pulse"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .padding(start = 4.dp, end = 12.dp)
            .height(56.dp) // Fixed height to prevent jumping
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
            Surface(
                shape = CircleShape,
                color = (if (isUnknown) StealthAmber else StealthPrimary).copy(alpha = 0.1f),
                modifier = Modifier
                    .size(42.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
            ) {}
            
            Surface(
                shape = CircleShape,
                color = Color(0xFF12141A),
                modifier = Modifier.size(32.dp),
                border = BorderStroke(1.dp, if (isUnknown) StealthAmber.copy(alpha = 0.5f) else StealthPrimary.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = emoji, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.Start) {
            BasicTextField(
                value = localNickname,
                onValueChange = { newName ->
                    localNickname = newName
                    onNicknameChange(newName.ifBlank { "?" })
                },
                modifier = Modifier
                    .widthIn(max = 60.dp)
                    .focusRequester(focusRequester)
                    .testTag("IdentityVibeInput"),
                textStyle = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = if (airIsStill) Color.Red else if (isUnknown) Color.White.copy(alpha = 0.4f) else StealthPrimary,
                    textAlign = TextAlign.Start,
                    letterSpacing = 0.5.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(StealthPrimary)
            )
            Text(
                text = if (isUnknown) "SET NAME" else "PERSONA",
                fontSize = 5.sp,
                fontWeight = FontWeight.Black,
                color = if (isUnknown) StealthAmber else Color.White.copy(alpha = 0.2f),
                letterSpacing = 0.5.sp
            )
        }
    }
}

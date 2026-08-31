package cc.thevar.blukit.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Sphere
import cc.thevar.blukit.ui.theme.*
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * THE RESONANCE TICKER: A tactical scrolling feed of local energy.
 */
@Composable
fun ResonanceTicker(
    state: BluetoothUiState,
    resonanceList: List<Pair<Source, Echo?>>,
    echoCounts: Map<String, Int>,
    localDeviceId: String,
    localNickname: String,
    pulsedPeers: Set<String>,
    onEchoClick: (String) -> Unit,
    onSourceClick: (Source) -> Unit,
    onSourceLongClick: (Source) -> Unit,
    modifier: Modifier = Modifier,
    isGrouped: Boolean = true,
    reverseLayout: Boolean = false,
    themeColor: Color = StealthPrimary,
    trend: String? = null
) {
    val listState = rememberLazyListState()
    
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp, top = 16.dp),
        reverseLayout = reverseLayout,
        verticalArrangement = if (reverseLayout) Arrangement.Bottom else Arrangement.Top
    ) {
        if (!reverseLayout && trend != null) {
            item {
                TickerSectionHeader(title = "CURRENT TREND: $trend", color = themeColor)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        items(resonanceList) { (source, echo) ->
            if (echo == null) {
                // Sphere Head
                TickerSectionHeader(title = source.name ?: "Unknown", color = themeColor)
            } else {
                val isMe = echo.senderId == localDeviceId
                val isPulsed = pulsedPeers.contains(echo.senderId)
                
                EchoItem(
                    echo = echo,
                    isSelected = false,
                    senderSource = source,
                    replyCount = 0,
                    isPulsed = isPulsed,
                    isMe = isMe,
                    isGrouped = isGrouped,
                    isMutual = false,
                    onEchoClick = { onEchoClick(echo.messageId) },
                    onSourceLongClick = { onSourceLongClick(source) }
                )
            }
        }
    }
}

@Composable
fun TickerSectionHeader(title: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title, 
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = StealthAlphaHigh),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        HorizontalDivider(color = color.copy(alpha = 0.1f), modifier = Modifier.weight(1f))
    }
}

@Composable
fun SphereSummary(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    themeColor: Color,
    count: Int,
    lastUpdate: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = StealthSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = themeColor.copy(alpha = StealthAlphaLow),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = themeColor, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = count.toString(), style = MaterialTheme.typography.titleMedium, color = themeColor)
                Text(text = lastUpdate, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun EchoItem(
    echo: Echo,
    isSelected: Boolean,
    senderSource: Source?,
    replyCount: Int,
    isPulsed: Boolean,
    isMe: Boolean,
    isGrouped: Boolean,
    isMutual: Boolean,
    onEchoClick: () -> Unit,
    onSourceLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    rowId: String = echo.messageId
) {
    val themeColor = if (echo.messageScope == Echo.MESSAGE_WHISPER) StealthRose else StealthPrimary
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEchoClick() }
            .background(if (isSelected) themeColor.copy(alpha = 0.05f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Source Avatar
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .combinedClickable(
                        onClick = { /* View Profile */ },
                        onLongClick = onSourceLongClick
                    ),
                shape = CircleShape,
                color = if (isPulsed) themeColor.copy(alpha = 0.1f) else StealthSurface,
                border = BorderStroke(
                    width = if (isPulsed) 2.dp else 1.dp,
                    color = if (isPulsed) themeColor else Color.White.copy(alpha = 0.1f)
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = senderSource?.emoji ?: "👤", fontSize = 20.sp)
                }
            }
            
            if (isGrouped) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .background(Color.White.copy(alpha = 0.05f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isMe) "You" else (senderSource?.name ?: "Unknown"),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isMe) StealthAmber else Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = sdf.format(Date(echo.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            EchoContent(echo = echo, themeColor = themeColor)

            if (replyCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.ChatBubbleOutline, 
                        contentDescription = null, 
                        tint = themeColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$replyCount RESONANCE", 
                        style = MaterialTheme.typography.labelSmall,
                        color = themeColor.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun EchoContent(echo: Echo, themeColor: Color) {
    when (echo.type) {
        Echo.TYPE_TEXT -> {
            Text(
                text = echo.content,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
        Echo.TYPE_IMAGE -> {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = StealthSurface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Image, contentDescription = null, tint = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(48.dp))
                }
            }
        }
        Echo.TYPE_FILE -> {
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Description, contentDescription = null, tint = themeColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = echo.fileName ?: "Document", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        Text(text = "${(echo.fileSize ?: 0) / 1024} KB", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    }
                }
            }
        }
        Echo.TYPE_NOTE_UPDATE -> {
             cc.thevar.blukit.ui.components.EchoRecordItem(
                record = echo,
                onStatusChange = { }, // Handled by parent if needed
                themeColor = themeColor
            )
        }
        Echo.TYPE_AI_SUMMARY -> {
            Surface(
                color = StealthAmber.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, StealthAmber.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = StealthAmber, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = echo.content, style = MaterialTheme.typography.bodySmall, color = StealthAmber)
                }
            }
        }
    }
}

@Composable
fun PersonaSignature(
    device: Source,
    isPulsed: Boolean,
    isSelected: Boolean,
    isPeerPulsed: Boolean,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    isStatic: Boolean = false,
    themeColor: Color = StealthPrimary,
    subLabel: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Signature")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Pulse"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "Glow"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(size * 1.5f)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
            if (!isStatic && (isPulsed || isPeerPulsed)) {
                Surface(
                    shape = CircleShape,
                    color = themeColor.copy(alpha = 0.1f * glowAlpha),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.4f * glowAlpha)),
                    modifier = Modifier.fillMaxSize().graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                ) {}
            }

            Surface(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ),
                shape = CircleShape,
                color = if (isSelected) themeColor.copy(alpha = 0.2f) else StealthSurface,
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) themeColor else Color.White.copy(alpha = 0.1f)
                ),
                tonalElevation = if (isSelected) 8.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = device.emoji, fontSize = (size.value * 0.45f).sp)
                }
            }
        }
        
        if (subLabel != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subLabel, 
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) themeColor else Color.White.copy(alpha = 0.4f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

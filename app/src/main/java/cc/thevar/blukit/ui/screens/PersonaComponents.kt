/**
 * BLUKIT UI COMPONENTS
 *
 * This module manages visual identities (Peer Signatures) and overlays 
 * used for onboarding.
 */
package cc.thevar.blukit.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Group
import cc.thevar.blukit.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EnergyTrails(
    modifier: Modifier = Modifier,
    count: Int = 6,
    color: Color = StealthPrimary,
    proximity: Float = 0f
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2.2f

        repeat(count) { i ->
            val angle = (i * (360f / count)) % 360f
            val rad = Math.toRadians(angle.toDouble())
            val x = (center.x + cos(rad) * radius).toFloat()
            val y = (center.y + sin(rad) * radius).toFloat()

            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = 1.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
/**
 * PEER SIGNATURE: High-fidelity visual identity for peers.
 */
@Composable
fun PersonaSignature(
    device: Source, 
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
    val isProjected = projectionEmoji != null
    val pulseScale = 1.0f
    
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
        if (!isStatic && (isPulsed || isPeerPulsed || isHighlighted || isMe)) {
            EnergyTrails(
                modifier = Modifier.matchParentSize(),
                color = personaThemeColor,
                proximity = device.proximityFactor
            )
        }
        val haloAlpha = (if (isHighlighted || isMe) 0.3f else 0.08f + proximityGlow + bloomBoost) * pulseScale
        Surface(
            shape = CircleShape, 
            color = personaThemeColor.copy(alpha = haloAlpha.coerceAtMost(0.45f)),
            modifier = Modifier.size(size * pulseScale * (if (isProjected) 1.8f else 1.4f) + (proximityGlow + bloomBoost).dp)
        ) {}
        Surface(
            modifier = Modifier.size(size).clip(CircleShape), 
            color = when { 
                isSelected -> Color.White.copy(alpha = 0.2f)
                isProjected || isPulsed -> StealthRose.copy(alpha = StealthAlphaLow)
                isPeerPulsed -> StealthAmber.copy(alpha = StealthAlphaLow)
                else -> StealthBlack 
            }, 
            border = BorderStroke(
                if (isSelected || isPulsed || isPeerPulsed || isProjected || isMe) (size.value / 24).dp.coerceAtLeast(1.dp) 
                else (size.value / 48).dp.coerceAtLeast(0.5.dp), 
                when { 
                    isSelected || isMe -> Color.White
                    isProjected || isPulsed -> StealthRose
                    isPeerPulsed -> StealthAmber
                    else -> MaterialTheme.colorScheme.outlineVariant 
                }
            ), 
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
                            val mediumIcon = when (device.medium) { 
                                Source.ConnectionMedium.BLUETOOTH -> Icons.Rounded.Bluetooth
                                Source.ConnectionMedium.WIFI -> Icons.Rounded.Wifi
                                Source.ConnectionMedium.LOCATION -> Icons.Rounded.LocationOn 
                            }
                            val iconSize = (size.value / 2.5f).dp
                            val icon = if (isMe) Icons.Rounded.Face else if (device.isConnecting || device.isGroupPending) Icons.Rounded.Sync else if (isSelected) Icons.Rounded.CheckCircle else mediumIcon
                            Icon(imageVector = icon, contentDescription = null, tint = when { isSelected || isMe -> Color.White; isPulsed -> StealthRose; isPeerPulsed -> StealthAmber; else -> Color.White.copy(alpha = StealthAlphaHigh) }, modifier = Modifier.size(iconSize))
                        }
                    }
                    
                    if (size >= 24.dp) {
                        val nameText = device.name ?: if (isMe) "YOU" else "?"
                        val displayText = if (isMe && nameText == "YOU") "YOU" else nameText.take(3)
                        Text(
                            text = displayText, 
                            style = MaterialTheme.typography.labelSmall,
                            color = personaThemeColor.copy(alpha = StealthAlphaHigh)
                        )
                        
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
        
        val finalLabel = subLabel ?: if (isMe) "YOU" else "SOURCE"
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

/**
 * GROUP SIGNATURE: Visual identity for Groups.
 */
@Composable
fun GroupSignature(
    device: Source, 
    memberCount: Int, 
    isPulsed: Boolean, 
    modifier: Modifier = Modifier,
    size: Dp = 64.dp, 
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    title: String? = null
) {
    val themeColor = if (isPulsed) StealthRose else StealthPrimary
    
    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size * 1.5f)) {
        Surface(shape = CircleShape, color = themeColor.copy(alpha = 0.05f), modifier = Modifier.size(size)) {}
        Surface(
            modifier = Modifier.size(size), 
            shape = CircleShape, 
            color = StealthBlack, 
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant), 
            tonalElevation = 4.dp
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { 
                if (icon != null) {
                    Icon(imageVector = icon, contentDescription = null, tint = themeColor, modifier = Modifier.size((size.value / 2.5f).dp))
                } else {
                    Text(text = device.emoji, fontSize = (size.value / 3).sp)
                }
                if (memberCount > 0) { 
                    Text(
                        text = "$memberCount Sources",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, 
                        color = themeColor
                    ) 
                } 
            } 
        }

        if (title != null && size > 40.dp) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
            )
        }
    }
}

/**
 * ONBOARDING OVERLAY: Interface for initial profile setup.
 */
@Composable
fun WelcomeGhost(
    nickname: String,
    emoji: String,
    onNicknameChange: (String) -> Unit,
    onEmojiChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    val emojis = listOf("👤", "👻", "📡", "🛸", "🪐", "🌟", "⚡", "🔥", "💧", "🧬", "🧿", "💎")

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
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                Surface(
                    shape = CircleShape,
                    color = StealthAmber.copy(alpha = 0.1f),
                    border = BorderStroke(2.dp, StealthAmber.copy(alpha = 0.3f)),
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

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                items(emojis) { em ->
                    val isSelected = em == emoji
                    Surface(
                        onClick = { onEmojiChange(em) },
                        shape = CircleShape,
                        color = if (isSelected) StealthAmber.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, if (isSelected) StealthAmber else Color.Transparent),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = em, fontSize = 20.sp)
                        }
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
                        text = "Welcome Source", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = StealthAmber
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    androidx.compose.foundation.text.BasicTextField(
                        value = nickname,
                        onValueChange = onNicknameChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(color = Color.White, textAlign = TextAlign.Center, fontWeight = FontWeight.Black),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(StealthAmber),
                        decorationBox = { innerTextField ->
                            if (nickname.isEmpty()) {
                                Text(
                                    text = "Who are you?", 
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
                        text = "Blukit is your sovereign life record. No internet required. Your records stay strictly inside your connection.",
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
                                text = "Lurk", 
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
                                text = "Own it", 
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
 * GROUP CREATION OVERLAY: Interface for naming and templating new Groups.
 */
@Composable
fun GroupRitualGhost(
    onNameChange: (String) -> Unit,
    onDone: (String?) -> Unit, 
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    nearbyGroups: List<Group> = emptyList(),
    onJoinGroup: (String) -> Unit = {},
    title: String = "Start a Group", 
    hint: String = "Name your Group" 
) {
    var groupName by remember { mutableStateOf("") }
    var selectedTemplateId by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    val coordinates = LocalPersonaCoordinates.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            coordinates.remove("GROUP_RITUAL")
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
                    val current = coordinates["GROUP_RITUAL"] ?: PersonaConnectionPoints()
                    coordinates["GROUP_RITUAL"] = current.copy(field = it.positionInRoot() + center)
                }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                Surface(
                    shape = CircleShape,
                    color = StealthPrimary.copy(alpha = 0.1f),
                    border = BorderStroke(2.dp, StealthPrimary.copy(alpha = 0.3f)),
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
                        val iconEmoji = cc.thevar.blukit.domain.model.RoomTemplates.ALL.find { it.id == selectedTemplateId }?.iconEmoji ?: "🌬️"
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
                    
                    androidx.compose.foundation.text.BasicTextField(
                        value = groupName,
                        onValueChange = { groupName = it; onNameChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(color = Color.White, textAlign = TextAlign.Center),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(StealthPrimary),
                        decorationBox = { innerTextField ->
                            if (groupName.isEmpty()) {
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
                        cc.thevar.blukit.domain.model.RoomTemplates.ALL.forEach { template ->
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

                    if (nearbyGroups.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Nearby Groups", 
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            nearbyGroups.take(3).forEach { group ->
                                Surface(
                                    onClick = { onJoinGroup(group.id) },
                                    color = StealthPrimary.copy(alpha = StealthAlphaLow),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, StealthPrimary.copy(alpha = StealthAlphaMedium))
                                ) {
                                    Text(
                                        text = group.name, 
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
                        enabled = groupName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "Create", 
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

data class GhostAction(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val color: Color,
    val onClick: () -> Unit
)

data class GhostMessageData(
    val emoji: String,
    val title: String,
    val subtitle: String? = null,
    val actions: List<GhostAction>,
    val themeColor: Color,
    val sourceId: String? = null
)

/**
 * MESSAGE OVERLAY: Immersive view for trending or important messages.
 */
@Composable
fun MessageGhost(
    data: GhostMessageData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coordinates = LocalPersonaCoordinates.current

    DisposableEffect(Unit) {
        onDispose {
            coordinates.remove("GHOST_MESSAGE")
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
        Box(
            modifier = Modifier
                .onGloballyPositioned { 
                    val center = Offset(it.size.width / 2f, it.size.height / 2f)
                    coordinates["GHOST_MESSAGE"] = PersonaConnectionPoints(field = it.positionInRoot() + center)
                }
                .size(1.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = StealthBlack,
                border = BorderStroke(2.dp, data.themeColor.copy(alpha = 0.5f)),
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
                        text = data.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
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
                        text = data.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = data.themeColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Box(modifier = Modifier.size(340.dp), contentAlignment = Alignment.Center) {
            data.actions.forEachIndexed { index, action ->
                val angle = (index * (360f / data.actions.size)) - 90f
                val radius = 130.dp
                val x = (cos(Math.toRadians(angle.toDouble())) * radius.value).dp
                val y = (sin(Math.toRadians(angle.toDouble())) * radius.value).dp

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
                            text = action.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

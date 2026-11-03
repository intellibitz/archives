/**
 * BLUKIT UI COMPONENTS
 *
 * This module manages visual identities and overlays used for onboarding.
 */
package cc.thevar.blukit.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cc.thevar.blukit.R
import cc.thevar.blukit.domain.model.Group
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * CONNECTION AURA: Visual trails signaling active device presence.
 */
@Composable
fun ConnectionAura(
    modifier: Modifier = Modifier,
    count: Int = 12,
    color: Color = StealthPrimary,
    intensity: Float = 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Aura")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing)
        ),
        label = "Rotation"
    )

    Box(modifier = modifier) {
        repeat(count) { i ->
            val rad = Math.toRadians((angle + (360 / count) * i).toDouble())
            val radius = 80.dp
            val x = (cos(rad) * radius.value).toFloat()
            val y = (sin(rad) * radius.value).toFloat()

            Box(
                modifier = Modifier
                    .offset(x.dp, y.dp)
                    .size(2.dp)
                    .background(color.copy(alpha = 0.2f * intensity), CircleShape)
            )
        }
    }
}

/**
 * PERSONA SIGNATURE: A visual representation of a user in the field.
 */
@Composable
fun PersonaSignature(
    source: Source,
    isSelected: Boolean = false,
    isPulsed: Boolean = false,
    isMe: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    showName: Boolean = true,
    isMutual: Boolean = false,
    status: String? = null,
    themeColor: Color? = null,
    badge: String? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val finalColor = themeColor ?: (if (isMe) StealthAmber else StealthPrimary)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(IntrinsicSize.Min)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Selection ring
            if (isSelected) {
                Surface(
                    modifier = Modifier.size(size + 12.dp),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(2.dp, finalColor)
                ) {}
            }

            // Pulse effect
            if (isPulsed) {
                val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "Scale"
                )
                Surface(
                    modifier = Modifier
                        .size(size)
                        .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale },
                    shape = CircleShape,
                    color = finalColor.copy(alpha = 0.1f)
                ) {}
            }

            // Avatar
            Surface(
                modifier = Modifier.size(size),
                shape = CircleShape,
                color = StealthBlack,
                border = BorderStroke(1.dp, finalColor.copy(alpha = 0.3f)),
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = source.emoji,
                        fontSize = (size.value * 0.45f).sp
                    )
                }
            }

            // Mutual tie indicator
            if (isMutual) {
                Icon(
                    imageVector = Icons.Rounded.VerifiedUser,
                    contentDescription = null,
                    tint = finalColor,
                    modifier = Modifier
                        .size(size / 3)
                        .align(Alignment.BottomEnd)
                        .background(StealthBlack, CircleShape)
                        .padding(2.dp)
                )
            }
        }

        if (showName) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isMe) "YOU" else (source.name ?: "Unknown"),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * GROUP SIGNATURE: Visual token for a Group context.
 */
@Composable
fun GroupSignature(
    group: Source,
    memberCount: Int = 0,
    isPulsed: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    icon: ImageVector? = null,
    title: String? = null
) {
    val themeColor = StealthRose
    
    Box(
        modifier = modifier.size(size + 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(size),
            shape = CircleShape,
            color = StealthBlack,
            border = BorderStroke(1.dp, themeColor.copy(alpha = 0.4f)),
            tonalElevation = 6.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(imageVector = icon, contentDescription = null, tint = themeColor, modifier = Modifier.size((size.value / 2.5f).dp))
                } else {
                    Text(text = group.emoji, fontSize = (size.value / 3).sp)
                }
                if (memberCount > 0) { 
                    Text(
                        text = "$memberCount People",
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
 * IDENTITY SETUP: Interface for initial profile configuration.
 */
@Composable
fun IdentitySetup(
    nickname: String,
    emoji: String,
    onNicknameChange: (String) -> Unit,
    onEmojiChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    val emojis = listOf("👤", "🐱", "🐶", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮", "🐷", "🐸")

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
        Surface(
            color = StealthSurface,
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, StealthAmber.copy(alpha = 0.4f)),
            modifier = Modifier.padding(horizontal = 40.dp).clickable(enabled = false) {}
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.identity_title).uppercase(), 
                    style = MaterialTheme.typography.labelSmall, 
                    color = StealthAmber
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                BasicTextField(
                    value = nickname,
                    onValueChange = onNicknameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(color = Color.White, textAlign = TextAlign.Center, fontWeight = FontWeight.Black),
                    cursorBrush = SolidColor(StealthAmber),
                    decorationBox = { innerTextField ->
                        if (nickname.isEmpty()) {
                            Text(
                                text = stringResource(R.string.identity_who_are_you), 
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
                    text = stringResource(R.string.identity_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(emojis) { e ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(if (emoji == e) StealthAmber.copy(alpha = 0.2f) else Color.Transparent, CircleShape)
                                .border(if (emoji == e) 1.dp else 0.dp, if (emoji == e) StealthAmber else Color.Transparent, CircleShape)
                                .clickable { onEmojiChange(e) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = e, fontSize = 24.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.identity_action_lurk).uppercase(), 
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                    
                    Button(
                        onClick = onDone,
                        enabled = nickname.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = StealthAmber, contentColor = Color.Black),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1.5f).height(48.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.identity_action_save).uppercase(), 
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * GROUP SETUP OVERLAY: Interface for naming new Groups.
 */
@Composable
fun GroupSetup(
    onNameChange: (String) -> Unit,
    onDone: (String?) -> Unit, 
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    nearbyGroups: List<Group> = emptyList(),
    onJoinGroup: (String) -> Unit = {},
    title: String = "Start a Group", 
    themeColor: String = "Normal"
) {
    var groupName by remember { mutableStateOf("") }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StealthBlack.copy(alpha = 0.9f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = StealthSurface,
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, StealthPrimary.copy(alpha = 0.4f)),
            modifier = Modifier.padding(horizontal = 32.dp).clickable(enabled = false) {}
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = StealthPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it; onNameChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Group Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StealthPrimary,
                        unfocusedBorderColor = StealthPrimary.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = Color.White.copy(alpha = 0.4f))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { onDone(groupName) },
                        enabled = groupName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = StealthPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CREATE")
                    }
                }
            }
        }
    }
}

data class AssistantAction(
    val icon: ImageVector,
    val label: String,
    val color: Color,
    val onClick: () -> Unit
)

data class AssistantMessageData(
    val emoji: String,
    val title: String,
    val subtitle: String? = null,
    val actions: List<AssistantAction> = emptyList(),
    val themeColor: Color = StealthPrimary,
    val sourceId: String? = null
)

/**
 * ASSISTANT MESSAGE: Displays system or AI notifications in the field.
 */
@Composable
fun AssistantMessage(
    data: AssistantMessageData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = StealthSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, data.themeColor.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = data.emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = data.title, style = MaterialTheme.typography.titleSmall, color = Color.White)
                if (data.subtitle != null) {
                    Text(text = data.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                }
                
                if (data.actions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        data.actions.forEach { action ->
                            TextButton(
                                onClick = action.onClick,
                                colors = ButtonDefaults.textButtonColors(contentColor = action.color)
                            ) {
                                Icon(action.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = action.label.uppercase(), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Rounded.Close, contentDescription = "Dismiss", tint = Color.White.copy(alpha = 0.4f))
            }
        }
    }
}

package cc.thevar.blukit.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.R
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthSurface

@Composable
fun ProfileScreen(
    onSaveNickname: (String) -> Unit,
    onSaveEmoji: (String) -> Unit,
    onToggleStealth: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    currentNickname: String? = null,
    currentEmoji: String = "🎭",
    isStealthMode: Boolean = false,
    onClearHistory: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var nickname by remember(currentNickname) { mutableStateOf(currentNickname ?: "vibe") }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val emojis = listOf(
        "🎭", "🏟️", "🛍️", "✈️", "🚗", "✨", "🧿", "💠", "🎡", "🌬️"
    )

    // Vibing Aura Animation
    val infiniteTransition = rememberInfiniteTransition(label = "Aura")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraScale"
    )
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraAlpha"
    )

    val haptics = LocalHapticFeedback.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            // Space for global badge
            Spacer(modifier = Modifier.statusBarsPadding().height(64.dp))
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Identity Ritual: Central Avatar
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                // Vibing Aura
                Box(
                    modifier = Modifier
                        .size(120.dp * auraScale)
                        .blur(20.dp)
                        .background(StealthPrimary.copy(alpha = auraAlpha), CircleShape)
                )
                
                Surface(
                    shape = CircleShape,
                    color = StealthSurface,
                    border = BorderStroke(2.dp, StealthPrimary),
                    modifier = Modifier.size(120.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = currentEmoji,
                            fontSize = 64.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = nickname,
                onValueChange = { 
                    nickname = it
                    onSaveNickname(it.ifBlank { "vibe" })
                },
                label = { Text("NAME YOUR VIBE", color = StealthPrimary.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("ANONYMOUS", color = StealthPrimary.copy(alpha = 0.2f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StealthPrimary,
                    unfocusedBorderColor = StealthPrimary.copy(alpha = 0.3f),
                    focusedLabelColor = StealthPrimary,
                    cursorColor = StealthPrimary
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Emoji Selection Grid
            Text(
                text = "PICK YOUR MOOD",
                style = MaterialTheme.typography.labelSmall,
                color = StealthPrimary.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(120.dp)
            ) {
                items(emojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (currentEmoji == emoji) StealthPrimary.copy(alpha = 0.2f) else Color.Transparent)
                            .border(
                                1.dp, 
                                if (currentEmoji == emoji) StealthPrimary else StealthPrimary.copy(alpha = 0.1f), 
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { 
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSaveEmoji(emoji) 
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 24.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Stealth Mode Toggle
            Surface(
                color = StealthSurface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.mask_stealth_mode),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = stringResource(R.string.mask_stealth_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = StealthPrimary.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = isStealthMode,
                        onCheckedChange = onToggleStealth,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = StealthPrimary,
                            checkedTrackColor = StealthPrimary.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            
            // STILLNESS (Formerly Danger Zone)
            var showStillness by remember { mutableStateOf(false) }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .clickable { showStillness = !showStillness },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (showStillness) "▼ STILLNESS" else "▶ STILLNESS",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (showStillness) StealthPrimary else StealthPrimary.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Bold
                )
                
                if (showStillness) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showClearHistoryDialog = true },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("CLEAR WHISPERS", fontSize = 10.sp)
                        }
                        OutlinedButton(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("DISSOLVE VIBE", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }


    // Confirmation Dialogs
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text(stringResource(R.string.conf_delete_whispers_title)) },
            text = { Text(stringResource(R.string.conf_delete_whispers_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text(stringResource(R.string.conf_delete_btn), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.conf_reset_identity_title)) },
            text = { Text(stringResource(R.string.conf_reset_identity_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLogout()
                        showLogoutDialog = false
                    }
                ) {
                    Text(stringResource(R.string.conf_reset_btn), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

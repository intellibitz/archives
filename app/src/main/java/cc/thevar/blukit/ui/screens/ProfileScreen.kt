package cc.thevar.blukit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.R

@Composable
fun ProfileScreen(
    onSaveNickname: (String) -> Unit,
    onSaveEmoji: (String) -> Unit,
    onToggleStealth: (Boolean) -> Unit,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier,
    currentNickname: String? = null,
    currentEmoji: String = "👤",
    isStealthMode: Boolean = false,
    onClearHistory: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var nickname by remember(currentNickname) { mutableStateOf(currentNickname ?: "") }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val emojis = listOf(
        "👤", "🐱", "🐶", "🦊", "🦁", "🤖", "👽", "👻",
        "🐯", "🐨", "🐼", "🐹", "🐸", "🐷", "🦄", "🐲",
        "🚀", "🌈", "🔥", "💎", "🎸", "🍕", "🎮", "🏀"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Emoji Selection
            Text(
                text = currentEmoji,
                fontSize = 80.sp,
                modifier = Modifier.padding(16.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                items(emojis) { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 32.sp,
                        modifier = Modifier
                            .clickable { onSaveEmoji(emoji) }
                            .padding(8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text(stringResource(R.string.profile_nickname_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Stealth Mode Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.profile_stealth_mode),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isStealthMode,
                    onCheckedChange = onToggleStealth
                )
            }
            Text(
                text = stringResource(R.string.profile_stealth_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))
            
            // P4: Additional actions
            if (currentNickname != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                TextButton(
                    onClick = { showClearHistoryDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.profile_clear_history), color = MaterialTheme.colorScheme.error)
                }
                
                TextButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.profile_logout))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { 
                    if (nickname.isNotBlank()) {
                        onSaveNickname(nickname)
                        onNavigateNext()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = nickname.isNotBlank()
            ) {
                Text(stringResource(R.string.profile_start_exploring))
            }
        }
    }

    // Confirmation Dialogs
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text(stringResource(R.string.profile_clear_history_confirm_title)) },
            text = { Text(stringResource(R.string.profile_clear_history_confirm_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text(stringResource(R.string.profile_clear_btn), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(stringResource(R.string.mod_cancel))
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.profile_logout_confirm_title)) },
            text = { Text(stringResource(R.string.profile_logout_confirm_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLogout()
                        showLogoutDialog = false
                    }
                ) {
                    Text(stringResource(R.string.profile_reset_btn), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.mod_cancel))
                }
            }
        )
    }
}

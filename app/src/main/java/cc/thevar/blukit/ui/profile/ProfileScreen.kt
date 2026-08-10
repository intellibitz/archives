package cc.thevar.blukit.ui.profile

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
    currentNickname: String?,
    currentEmoji: String,
    isStealthMode: Boolean,
    onSaveNickname: (String) -> Unit,
    onSaveEmoji: (String) -> Unit,
    onToggleStealth: (Boolean) -> Unit,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nickname by remember(currentNickname) { mutableStateOf(currentNickname ?: "") }
    val emojis = listOf("👤", "🐱", "🐶", "🦊", "🦁", "🤖", "👽", "👻")

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
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
}

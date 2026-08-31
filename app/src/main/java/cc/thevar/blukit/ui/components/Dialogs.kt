package cc.thevar.blukit.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.ui.theme.*

@Composable
fun MessageActionMenu(
    message: Message,
    isMe: Boolean,
    onInvite: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    onBroadcast: () -> Unit,
    onVote: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StealthSurface,
        title = { Text("Message Actions", style = MaterialTheme.typography.titleMedium, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isMe) {
                    MenuActionItem(Icons.Rounded.Campaign, "Broadcast", StealthAmber, onClick = { onBroadcast(); onDismiss() })
                    MenuActionItem(Icons.Rounded.Shield, "Verify (Consensus)", StealthPrimary, onClick = { onVote(1); onDismiss() })
                }
                MenuActionItem(Icons.Rounded.Hearing, "Private Message", StealthRose, onClick = { onInvite(); onDismiss() })
                if (isMe) {
                    MenuActionItem(Icons.Rounded.Delete, "Purge Record", StealthError, onClick = { onDelete(); onDismiss() })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE", color = Color.White.copy(alpha = 0.4f)) }
        }
    )
}

@Composable
fun MenuActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick, 
        color = Color.White.copy(alpha = 0.03f), 
        shape = RoundedCornerShape(12.dp), 
        modifier = modifier.fillMaxWidth()
    ) { 
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

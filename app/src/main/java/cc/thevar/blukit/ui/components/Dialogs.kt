package cc.thevar.blukit.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.ui.theme.*

@Composable
fun SourceOptionsMenu(
    device: Source,
    isTied: Boolean,
    isBlocked: Boolean,
    isRequesting: Boolean,
    activeGroupId: String? = null,
    isAlreadyInActiveGroup: Boolean = false,
    onEcho: () -> Unit,
    onAccept: () -> Unit,
    onDeny: () -> Unit,
    onDisconnect: () -> Unit,
    onSelect: () -> Unit,
    onIdentify: () -> Unit,
    onBlock: () -> Unit,
    onUnblock: () -> Unit,
    onSync: () -> Unit = {},
    onAddToGroup: (String) -> Unit = {},
    onRemoveFromGroup: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StealthSurface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(text = device.emoji, fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = device.name ?: "Source", 
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isRequesting) {
                    MenuActionItem(Icons.Rounded.Handshake, "Accept Resonance", StealthPrimary, onClick = onAccept)
                    MenuActionItem(Icons.Rounded.Close, "Deny Resonance", StealthError, onClick = onDeny)
                } else if (activeGroupId != null) {
                    if (isAlreadyInActiveGroup) {
                        MenuActionItem(Icons.Rounded.PersonRemove, "Remove from Sphere", StealthRose, onClick = { onRemoveFromGroup(activeGroupId) })
                    } else {
                        MenuActionItem(Icons.Rounded.PersonAdd, "Add to this Sphere", StealthPrimary, onClick = { onAddToGroup(activeGroupId) })
                    }
                } else if (isTied) {
                    MenuActionItem(Icons.Rounded.Sync, "Harmonize Records", StealthAmber, onClick = onSync)
                    MenuActionItem(Icons.Rounded.SettingsInputAntenna, "Disconnect", StealthRose, onClick = onDisconnect)
                } else {
                    MenuActionItem(Icons.Rounded.Hearing, "Private Echo", StealthPrimary, onClick = onEcho)
                    MenuActionItem(Icons.Rounded.SettingsInputAntenna, "Resonate", StealthRose, onClick = onSelect)
                }
                
                MenuActionItem(Icons.Rounded.Radar, "Identify", Color.White, onClick = onIdentify)
                if (isBlocked) MenuActionItem(Icons.Rounded.LockOpen, "Unblock Source", StealthPrimary, onClick = onUnblock) 
                else MenuActionItem(Icons.Rounded.Block, "Block Source", StealthError, onClick = onBlock)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "BACK", 
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }
    )
}

@Composable
fun EchoActionMenu(
    echo: Echo,
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
        title = { Text("Echo Actions", style = MaterialTheme.typography.titleMedium, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isMe) {
                    MenuActionItem(Icons.Rounded.Campaign, "Broadcast", StealthAmber, onClick = { onBroadcast(); onDismiss() })
                    MenuActionItem(Icons.Rounded.Shield, "Verify (Consensus)", StealthPrimary, onClick = { onVote(1); onDismiss() })
                }
                MenuActionItem(Icons.Rounded.Hearing, "Private Pulse", StealthRose, onClick = { onInvite(); onDismiss() })
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

@Composable
fun RecordEditor(
    record: Echo?,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(record?.content ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StealthBlack,
        title = { Text(if (record == null) "New Record" else "Edit Record", style = MaterialTheme.typography.titleMedium, color = StealthRose) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().height(200.dp),
                textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 14.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StealthRose,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) {
                Text("SEND", color = StealthRose, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("DISCARD", color = Color.White.copy(alpha = 0.4f))
            }
        }
    )
}

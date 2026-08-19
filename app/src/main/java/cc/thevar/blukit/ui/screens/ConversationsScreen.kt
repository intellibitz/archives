package cc.thevar.blukit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.VibeGroup
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConversationsScreen(
    state: BluetoothUiState,
    isGroupType: Boolean,
    onVibeClick: (VibeGroup) -> Unit,
    onDeleteGroup: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var groupToDelete by remember { mutableStateOf<VibeGroup?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        val title = if (isGroupType) "GROUPS" else "WHISPER"
        val icon = if (isGroupType) Icons.Rounded.Flare else Icons.Rounded.Hearing
        BlukitTopTitle(title = title, icon = icon)
        
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val conversations = state.groups.filter { 
                if (isGroupType) it.type == VibeGroup.TYPE_TIE else it.type == VibeGroup.TYPE_SIDE 
            }
            if (conversations.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "NO $title YET", 
                            color = Color.White.copy(alpha = 0.2f), 
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
            items(conversations, key = { it.id }) { group ->
                VibeGroupItem(group, state.messages, onVibeClick, { groupToDelete = it })
            }
        }
    }

    if (groupToDelete != null) {
        AlertDialog(
            onDismissRequest = { groupToDelete = null },
            containerColor = Color.Black,
            titleContentColor = StealthRose,
            textContentColor = Color.White,
            title = { Text("DELETE RESONANCE?", fontWeight = FontWeight.Black) },
            text = { Text("THIS WILL PERMANENTLY REMOVE THIS CONVERSATION.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteGroup(groupToDelete!!.id)
                    groupToDelete = null
                }) {
                    Text("DELETE", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { groupToDelete = null }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VibeGroupItem(
    group: VibeGroup,
    messages: List<MessagePayload>,
    onVibeClick: (VibeGroup) -> Unit,
    onLongClick: (VibeGroup) -> Unit
) {
    val lastMessage = remember(group.id, messages) {
        messages.filter { it.groupId == group.id }.lastOrNull()
    }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { onVibeClick(group) },
                onLongClick = { onLongClick(group) }
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (group.type == VibeGroup.TYPE_TIE) StealthRose.copy(alpha = 0.2f) else StealthPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (group.type == VibeGroup.TYPE_TIE) Icons.Rounded.Flare else Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = if (group.type == VibeGroup.TYPE_TIE) StealthRose else StealthPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = group.name.uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                    if (lastMessage != null) {
                        Text(
                            text = timeFormatter.format(Date(lastMessage.timestamp)),
                            fontSize = 8.sp,
                            color = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = lastMessage?.content ?: "No vibes yet...",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

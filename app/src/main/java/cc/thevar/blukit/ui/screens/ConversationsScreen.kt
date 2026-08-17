package cc.thevar.blukit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.VibeGroup
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ConversationsScreen(
    state: BluetoothUiState,
    onVibeClick: (VibeGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = "ACTIVE VIBES",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = StealthPrimary
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (state.groups.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "NO PRIVATE VIBES YET",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(state.groups.sortedByDescending { it.lastVibeTimestamp }) { group ->
                    VibeConversationItem(
                        group = group,
                        lastMessage = state.messages.findLast { it.groupId == group.id }?.content ?: "START THE VIBE...",
                        time = timeFormatter.format(Date(group.lastVibeTimestamp)),
                        onClick = { onVibeClick(group) }
                    )
                }
            }
        }
    }
}

@Composable
fun VibeConversationItem(
    group: VibeGroup,
    lastMessage: String,
    time: String,
    onClick: () -> Unit
) {
    val isTie = group.type == VibeGroup.TYPE_TIE
    
    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isTie) StealthRose.copy(alpha = 0.3f) else StealthPrimary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isTie) StealthRose.copy(alpha = 0.1f) else StealthPrimary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isTie) Icons.Rounded.VerifiedUser else Icons.Rounded.Groups,
                    contentDescription = null,
                    tint = if (isTie) StealthRose else StealthPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = group.name.uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isTie) {
                        Text(
                            text = "TIE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = StealthRose,
                            modifier = Modifier
                                .background(StealthRose.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = lastMessage,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
            
            Text(
                text = time,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}

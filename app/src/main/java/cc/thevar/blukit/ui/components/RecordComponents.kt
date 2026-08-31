package cc.thevar.blukit.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageRecordItem(
    record: Message,
    onStatusChange: (Int) -> Unit,
    themeColor: Color = StealthPrimary,
    modifier: Modifier = Modifier
) {
    val isCompleted = record.taskStatus == Message.TASK_COMPLETED
    val isBlocked = record.taskStatus == Message.TASK_BLOCKED
    val isAbandoned = record.taskStatus == Message.TASK_ABANDONED
    val sdf = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Surface(
        color = StealthSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, when {
            isBlocked -> StealthError.copy(alpha = StealthAlphaHigh)
            isAbandoned -> StealthGray.copy(alpha = StealthAlphaHigh)
            else -> themeColor.copy(alpha = StealthAlphaMedium)
        }),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(when {
                        isCompleted -> themeColor
                        isBlocked -> StealthError.copy(alpha = StealthAlphaLow)
                        else -> Color.Transparent
                    })
                    .border(2.dp, if (isBlocked) StealthError else themeColor, CircleShape)
                    .clickable {
                        val nextStatus = when {
                            isCompleted -> Message.TASK_PENDING
                            else -> Message.TASK_COMPLETED
                        }
                        onStatusChange(nextStatus)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Complete",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                } else if (isBlocked) {
                    Icon(
                        imageVector = Icons.Rounded.Block,
                        contentDescription = "Blocked",
                        tint = StealthError,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.content, 
                    style = MaterialTheme.typography.bodyLarge,
                    color = when {
                        isCompleted -> StealthGray
                        isAbandoned -> StealthGray.copy(alpha = StealthAlphaHigh)
                        else -> Color.White
                    },
                    textDecoration = if (isCompleted || isAbandoned) TextDecoration.LineThrough else null
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    val statusText = when {
                        isBlocked -> "Preserved (Blocked)"
                        isAbandoned -> "Discarded"
                        isCompleted -> "Archived"
                        else -> "Record: ${record.dueDate?.let { sdf.format(Date(it)) } ?: "Permanent"}"
                    }
                    val statusColor = when {
                        isBlocked -> StealthError
                        isAbandoned -> StealthGray
                        else -> themeColor.copy(alpha = StealthAlphaHigh)
                    }

                    Icon(
                        imageVector = if (isCompleted) Icons.Rounded.TaskAlt else Icons.Rounded.Event,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }

            Row {
                if (!isCompleted && !isAbandoned) {
                    IconButton(onClick = { onStatusChange(if (isBlocked) Message.TASK_PENDING else Message.TASK_BLOCKED) }) {
                        Icon(
                            Icons.Rounded.PriorityHigh, 
                            contentDescription = "Priority", 
                            tint = if (isBlocked) StealthError else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageRecordCreator(
    onRecordCreated: (String, Long?) -> Unit,
    themeColor: Color = StealthPrimary,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StealthBlack)
            .padding(16.dp)
            .border(1.dp, themeColor.copy(alpha = StealthAlphaBorder), RoundedCornerShape(28.dp))
            .padding(24.dp)
    ) {
        Text(
            text = "New Shared Record",
            style = MaterialTheme.typography.titleMedium,
            color = themeColor
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = themeColor,
                unfocusedBorderColor = themeColor.copy(alpha = StealthAlphaMedium)
            ),
            placeholder = { 
                Text(
                    text = "Describe the record...", 
                    style = MaterialTheme.typography.bodyLarge,
                    color = StealthGray
                ) 
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { selectedDate = System.currentTimeMillis() + 86400000 }) {
                Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp), tint = themeColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Set Timestamp", 
                    style = MaterialTheme.typography.labelLarge,
                    color = themeColor
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    if (text.isNotBlank()) {
                        onRecordCreated(text, selectedDate)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Text(
                    text = "CONNECT", 
                    style = MaterialTheme.typography.labelLarge,
                    color = StealthOnPrimary
                )
            }
        }
    }
}

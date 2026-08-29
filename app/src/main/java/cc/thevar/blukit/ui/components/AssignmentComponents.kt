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
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.ui.theme.StealthAmber
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthRose
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AssignmentItem(
    assignment: MessagePayload,
    onStatusChange: (Int) -> Unit,
    themeColor: Color = StealthPrimary,
    modifier: Modifier = Modifier
) {
    val isCompleted = assignment.taskStatus == MessagePayload.TASK_COMPLETED
    val isBlocked = assignment.taskStatus == MessagePayload.TASK_BLOCKED
    val isAbandoned = assignment.taskStatus == MessagePayload.TASK_ABANDONED
    val sdf = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Surface(
        color = Color.Black,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, when {
            isBlocked -> Color.Red.copy(alpha = 0.4f)
            isAbandoned -> Color.Gray.copy(alpha = 0.4f)
            else -> themeColor.copy(alpha = 0.2f)
        }),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task Toggle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(when {
                        isCompleted -> themeColor
                        isBlocked -> Color.Red.copy(alpha = 0.2f)
                        else -> Color.Transparent
                    })
                    .border(2.dp, if (isBlocked) Color.Red else themeColor, CircleShape)
                    .clickable {
                        val nextStatus = when {
                            isCompleted -> MessagePayload.TASK_PENDING
                            else -> MessagePayload.TASK_COMPLETED
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
                        modifier = Modifier.size(16.dp)
                    )
                } else if (isBlocked) {
                    Icon(
                        imageVector = Icons.Rounded.Block,
                        contentDescription = "Blocked",
                        tint = Color.Red,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = assignment.content.uppercase(),
                    color = when {
                        isCompleted -> Color.Gray
                        isAbandoned -> Color.Gray.copy(alpha = 0.5f)
                        else -> Color.White
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (isCompleted || isAbandoned) TextDecoration.LineThrough else null
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusText = when {
                        isBlocked -> "BLOCKED"
                        isAbandoned -> "ABANDONED"
                        isCompleted -> "ACCOMPLISHED"
                        else -> "MISSION END: ${assignment.dueDate?.let { sdf.format(Date(it)) } ?: "OPEN ENDED"}"
                    }
                    val statusColor = when {
                        isBlocked -> Color.Red
                        isAbandoned -> Color.Gray
                        else -> themeColor.copy(alpha = 0.6f)
                    }

                    Icon(
                        imageVector = if (isCompleted) Icons.Rounded.TaskAlt else Icons.Rounded.Event,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Context Menu for Blocked/Abandoned
            Row {
                if (!isCompleted && !isAbandoned) {
                    IconButton(onClick = { onStatusChange(if (isBlocked) MessagePayload.TASK_PENDING else MessagePayload.TASK_BLOCKED) }) {
                        Icon(
                            Icons.Rounded.PriorityHigh, 
                            contentDescription = "Block", 
                            tint = if (isBlocked) Color.Red else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                if (assignment.assigneeId != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .background(StealthAmber.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ASSIGNED",
                            color = StealthAmber,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AssignmentCreator(
    onAssignmentCreated: (String, Long?) -> Unit,
    themeColor: Color = StealthPrimary,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(16.dp)
            .border(1.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "NEW MISSION",
            color = themeColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = themeColor,
                unfocusedIndicatorColor = themeColor.copy(alpha = 0.5f)
            ),
            placeholder = { Text("Objective details...", color = Color.Gray, fontSize = 14.sp) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { /* In real app, trigger date picker */ selectedDate = System.currentTimeMillis() + 86400000 }) {
                Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = themeColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SET MISSION END", color = themeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    if (text.isNotBlank()) {
                        onAssignmentCreated(text, selectedDate)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColor)
            ) {
                Text("PULSE", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }
    }
}

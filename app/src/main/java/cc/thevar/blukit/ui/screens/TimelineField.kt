package cc.thevar.blukit.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.ui.theme.StealthPrimary
import java.text.SimpleDateFormat
import java.util.*

/**
 * TIMELINE FIELD: A visual chronological path of mesh memories.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineField(
    messages: List<MessagePayload>,
    onBack: () -> Unit
) {
    val memories = remember(messages) {
        messages.filter { it.type == MessagePayload.TYPE_MEMORY || it.type == MessagePayload.TYPE_IMAGE }
            .sortedBy { it.timestamp }
    }
    
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MEMORY TIMELINE", fontWeight = FontWeight.Black, fontSize = 14.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        if (memories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("NO MEMORIES FOUND. PIN A PULSE TO PRESERVE IT.", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                items(memories) { memory ->
                    MemoryItem(memory, sdf.format(Date(memory.timestamp)))
                }
            }
        }
    }
}

@Composable
private fun MemoryItem(memory: MessagePayload, dateStr: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
            Text(text = memory.senderEmoji ?: "👤", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(modifier = Modifier.height(100.dp).width(2.dp)) {
                drawLine(
                    color = StealthPrimary.copy(alpha = 0.3f),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(text = dateStr, fontSize = 9.sp, color = StealthPrimary, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = memory.content.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "FROM ${memory.senderName.uppercase()}", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp)
                }
            }
        }
    }
}

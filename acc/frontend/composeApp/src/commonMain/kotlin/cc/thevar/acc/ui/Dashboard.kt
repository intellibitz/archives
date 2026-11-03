package cc.thevar.acc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.acc.protocol.*

@Composable
fun ThoughtStream(messages: List<AgentMessage>) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }
    
    Column {
        Text("AGENT THOUGHT STREAM", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                AgentMessageCard(message)
            }
        }
    }
}

@Composable
fun SystemPanel(state: SystemState?, onCommand: (String) -> Unit, onSpawn: (String, String) -> Unit, onStartEngine: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("HARDWARE STATUS", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        
        state?.stats?.let { stats ->
            StatItem("CPU", "${stats.cpuUtilization}%", stats.cpuUtilization / 100f)
            StatItem("RAM", "${stats.ramUsed.toInt()}G / ${stats.ramTotal.toInt()}G", stats.ramUsed / stats.ramTotal)
            if (stats.gpu.active) {
                StatItem("GPU", "${stats.gpu.utilization}%", stats.gpu.utilization / 100f)
                Text("VRAM: ${stats.gpu.memoryUsed.toInt()}MB / ${stats.gpu.memoryTotal.toInt()}MB", fontSize = 10.sp, color = Color.Gray)
                Text("Temp: ${stats.gpu.temperature}°C | Pwr: ${stats.gpu.power}W", fontSize = 10.sp, color = Color.Gray)
            }
            Text("Disk Usage: ${stats.diskUsage}", fontSize = 11.sp, color = Color.LightGray)
        } ?: Text("Loading stats...", color = Color.DarkGray)

        if (state?.engineOnline == false) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ollama is offline", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                        Text("Start engine to use local models", fontSize = 9.sp, color = Color.LightGray)
                    }
                    Button(
                        onClick = { onStartEngine("ollama") },
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f), contentColor = Color.Red)
                    ) {
                        Text("Start", fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("FLEET STATUS", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(0.6f)) {
            state?.fleet?.let { fleet ->
                items(fleet) { model ->
                    FleetItem(model, onProvision = { onCommand("up $it") }, onSpawn = onSpawn)
                }
            }
        }

        state?.partialDownloads?.takeIf { it.isNotEmpty() }?.let { partials ->
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("PARTIAL DOWNLOADS", style = MaterialTheme.typography.labelLarge, color = Color.Yellow)
                Spacer(modifier = Modifier.weight(1f))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(0.4f)) {
                items(partials) { name ->
                    PartialDownloadItem(name, onResume = { onCommand("up $it") })
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, progress: Float) {
    Column {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 12.sp)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = if (progress > 0.8f) Color.Red else MaterialTheme.colorScheme.secondary,
            trackColor = Color.DarkGray
        )
    }
}

@Composable
fun FleetItem(model: ModelStatus, onProvision: (String) -> Unit, onSpawn: (String, String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(
            if (model.isRunning) Color.Green else if (model.isInstalled) Color.Cyan else Color.DarkGray
        ))
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(model.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (model.isInstalled) Color.White else Color.Gray)
            Text(model.provider.uppercase(), fontSize = 9.sp, color = Color.Gray)
        }
        
        if (model.isInstalled) {
            IconButton(
                onClick = { onSpawn(model.name, "${model.provider}/${model.name}") },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.Cyan, modifier = Modifier.size(16.dp))
            }
        } else if (model.provider == "ollama") {
            IconButton(
                onClick = { onProvision(model.name) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Download, null, tint = Color.Green, modifier = Modifier.size(16.dp))
            }
        }
        
        Text(model.type, fontSize = 10.sp, color = if (model.type == "PRIV") Color.Magenta else Color.Cyan, modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
fun PartialDownloadItem(name: String, onResume: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Icon(Icons.Default.Pending, null, tint = Color.Yellow, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(name, fontSize = 11.sp, color = Color.White, modifier = Modifier.weight(1f))
        
        IconButton(
            onClick = { onResume(name) },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(Icons.Default.PlayArrow, null, tint = Color.Yellow, modifier = Modifier.size(16.dp))
        }
        
        Text("PARTIAL", fontSize = 9.sp, color = Color.Yellow)
    }
}

@Composable
fun AgentMessageCard(message: AgentMessage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (message.status) {
                AgentStatus.THINKING -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                AgentStatus.EXECUTING -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                AgentStatus.ERROR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(message.agentName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.weight(1f))
                StatusBadge(message.status.name, when(message.status) {
                    AgentStatus.THINKING -> Color.Blue
                    AgentStatus.EXECUTING -> Color.Green
                    AgentStatus.ERROR -> Color.Red
                    AgentStatus.DONE -> Color.Magenta
                    else -> Color.Gray
                })
            }
            Text(message.content, modifier = Modifier.padding(vertical = 4.dp))
            
            message.thoughts.forEach { thought ->
                Text("• ${thought.summary}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            message.actions.forEach { action ->
                ToolActionItem(action)
            }
        }
    }
}

@Composable
fun ToolActionItem(action: AgentAction) {
    Surface(
        color = Color.Black.copy(alpha = 0.3f),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("> ${action.tool} ${action.input}", fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = Color.Cyan)
            action.result?.let {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.DarkGray)
                Text(it, fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = Color.LightGray)
            }
        }
    }
}

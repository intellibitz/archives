package cc.thevar.acc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.acc.protocol.*

@Composable
fun Sidebar(
    systemState: SystemState?,
    onCommand: (String) -> Unit,
    onUpdate: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(200.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("CONTROLS", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        
        ControlButton("Update Acc", Icons.Default.SystemUpdate, Color.Magenta) { onUpdate() }

        systemState?.partialDownloads?.takeIf { it.isNotEmpty() }?.let { partials ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Yellow.copy(alpha = 0.1f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Yellow.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Partial Downloads:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Yellow)
                    partials.forEach { 
                        Text("• $it", fontSize = 9.sp, color = Color.LightGray)
                    }
                    Button(
                        onClick = { onCommand("up") },
                        modifier = Modifier.fillMaxWidth().height(24.dp).padding(top = 4.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow.copy(alpha = 0.2f), contentColor = Color.Yellow)
                    ) {
                        Text("Resume All", fontSize = 9.sp)
                    }
                }
            }
        }

        ControlButton("Provision (UP)", Icons.Default.CloudDownload, Color.Green) { onCommand("up") }
        ControlButton("Hardware Tune", Icons.Default.SettingsSuggest, Color(0xFFBB86FC)) { onCommand("tune-hw") }
        ControlButton("Sync Service", Icons.Default.Sync, Color.Cyan) { onCommand("sync") }
        ControlButton("Auto-Scale", Icons.Default.AutoGraph, Color.Green) { onCommand("auto-scale") }
        ControlButton("Benchmark", Icons.Default.Speed, Color.Yellow) { onCommand("benchmark") }
        ControlButton("Prune Models", Icons.Default.DeleteSweep, Color.Red) { onCommand("prune") }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("WORKERS", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        systemState?.workers?.forEach { worker ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(
                    when(worker.status) {
                        WorkerStatus.RUNNING -> Color.Green
                        WorkerStatus.COMPLETED -> Color.Cyan
                        WorkerStatus.CRASHED -> Color.Red
                        else -> Color.Gray
                    }
                ))
                Spacer(modifier = Modifier.width(8.dp))
                Text(worker.name, fontSize = 10.sp, color = Color.LightGray)
                if (worker.restarts > 0) {
                    Text(" (${worker.restarts})", fontSize = 9.sp, color = Color.Yellow)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        ControlButton("Stop All", Icons.Default.Stop, Color.Red) { onCommand("stop") }
    }
}

@Composable
fun ControlButton(text: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.1f), contentColor = color),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

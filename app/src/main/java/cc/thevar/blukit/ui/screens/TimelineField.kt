package cc.thevar.blukit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * THE LEDGER FIELD: A visual chronological path of existence records.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineField(
    echoes: List<Echo>,
    onBack: () -> Unit
) {
    val records = remember(echoes) {
        echoes.filter { it.type == Echo.TYPE_MEMORY || it.type == Echo.TYPE_IMAGE }
            .sortedBy { it.timestamp }
    }
    
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Scaffold(
        containerColor = StealthBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "THE LEDGER", 
                        style = MaterialTheme.typography.titleMedium,
                        letterSpacing = 2.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StealthBlack,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (records.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No saved records found", 
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                items(records) { record ->
                    RecordItem(record, sdf.format(Date(record.timestamp)))
                }
            }
        }
    }
}

@Composable
private fun RecordItem(record: Echo, dateStr: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
            Text(text = record.senderEmoji ?: "👤", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(modifier = Modifier.height(100.dp).width(2.dp)) {
                drawLine(
                    color = StealthPrimary.copy(alpha = 0.2f),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dateStr, 
                style = MaterialTheme.typography.labelSmall,
                color = StealthPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = StealthSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = StealthAlphaLow))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = record.content, 
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "from ${record.senderName}", 
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

package cc.thevar.blukit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    val ledgerEntries = remember(echoes) {
        val records = echoes.filter { it.type == Echo.TYPE_MEMORY || it.type == Echo.TYPE_IMAGE || it.type == Echo.TYPE_AI_SUMMARY }
            .sortedByDescending { it.timestamp }
        records
    }
    
    val sdf = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

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
        if (ledgerEntries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "THE LEDGER IS EMPTY", 
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(ledgerEntries) { entry ->
                    if (entry.type == Echo.TYPE_AI_SUMMARY) {
                        SynthesisHeader(entry)
                    } else {
                        RecordItem(entry, sdf.format(Date(entry.timestamp)))
                    }
                }
            }
        }
    }
}

@Composable
private fun SynthesisHeader(echo: Echo) {
    Surface(
        color = AtmosphereSocial.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AtmosphereSocial.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = AtmosphereSocial, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = echo.content.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = AtmosphereSocial,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun RecordItem(record: Echo, dateStr: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
            Text(text = record.senderEmoji ?: "👤", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(modifier = Modifier.height(80.dp).width(1.dp)) {
                drawLine(
                    color = StealthPrimary.copy(alpha = 0.1f),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dateStr, 
                    style = MaterialTheme.typography.labelSmall,
                    color = StealthPrimary.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.weight(1f))
                if (record.anchoredCount >= 2) {
                    Surface(
                        color = StealthAmber.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, StealthAmber.copy(alpha = 0.4f))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Security, contentDescription = null, tint = StealthAmber, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "ANCHORED", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = StealthAmber)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = StealthSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = record.content, 
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Source: ${record.senderName}", 
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.3f)
                    )
                    
                    if (record.anchoredCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Carried by ${record.anchoredCount} nearby Sources",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = StealthAmber.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

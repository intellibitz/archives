package cc.thevar.blukit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.ui.theme.*

/**
 * RESONANCE STREAM FIELD: A real-time stream of every Echo in the field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveFeedField(
    echoes: List<Echo>,
    sources: List<Source>,
    onBack: () -> Unit,
    onEchoClick: (String) -> Unit
) {
    Scaffold(
        containerColor = StealthBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "RESONANCE STREAM", 
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(echoes.reversed(), key = { it.messageId }) { echo ->
                val source = sources.find { it.id == echo.senderId || it.persistentId == echo.senderId }
                MessageItem(
                    msg = echo,
                    isSelected = false,
                    senderDevice = source,
                    pulseCount = 0,
                    isPulsed = false,
                    isMe = false,
                    isGrouped = false,
                    isMutual = false,
                    rowId = "live_${echo.messageId}",
                    onPulseClick = { onEchoClick(echo.messageId) },
                    onDeviceLongClick = {}
                )
            }
        }
    }
}

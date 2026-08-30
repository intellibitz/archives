package cc.thevar.blukit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.ui.theme.StealthBlack

/**
 * LIVE FEED FIELD: A flat, real-time stream of every message on the mesh.
 * This view ignores groups to provide an "X-like" discovery experience.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveFeedField(
    messages: List<MessagePayload>,
    peers: List<P2PDevice>,
    onBack: () -> Unit,
    onNavigateToPulse: (String) -> Unit
) {
    Scaffold(
        containerColor = StealthBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("LIVE FEED", fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.sp) },
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
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(messages.reversed(), key = { it.messageId }) { msg ->
                val sender = peers.find { it.id == msg.senderId || it.persistentId == msg.senderId }
                AnimatedPulseItem(
                    msg = msg,
                    isSelected = false,
                    senderDevice = sender,
                    pulseCount = 0,
                    isPulsed = false,
                    isMe = msg.senderId == "YOU",
                    isGrouped = false,
                    isMutual = false,
                    rowId = "live_${msg.messageId}",
                    onPulseClick = { onNavigateToPulse(msg.messageId) },
                    onDeviceLongClick = {}
                )
            }
        }
    }
}

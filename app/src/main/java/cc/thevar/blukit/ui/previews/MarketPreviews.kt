package cc.thevar.blukit.ui.previews

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.thevar.blukit.R
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.ui.screens.TieScreen
import cc.thevar.blukit.ui.viewmodels.*
import cc.thevar.blukit.ui.screens.RipplesField
import cc.thevar.blukit.ui.theme.BlukitTheme
import cc.thevar.blukit.ui.theme.StealthPrimary

@Preview(name = "Radar - Phone", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewRadarPhone() {
    BlukitTheme {
        RipplesField(
            state = BluetoothUiState(
                crowd = MeshCrowd(
                    scannedDevices = listOf(
                        P2PDevice("1", "?"),
                        P2PDevice("2", "?"),
                        P2PDevice("3", "?")
                    )
                )
            ),
            localDeviceId = "me",
            localEmoji = "👤",
            activeBubbles = emptyList(),
            onDeviceClick = {},
            onStartScan = {}
        )
    }
}

@Preview(name = "Chat - Phone", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewChatPhone() {
    BlukitTheme {
        TieScreen(
            state = BluetoothUiState(
                session = ResonanceSession(
                    messages = listOf(
                        MessagePayload(
                            messageId = "1",
                            senderId = "user1",
                            senderName = "?",
                            receiverId = "me",
                            content = "Hello!",
                            timestamp = 1628610000000,
                            status = MessagePayload.STATUS_DELIVERED
                        ),
                        MessagePayload(
                            messageId = "2",
                            senderId = "me",
                            senderName = "Me",
                            receiverId = "user1",
                            content = "Hey there!",
                            timestamp = 1628610060000,
                            status = MessagePayload.STATUS_SENT
                        )
                    ),
                    connectionState = AirConnectionState.Connected(P2PDevice("user1", "?"))
                )
            ),
            localDeviceId = "me",
            localEmoji = "👤",
            groupId = "group1",
            onDisconnect = {},
            onSendMessage = { _, _ -> },
            onBlockUser = {},
            onEnterPip = {}
        )
    }
}

@Preview(name = "Radar - Tablet", device = Devices.TABLET, showBackground = true)
@Composable
fun PreviewRadarTablet() {
    BlukitTheme {
        RipplesField(
            state = BluetoothUiState(
                crowd = MeshCrowd(
                    scannedDevices = listOf(
                        P2PDevice("1", "Vibe 1"),
                        P2PDevice("2", "Vibe 2"),
                        P2PDevice("3", "Vibe 3"),
                        P2PDevice("4", "Vibe 4")
                    )
                )
            ),
            localDeviceId = "me",
            localEmoji = "👤",
            activeBubbles = emptyList(),
            onDeviceClick = {},
            onStartScan = {}
        )
    }
}

@Preview(name = "Play Store Icon", widthDp = 512, heightDp = 512)
@Composable
fun PreviewPlayStoreIcon() {
    Box(
        modifier = Modifier
            .size(512.dp)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(400.dp)
        )
    }
}

@Preview(name = "Feature Graphic", widthDp = 1024, heightDp = 500)
@Composable
fun PreviewFeatureGraphic() {
    BlukitTheme(stealthMode = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "BLUKIT",
                    style = MaterialTheme.typography.displayLarge,
                    color = StealthPrimary, 
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "MAKE PEOPLE VIBE",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

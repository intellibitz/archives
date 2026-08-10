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
import cc.thevar.blukit.ui.chat.ChatScreen
import cc.thevar.blukit.ui.discovery.BluetoothUiState
import cc.thevar.blukit.ui.discovery.RadarScreen
import cc.thevar.blukit.ui.profile.ProfileScreen
import cc.thevar.blukit.ui.theme.BlukitTheme

@Preview(name = "Radar - Phone", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewRadarPhone() {
    BlukitTheme {
        RadarScreen(
            state = BluetoothUiState(
                scannedDevices = listOf(
                    P2PDevice("Peer 1", "1"),
                    P2PDevice("Peer 2", "2"),
                    P2PDevice("Peer 3", "3")
                )
            ),
            onDeviceClick = {}
        )
    }
}

@Preview(name = "Chat - Phone", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewChatPhone() {
    BlukitTheme {
        ChatScreen(
            state = BluetoothUiState(
                messages = listOf(
                    MessagePayload(
                        messageId = "1",
                        senderId = "user1",
                        senderName = "Peer 1",
                        content = "Hello!",
                        timestamp = 1628610000000
                    ),
                    MessagePayload(
                        messageId = "2",
                        senderId = "me",
                        senderName = "Me",
                        content = "Hey there!",
                        timestamp = 1628610060000
                    )
                ),
                isConnected = true
            ),
            localDeviceId = "me",
            onDisconnect = {},
            onSendMessage = {},
            onBlockUser = {}
        )
    }
}

@Preview(name = "Profile - Stealth", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewProfileStealth() {
    BlukitTheme(stealthMode = true) {
        ProfileScreen(
            currentNickname = "StealthUser",
            currentEmoji = "🦊",
            isStealthMode = true,
            onSaveNickname = {},
            onSaveEmoji = {},
            onToggleStealth = {},
            onNavigateNext = {}
        )
    }
}

@Preview(name = "Radar - Tablet", device = Devices.TABLET, showBackground = true)
@Composable
fun PreviewRadarTablet() {
    BlukitTheme {
        RadarScreen(
            state = BluetoothUiState(
                scannedDevices = listOf(
                    P2PDevice("Peer 1", "1"),
                    P2PDevice("Peer 2", "2"),
                    P2PDevice("Peer 3", "3"),
                    P2PDevice("Peer 4", "4")
                )
            ),
            onDeviceClick = {}
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
                    text = "Blukit",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color(0xFFFFB300), 
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Decentralized Offline Mesh Chat",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

package cc.thevar.blukit.ui.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import cc.thevar.blukit.data.bluetooth.BluetoothDeviceDomain
import cc.thevar.blukit.data.bluetooth.BluetoothPayload
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
                    BluetoothDeviceDomain("Peer 1", "1"),
                    BluetoothDeviceDomain("Peer 2", "2"),
                    BluetoothDeviceDomain("Peer 3", "3")
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
                    BluetoothPayload(
                        messageId = "1",
                        senderId = "user1",
                        senderName = "Peer 1",
                        content = "Hello!",
                        timestamp = 1628610000000
                    ),
                    BluetoothPayload(
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
                    BluetoothDeviceDomain("Peer 1", "1"),
                    BluetoothDeviceDomain("Peer 2", "2"),
                    BluetoothDeviceDomain("Peer 3", "3"),
                    BluetoothDeviceDomain("Peer 4", "4")
                )
            ),
            onDeviceClick = {}
        )
    }
}

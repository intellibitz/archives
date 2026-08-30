package cc.thevar.blukit.ui.previews

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import cc.thevar.blukit.R
import cc.thevar.blukit.domain.model.MeshMessage
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MeshRoom
import cc.thevar.blukit.ui.screens.*
import cc.thevar.blukit.ui.theme.BlukitTheme
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthBlack
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import cc.thevar.blukit.ui.viewmodels.NearbyPeers
import cc.thevar.blukit.ui.viewmodels.MeshSession
import cc.thevar.blukit.ui.viewmodels.RadioConnectionState

@Preview(name = "Radar - Phone", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewRadarPhone() {
    BlukitTheme {
        DiscoveryField(
            state = BluetoothUiState(
                crowd = NearbyPeers(
                    scannedDevices = listOf(
                        P2PDevice("1", "?"),
                        P2PDevice("2", "?"),
                        P2PDevice("3", "?")
                    )
                )
            ),
            localDeviceId = "me",
            header = { Text("PREVIEW HEADER", color = Color.White) }
        )
    }
}

@Preview(name = "Chat - Phone", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewChatPhone() {
    BlukitTheme {
        ChannelField(
            state = BluetoothUiState(
                session = MeshSession(
                    messages = listOf(
                        MeshMessage(
                            messageId = "1",
                            senderId = "user1",
                            senderName = "?",
                            receiverId = "me",
                            content = "Hello!",
                            timestamp = 1628610000000,
                            status = MeshMessage.STATUS_DELIVERED
                        ),
                        MeshMessage(
                            messageId = "2",
                            senderId = "me",
                            senderName = "Me",
                            receiverId = "user1",
                            content = "Hey there!",
                            timestamp = 1628610060000,
                            status = MeshMessage.STATUS_SENT
                        )
                    ),
                    connectionState = RadioConnectionState.Connected(P2PDevice("user1", "?"))
                )
            ),
            localDeviceId = "me",
            header = { Text("PREVIEW HEADER", color = Color.White) },
            roomId = "group1",
        )
    }
}

@Preview(name = "Play Store Icon", widthDp = 512, heightDp = 512)
@Composable
fun PreviewPlayStoreIcon() {
    Box(
        modifier = Modifier
            .size(512.dp)
            .background(StealthBlack),
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
                .background(StealthBlack),
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
                    text = "DISCOVER THE ROOM",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(name = "Blukit Header", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewTacticalHeader() {
    BlukitTheme {
        BlukitHeader(
            themeColor = StealthPrimary,
            onAwakenBluetooth = {},
            onAwakenWifi = {},
            onGrantPermissions = {},
            onOpenSettings = {},
            onShowPrivacy = {}
        )
    }
}

@Preview(name = "Identity Stage", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewHumanityStage() {
    BlukitTheme {
        Box(modifier = Modifier.fillMaxSize().background(StealthBlack)) {
            IdentityStage(
                title = "GLOBAL ROOM",
                breadcrumbTrail = listOf("DISCOVERY", "ROOM"),
                onCrumbClick = {},
                activeRooms = emptyList(),
                onShowTimeline = {},
                onResetProfile = {},
                onBack = {},
                themeColor = StealthPrimary
            )
        }
    }
}

@Preview(name = "LiveMessageTicker - Headers", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewLiveMessageTickerHeaders() {
    val me = "me"
    val user1 = "user1"
    val user2 = "user2"
    
    val messages = listOf(
        MeshMessage("1", user1, "Alice", "👩", groupId = "air_hub", groupName = "GLOBAL ROOM", content = "Public message!", timestamp = System.currentTimeMillis(), messageScope = MeshMessage.SCOPE_PUBLIC),
        MeshMessage("2", me, "ME", "👤", groupId = "silence", groupName = "SILENCE", content = "Local trace", timestamp = System.currentTimeMillis() - 1000, messageScope = MeshMessage.MESSAGE_SILENCE),
        MeshMessage("3", user2, "Bob", "👨", groupId = "tie1", groupName = "PARTY", content = "Private room", timestamp = System.currentTimeMillis() - 2000, messageScope = MeshMessage.SCOPE_PRIVATE)
    )
    
    val groups = listOf(
        MeshRoom("air_hub", "GLOBAL ROOM", setOf(user1, me), MeshRoom.SCOPE_PUBLIC),
        MeshRoom("silence", "SILENCE", setOf(me), MeshRoom.SCOPE_LOCAL),
        MeshRoom("tie1", "PARTY", setOf(user2, me, "user3"), MeshRoom.SCOPE_PRIVATE)
    )
    
    val energyList = listOf(
        Pair(P2PDevice(user1, "Alice", "👩"), messages[0]),
        Pair(P2PDevice(me, "ME", "👤"), messages[1]),
        Pair(P2PDevice(user2, "Bob", "👨"), messages[2])
    )

    BlukitTheme(stealthMode = true) {
        Box(modifier = Modifier.fillMaxSize().background(StealthBlack).padding(16.dp)) {
            LiveMessageTicker(
                state = BluetoothUiState(
                    session = MeshSession(
                        messages = messages,
                        groups = groups
                    )
                ),
                energyList = energyList,
                pulseCounts = mapOf(user1 to 1, me to 1, user2 to 5),
                localDeviceId = me,
                localNickname = "ME",
                pulsedPeers = emptySet(),
                isGrouped = true,
                onPulseClick = {},
                onDeviceClick = {},
                onDeviceLongClick = {},
                reverseLayout = false
            )
        }
    }
}

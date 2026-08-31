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
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Sphere
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
        SensingField(
            state = BluetoothUiState(
                crowd = NearbyPeers(
                    scannedDevices = listOf(
                        Source("1", "?"),
                        Source("2", "?"),
                        Source("3", "?")
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
        PrivateSphereField(
            state = BluetoothUiState(
                session = MeshSession(
                    messages = listOf(
                        Echo(
                            messageId = "1",
                            senderId = "user1",
                            senderName = "?",
                            receiverId = "me",
                            content = "Hello!",
                            timestamp = 1628610000000,
                            status = Echo.STATUS_DELIVERED
                        ),
                        Echo(
                            messageId = "2",
                            senderId = "me",
                            senderName = "Me",
                            receiverId = "user1",
                            content = "Hey there!",
                            timestamp = 1628610060000,
                            status = Echo.STATUS_SENT
                        )
                    ),
                    connectionState = RadioConnectionState.Connected(Source("user1", "?"))
                )
            ),
            localDeviceId = "me",
            header = { Text("PREVIEW HEADER", color = Color.White) },
            sphereId = "group1",
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
                    text = "OWN YOUR ECHO",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(name = "Resonance Header", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewTacticalHeader() {
    BlukitTheme {
        ResonanceHeader(
            themeColor = StealthPrimary,
            onAwakenBluetooth = {},
            onAwakenWifi = {},
            onGrantPermissions = {},
            onOpenSettings = {},
            onLogout = {}
        )
    }
}

@Preview(name = "Identity Stage", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewHumanityStage() {
    BlukitTheme {
        Box(modifier = Modifier.fillMaxSize().background(StealthBlack)) {
            IdentityStage(
                title = "GLOBAL SPHERE",
                breadcrumbTrail = listOf("SENSING", "SPHERE"),
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

@Preview(name = "ResonanceTicker - Headers", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewLiveMessageTickerHeaders() {
    val me = "me"
    val user1 = "user1"
    val user2 = "user2"
    
    val echoes = listOf(
        Echo("1", user1, "Alice", "👩", groupId = "air_hub", groupName = "GLOBAL SPHERE", content = "Public record!", timestamp = System.currentTimeMillis(), messageScope = Echo.SCOPE_PUBLIC),
        Echo("2", me, "ME", "👤", groupId = "silence", groupName = "SILENCE", content = "Local trace", timestamp = System.currentTimeMillis() - 1000, messageScope = Echo.MESSAGE_SILENCE),
        Echo("3", user2, "Bob", "👨", groupId = "tie1", groupName = "PARTY", content = "Private record", timestamp = System.currentTimeMillis() - 2000, messageScope = Echo.MESSAGE_WHISPER)
    )
    
    val spheres = listOf(
        Sphere("air_hub", "GLOBAL SPHERE", setOf(user1, me), Sphere.SCOPE_PUBLIC),
        Sphere("silence", "SILENCE", setOf(me), Sphere.SCOPE_LOCAL),
        Sphere("tie1", "PARTY", setOf(user2, me, "user3"), Sphere.SCOPE_PRIVATE)
    )
    
    val resonanceList = listOf(
        Pair(Source(user1, "Alice", "👩"), echoes[0]),
        Pair(Source(me, "ME", "👤"), echoes[1]),
        Pair(Source(user2, "Bob", "👨"), echoes[2])
    )

    BlukitTheme(stealthMode = true) {
        Box(modifier = Modifier.fillMaxSize().background(StealthBlack).padding(16.dp)) {
            ResonanceTicker(
                state = BluetoothUiState(
                    session = MeshSession(
                        messages = echoes,
                        groups = spheres
                    )
                ),
                resonanceList = resonanceList,
                echoCounts = mapOf(user1 to 1, me to 1, user2 to 5),
                localDeviceId = me,
                localNickname = "ME",
                pulsedPeers = emptySet(),
                isGrouped = true,
                onEchoClick = {},
                onSourceClick = {},
                onSourceLongClick = {},
                reverseLayout = false
            )
        }
    }
}

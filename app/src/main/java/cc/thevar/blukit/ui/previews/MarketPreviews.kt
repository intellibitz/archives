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
import cc.thevar.blukit.R
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.Resonance
import cc.thevar.blukit.ui.viewmodels.*
import cc.thevar.blukit.ui.screens.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import cc.thevar.blukit.ui.theme.BlukitTheme
import cc.thevar.blukit.ui.theme.StealthPrimary

@Preview(name = "Radar - Phone", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewRadarPhone() {
    BlukitTheme {
        EventField(
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
            header = { Text("PREVIEW HEADER", color = Color.White) },
            onDeviceClick = {},
            onDeletePulse = {},
            onWhisper = {}
        )
    }
}

@Preview(name = "Chat - Phone", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewChatPhone() {
    BlukitTheme {
        ChainField(
            state = BluetoothUiState(
                session = PulseSession(
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
                    connectionState = RadioConnectionState.Connected(P2PDevice("user1", "?"))
                )
            ),
            localDeviceId = "me",
            header = { Text("PREVIEW HEADER", color = Color.White) },
            groupId = "group1",
            onDisconnect = {},
            onSendMessage = { _, _ -> }
        )
    }
}

@Preview(name = "Radar - Tablet", device = Devices.TABLET, showBackground = true)
@Composable
fun PreviewRadarTablet() {
    BlukitTheme {
        EventField(
            state = BluetoothUiState(
                crowd = MeshCrowd(
                    scannedDevices = listOf(
                        P2PDevice("1", "Pulse 1"),
                        P2PDevice("2", "Pulse 2"),
                        P2PDevice("3", "Pulse 3"),
                        P2PDevice("4", "Pulse 4")
                    )
                )
            ),
            localDeviceId = "me",
            header = { Text("PREVIEW HEADER", color = Color.White) },
            onDeviceClick = {},
            onDeletePulse = {},
            onWhisper = {}
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
                    text = "MAKE PEOPLE PULSE",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(name = "Tactical Header", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewTacticalHeader() {
    BlukitTheme {
        BlukitTacticalHeader(
            isStealthMode = false,
            lowPowerMode = false,
            isBluetoothOff = false,
            isWifiOff = false,
            isPermissionMissing = false,
            isPermanentlyDenied = false,
            themeColor = StealthPrimary,
            onToggleStealth = {},
            onToggleLowPower = {},
            onAwakenBluetooth = {},
            onAwakenWifi = {},
            onGrantPermissions = {},
            onOpenSettings = {},
            onShowPrivacy = {}
        )
    }
}

@Preview(name = "Humanity Stage", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewHumanityStage() {
    BlukitTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            BlukitHumanityStage(
                title = "THE CROWD",
                breadcrumbTrail = listOf("Event", "Crowd"),
                onCrumbClick = {},
                activeCrowds = emptyList(),
                onShowTimeline = {},
                onResetProfile = {},
                onTitleClick = {},
                onBack = {},
                themeColor = StealthPrimary
            )
        }
    }
}

@Preview(name = "PulsingResonanceTicker - Headers", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewPulsingResonanceTickerHeaders() {
    val me = "me"
    val user1 = "user1"
    val user2 = "user2"
    
    val messages = listOf(
        MessagePayload("1", user1, "Alice", "👩", groupId = "air_hub", groupName = "THE CROWD", content = "Public pulse!", timestamp = System.currentTimeMillis(), pulseType = MessagePayload.PULSE_SHOUT),
        MessagePayload("2", me, "ME", "👤", groupId = "silence", groupName = "SILENCE", content = "Local trace", timestamp = System.currentTimeMillis() - 1000, pulseType = MessagePayload.PULSE_SILENCE),
        MessagePayload("3", user2, "Bob", "👨", groupId = "tie1", groupName = "PARTY", content = "Private group", timestamp = System.currentTimeMillis() - 2000, pulseType = MessagePayload.PULSE_WHISPER)
    )
    
    val groups = listOf(
        Resonance("air_hub", "THE CROWD", setOf(user1, me), Resonance.SCOPE_PUBLIC),
        Resonance("silence", "SILENCE", setOf(me), Resonance.SCOPE_LOCAL),
        Resonance("tie1", "PARTY", setOf(user2, me, "user3"), Resonance.SCOPE_PRIVATE)
    )
    
    val energyList = listOf(
        Pair(P2PDevice(user1, "Alice", "👩"), messages[0]),
        Pair(P2PDevice(me, "ME", "👤"), messages[1]),
        Pair(P2PDevice(user2, "Bob", "👨"), messages[2])
    )

    BlukitTheme(stealthMode = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp)) {
            PulsingResonanceTicker(
                state = BluetoothUiState(
                    session = PulseSession(
                        messages = messages,
                        groups = groups
                    )
                ),
                energyList = energyList,
                pulseCounts = mapOf(user1 to 1, me to 1, user2 to 5),
                localDeviceId = me,
                pulsedPeers = emptySet(),
                isGrouped = true,
                onPulseClick = {},
                onDeviceLongClick = {},
                onDeletePulse = {},
                reverseLayout = false
            )
        }
    }
}

@Preview(name = "Crowd Ritual Ghost", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewCrowdRitualGhost() {
    BlukitTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            CrowdRitualGhost(
                onNameChange = {},
                onDone = { _ -> },
                onDismiss = {},
                nearbyAirs = listOf(
                    Resonance("1", "GATE 7", emptySet(), Resonance.SCOPE_PUBLIC),
                    Resonance("2", "CONCERT", emptySet(), Resonance.SCOPE_PUBLIC)
                ),
                title = "EVENT RITUAL",
                hint = "NAME THE EVENT"
            )
        }
    }
}

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
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Group
import cc.thevar.blukit.ui.screens.*
import cc.thevar.blukit.ui.theme.BlukitTheme
import cc.thevar.blukit.ui.theme.StealthPrimary
import cc.thevar.blukit.ui.theme.StealthBlack
import cc.thevar.blukit.ui.viewmodels.ConnectionUiState
import cc.thevar.blukit.ui.viewmodels.ConnectionPeers
import cc.thevar.blukit.ui.viewmodels.ConnectionSession
import cc.thevar.blukit.ui.viewmodels.RadioConnectionState
import cc.thevar.blukit.ui.components.*
import cc.thevar.blukit.domain.power.HarmonyReport

@Preview(name = "Radar - Phone", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewRadarPhone() {
    BlukitTheme {
        NearbyField(
            state = ConnectionUiState(
                crowd = ConnectionPeers(
                    scannedDevices = listOf(
                        Source("1", "Alice"),
                        Source("2", "Bob"),
                        Source("3", "Charlie")
                    )
                )
            ),
            localDeviceId = "me",
            header = { Text("PREVIEW HEADER", color = Color.White) },
            breadcrumbTrail = listOf("NEARBY"),
            onCrumbClick = {},
            userNickname = "ME",
            harmonyReport = HarmonyReport(),
            onShowTimeline = {},
            onResetProfile = {},
            onNavigateToGroup = {},
            onNavigateToMessage = {},
            onSourceLongClick = {},
            onAcceptRadio = {},
            onDenyRadio = {},
            onRestoreCrowd = {},
            onNavigateToLiveFeed = {},
            onSearchToggle = {},
            isSearchActive = false,
            onStartWhisper = {},
            onStartSubGroup = {},
            onClearSelection = {},
            onCreatePublicRoom = { _, _ -> }
        )
    }
}

@Preview(name = "Chat - Phone", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewChatPhone() {
    val user1 = Source("user1", "Alice")
    val me = "me"
    BlukitTheme {
        PrivateGroupField(
            state = ConnectionUiState(
                session = ConnectionSession(
                    messages = listOf(
                        Message(
                            messageId = "1",
                            senderId = "user1",
                            senderName = "Alice",
                            receiverId = "me",
                            content = "Hello!",
                            timestamp = 1628610000000,
                            status = Message.STATUS_DELIVERED
                        ),
                        Message(
                            messageId = "2",
                            senderId = "me",
                            senderName = "Me",
                            receiverId = "user1",
                            content = "Hey there!",
                            timestamp = 1628610060000,
                            status = Message.STATUS_SENT
                        )
                    ),
                    connectionState = RadioConnectionState.Connected(user1)
                )
            ),
            localDeviceId = me,
            header = { Text("PREVIEW HEADER", color = Color.White) },
            groupId = "group1",
            breadcrumbTrail = listOf("NEARBY", "ALICE"),
            onCrumbClick = {},
            userNickname = "ME",
            activeGroups = emptyList(),
            onShowTimeline = {},
            onResetProfile = {},
            onBack = {},
            onNavigateToMessage = {},
            onNavigateToGroup = {},
            onSourceLongClick = {},
            onSend = {},
            onUpdateRecord = { _, _, _, _ -> },
            onVaultGroup = { _, _ -> },
            onSeniorVaultGroup = { _, _ -> },
            onRemoveMember = { _, _ -> },
            onAssignRole = { _, _, _ -> },
            onPushRitual = { _, _ -> },
            showMemberManagement = false,
            onShowManagement = {},
            onDismissManagement = {},
            onStartWhisper = {},
            onStartSubGroup = {},
            onClearSelection = {},
            isStealthMode = false,
            lowPowerMode = false,
            onToggleStealth = {},
            onToggleLowPower = {},
            trend = null,
            isSearchActive = false,
            onSearchToggle = {},
            onAcceptRadio = {},
            onDenyRadio = {}
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
                    text = "OWN YOUR DATA",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(name = "Blukit Toolbar", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewBlukitToolbar() {
    BlukitTheme {
        BlukitToolbar(
            title = "Public Hub",
            onLogout = {},
            onResetProfile = {},
            themeColor = StealthPrimary,
            connectionStatus = "3 online"
        )
    }
}

@Preview(name = "Identity Stage", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewHumanityStage() {
    BlukitTheme {
        Box(modifier = Modifier.fillMaxSize().background(StealthBlack)) {
            IdentityStage(
                title = "GLOBAL GROUP",
                onLogout = {},
                onResetProfile = {},
                onBack = {},
                themeColor = StealthPrimary
            )
        }
    }
}

@Preview(name = "Source Options Menu", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewSourceOptionsMenu() {
    BlukitTheme {
        Box(modifier = Modifier.fillMaxSize().background(StealthBlack)) {
            SourceOptionsMenu(
                device = Source("1", "Alice", "👩"),
                isTied = true,
                isBlocked = false,
                isRequesting = false,
                activeGroupId = "group1",
                isAlreadyInActiveGroup = false,
                onMessage = {},
                onAccept = {},
                onDeny = {},
                onDisconnect = {},
                onSelect = {},
                onIdentify = {},
                onBlock = {},
                onUnblock = {},
                onSync = {},
                onAddToGroup = {},
                onRemoveFromGroup = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "ConnectionTicker - Headers", device = Devices.PHONE, showBackground = true)
@Composable
fun PreviewConnectionTickerHeaders() {
    val me = "me"
    val user1 = "user1"
    val user2 = "user2"
    
    val messages = listOf(
        Message("1", user1, "Alice", "👩", groupId = "connection_hub", groupName = "GLOBAL GROUP", content = "Public record!", timestamp = System.currentTimeMillis(), messageScope = Message.SCOPE_PUBLIC),
        Message("2", me, "ME", "👤", groupId = "local_hub", groupName = "LOCAL", content = "Local trace", timestamp = System.currentTimeMillis() - 1000, messageScope = Message.MESSAGE_SILENCE),
        Message("3", user2, "Bob", "👨", groupId = "group1", groupName = "PARTY", content = "Private record", timestamp = System.currentTimeMillis() - 2000, messageScope = Message.MESSAGE_WHISPER)
    )
    
    val groups = listOf(
        Group("connection_hub", "GLOBAL GROUP", setOf(user1, me), Group.SCOPE_PUBLIC),
        Group("local_hub", "LOCAL", setOf(me), Group.SCOPE_LOCAL),
        Group("group1", "PARTY", setOf(user2, me, "user3"), Group.SCOPE_PRIVATE)
    )
    
    val connectionList = listOf(
        Pair(Source(user1, "Alice", "👩"), messages[0]),
        Pair(Source(me, "ME", "👤"), messages[1]),
        Pair(Source(user2, "Bob", "👨"), messages[2])
    )

    BlukitTheme(stealthMode = true) {
        Box(modifier = Modifier.fillMaxSize().background(StealthBlack).padding(16.dp)) {
            ConnectionTicker(
                state = ConnectionUiState(
                    session = ConnectionSession(
                        messages = messages,
                        groups = groups
                    )
                ),
                connectionList = connectionList,
                messageCounts = mapOf(user1 to 1, me to 1, user2 to 5),
                localDeviceId = me,
                localNickname = "ME",
                pulsedPeers = emptySet(),
                isGrouped = true,
                onMessageClick = {},
                onSourceClick = {},
                onSourceLongClick = {},
                reverseLayout = false
            )
        }
    }
}

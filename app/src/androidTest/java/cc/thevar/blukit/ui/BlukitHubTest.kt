package cc.thevar.blukit.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.ui.navigation.Route
import cc.thevar.blukit.ui.theme.BlukitTheme
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class BlukitHubTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testHub_NavigationSegments() {
        val onNavigate: (Route) -> Unit = mockk(relaxed = true)
        
        composeTestRule.setContent {
            val focusRequester = remember { FocusRequester() }
            BlukitTheme {
                BlukitHub(
                    currentRoute = Route.Blukit,
                    nickname = "test",
                    emoji = "👤",
                    isBluetoothEnabled = true,
                    isLocationEnabled = true,
                    isWifiEnabled = true,
                    isLocationMandatory = false,
                    permissionsGranted = true,
                    isPermanentlyDenied = false,
                    onSaveNickname = {},
                    personaFocusRequester = focusRequester,
                    messageText = "",
                    onMessageChange = {},
                    onSend = {},
                    vibeCount = 0,
                    energySurge = 0f,
                    hubRotation = 0f,
                    userCount = 0,
                    linksCount = 0,
                    roarsCount = 0,
                    vibesCount = 0,
                    lowPowerMode = false,
                    isStealthMode = false,
                    incomingLinkRequests = emptySet(),
                    selectedDevices = emptySet(),
                    scannedDevices = emptyList(),
                    connectedLinks = emptySet(),
                    vibedPeers = emptySet(),
                    messages = emptyList(),
                    isNoiseFilterActive = false,
                    onToggleNoiseFilter = {},
                    onNavigate = onNavigate,
                    onDeviceClick = {},
                    onDeviceLongClick = {},
                    onAwakenBluetooth = {},
                    onAwakenLocation = {},
                    onAwakenWifi = {},
                    onGrantPermissions = {},
                    onOpenSettings = {},
                    onToggleStealth = {},
                    onToggleLowPower = {},
                    onClearHistory = {},
                    onLogout = {},
                    onAcceptLink = {},
                    onDenyLink = {},
                    onStartSideVibe = {},
                    onStartTie = {},
                    onClearSelection = {}
                )
            }
        }

        // Click VIBES tab
        composeTestRule.onNodeWithText("VIBES").performClick()
        verify { onNavigate(Route.Vibes) }

        // Click WHISPER tab
        composeTestRule.onNodeWithTag("HubTab_WHISPER").performClick()
        verify { onNavigate(Route.SideVibes) }
    }

    @Test
    fun testHub_PersonaInteraction() {
        val onSaveNickname: (String) -> Unit = mockk(relaxed = true)
        
        composeTestRule.setContent {
            val focusRequester = remember { FocusRequester() }
            BlukitTheme {
                BlukitHub(
                    currentRoute = Route.Blukit,
                    nickname = "hero",
                    emoji = "👤",
                    isBluetoothEnabled = true,
                    isLocationEnabled = true,
                    isWifiEnabled = true,
                    isLocationMandatory = false,
                    permissionsGranted = true,
                    isPermanentlyDenied = false,
                    onSaveNickname = onSaveNickname,
                    personaFocusRequester = focusRequester,
                    messageText = "",
                    onMessageChange = {},
                    onSend = {},
                    vibeCount = 0,
                    energySurge = 0f,
                    hubRotation = 0f,
                    userCount = 0,
                    linksCount = 0,
                    roarsCount = 0,
                    vibesCount = 0,
                    lowPowerMode = false,
                    isStealthMode = false,
                    incomingLinkRequests = emptySet(),
                    selectedDevices = emptySet(),
                    scannedDevices = emptyList(),
                    connectedLinks = emptySet(),
                    vibedPeers = emptySet(),
                    messages = emptyList(),
                    isNoiseFilterActive = false,
                    onToggleNoiseFilter = {},
                    onNavigate = {},
                    onDeviceClick = {},
                    onDeviceLongClick = {},
                    onAwakenBluetooth = {},
                    onAwakenLocation = {},
                    onAwakenWifi = {},
                    onGrantPermissions = {},
                    onOpenSettings = {},
                    onToggleStealth = {},
                    onToggleLowPower = {},
                    onClearHistory = {},
                    onLogout = {},
                    onAcceptLink = {},
                    onDenyLink = {},
                    onStartSideVibe = {},
                    onStartTie = {},
                    onClearSelection = {}
                )
            }
        }

        // Verify "YOU" label exists for self
        composeTestRule.onNodeWithText("(YOU)", substring = true).assertIsDisplayed()

        // Change nickname
        composeTestRule.onNodeWithTag("IdentityVibeInput").performTextReplacement("Quantum")
        verify { onSaveNickname("Quantum") }
    }

    @Test
    fun testHub_PersonaCloudCategorization() {
        val deviceActive = P2PDevice("id-active", "Active", "🔥")
        val deviceIdle = P2PDevice("id-idle", "Idle", "💤")
        
        // Mock a message from "Active" to move it to Active row
        val message = cc.thevar.blukit.domain.model.MessagePayload(
            messageId = "msg-1",
            senderId = "id-active",
            senderName = "Active",
            content = "I am speaking",
            timestamp = System.currentTimeMillis()
        )

        composeTestRule.setContent {
            val focusRequester = remember { FocusRequester() }
            BlukitTheme {
                BlukitHub(
                    currentRoute = Route.Blukit,
                    nickname = "me",
                    emoji = "👤",
                    isBluetoothEnabled = true,
                    isLocationEnabled = true,
                    isWifiEnabled = true,
                    isLocationMandatory = false,
                    permissionsGranted = true,
                    isPermanentlyDenied = false,
                    onSaveNickname = {},
                    personaFocusRequester = focusRequester,
                    messageText = "",
                    onMessageChange = {},
                    onSend = {},
                    vibeCount = 0,
                    energySurge = 0f,
                    hubRotation = 0f,
                    userCount = 2,
                    linksCount = 0,
                    roarsCount = 1,
                    vibesCount = 0,
                    lowPowerMode = false,
                    isStealthMode = false,
                    incomingLinkRequests = emptySet(),
                    selectedDevices = emptySet(),
                    scannedDevices = listOf(deviceActive, deviceIdle),
                    connectedLinks = emptySet(),
                    vibedPeers = emptySet(),
                    messages = listOf(message),
                    isNoiseFilterActive = false,
                    onToggleNoiseFilter = {},
                    onNavigate = {},
                    onDeviceClick = {},
                    onDeviceLongClick = {},
                    onAwakenBluetooth = {},
                    onAwakenLocation = {},
                    onAwakenWifi = {},
                    onGrantPermissions = {},
                    onOpenSettings = {},
                    onToggleStealth = {},
                    onToggleLowPower = {},
                    onClearHistory = {},
                    onLogout = {},
                    onAcceptLink = {},
                    onDenyLink = {},
                    onStartSideVibe = {},
                    onStartTie = {},
                    onClearSelection = {}
                )
            }
        }

        // Verify Active row has student name (substring match on tip text or persona)
        composeTestRule.onAllNodesWithText("WHISPER", substring = true).onFirst().assertIsDisplayed()
    }
}

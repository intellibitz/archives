package cc.thevar.blukit.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.model.VibeGroup
import cc.thevar.blukit.ui.theme.BlukitTheme
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class TieScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testTieScreen_MessageFlow() {
        val onSendMessage: (String, String) -> Unit = mockk(relaxed = true)
        val device = P2PDevice("id-peer", "Friend", "🤝")
        val group = VibeGroup("group-1", "Vibe Group", setOf("me", "id-peer"), VibeGroup.TYPE_TIE)
        
        composeTestRule.setContent {
            BlukitTheme {
                TieScreen(
                    state = BluetoothUiState(
                        groups = listOf(group),
                        scannedDevices = listOf(device),
                        connectedLinks = setOf("id-peer")
                    ),
                    localDeviceId = "me",
                    localEmoji = "👤",
                    localNickname = "hero",
                    onNicknameChange = {},
                    groupId = "group-1",
                    onDisconnect = {},
                    onNavigateBack = {},
                    onSendMessage = onSendMessage,
                    onBlockUser = {},
                    onEnterPip = {}
                )
            }
        }

        // Verify branding and title
        composeTestRule.onNodeWithText("VIBE GROUP", ignoreCase = true, substring = true).assertIsDisplayed()
        
        // Type a message
        composeTestRule.onNodeWithTag("SendVibeInput").performTextInput("Secret Vibe")
        
        // Send it
        composeTestRule.onNodeWithTag("SendVibeButton").performClick()
        
        // Verify call
        verify { onSendMessage("Secret Vibe", "group-1") }
    }

    @Test
    fun testTieScreen_ContextualPersonaCloud() {
        val device = P2PDevice("id-peer", "Friend", "🤝")
        val group = VibeGroup("group-1", "Vibe Group", setOf("me", "id-peer"), VibeGroup.TYPE_TIE)
        
        composeTestRule.setContent {
            BlukitTheme {
                TieScreen(
                    state = BluetoothUiState(
                        groups = listOf(group),
                        scannedDevices = listOf(device),
                        connectedLinks = setOf("id-peer")
                    ),
                    localDeviceId = "me",
                    localEmoji = "👤",
                    localNickname = "hero",
                    onNicknameChange = {},
                    groupId = "group-1",
                    onDisconnect = {},
                    onNavigateBack = {},
                    onSendMessage = { _, _ -> },
                    onBlockUser = {},
                    onEnterPip = {}
                )
            }
        }

        // Verify the peer appears in the Hub Persona Cloud within the chat
        composeTestRule.onAllNodesWithTag("PersonaNode_id-peer", useUnmergedTree = true).onFirst().assertExists()
    }
}

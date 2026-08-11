package cc.thevar.blukit.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.ui.theme.BlukitTheme
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import org.junit.Rule
import org.junit.Test

class LobbyScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLobbyScreenDisplaysMultipleSenders() {
        val messages = listOf(
            MessagePayload(
                messageId = "1",
                senderId = "user-1",
                senderName = "Peer A",
                senderEmoji = "👤",
                content = "Lobby message 1",
                timestamp = System.currentTimeMillis()
            ),
            MessagePayload(
                messageId = "2",
                senderId = "user-2",
                senderName = "Peer B",
                senderEmoji = "👤",
                content = "Lobby message 2",
                timestamp = System.currentTimeMillis()
            )
        )

        composeTestRule.setContent {
            BlukitTheme {
                LobbyScreen(
                    state = BluetoothUiState(messages = messages, isConnected = true),
                    localDeviceId = "me",
                    onBroadcastMessage = {},
                    onBlockUser = {},
                    onEnterPip = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Stadium Square").assertExists()
        composeTestRule.onNodeWithText("Peer A").assertExists()
        composeTestRule.onNodeWithText("Peer B").assertExists()
        composeTestRule.onNodeWithText("Lobby message 1").assertExists()
        composeTestRule.onNodeWithText("Lobby message 2").assertExists()
    }
}

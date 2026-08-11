package cc.thevar.blukit.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.ui.theme.BlukitTheme
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import org.junit.Rule
import org.junit.Test

class ChatScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testChatScreenDisplaysMessages() {
        val messages = listOf(
            MessagePayload(
                messageId = "1",
                senderId = "user-1",
                senderName = "Peer A",
                content = "Hello from Peer A",
                timestamp = System.currentTimeMillis(),
                status = MessagePayload.STATUS_DELIVERED
            ),
            MessagePayload(
                messageId = "2",
                senderId = "me",
                senderName = "Me",
                content = "My local message",
                timestamp = System.currentTimeMillis(),
                status = MessagePayload.STATUS_SENT
            )
        )

        composeTestRule.setContent {
            BlukitTheme {
                ChatScreen(
                    state = BluetoothUiState(messages = messages, isConnected = true),
                    localDeviceId = "me",
                    peerName = "Peer A",
                    peerEmoji = "🐱",
                    onDisconnect = {},
                    onNavigateBack = {},
                    onSendMessage = {},
                    onBlockUser = {},
                    onEnterPip = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Hello from Peer A").assertExists()
        composeTestRule.onNodeWithText("My local message").assertExists()
        
        // Peer A appears in TopAppBar AND as sender name in bubble
        composeTestRule.onAllNodesWithText("Peer A").assertCountEquals(2)
    }
}

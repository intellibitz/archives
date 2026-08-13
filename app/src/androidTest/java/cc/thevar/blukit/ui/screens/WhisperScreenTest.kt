package cc.thevar.blukit.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.ui.theme.BlukitTheme
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import org.junit.Rule
import org.junit.Test

class WhisperScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testWhisperScreenDisplaysMessages() {
        val messages = listOf(
            MessagePayload(
                messageId = "1",
                senderId = "user-1",
                senderName = "Peer A",
                senderEmoji = "🐱",
                receiverId = "mask-1",
                content = "Hello from Peer A",
                timestamp = System.currentTimeMillis(),
                status = MessagePayload.STATUS_DELIVERED
            ),
            MessagePayload(
                messageId = "2",
                senderId = "mask-1",
                senderName = "Mask",
                senderEmoji = "😎",
                receiverId = "user-1",
                content = "My local message",
                timestamp = System.currentTimeMillis(),
                status = MessagePayload.STATUS_SENT
            )
        )

        composeTestRule.setContent {
            BlukitTheme {
                WhisperScreen(
                    state = BluetoothUiState(messages = messages, isConnected = true),
                    localDeviceId = "mask-1",
                    peerId = "user-1",
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
        
        // Peer A appears in TopAppBar
        composeTestRule.onNodeWithText("🐱Peer A").assertExists()
        // And as sender name in bubble (UPPERCASE)
        composeTestRule.onNodeWithText("PEER A").assertExists()
    }
}

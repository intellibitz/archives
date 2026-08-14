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
    fun testTieScreenDisplaysMessages() {
        val messages = listOf(
            MessagePayload(
                messageId = "1",
                senderId = "user-1",
                senderName = "Vibe A",
                senderEmoji = "🐱",
                receiverId = "mask-1",
                content = "Hello from Vibe A",
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
                TieScreen(
                    state = BluetoothUiState(messages = messages),
                    localDeviceId = "mask-1",
                    localEmoji = "👤",
                    vibeId = "user-1",
                    vibeName = "Vibe A",
                    vibeEmoji = "🐱",
                    onDisconnect = {},
                    onNavigateBack = {},
                    onSendMessage = {},
                    onBlockUser = {},
                    onEnterPip = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Hello from Vibe A").assertExists()
        composeTestRule.onNodeWithText("My local message").assertExists()
        
        // Vibe A name in bubble (UPPERCASE)
        composeTestRule.onNodeWithText("VIBE A").assertExists()
    }
}

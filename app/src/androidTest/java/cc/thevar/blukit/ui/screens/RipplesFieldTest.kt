package cc.thevar.blukit.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.ui.theme.BlukitTheme
import cc.thevar.blukit.ui.viewmodels.BluetoothUiState
import org.junit.Rule
import org.junit.Test

class RipplesFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testRipplesField_DisplaysPeers() {
        val peers = listOf(
            P2PDevice("1", "GhostA", "🌬️"),
            P2PDevice("2", "GhostB", "🌬️")
        )

        composeTestRule.setContent {
            BlukitTheme {
                RipplesField(
                    state = BluetoothUiState(scannedDevices = peers),
                    localDeviceId = "me",
                    localEmoji = "👤",
                    activeBubbles = emptyList(),
                    onDeviceClick = {},
                    onStartScan = {}
                )
            }
        }

        // Verify Peer nodes exist by tag
        composeTestRule.onNodeWithTag("PersonaNode_1", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("PersonaNode_2", useUnmergedTree = true).assertExists()
    }

    @Test
    fun testRipplesField_ShowsActiveBubbles() {
        val bubble = BubbleData("peer-1", "BOOM!", System.currentTimeMillis(), "msg-1")
        val peers = listOf(P2PDevice("peer-1", "GhostA", "🌬️"))

        composeTestRule.setContent {
            BlukitTheme {
                RipplesField(
                    state = BluetoothUiState(scannedDevices = peers),
                    localDeviceId = "me",
                    localEmoji = "👤",
                    activeBubbles = listOf(bubble),
                    onDeviceClick = {},
                    onStartScan = {}
                )
            }
        }

        // Verify message content appears as a bubble
        composeTestRule.onNodeWithText("BOOM!", ignoreCase = true, substring = true).assertExists()
    }
}

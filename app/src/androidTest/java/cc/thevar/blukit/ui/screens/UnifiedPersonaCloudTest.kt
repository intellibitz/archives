package cc.thevar.blukit.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.ui.theme.BlukitTheme
import org.junit.Rule
import org.junit.Test

class UnifiedPersonaCloudTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testUnifiedPersonaCloud_DisplaysActiveAndIdle() {
        val activeDevice = P2PDevice("id-1", "Active", "🔥", signalStrength = -50)
        val idleDevice = P2PDevice("id-2", "Idle", "💤", signalStrength = -80)
        
        val activeBubbles = listOf(
            BubbleData("id-1", "Message", System.currentTimeMillis(), "msg-1")
        )

        composeTestRule.setContent {
            BlukitTheme {
                UnifiedPersonaCloud(
                    devices = listOf(activeDevice, idleDevice),
                    vibedPeers = emptySet(),
                    connectedLinks = emptySet(),
                    activeBubbles = activeBubbles,
                    onDeviceClick = {}
                )
            }
        }

        // Check for headers (now in tips text)
        composeTestRule.onAllNodesWithText("TAP TO FOCUS", ignoreCase = true, substring = true).onFirst().assertIsDisplayed()
    }
}

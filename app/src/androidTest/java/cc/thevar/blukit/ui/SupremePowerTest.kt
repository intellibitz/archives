package cc.thevar.blukit.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.power.SupremePowerReport
import cc.thevar.blukit.ui.theme.BlukitTheme
import org.junit.Rule
import org.junit.Test

class SupremePowerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSupremePowerBadge_ExpandAndCollapse() {
        val report = SupremePowerReport(
            userCount = 10,
            connectedLinksCount = 2,
            totalMessages = 5,
            harmony = 0.4f,
            aiInsight = "The Vibes are healthy",
            currentBreeze = null
        )

        composeTestRule.setContent {
            BlukitTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    UnifiedBlukitBadge(
                        subtitle = "SPREAD THE VIBES",
                        report = report,
                        isBluetoothEnabled = true,
                        isLocationEnabled = true,
                        permissionsGranted = true,
                        isStealthMode = false,
                    currentRoute = cc.thevar.blukit.ui.navigation.Route.Shout,
                    emojiAvatar = "👤",
                    nickname = "vibe",
                    incomingLinkRequests = emptySet<P2PDevice>(),
                    onNavigate = {},
                    onAwakenBluetooth = {},
                    onAwakenLocation = {},
                    onGrantPermissions = {},
                    onSaveNickname = {},
                    onToggleStealth = {},
                    onClearHistory = {},
                    onLogout = {},
                    onAcceptLink = {},
                    onDenyLink = {}
                )
                }
            }
        }

        // Check closed state
        composeTestRule.onNodeWithText("BLUKIT", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("IntelSection").assertDoesNotExist()

        // Click to expand using testTag for reliability
        composeTestRule.onNodeWithTag("BlukitBadge").performClick()
        
        // Check expanded state - wait for animation with generous timeout
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("IntelSection", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("VOICES", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("10", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("THE VIBES ARE HEALTHY", substring = true, ignoreCase = true, useUnmergedTree = true).assertIsDisplayed() 

        // Click to collapse
        composeTestRule.onNodeWithTag("BlukitBadge").performClick()
        
        // Wait for collapse animation - check if IntelSection is no longer visible
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("IntelSection")
                .fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithTag("IntelSection").assertDoesNotExist()
    }
}

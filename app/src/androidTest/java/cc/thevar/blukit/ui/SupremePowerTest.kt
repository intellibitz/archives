package cc.thevar.blukit.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
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
            connectedPeerCount = 2,
            trafficDensity = "ACTIVE",
            aiInsight = "Mesh is healthy",
            signalStability = "STABLE"
        )

        composeTestRule.setContent {
            BlukitTheme {
                UnifiedBlukitBadge(
                    title = "THE AIR",
                    subtitle = "FEEL THE VIBES",
                    report = report,
                    isDiscovering = true,
                    isBluetoothEnabled = true,
                    isLocationEnabled = true,
                    permissionsGranted = true,
                    onAwakenBluetooth = {},
                    onAwakenLocation = {},
                    onGrantPermissions = {}
                )
            }
        }

        // Check closed state
        composeTestRule.onNodeWithText("BLUKIT").assertIsDisplayed()
        composeTestRule.onNodeWithText("HEARTS").assertDoesNotExist()

        // Click to expand
        composeTestRule.onNodeWithText("BLUKIT").performClick()

        // Check expanded state
        composeTestRule.onNodeWithText("HEARTS").assertIsDisplayed()
        composeTestRule.onNodeWithText("10").assertIsDisplayed()
        composeTestRule.onNodeWithText("ACTIVE").assertIsDisplayed()
        composeTestRule.onNodeWithText("MESH IS HEALTHY").assertIsDisplayed() // Uppercase check

        // Click to collapse
        composeTestRule.onNodeWithText("BLUKIT").performClick()
        composeTestRule.onNodeWithText("HEARTS").assertDoesNotExist()
    }
}

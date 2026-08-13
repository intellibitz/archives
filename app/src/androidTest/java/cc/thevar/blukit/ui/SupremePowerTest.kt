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
    fun testSupremePowerOverlay_ExpandAndCollapse() {
        val report = SupremePowerReport(
            userCount = 10,
            connectedPeerCount = 2,
            trafficDensity = "ACTIVE",
            aiInsight = "Mesh is healthy"
        )

        composeTestRule.setContent {
            BlukitTheme {
                SupremePowerOverlay(report = report)
            }
        }

        // Check closed state (only icon and label)
        composeTestRule.onNodeWithText("MESH INTEL").assertIsDisplayed()
        composeTestRule.onNodeWithText("ACTIVE").assertDoesNotExist()

        // Click to expand
        composeTestRule.onNodeWithText("MESH INTEL").performClick()

        // Check expanded state
        composeTestRule.onNodeWithText("NODES").assertIsDisplayed()
        composeTestRule.onNodeWithText("10").assertIsDisplayed()
        composeTestRule.onNodeWithText("ACTIVE").assertIsDisplayed()
        composeTestRule.onNodeWithText("MESH IS HEALTHY").assertIsDisplayed() // Uppercase check

        // Click to collapse
        composeTestRule.onNodeWithText("MESH INTEL").performClick()
        composeTestRule.onNodeWithText("NODES").assertDoesNotExist()
    }
}

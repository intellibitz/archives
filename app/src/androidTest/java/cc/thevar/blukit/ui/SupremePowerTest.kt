package cc.thevar.blukit.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.power.SupremePowerReport
import cc.thevar.blukit.ui.theme.BlukitTheme
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class SupremePowerTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

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
                        energy = 0f,
                        rotation = 0f,
                        userCount = report.userCount,
                        linksCount = report.connectedLinksCount,
                        lowPowerMode = true,
                        permissionsGranted = true,
                        isPermanentlyDenied = false,
                        isStealthMode = false,
                        currentRoute = cc.thevar.blukit.ui.navigation.Route.Crowd,
                        nickname = "vibe",
                        incomingLinkRequests = emptySet<P2PDevice>(),
                        currentBreeze = report.currentBreeze,
                        isBluetoothEnabled = true,
                        isLocationEnabled = true,
                        isWifiEnabled = true,
                        onNavigate = {},
                        onAwakenBluetooth = {},
                        onAwakenLocation = {},
                        onAwakenWifi = {},
                        onGrantPermissions = {},
                        onOpenSettings = {},
                        onSaveNickname = {},
                        onToggleStealth = {},
                        onToggleLowPower = {},
                        onClearHistory = {},
                        onLogout = {},
                        onAcceptLink = {}
                )
                }
            }
        }

        // Check closed state
        composeTestRule.onNodeWithText("BLUKIT", substring = true).assertIsDisplayed()
        
        // Check for picker content
        composeTestRule.onNodeWithText("CROWD (10)", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("FRIENDS (2)", useUnmergedTree = true).assertIsDisplayed()

        // Click BLUKIT icon to expand (Feedback 17 logic)
        composeTestRule.onNodeWithText("BLUKIT", substring = true).performClick()
        
        // Check expanded state - look for identity input
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("IdentityVibeInput"), 5000)
        composeTestRule.onNodeWithTag("IdentityVibeInput").assertIsDisplayed()
    }
}

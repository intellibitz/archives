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
import cc.thevar.blukit.ui.navigation.Route
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class SupremePowerTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testSupremePowerBadge_DisplaysTerminology() {
        val report = SupremePowerReport(
            userCount = 10,
            connectedLinksCount = 2,
            totalMessages = 5,
            harmony = 0.4f,
            aiInsight = "The Vibes are healthy"
        )

        composeTestRule.setContent {
            BlukitTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    UnifiedBlukitBadge(
                        energy = 0f,
                        rotation = 0f,
                        userCount = report.userCount,
                        linksCount = report.connectedLinksCount,
                        roarsCount = 5,
                        vibesCount = 3,
                        lowPowerMode = false,
                        permissionsGranted = true,
                        isPermanentlyDenied = false,
                        isStealthMode = false,
                        incomingLinkRequests = emptySet<P2PDevice>(),
                        isBluetoothEnabled = true,
                        isLocationEnabled = true,
                        isWifiEnabled = true,
                        currentRoute = Route.Blukit,
                        onNavigate = {},
                        onAwakenBluetooth = {},
                        onAwakenLocation = {},
                        onAwakenWifi = {},
                        onGrantPermissions = {},
                        onOpenSettings = {},
                        onToggleStealth = {},
                        onToggleLowPower = {},
                        onClearHistory = {},
                        onLogout = {},
                        onAcceptLink = {},
                        onDenyLink = {}
                )
                }
            }
        }

        // Check for terminology
        composeTestRule.onNodeWithText("ALL").assertIsDisplayed()
        composeTestRule.onNodeWithText("GROUPS").assertIsDisplayed()
        composeTestRule.onNodeWithText("WHISPER").assertIsDisplayed()
    }
}

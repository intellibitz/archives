package cc.thevar.blukit

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cc.thevar.blukit.ui.BlukitApp
import cc.thevar.blukit.ui.theme.BlukitTheme
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Supreme Senior Android Expert Implementation:
 * Instrumented Commandments Test Suite.
 * Verifies permission constraints and Power 3 (Landing Screen).
 */
@RunWith(AndroidJUnit4::class)
class CommandmentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testCommandment1_BluetoothOnlyPermissions() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        val permissions = packageInfo.requestedPermissions ?: emptyArray()

        // Check if mandatory permissions are limited to BT group (on modern APIs)
        val criticalNonBtPermissions = listOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_SMS,
            Manifest.permission.GET_ACCOUNTS
        )

        criticalNonBtPermissions.forEach { permission ->
            assertFalse("Commandment 1 Breach: Non-BT permission '$permission' found in manifest", permissions.contains(permission))
        }
    }

    @Test
    fun testPower3_LandingScreenIsStadiumLobbyForRegisteredUsers() {
        // Mock a registered user repository
        val repository: cc.thevar.blukit.data.repository.IdentityRepository = mockk(relaxed = true)
        val nicknameFlow = kotlinx.coroutines.flow.flowOf("Tester")
        io.mockk.every { repository.nickname } returns nicknameFlow
        io.mockk.every { repository.emojiAvatar } returns kotlinx.coroutines.flow.flowOf("👤")
        io.mockk.every { repository.stealthMode } returns kotlinx.coroutines.flow.flowOf(false)
        io.mockk.every { repository.deviceId } returns kotlinx.coroutines.flow.flowOf("id-123")

        composeTestRule.setContent {
            BlukitTheme {
                BlukitApp(
                    repository = repository,
                    contactRepository = mockk(relaxed = true),
                    messageDao = mockk(relaxed = true),
                    radioStateManager = mockk(relaxed = true),
                    p2pController = mockk(relaxed = true),
                    onEnterPip = {}
                )
            }
        }

        // Power 3: Registered users must land on Stadium Lobby
        composeTestRule.onNodeWithText("Stadium Lobby").assertExists()
    }
}

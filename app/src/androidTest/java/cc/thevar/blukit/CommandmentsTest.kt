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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test suite for validating core application commandments.
 * This verifies permission constraints (Commandment 1) and ensures that
 * users correctly land on 'The Vibes' (Power 1) upon application start.
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
    fun testPower1_LandingScreenIsTheVibes() {
        // Mock a repository
        val repository: cc.thevar.blukit.data.repository.IdentityRepository = mockk(relaxed = true)
        val nicknameFlow = kotlinx.coroutines.flow.MutableStateFlow<String?>("vibe")
        io.mockk.every { repository.nicknameFlow } returns nicknameFlow
        io.mockk.every { repository.emojiAvatar } returns kotlinx.coroutines.flow.MutableStateFlow("🟦")
        io.mockk.every { repository.stealthMode } returns kotlinx.coroutines.flow.MutableStateFlow(false)
        io.mockk.every { repository.blockedUsers } returns kotlinx.coroutines.flow.MutableStateFlow(emptySet())
        io.mockk.every { repository.getDeviceId() } returns "id-123"

                val p2p: cc.thevar.blukit.network.p2p.P2PController = mockk(relaxed = true)
        io.mockk.every { p2p.scannedDevices } returns kotlinx.coroutines.flow.MutableStateFlow(emptyList())
        io.mockk.every { p2p.connectedLinks } returns kotlinx.coroutines.flow.MutableStateFlow(emptySet())
        io.mockk.every { p2p.isDiscovering } returns kotlinx.coroutines.flow.MutableStateFlow(false)
        io.mockk.every { p2p.isAdvertising } returns kotlinx.coroutines.flow.MutableStateFlow(false)
        io.mockk.every { p2p.isConnected } returns kotlinx.coroutines.flow.MutableStateFlow(false)
        io.mockk.every { p2p.messages } returns kotlinx.coroutines.flow.MutableStateFlow(emptyList())
        io.mockk.every { p2p.errors } returns kotlinx.coroutines.flow.MutableStateFlow("")

        val spm: cc.thevar.blukit.data.power.SupremePowerManager = mockk(relaxed = true)
        io.mockk.every { spm.report } returns kotlinx.coroutines.flow.MutableStateFlow(cc.thevar.blukit.domain.power.SupremePowerReport())

        composeTestRule.setContent {
            BlukitTheme {
                BlukitApp(
                    repository = repository,
                    contactRepository = mockk(relaxed = true),
                    messageDao = mockk(relaxed = true),
                    radioStateManager = mockk(relaxed = true),
                    p2pController = p2p,
                    supremePowerManager = spm,
                    onEnterPip = {}
                )
            }
        }

        // Power 1: Users must land on SPREAD THE VIBES
        composeTestRule.onNodeWithText("SPREAD THE VIBES", ignoreCase = true).assertExists()
    }
}

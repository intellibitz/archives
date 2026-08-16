package cc.thevar.blukit

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.ui.BlukitApp
import cc.thevar.blukit.ui.theme.BlukitTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
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
        // Mock dependencies
        val repository: cc.thevar.blukit.data.repository.IdentityRepository = mockk(relaxed = true)
        val vibeStore: VibeStore = mockk(relaxed = true)
        val contactRepository: cc.thevar.blukit.data.repository.ContactRepository = mockk(relaxed = true)
        val radioStateManager: cc.thevar.blukit.data.system.RadioStateManager = mockk(relaxed = true)
        val p2p: cc.thevar.blukit.network.p2p.P2PController = mockk(relaxed = true)
        val spm: cc.thevar.blukit.data.power.SupremePowerManager = mockk(relaxed = true)

        val nicknameFlow = MutableStateFlow<String?>("vibe")
        every { repository.nicknameFlow } returns nicknameFlow
        every { repository.emojiAvatar } returns MutableStateFlow("🟦")
        every { repository.stealthMode } returns MutableStateFlow(false)
        every { repository.blockedUsers } returns MutableStateFlow(emptySet())
        every { repository.getDeviceId() } returns "id-123"

        every { p2p.scannedDevices } returns MutableStateFlow(emptyList())
        every { p2p.connectedLinks } returns MutableStateFlow(emptySet())
        every { p2p.isDiscovering } returns MutableStateFlow(false)
        every { p2p.isAdvertising } returns MutableStateFlow(false)
        every { p2p.isConnected } returns MutableStateFlow(false)
        every { p2p.messages } returns MutableStateFlow(emptyList())
        every { p2p.errors } returns MutableStateFlow(null)

        every { spm.report } returns MutableStateFlow(cc.thevar.blukit.domain.power.SupremePowerReport())
        every { vibeStore.getAllMessages() } returns MutableStateFlow(emptyList())

        composeTestRule.setContent {
            BlukitTheme {
                BlukitApp(
                    repository = repository,
                    contactRepository = contactRepository,
                    vibeStore = vibeStore,
                    radioStateManager = radioStateManager,
                    p2pController = p2p,
                    supremePowerManager = spm,
                    onEnterPip = {}
                )
            }
        }

        // Power 1: Users must land on SPREAD VIBES
        composeTestRule.onNodeWithText("SPREAD VIBES", ignoreCase = true, substring = true).assertExists()
    }
}

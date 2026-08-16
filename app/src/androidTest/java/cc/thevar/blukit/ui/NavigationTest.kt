package cc.thevar.blukit.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.network.p2p.P2PController
import cc.thevar.blukit.ui.theme.BlukitTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val repository: IdentityRepository = mockk(relaxed = true)
    private val contactRepository: ContactRepository = mockk(relaxed = true)
    private val vibeStore: VibeStore = mockk(relaxed = true)
    private val radioStateManager: RadioStateManager = mockk(relaxed = true)
    private val p2pController: P2PController = mockk(relaxed = true)

    @Before
    fun setUp() {
        every { repository.nicknameFlow } returns MutableStateFlow("vibe")
        every { repository.emojiAvatar } returns MutableStateFlow("👤")
        every { repository.stealthMode } returns MutableStateFlow(false)
        every { repository.blockedUsers } returns MutableStateFlow(emptySet())
        every { repository.getDeviceId() } returns "test-device-id"
        every { repository.getCurrentNickname() } returns "vibe"
        
        every { p2pController.scannedDevices } returns MutableStateFlow(emptyList())
        every { p2pController.connectedLinks } returns MutableStateFlow(emptySet())
        every { p2pController.isDiscovering } returns MutableStateFlow(false)
        every { p2pController.isAdvertising } returns MutableStateFlow(false)
        every { p2pController.errors } returns MutableStateFlow(null)
        every { p2pController.isConnected } returns MutableStateFlow(false)
        every { p2pController.messages } returns MutableStateFlow(emptyList())
        
        every { radioStateManager.radioStates } returns MutableStateFlow(cc.thevar.blukit.data.system.RadioStates(false, false, false))
        every { vibeStore.getAllMessages() } returns MutableStateFlow(emptyList())
    }

    @Test
    fun testBottomNavigation() {
        startApp()
        
        // 1. Initial State - Spread vibes (Input placeholder or empty screen hint)
        composeTestRule.onNodeWithText("VIBE", substring = true).assertIsDisplayed()

        // 3. KNOWN (Mutual) - Switch via visual picker
        composeTestRule.onNode(hasText("KNOWN", substring = true), useUnmergedTree = true).performClick()
        // Check for specific "KNOWN" stat in center or something unique to VIBES screen
        composeTestRule.onAllNodes(hasText("KNOWN", substring = true), useUnmergedTree = true).onLast().assertIsDisplayed()
        
        // 4. Back to BLUKIT - Switch via visual picker
        composeTestRule.onNode(hasText("BLUKIT", substring = true), useUnmergedTree = true).performClick()
        composeTestRule.onAllNodesWithText("VIBE", substring = true).onFirst().assertIsDisplayed()
    }

    private fun startApp() {
        val spm: cc.thevar.blukit.data.power.SupremePowerManager = mockk(relaxed = true)
        every { spm.report } returns MutableStateFlow(cc.thevar.blukit.domain.power.SupremePowerReport())

        composeTestRule.setContent {
            BlukitTheme {
                BlukitApp(
                    repository = repository,
                    contactRepository = contactRepository,
                    vibeStore = vibeStore,
                    radioStateManager = radioStateManager,
                    p2pController = p2pController,
                    supremePowerManager = spm,
                    onEnterPip = {}
                )
            }
        }
    }
}

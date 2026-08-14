package cc.thevar.blukit.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
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
    val composeTestRule = createComposeRule()

    private val repository: IdentityRepository = mockk(relaxed = true)
    private val contactRepository: ContactRepository = mockk(relaxed = true)
    private val messageDao: cc.thevar.blukit.data.local.dao.MessageDao = mockk(relaxed = true)
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
        every { p2pController.errors } returns MutableStateFlow("")
        every { p2pController.isConnected } returns MutableStateFlow(false)
        every { p2pController.messages } returns MutableStateFlow(emptyList())
        
        every { radioStateManager.radioStates } returns MutableStateFlow(cc.thevar.blukit.data.system.RadioStates(false, false))
    }

    @Test
    fun testBottomNavigation() {
        startApp()
        
        // 1. Initial State - Feel the vibes
        composeTestRule.onNodeWithText("FEEL THE VIBES").assertIsDisplayed()

        // 2. Expand Hub
        composeTestRule.onNodeWithText("BLUKIT", substring = true).performClick()

        // 3. Ties (YOUR TIES) - Click Ties stat row in expanded hub
        composeTestRule.onNodeWithTag("TiesStat").performClick()
        composeTestRule.onNodeWithText("YOUR TIES").assertIsDisplayed()
        
        // 4. Back to Vibes - Click Branding Section then Vibes stat
        composeTestRule.onNodeWithText("BLUKIT", substring = true).performClick()
        composeTestRule.onNodeWithTag("VibesStat").performClick()
        composeTestRule.onNodeWithText("FEEL THE VIBES").assertIsDisplayed()
    }

    private fun startApp() {
        val spm: cc.thevar.blukit.data.power.SupremePowerManager = mockk(relaxed = true)
        every { spm.report } returns MutableStateFlow(cc.thevar.blukit.domain.power.SupremePowerReport())

        composeTestRule.setContent {
            BlukitTheme {
                BlukitApp(
                    repository = repository,
                    contactRepository = contactRepository,
                    messageDao = messageDao,
                    radioStateManager = radioStateManager,
                    p2pController = p2pController,
                    supremePowerManager = spm,
                    onEnterPip = {}
                )
            }
        }
    }
}

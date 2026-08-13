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
        every { repository.nickname } returns MutableStateFlow("vibe")
        every { repository.emojiAvatar } returns MutableStateFlow("🟦")
        every { repository.stealthMode } returns MutableStateFlow(false)
        every { repository.deviceId } returns MutableStateFlow("test-device-id")
        
        every { p2pController.scannedDevices } returns MutableStateFlow(emptyList())
        every { p2pController.connectedPeers } returns MutableStateFlow(emptySet())
        every { p2pController.isDiscovering } returns MutableStateFlow(false)
        every { p2pController.isAdvertising } returns MutableStateFlow(false)
        every { p2pController.errors } returns MutableSharedFlow()
        every { p2pController.isConnected } returns MutableStateFlow(false)
        every { p2pController.messages } returns MutableStateFlow(emptyList())
        
        every { radioStateManager.radioStates } returns MutableStateFlow(cc.thevar.blukit.data.system.RadioStates(false, false))
    }

    @Test
    fun testBottomNavigation() {
        startApp()
        
        // 1. Air (The Air) - Initial
        composeTestRule.onNodeWithText("The Air").assertIsDisplayed()
        
        // 2. Nearby (Presence)
        composeTestRule.onNodeWithText("Nearby").performClick()
        composeTestRule.onNodeWithText("Nearby").assertIsDisplayed()
        
        // 3. Whispers
        composeTestRule.onNodeWithText("Whispers").performClick()
        composeTestRule.onNodeWithText("Whispers").assertIsDisplayed()
        
        // 4. Vibe
        composeTestRule.onNodeWithText("Vibe").performClick()
        composeTestRule.onNodeWithText("Vibe").assertIsDisplayed()
        
        // 5. Back to Air
        composeTestRule.onNodeWithText("Air").performClick()
        composeTestRule.onNodeWithText("The Air").assertIsDisplayed()
    }

    private fun startApp() {
        composeTestRule.setContent {
            BlukitTheme {
                BlukitApp(
                    repository = repository,
                    contactRepository = contactRepository,
                    messageDao = messageDao,
                    radioStateManager = radioStateManager,
                    p2pController = p2pController,
                    onEnterPip = {}
                )
            }
        }
    }
}

package cc.thevar.blukit.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.RadioStates
import cc.thevar.blukit.data.power.SupremePowerManager
import cc.thevar.blukit.network.p2p.P2PController
import cc.thevar.blukit.ui.theme.BlukitTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FlowsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val repository: IdentityRepository = mockk(relaxed = true)
    private val contactRepository: ContactRepository = mockk(relaxed = true)
    private val messageDao: cc.thevar.blukit.data.local.dao.MessageDao = mockk(relaxed = true)
    private val radioStateManager: RadioStateManager = mockk(relaxed = true)
    private val p2pController: P2PController = mockk(relaxed = true)
    private val supremePowerManager: SupremePowerManager = mockk(relaxed = true)

    private val nicknameFlow = MutableStateFlow("vibe")
    private val emojiFlow = MutableStateFlow("🟦")
    private val stealthModeFlow = MutableStateFlow(false)
    private val deviceIdFlow = MutableStateFlow("test-device-id")
    
    private val scannedDevicesFlow = MutableStateFlow(emptyList<cc.thevar.blukit.domain.model.P2PDevice>())
    private val radioStatesFlow = MutableStateFlow(RadioStates(false, false))
    private val connectedPeersFlow = MutableStateFlow(emptySet<String>())
    private val isDiscoveringFlow = MutableStateFlow(false)
    private val messagesFlow = MutableStateFlow(emptyList<cc.thevar.blukit.domain.model.MessagePayload>())
    private val errorsFlow = MutableSharedFlow<String>()

    @Before
    fun setUp() {
        every { repository.nickname } returns nicknameFlow
        every { repository.emojiAvatar } returns emojiFlow
        every { repository.stealthMode } returns stealthModeFlow
        every { repository.deviceId } returns deviceIdFlow
        
        every { p2pController.scannedDevices } returns scannedDevicesFlow
        every { p2pController.connectedPeers } returns connectedPeersFlow
        every { p2pController.isDiscovering } returns isDiscoveringFlow
        every { p2pController.errors } returns errorsFlow
        every { p2pController.isConnected } returns MutableStateFlow(false)
        every { p2pController.messages } returns messagesFlow
        every { p2pController.isAdvertising } returns MutableStateFlow(false)
        
        every { radioStateManager.radioStates } returns radioStatesFlow
        every { supremePowerManager.report } returns MutableStateFlow(cc.thevar.blukit.domain.power.SupremePowerReport())
    }

    @Test
    fun testLandingOnWatch() {
        startApp()
        
        // Should land on Air (THE AIR)
        composeTestRule.onNodeWithText("THE AIR").assertIsDisplayed()
    }

    @Test
    fun testSmartShoutFlow_TriggerPermissionPrompt() {
        startApp()
        
        // Type a shout
        composeTestRule.onNodeWithText("SEND VIBES TO EVERYONE...").performTextInput("Hello Mesh!")
        
        // Click Send
        composeTestRule.onNodeWithContentDescription("Send").performClick()
        
        // Should show Smart Flow prompt
        composeTestRule.onNodeWithText("Connect with others?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Join the Circle").assertIsDisplayed()
    }

    @Test
    fun testNavigateToMaskAndChangeNickname() {
        startApp()
        
        // Click Vibe tab
        composeTestRule.onNodeWithText("Vibe").performClick()
        
        // Should be on Vibe screen
        composeTestRule.onNodeWithText("VIBE").assertIsDisplayed()
        
        // Change nickname
        composeTestRule.onAllNodesWithText("vibe")[0].performTextReplacement("NewUser")
        
        // Save
        composeTestRule.onNodeWithText("ENTER THE VIBES").performClick()
        
        verify { repository.saveNickname("NewUser") }
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
                    supremePowerManager = supremePowerManager,
                    onEnterPip = {}
                )
            }
        }
    }
}

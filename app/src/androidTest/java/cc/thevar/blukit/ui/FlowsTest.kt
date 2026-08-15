package cc.thevar.blukit.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.RadioStates
import cc.thevar.blukit.data.power.SupremePowerManager
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.network.p2p.P2PController
import cc.thevar.blukit.ui.theme.BlukitTheme
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class FlowsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val repository: IdentityRepository = mockk(relaxed = true)
    private val contactRepository: ContactRepository = mockk(relaxed = true)
    private val messageDao: cc.thevar.blukit.data.local.dao.MessageDao = mockk(relaxed = true)
    private val radioStateManager: RadioStateManager = mockk(relaxed = true)
    private val p2pController: P2PController = mockk(relaxed = true)
    private val supremePowerManager: SupremePowerManager = mockk(relaxed = true)

    private val nicknameFlow = MutableStateFlow<String?>("vibe")
    private val emojiFlow = MutableStateFlow("👤")
    private val scannedDevicesFlow = MutableStateFlow(emptyList<P2PDevice>())
    private val incomingRequestsFlow = MutableStateFlow(emptySet<P2PDevice>())
    private val connectedLinksFlow = MutableStateFlow(emptySet<String>())
    private val isConnectedFlow = MutableStateFlow(false)
    private val isDiscoveringFlow = MutableStateFlow(false)
    private val isAdvertisingFlow = MutableStateFlow(false)
    private val errorsFlow = MutableStateFlow("")
    private val messagesFlow = MutableStateFlow(emptyList<cc.thevar.blukit.domain.model.MessagePayload>())
    private val radioStatesFlow = MutableStateFlow(RadioStates(true, true))
    private val reportFlow = MutableStateFlow(cc.thevar.blukit.domain.power.SupremePowerReport())

    @Before
    fun setUp() {
        every { repository.nicknameFlow } returns nicknameFlow
        every { repository.emojiAvatar } returns emojiFlow
        every { repository.getDeviceId() } returns "test-device-id"
        every { repository.stealthMode } returns MutableStateFlow(false)
        every { repository.blockedUsers } returns MutableStateFlow(emptySet())
        
        every { p2pController.scannedDevices } returns scannedDevicesFlow
        every { p2pController.incomingLinkRequests } returns incomingRequestsFlow
        every { p2pController.connectedLinks } returns connectedLinksFlow
        every { p2pController.isDiscovering } returns isDiscoveringFlow
        every { p2pController.errors } returns errorsFlow
        every { p2pController.isConnected } returns isConnectedFlow
        every { p2pController.messages } returns messagesFlow
        every { p2pController.isAdvertising } returns isAdvertisingFlow
        
        every { radioStateManager.radioStates } returns radioStatesFlow
        every { supremePowerManager.report } returns reportFlow
    }

    @Test
    fun testLinkSetup_AcceptanceFlow() {
        // Mock an incoming vibe wanting to link
        val incomingVibe = P2PDevice("id-123", "Mystic Vibe", "👤")
        incomingRequestsFlow.value = setOf(incomingVibe)

        startApp()
        
        // Ensure the hub expands to reveal the setup
        composeTestRule.onNodeWithText("BLUKIT", substring = true).performClick()
        
        composeTestRule.onRoot().printToLog("UI_TREE")

        // Wait for the Incoming Vibe setup - look for the text in a more flexible way
        composeTestRule.waitUntilAtLeastOneExists(hasText("wants to bridge a link", substring = true).or(hasText("MYSTIC", substring = true)), 10000)
        
        // Accept the setup if visible
        composeTestRule.onAllNodesWithText("ACCEPT", substring = true).onFirst().performClick()
        
        // Verify P2P Controller is notified
        verify { p2pController.acceptLink(incomingVibe) }
    }

    @Test
    fun testNavigateToVibeAndChangeIdentity() {
        startApp()
        
        // Expand the Hub
        composeTestRule.onNodeWithTag("BlukitBadge").performClick()
        
        // Change nickname using the new IdentityVibeInput tag
        composeTestRule.onNodeWithTag("IdentityVibeInput").performTextReplacement("QuantumVibe")
        
        // Verify repository update
        verify { repository.saveNickname("QuantumVibe") }
    }

    @Test
    fun testHarmonyCheck() {
        // Break Harmony
        radioStatesFlow.value = RadioStates(isBluetoothEnabled = false, isLocationEnabled = true)
        
        startApp()
        
        // Wait for the Magic Bar to reflect the stillness of the vibes
        composeTestRule.waitUntilAtLeastOneExists(hasText("THE VIBES ARE STILL", substring = true), 10000)
        // Use onFirst() because multiple might be found if UI overlaps during animation
        composeTestRule.onAllNodesWithText("AWAKEN", substring = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun testSendVibeFlow() {
        // Mock a device in range and a connection
        val device = P2PDevice("vibe-1", "Aura Vibe", "👤")
        scannedDevicesFlow.value = listOf(device)
        connectedLinksFlow.value = setOf("vibe-1")
        isConnectedFlow.value = true

        startApp()
        
        // Click to navigate to the Tie screen - match the 6-char display limit
        composeTestRule.onNodeWithText("AURA V", substring = true).performClick()
        
        // Wait for the SendVibeInput to appear in the Tie screen
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SendVibeInput"), 5000)
        
        // Send a vibe
        composeTestRule.onNodeWithTag("SendVibeInput").performTextInput("Hello from the Air!")
        composeTestRule.onNodeWithContentDescription("Send", substring = true).performClick()
        
        // Verify the vibe was sent through the controller
        composeTestRule.waitForIdle()
        coVerify(timeout = 5000) { p2pController.sendMessage("Hello from the Air!", "vibe-1") }
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

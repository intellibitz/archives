package cc.thevar.blukit.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.RadioStates
import cc.thevar.blukit.data.power.SupremePowerManager
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.network.p2p.P2PController
import cc.thevar.blukit.ui.theme.BlukitTheme
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import androidx.test.rule.GrantPermissionRule
import android.Manifest

@OptIn(ExperimentalTestApi::class)
class FlowsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.NEARBY_WIFI_DEVICES
    )

    private val repository: IdentityRepository = mockk(relaxed = true)
    private val contactRepository: ContactRepository = mockk(relaxed = true)
    private val vibeStore: VibeStore = mockk(relaxed = true)
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
    private val errorsFlow = MutableStateFlow<cc.thevar.blukit.network.p2p.P2PError?>(null)
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
        every { vibeStore.getAllMessages() } returns MutableStateFlow(emptyList())
    }

    @Test
    fun testLinkSetup_AcceptanceFlow() {
        // Mock an incoming vibe wanting to link
        val incomingVibe = P2PDevice("id-123", "Mystic Vibe", "👤")
        incomingRequestsFlow.value = setOf(incomingVibe)

        startApp()
        
        composeTestRule.onNodeWithText("BLUKIT", substring = true).performClick()

        // Wait for the Incoming Vibe setup - look for the text in a more flexible way
        composeTestRule.waitUntilAtLeastOneExists(hasText("NEW VIBE REQUEST", substring = true).or(hasText("MYSTIC", substring = true)), 10000)
        
        // Roar the setup if visible
        composeTestRule.onNodeWithTag("AcceptLinkButton").performClick()
        
        // Verify P2P Controller is notified
        coVerify { p2pController.acceptLink(any()) }
    }

    @Test
    fun testNavigateToVibeAndChangeIdentity() {
        startApp()
        
        // Expand the Hub via the BLUKIT icon (Feedback 17 logic)
        composeTestRule.onNodeWithText("BLUKIT", substring = true).performClick()

        // Wait for animation to finish and input to be available
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("IdentityVibeInput"), 10000)
        
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
        composeTestRule.waitUntilAtLeastOneExists(hasText("RADIOS OFF", substring = true), 10000)
        // Use onFirst() because multiple might be found if UI overlaps during animation
        composeTestRule.waitUntilAtLeastOneExists(hasText("ENABLE RADIOS", substring = true), 15000)
        composeTestRule.onAllNodesWithText("ENABLE RADIOS", substring = true).onFirst().assertIsDisplayed().assertHasClickAction()
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
        composeTestRule.waitUntilAtLeastOneExists(hasText("AURA V", substring = true), 5000)
        composeTestRule.onNodeWithText("AURA V", substring = true).performClick()
        
        // Wait for the SendVibeInput to appear in the Tie screen
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SendVibeInput"), 5000)
        
        // Send a vibe
        composeTestRule.onNodeWithTag("SendVibeInput").performTextInput("Hello from the Air!")
        
        // Wait for button to be enabled and then click
        composeTestRule.onNodeWithTag("SendVibeButton").assertIsEnabled().performClick()
        
        // Verify the vibe was sent through the controller
        composeTestRule.waitUntil(15000) {
            try {
                coVerify(atLeast = 1) { p2pController.sendMessage(any(), any()) }
                true
            } catch (e: Throwable) {
                false
            }
        }
    }

    private fun startApp() {
        composeTestRule.setContent {
            BlukitTheme {
                BlukitApp(
                    repository = repository,
                    contactRepository = contactRepository,
                    vibeStore = vibeStore,
                    radioStateManager = radioStateManager,
                    p2pController = p2pController,
                    supremePowerManager = supremePowerManager,
                    onEnterPip = {}
                )
            }
        }
        composeTestRule.waitForIdle()
    }
}

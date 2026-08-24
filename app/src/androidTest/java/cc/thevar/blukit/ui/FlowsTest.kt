package cc.thevar.blukit.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.power.SupremePowerReport
import cc.thevar.blukit.network.p2p.P2PController
import cc.thevar.blukit.ui.theme.BlukitTheme
import cc.thevar.blukit.data.system.RadioStates
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.domain.usecase.ConnectivityUseCase
import cc.thevar.blukit.data.system.HapticManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest

@OptIn(ExperimentalTestApi::class)
class FlowsTest : KoinTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val repository: IdentityRepository = mockk(relaxed = true)
    private val contactRepository: ContactRepository = mockk(relaxed = true)
    private val vibeStore: VibeStore = mockk(relaxed = true)
    private val radioStateManager: RadioStateManager = mockk(relaxed = true)
    private val p2pController: P2PController = mockk(relaxed = true)
    private val supremePowerManager: cc.thevar.blukit.data.power.SupremePowerManager = mockk(relaxed = true)
    private val permissionManager: SpreadPermissionManager = mockk(relaxed = true)
    private val connectivityUseCase: ConnectivityUseCase = mockk(relaxed = true)
    private val hapticManager: HapticManager = mockk(relaxed = true)

    private val testModule = module {
        single { repository }
        single { contactRepository }
        single { vibeStore }
        single { radioStateManager }
        single { p2pController }
        single { supremePowerManager }
        single { permissionManager }
        single { connectivityUseCase }
        single { hapticManager }
    }

    private val radioStatesFlow = MutableStateFlow(RadioStates(isBluetoothEnabled = true, isLocationEnabled = true, isWifiEnabled = true))
    private val scannedDevicesFlow = MutableStateFlow<List<P2PDevice>>(emptyList())
    private val connectedLinksFlow = MutableStateFlow<Set<String>>(emptySet())
    private val incomingRequestsFlow = MutableStateFlow<Set<P2PDevice>>(emptySet())
    private val isConnectedFlow = MutableStateFlow(false)
    private val messagesFlow = MutableStateFlow<List<cc.thevar.blukit.domain.model.MessagePayload>>(emptyList())
    private val isAdvertisingFlow = MutableStateFlow(false)
    private val errorsFlow = MutableStateFlow<cc.thevar.blukit.network.p2p.P2PError?>(null)
    private val reportFlow = MutableStateFlow(SupremePowerReport())

    @Before
    fun setUp() {
        loadKoinModules(testModule)
        
        every { repository.nicknameFlow } returns MutableStateFlow("vibe")
        every { repository.emojiAvatar } returns MutableStateFlow("👤")
        every { repository.stealthMode } returns MutableStateFlow(false)
        every { repository.lowPowerMode } returns MutableStateFlow(false)
        every { repository.blockedUsers } returns MutableStateFlow(emptySet())
        every { repository.vibedPeers } returns MutableStateFlow(emptySet())
        every { repository.getDeviceId() } returns "test-id"
        every { repository.getCurrentNickname() } returns "vibe"

        every { p2pController.scannedDevices } returns scannedDevicesFlow
        every { p2pController.connectedLinks } returns connectedLinksFlow
        every { p2pController.incomingLinkRequests } returns incomingRequestsFlow
        every { p2pController.outgoingLinkRequests } returns MutableStateFlow(emptySet())
        every { p2pController.isDiscovering } returns MutableStateFlow(false)
        every { p2pController.errors } returns errorsFlow
        every { p2pController.isConnected } returns isConnectedFlow
        every { p2pController.messages } returns messagesFlow
        every { p2pController.isAdvertising } returns isAdvertisingFlow
        every { p2pController.discoveredAirs } returns MutableStateFlow(cc.thevar.blukit.domain.model.VibeGroup(id="air", name="Air"))
        
        coEvery { p2pController.sendMessage(any(), any(), any(), any(), any(), any()) } answers {
            val content = firstArg<String>()
            val receiver = secondArg<String?>()
            val newMsg = cc.thevar.blukit.domain.model.MessagePayload(
                messageId = java.util.UUID.randomUUID().toString(),
                senderId = "test-id",
                senderName = "vibe",
                content = content,
                timestamp = System.currentTimeMillis(),
                receiverId = receiver
            )
            messagesFlow.value = messagesFlow.value + newMsg
            newMsg
        }

        every { radioStateManager.radioStates } returns radioStatesFlow
        every { supremePowerManager.report } returns reportFlow
        every { vibeStore.getAllMessages() } returns MutableStateFlow(emptyList())
        every { vibeStore.groups } returns MutableStateFlow(emptyList())
        every { vibeStore.activeGroups } returns MutableStateFlow(emptyList())
        every { vibeStore.archivedGroups } returns MutableStateFlow(emptyList())
        every { vibeStore.vaultedGroups } returns MutableStateFlow(emptyList())
        every { vibeStore.messages } returns MutableStateFlow(emptyList())

        every { permissionManager.requiredPermissions } returns emptyList()
        every { permissionManager.essentialPermissions } returns emptyList()
    }

    @After
    fun tearDown() {
        unloadKoinModules(testModule)
    }

    @Test
    fun testLinkSetup_AcceptanceFlow() {
        // Mock an incoming vibe wanting to link
        val incomingVibe = P2PDevice("id-123", "Mystic Vibe", "👤")
        incomingRequestsFlow.value = setOf(incomingVibe)

        startApp()
        
        // Expansion is built-in now, wait for the action button
        composeTestRule.waitUntilAtLeastOneExists(hasText("JOIN"), 20000)
        
        // Accept the vibe
        composeTestRule.onAllNodes(hasText("JOIN"), useUnmergedTree = true).onFirst().performClick()
        
        // Verify P2P Controller is notified
        coVerify { p2pController.acceptLink(any()) }
    }

    @Test
    fun testNavigateToVibeAndChangeIdentity() {
        startApp()
        
        // Persona field is now always visible in the omnipotent hub
        // Nickname must be <= 8 characters
        composeTestRule.onNodeWithTag("IdentityVibeInput").performTextReplacement("Quantum")
        
        // Verify repository update
        verify { repository.saveNickname("Quantum") }
    }

    @Test
    fun testHarmonyCheck() {
        // Break Harmony
        radioStatesFlow.value = RadioStates(isBluetoothEnabled = false, isLocationEnabled = true, isWifiEnabled = true)
        
        startApp()
        
        // Wait for the Magic Bar to reflect the stillness of the vibes
        composeTestRule.waitUntilAtLeastOneExists(hasText("AWAKEN", ignoreCase = true, substring = true), 20000)
        // Use onFirst() because multiple might be found if UI overlaps during animation
        composeTestRule.onAllNodesWithText("AWAKEN", ignoreCase = true, substring = true, useUnmergedTree = true).onFirst().performClick()
    }

    @Test
    fun testSendVibeFlow() {
        // Mock a device in range and a connection
        val device = P2PDevice("vibe-1", "Aura Vibe", "👤")
        scannedDevicesFlow.value = listOf(device)
        connectedLinksFlow.value = setOf("vibe-1")
        isConnectedFlow.value = true

        startApp()
        composeTestRule.waitForIdle()
        
        // Wait for the persona to appear in the cloud
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("PersonaNode_vibe-1"), 30000)
        composeTestRule.onNodeWithTag("PersonaNode_vibe-1", useUnmergedTree = true).performClick()
        
        // Wait for the SendVibeInput to appear
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SendVibeInput"), 30000)
        
        // Send a vibe
        composeTestRule.onNodeWithTag("SendVibeInput").performTextInput("Hello from the Air!")
        composeTestRule.waitForIdle()

        // Click Send button
        composeTestRule.onAllNodesWithTag("SendVibeButton", useUnmergedTree = true).onFirst().performClick()
        
        // Verify the message appeared
        composeTestRule.waitUntilAtLeastOneExists(hasText("Hello from the Air!", ignoreCase = true, substring = true), 30000)
    }

    private fun startApp() {
        composeTestRule.setContent {
            BlukitTheme {
                BlukitApp(
                    onEnterPip = {}
                )
            }
        }
    }
}

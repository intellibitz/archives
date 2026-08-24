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
import cc.thevar.blukit.domain.model.VibeGroup
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.network.p2p.P2PError
import cc.thevar.blukit.ui.viewmodels.BluetoothViewModel
import cc.thevar.blukit.ui.viewmodels.MainViewModel
import cc.thevar.blukit.ui.viewmodels.SupremePowerViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.koin.test.KoinTest
import androidx.test.platform.app.InstrumentationRegistry
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import java.util.UUID

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

    private val radioStatesFlow = MutableStateFlow(RadioStates(isBluetoothEnabled = true, isLocationEnabled = true, isWifiEnabled = true))
    private val scannedDevicesFlow = MutableStateFlow<List<P2PDevice>>(emptyList())
    private val connectedRadiosFlow = MutableStateFlow<Set<String>>(emptySet())
    private val incomingRequestsFlow = MutableStateFlow<Set<P2PDevice>>(emptySet())
    private val isConnectedFlow = MutableStateFlow(false)
    private val messagesFlow = MutableStateFlow<List<MessagePayload>>(emptyList())
    private val isAdvertisingFlow = MutableStateFlow(false)
    private val errorsFlow = MutableStateFlow<P2PError?>(null)
    private val reportFlow = MutableStateFlow(SupremePowerReport())
    private val groupsFlow = MutableStateFlow<List<VibeGroup>>(emptyList())
    private val permissionsGrantedFlow = MutableStateFlow(true)

    private val testModule = module {
        single(createdAtStart = true) { repository }
        single(createdAtStart = true) { contactRepository }
        single(createdAtStart = true) { vibeStore }
        single(createdAtStart = true) { radioStateManager }
        single(createdAtStart = true) { p2pController }
        single(createdAtStart = true) { supremePowerManager }
        single(createdAtStart = true) { permissionManager }
        single(createdAtStart = true) { connectivityUseCase }
        single(createdAtStart = true) { hapticManager }

        viewModel { MainViewModel(get(), get()) }
        viewModel { BluetoothViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { SupremePowerViewModel(get()) }
    }

    @Before
    fun setUp() {
        // Ensure clean state by stopping Koin if it's already running
        if (GlobalContext.getOrNull() != null) {
            stopKoin()
        }

        // Setup stubs BEFORE starting Koin to ensure ViewModels get the mocks
        every { repository.nicknameFlow } returns MutableStateFlow("vibe")
        every { repository.emojiAvatar } returns MutableStateFlow("👤")
        every { repository.stealthMode } returns MutableStateFlow(false)
        every { repository.lowPowerMode } returns MutableStateFlow(false)
        every { repository.blockedUsers } returns MutableStateFlow(emptySet())
        every { repository.vibedPeers } returns MutableStateFlow(emptySet())
        every { repository.getDeviceId() } returns "test-id"
        every { repository.getCurrentNickname() } returns "vibe"

        every { p2pController.scannedDevices } returns scannedDevicesFlow
        every { p2pController.connectedRadios } returns connectedRadiosFlow
        every { p2pController.incomingRadioRequests } returns incomingRequestsFlow
        every { p2pController.outgoingRadioRequests } returns MutableStateFlow(emptySet())
        every { p2pController.isDiscovering } returns MutableStateFlow(true)
        every { p2pController.errors } returns errorsFlow
        every { p2pController.isConnected } returns isConnectedFlow
        every { p2pController.messages } returns messagesFlow
        every { p2pController.isAdvertising } returns isAdvertisingFlow
        every { p2pController.discoveredCrowds } returns MutableSharedFlow<cc.thevar.blukit.domain.model.VibeGroup>()
        every { p2pController.syncProgress } returns MutableStateFlow(null)
        
        coEvery { p2pController.sendMessage(any(), any(), any(), any(), any(), any(), any()) } answers {
            val content = firstArg<String>()
            val receiver = secondArg<String?>()
            val newMsg = MessagePayload(
                messageId = UUID.randomUUID().toString(),
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
        
        every { vibeStore.groups } returns groupsFlow
        every { vibeStore.activeGroups } returns groupsFlow
        every { vibeStore.archivedGroups } returns MutableStateFlow(emptyList())
        every { vibeStore.vaultedGroups } returns MutableStateFlow(emptyList())
        every { vibeStore.messages } returns messagesFlow
        every { vibeStore.getAllMessages() } returns messagesFlow
        coEvery { vibeStore.getGroup(any()) } returns null
        every { vibeStore.autoArchiveCrowds() } returns Unit
        coEvery { vibeStore.pruneMedia(any()) } returns Unit
        coEvery { vibeStore.updateGroupLastVibe(any(), any()) } returns Unit

        every { permissionManager.requiredPermissions } returns listOf(android.Manifest.permission.BLUETOOTH_SCAN)
        every { permissionManager.essentialPermissions } returns listOf(android.Manifest.permission.BLUETOOTH_SCAN)
        every { permissionManager.permissionsGranted } returns permissionsGrantedFlow

        startKoin {
            allowOverride(true)
            androidContext(InstrumentationRegistry.getInstrumentation().targetContext)
            modules(testModule)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testLinkSetup_AcceptanceFlow() {
        val incomingVibe = P2PDevice("id-123", "Mystic Vibe", "👤")
        // Update flow BEFORE starting app
        incomingRequestsFlow.value = setOf(incomingVibe)

        startApp()
        
        composeTestRule.onRoot().printToLog("DEBUG_HIERARCHY")
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("IncomingRequestRow"), 30000)
        
        // Accept the vibe
        composeTestRule.onNodeWithTag("AcceptRequestButton", useUnmergedTree = true).performClick()
        
        verify { p2pController.acceptRadio(any()) }
    }

    @Test
    fun testNavigateToVibeAndChangeIdentity() {
        startApp()
        
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("IdentityVibeInput"), 30000)
        composeTestRule.onNodeWithTag("IdentityVibeInput", useUnmergedTree = true).performTextReplacement("Quantum")
        
        verify { repository.saveNickname("Quantum") }
    }

    @Test
    fun testHarmonyCheck() {
        // Update state BEFORE starting app
        radioStatesFlow.value = RadioStates(isBluetoothEnabled = false, isLocationEnabled = true, isWifiEnabled = true)
        
        startApp()
        
        composeTestRule.waitUntilAtLeastOneExists(hasText("AWAKEN", ignoreCase = true, substring = true), 20000)
        composeTestRule.onAllNodesWithText("AWAKEN", ignoreCase = true, substring = true, useUnmergedTree = true).onFirst().performClick()
    }

    @Test
    fun testSendVibeFlow() {
        val device = P2PDevice("vibe-1", "Aura Vibe", "👤")
        // Update flows BEFORE starting app
        scannedDevicesFlow.value = listOf(device)
        connectedRadiosFlow.value = setOf("vibe-1")
        isConnectedFlow.value = true

        startApp()
        
        composeTestRule.onRoot().printToLog("DEBUG_HIERARCHY")
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("PersonaNode_vibe-1"), 30000)
        composeTestRule.onNodeWithTag("PersonaNode_vibe-1", useUnmergedTree = true).performClick()
        
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SendVibeInput"), 30000)
        
        composeTestRule.onNodeWithTag("SendVibeInput", useUnmergedTree = true).performTextInput("Hello from the Air!")
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("SendVibeButton", useUnmergedTree = true).onFirst().performClick()
        
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
        composeTestRule.waitForIdle()
    }
}

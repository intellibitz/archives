package cc.thevar.blukit.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import cc.thevar.blukit.data.local.PulseStore
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
import cc.thevar.blukit.domain.model.Resonance
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
    private val pulseStore: PulseStore = mockk(relaxed = true)
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
    private val groupsFlow = MutableStateFlow<List<Resonance>>(emptyList())
    private val permissionsGrantedFlow = MutableStateFlow(true)

    private val testModule = module {
        single(createdAtStart = true) { repository }
        single(createdAtStart = true) { contactRepository }
        single(createdAtStart = true) { pulseStore }
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
        every { repository.nicknameFlow } returns MutableStateFlow("pulse")
        every { repository.emojiAvatar } returns MutableStateFlow("👤")
        every { repository.stealthMode } returns MutableStateFlow(false)
        every { repository.lowPowerMode } returns MutableStateFlow(false)
        every { repository.blockedUsers } returns MutableStateFlow(emptySet())
        every { repository.pulsedPeers } returns MutableStateFlow(emptySet())
        every { repository.getDeviceId() } returns "test-id"
        every { repository.getCurrentNickname() } returns "pulse"

        every { p2pController.scannedDevices } returns scannedDevicesFlow
        every { p2pController.connectedRadios } returns connectedRadiosFlow
        every { p2pController.incomingRadioRequests } returns incomingRequestsFlow
        every { p2pController.outgoingRadioRequests } returns MutableStateFlow(emptySet())
        every { p2pController.isDiscovering } returns MutableStateFlow(true)
        every { p2pController.errors } returns errorsFlow
        every { p2pController.isConnected } returns isConnectedFlow
        every { p2pController.messages } returns messagesFlow
        every { p2pController.isAdvertising } returns isAdvertisingFlow
        every { p2pController.discoveredCrowds } returns MutableSharedFlow<Resonance>()
        every { p2pController.syncProgress } returns MutableStateFlow(null)
        
        coEvery { p2pController.sendMessage(any(), any(), any(), any(), any(), any(), any()) } answers {
            val content = firstArg<String>()
            val receiver = secondArg<String?>()
            val newMsg = MessagePayload(
                messageId = UUID.randomUUID().toString(),
                senderId = "test-id",
                senderName = "pulse",
                content = content,
                timestamp = System.currentTimeMillis(),
                receiverId = receiver
            )
            messagesFlow.value = messagesFlow.value + newMsg
            newMsg
        }

        every { radioStateManager.radioStates } returns radioStatesFlow
        every { supremePowerManager.report } returns reportFlow
        
        every { pulseStore.groups } returns groupsFlow
        every { pulseStore.activeGroups } returns groupsFlow
        every { pulseStore.archivedGroups } returns MutableStateFlow(emptyList())
        every { pulseStore.vaultedGroups } returns MutableStateFlow(emptyList())
        every { pulseStore.messages } returns messagesFlow
        every { pulseStore.getAllMessages() } returns messagesFlow
        coEvery { pulseStore.getGroup(any()) } returns null
        every { pulseStore.autoArchiveCrowds() } returns Unit
        coEvery { pulseStore.pruneMedia(any()) } returns Unit
        coEvery { pulseStore.updateGroupLastPulse(any(), any()) } returns Unit

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
    fun testNavigateToPulseAndChangeIdentity() {
        startApp()
        
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("IdentityPulseInput"), 30000)
        composeTestRule.onNodeWithTag("IdentityPulseInput", useUnmergedTree = true).performTextReplacement("Quantum")
        
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

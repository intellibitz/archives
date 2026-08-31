package cc.thevar.blukit.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.platform.app.InstrumentationRegistry
import cc.thevar.blukit.data.local.EchoLedger
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.RadioStates
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Source
import cc.thevar.blukit.domain.model.Sphere
import cc.thevar.blukit.domain.power.HarmonyReport
import cc.thevar.blukit.domain.logic.AtmosphereManager
import cc.thevar.blukit.domain.usecase.ConnectivityUseCase
import cc.thevar.blukit.network.p2p.ResonanceController
import cc.thevar.blukit.network.p2p.ResonanceError
import cc.thevar.blukit.ui.theme.BlukitTheme
import cc.thevar.blukit.ui.viewmodels.BluetoothViewModel
import cc.thevar.blukit.ui.viewmodels.MainViewModel
import cc.thevar.blukit.ui.viewmodels.HarmonyViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import java.util.UUID

@OptIn(ExperimentalTestApi::class)
class FlowsTest : KoinTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val repository: IdentityRepository = mockk(relaxed = true)
    private val contactRepository: ContactRepository = mockk(relaxed = true)
    private val echoLedger: EchoLedger = mockk(relaxed = true)
    private val radioStateManager: RadioStateManager = mockk(relaxed = true)
    private val resonanceController: ResonanceController = mockk(relaxed = true)
    private val harmonyManager: cc.thevar.blukit.data.power.HarmonyManager = mockk(relaxed = true)
    private val permissionManager: SpreadPermissionManager = mockk(relaxed = true)
    private val connectivityUseCase: ConnectivityUseCase = mockk(relaxed = true)
    private val hapticManager: HapticManager = mockk(relaxed = true)
    private val atmosphereManager: AtmosphereManager = mockk(relaxed = true)

    private val radioStatesFlow = MutableStateFlow(RadioStates(isBluetoothEnabled = true, isLocationEnabled = true, isWifiEnabled = true))
    private val scannedSourcesFlow = MutableStateFlow<List<Source>>(emptyList())
    private val connectedRadiosFlow = MutableStateFlow<Set<String>>(emptySet())
    private val incomingRequestsFlow = MutableStateFlow<Set<Source>>(emptySet())
    private val isConnectedFlow = MutableStateFlow(false)
    private val echoesFlow = MutableStateFlow<List<Echo>>(emptyList())
    private val isAdvertisingFlow = MutableStateFlow(false)
    private val errorsFlow = MutableStateFlow<ResonanceError?>(null)
    private val reportFlow = MutableStateFlow(HarmonyReport())
    private val spheresFlow = MutableStateFlow<List<Sphere>>(emptyList())
    private val permissionsGrantedFlow = MutableStateFlow(true)

    private val testModule = module {
        single(createdAtStart = true) { repository }
        single(createdAtStart = true) { contactRepository }
        single(createdAtStart = true) { echoLedger }
        single(createdAtStart = true) { radioStateManager }
        single(createdAtStart = true) { resonanceController }
        single(createdAtStart = true) { harmonyManager }
        single(createdAtStart = true) { permissionManager }
        single(createdAtStart = true) { connectivityUseCase }
        single(createdAtStart = true) { hapticManager }
        single(createdAtStart = true) { atmosphereManager }

        viewModelOf(::MainViewModel)
        viewModelOf(::BluetoothViewModel)
        viewModelOf(::HarmonyViewModel)
    }

    @Before
    fun setUp() {
        if (GlobalContext.getOrNull() != null) {
            stopKoin()
        }

        every { repository.nicknameFlow } returns MutableStateFlow("pulse")
        every { repository.emojiAvatar } returns MutableStateFlow("👤")
        every { repository.stealthMode } returns MutableStateFlow(false)
        every { repository.lowPowerMode } returns MutableStateFlow(false)
        every { repository.blockedUsers } returns MutableStateFlow(emptySet())
        every { repository.pulsedPeers } returns MutableStateFlow(emptySet())
        every { repository.getDeviceId() } returns "test-id"
        every { repository.getCurrentNickname() } returns "pulse"

        every { resonanceController.scannedDevices } returns scannedSourcesFlow
        every { resonanceController.connectedGroups } returns connectedRadiosFlow
        every { resonanceController.incomingRadioRequests } returns incomingRequestsFlow
        every { resonanceController.outgoingRadioRequests } returns MutableStateFlow(emptySet())
        every { resonanceController.isDiscovering } returns MutableStateFlow(true)
        every { resonanceController.errors } returns errorsFlow
        every { resonanceController.isConnected } returns isConnectedFlow
        every { resonanceController.messages } returns echoesFlow
        every { resonanceController.isAdvertising } returns isAdvertisingFlow
        every { resonanceController.discoveredRooms } returns MutableSharedFlow<Sphere>()
        every { resonanceController.syncProgress } returns MutableStateFlow(null)
        
        coEvery { resonanceController.sendMessage(any(), any(), any(), any(), any(), any(), any()) } answers {
            val content = firstArg<String>()
            val receiver = secondArg<String?>()
            val newEcho = Echo(
                messageId = UUID.randomUUID().toString(),
                senderId = "test-id",
                senderName = "pulse",
                content = content,
                timestamp = System.currentTimeMillis(),
                receiverId = receiver
            )
            echoesFlow.value = echoesFlow.value + newEcho
            newEcho
        }

        every { radioStateManager.radioStates } returns radioStatesFlow
        every { harmonyManager.report } returns reportFlow
        
        every { echoLedger.spheres } returns spheresFlow
        every { echoLedger.activeSpheres } returns spheresFlow
        every { echoLedger.archivedSpheres } returns MutableStateFlow(emptyList())
        every { echoLedger.vaultedSpheres } returns MutableStateFlow(emptyList())
        every { echoLedger.echoes } returns echoesFlow
        every { echoLedger.getAllEchoes() } returns echoesFlow
        coEvery { echoLedger.getSphere(any()) } returns null
        every { echoLedger.autoArchiveSpheres() } returns Unit
        coEvery { echoLedger.pruneMedia(any()) } returns Unit
        coEvery { echoLedger.updateSphereLastEcho(any(), any()) } returns Unit

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
        radioStatesFlow.value = RadioStates(isBluetoothEnabled = false, isLocationEnabled = true, isWifiEnabled = true)
        
        startApp()
        
        composeTestRule.waitUntilAtLeastOneExists(hasText("AWAKE", ignoreCase = true, substring = true), 20000)
        composeTestRule.onAllNodesWithText("AWAKE", ignoreCase = true, substring = true, useUnmergedTree = true).onFirst().performClick()
    }

    private fun startApp() {
        composeTestRule.setContent {
            BlukitTheme {
                BlukitApp()
            }
        }
        composeTestRule.waitForIdle()
    }
}

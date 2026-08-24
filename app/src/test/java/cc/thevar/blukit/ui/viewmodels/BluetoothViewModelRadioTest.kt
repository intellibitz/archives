package cc.thevar.blukit.ui.viewmodels

import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.RadioStates
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.network.p2p.P2PController
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BluetoothViewModelRadioTest {

    private val p2pController: P2PController = mockk(relaxed = true)
    private val radioStateManager: RadioStateManager = mockk(relaxed = true)
    private val repository: IdentityRepository = mockk(relaxed = true)
    private val permissionManager: SpreadPermissionManager = mockk(relaxed = true)
    private val vibeStore: VibeStore = mockk(relaxed = true)

    private val radioStates = MutableStateFlow(RadioStates(true, true, true))
    private val permissionsGranted = MutableStateFlow(true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any<String>(), any<String>()) } returns 0
        every { android.util.Log.i(any<String>(), any<String>()) } returns 0
        every { android.util.Log.w(any<String>(), any<String>()) } returns 0
        every { android.util.Log.e(any<String>(), any<String>()) } returns 0

        every { radioStateManager.radioStates } returns radioStates
        every { repository.vibedPeers } returns MutableStateFlow(emptySet())
        every { vibeStore.groups } returns MutableStateFlow(emptyList())
        every { p2pController.scannedDevices } returns MutableStateFlow(emptyList())
        every { p2pController.messages } returns MutableStateFlow(emptyList())
        every { p2pController.connectedLinks } returns MutableStateFlow(emptySet())
        every { p2pController.incomingLinkRequests } returns MutableStateFlow(emptySet())
        every { p2pController.isConnected } returns MutableStateFlow(false)
        every { p2pController.isDiscovering } returns MutableStateFlow(false)
        every { p2pController.isAdvertising } returns MutableStateFlow(false)
        every { p2pController.errors } returns MutableStateFlow(null)
        every { permissionManager.permissionsGranted } returns permissionsGranted
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun `scan starts when radios are on and permissions granted`() = runTest {
        val useCase = cc.thevar.blukit.domain.usecase.ConnectivityUseCase(
            p2pController, radioStateManager, permissionManager, backgroundScope
        )
        val viewModel = BluetoothViewModel(p2pController, radioStateManager, repository, permissionManager, vibeStore, useCase)
        
        permissionsGranted.value = true
        radioStates.value = RadioStates(true, true, true)
        
        runCurrent()
        
        viewModel.startScan()
        
        verify(atLeast = 1) { p2pController.startDiscovery() }
        verify(atLeast = 1) { p2pController.startAdvertising() }
    }

    @Test
    fun `scan does not start if bluetooth is off`() = runTest {
        val useCase = cc.thevar.blukit.domain.usecase.ConnectivityUseCase(
            p2pController, radioStateManager, permissionManager, backgroundScope
        )
        val viewModel = BluetoothViewModel(p2pController, radioStateManager, repository, permissionManager, vibeStore, useCase)

        permissionsGranted.value = true
        radioStates.value = RadioStates(false, true, true)
        
        runCurrent()
        
        // Clear recorded calls from init's harmony observer
        clearMocks(p2pController, recordedCalls = true, answers = false)
        
        viewModel.startScan()
        
        verify(exactly = 0) { p2pController.startDiscovery() }
    }
}

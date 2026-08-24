package cc.thevar.blukit.ui.viewmodels

import app.cash.turbine.test
import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.domain.usecase.ConnectivityUseCase
import cc.thevar.blukit.network.p2p.P2PController
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BluetoothViewModelTest {

    private val p2pController: P2PController = mockk(relaxed = true)
    private val radioStateManager: RadioStateManager = mockk(relaxed = true)
    private val repository: IdentityRepository = mockk(relaxed = true)
    private val permissionManager: SpreadPermissionManager = mockk(relaxed = true)
    private val vibeStore: VibeStore = mockk(relaxed = true)
    private val connectivityUseCase: ConnectivityUseCase = mockk(relaxed = true)

    private val scannedDevicesFlow = MutableStateFlow<List<P2PDevice>>(emptyList())
    private val messagesFlow = MutableStateFlow<List<cc.thevar.blukit.domain.model.MessagePayload>>(emptyList())
    private val activeGroupsFlow = MutableStateFlow<List<cc.thevar.blukit.domain.model.VibeGroup>>(emptyList())
    private val archivedGroupsFlow = MutableStateFlow<List<cc.thevar.blukit.domain.model.VibeGroup>>(emptyList())
    private val connectedLinksFlow = MutableStateFlow<Set<String>>(emptySet())
    private val radioStatesFlow = MutableStateFlow(cc.thevar.blukit.data.system.RadioStates(false, false, false))

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: BluetoothViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        every { p2pController.scannedDevices } returns scannedDevicesFlow
        every { p2pController.messages } returns messagesFlow
        every { p2pController.connectedLinks } returns connectedLinksFlow
        every { p2pController.isDiscovering } returns MutableStateFlow(false)
        every { p2pController.isAdvertising } returns MutableStateFlow(false)
        every { p2pController.errors } returns MutableStateFlow(null)
        every { p2pController.isConnected } returns MutableStateFlow(false)
        every { p2pController.incomingLinkRequests } returns MutableStateFlow(emptySet())
        every { p2pController.outgoingLinkRequests } returns MutableStateFlow(emptySet())
        every { p2pController.discoveredAirs } returns MutableSharedFlow()
        
        every { repository.vibedPeers } returns MutableStateFlow(emptySet())
        every { repository.blockedUsers } returns MutableStateFlow(emptySet())
        every { repository.getDeviceId() } returns "my-id"
        every { vibeStore.activeGroups } returns activeGroupsFlow
        every { vibeStore.archivedGroups } returns archivedGroupsFlow
        every { vibeStore.groups } returns MutableStateFlow(emptyList())
        every { radioStateManager.radioStates } returns radioStatesFlow
        every { permissionManager.permissionsGranted } returns MutableStateFlow(true)
        every { connectivityUseCase.manualConnectionStatus } returns MutableStateFlow(null)

        viewModel = BluetoothViewModel(
            p2pController, radioStateManager, repository, permissionManager, vibeStore, connectivityUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `test radio harmony reflection`() = runTest(testDispatcher) {
        viewModel.state.test(timeout = kotlin.time.Duration.parse("10s")) {
            var current = awaitItem()
            
            // Toggle Bluetooth
            radioStatesFlow.value = cc.thevar.blukit.data.system.RadioStates(true, false, false)
            
            current = awaitItem()
            while (!current.harmony.isBluetoothEnabled) { current = awaitItem() }
            assertTrue(current.harmony.isBluetoothEnabled)
        }
    }

    @Test
    fun `test selection management`() = runTest(testDispatcher) {
        viewModel.state.test(timeout = kotlin.time.Duration.parse("10s")) {
            awaitItem() // Initial
            
            viewModel.toggleDeviceSelection("device-1")
            var current = awaitItem()
            assertTrue(current.crowd.selectedDevices.contains("device-1"))
            
            viewModel.toggleDeviceSelection("device-2")
            current = awaitItem()
            assertTrue(current.crowd.selectedDevices.contains("device-2"))
            
            viewModel.clearSelection()
            current = awaitItem()
            assertTrue(current.crowd.selectedDevices.isEmpty())
        }
    }
}

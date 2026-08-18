package cc.thevar.blukit.ui.viewmodels

import app.cash.turbine.test
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.RadioStates
import cc.thevar.blukit.network.p2p.P2PController
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

/**
 * Ensures Harmony and The Vibes are correctly reflected.
 * Uses UnconfinedTestDispatcher for robust, immediate state assertions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BluetoothViewModelTest {

    private val p2pController: P2PController = mockk(relaxed = true)
    private val radioStateManager: RadioStateManager = mockk(relaxed = true)
    private val repository: cc.thevar.blukit.data.repository.IdentityRepository = mockk(relaxed = true)
    private val vibeStore: cc.thevar.blukit.data.local.VibeStore = mockk(relaxed = true)
    private lateinit var viewModel: BluetoothViewModel

    private val harmonyFlow = MutableStateFlow(RadioStates(isBluetoothEnabled = false, isLocationEnabled = false, isWifiEnabled = false))
    private val permissionFlow = MutableStateFlow(false)
    private val errorFlow = MutableStateFlow<cc.thevar.blukit.network.p2p.P2PError?>(null)
    private val scannedDevicesFlow = MutableStateFlow<List<cc.thevar.blukit.domain.model.P2PDevice>>(emptyList())
    private val messagesFlow = MutableStateFlow<List<cc.thevar.blukit.domain.model.MessagePayload>>(emptyList())
    private val groupsFlow = MutableStateFlow<List<cc.thevar.blukit.domain.model.VibeGroup>>(emptyList())
    private val connectedLinksFlow = MutableStateFlow<Set<String>>(emptySet())
    
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        io.mockk.mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any<String>(), any<String>()) } returns 0
        every { android.util.Log.i(any<String>(), any<String>()) } returns 0
        every { android.util.Log.w(any<String>(), any<String>()) } returns 0
        every { android.util.Log.e(any<String>(), any<String>()) } returns 0
        
        every { radioStateManager.radioStates } returns harmonyFlow
        every { p2pController.scannedDevices } returns scannedDevicesFlow
        every { p2pController.connectedLinks } returns connectedLinksFlow
        every { p2pController.incomingLinkRequests } returns MutableStateFlow(emptySet())
        every { p2pController.isDiscovering } returns MutableStateFlow(false)
        every { p2pController.isAdvertising } returns MutableStateFlow(false)
        every { p2pController.errors } returns errorFlow
        every { p2pController.isConnected } returns MutableStateFlow(false)
        every { p2pController.messages } returns messagesFlow
        
        every { repository.vibedPeers } returns MutableStateFlow(emptySet())
        every { vibeStore.groups } returns groupsFlow

        val permissionManager = mockk<cc.thevar.blukit.data.system.SpreadPermissionManager>(relaxed = true)
        every { permissionManager.permissionsGranted } returns permissionFlow

        viewModel = BluetoothViewModel(p2pController, radioStateManager, repository, permissionManager, vibeStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test Harmony reflects The Vibes accurately`() = runTest {
        viewModel.state.test(timeout = 10.seconds) {
            // Initial state check
            val initial = awaitItem()
            assertEquals("Harmony should start cold", false, initial.isBluetoothEnabled)

            // Awaken The Vibes
            harmonyFlow.value = RadioStates(isBluetoothEnabled = true, isLocationEnabled = true, isWifiEnabled = true)
            
            val active = awaitItem()
            assertTrue("The Vibes should be alive in Harmony", active.isBluetoothEnabled)
            assertTrue("The Air should be clear in Harmony", active.isLocationEnabled)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test Awakening The Vibes triggers discovery`() {
        harmonyFlow.value = RadioStates(isBluetoothEnabled = true, isLocationEnabled = true, isWifiEnabled = true)
        permissionFlow.value = true
        
        viewModel.startScan()
        verify { p2pController.startDiscovery() }
        verify { p2pController.startAdvertising() }
    }

    @Test
    fun `test Stilling The Vibes stops discovery`() {
        viewModel.stopScan()
        verify { p2pController.stopDiscovery() }
        verify { p2pController.stopAdvertising() }
    }

    @Test
    fun `test disturbed Air reflects in The Vibes`() = runTest {
        viewModel.state.test(timeout = 10.seconds) {
            skipItems(1) // Initial state
            
            errorFlow.value = cc.thevar.blukit.network.p2p.P2PError.GenericError("The Air is Disturbed")
            
            val disturbed = awaitItem()
            assertEquals("The Vibes must reflect the disturbed Air", "The Air is Disturbed", disturbed.uiError?.message)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test automatic scan when healthy`() = runTest {
        // healthy radios + permissions
        harmonyFlow.value = RadioStates(isBluetoothEnabled = true, isLocationEnabled = true, isWifiEnabled = true)
        permissionFlow.value = true
        
        // Use TestScope to let background jobs run
        advanceUntilIdle()
        
        verify { p2pController.startDiscovery() }
        verify { p2pController.startAdvertising() }
    }

    @Test
    fun `test selection management`() = runTest {
        viewModel.state.test {
            skipItems(1) // Initial empty state
            
            viewModel.toggleDeviceSelection("device-1")
            assertTrue(awaitItem().selectedDevices.contains("device-1"))
            
            viewModel.toggleDeviceSelection("device-1")
            assertTrue(awaitItem().selectedDevices.isEmpty())
            
            viewModel.toggleDeviceSelection("device-2")
            assertTrue(awaitItem().selectedDevices.contains("device-2"))
            
            viewModel.clearSelection()
            assertTrue(awaitItem().selectedDevices.isEmpty())
        }
    }

    @Test
    fun `test energy surge on message`() = runTest {
        viewModel.energySurge.test {
            assertEquals(0f, awaitItem())
            
            messagesFlow.value = listOf(mockk(relaxed = true))
            
            assertEquals(1f, awaitItem())
            assertEquals(0f, awaitItem())
        }
    }
}

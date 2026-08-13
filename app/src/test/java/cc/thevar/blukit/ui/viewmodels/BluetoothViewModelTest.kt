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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BluetoothViewModelTest {

    private val p2pController: P2PController = mockk(relaxed = true)
    private val radioStateManager: RadioStateManager = mockk(relaxed = true)
    private lateinit var viewModel: BluetoothViewModel

    private val scannedDevicesFlow = MutableStateFlow(emptyList<cc.thevar.blukit.domain.model.P2PDevice>())
    private val radioStatesFlow = MutableStateFlow(RadioStates(isBluetoothEnabled = false, isLocationEnabled = false))
    private val connectedPeersFlow = MutableStateFlow(emptySet<String>())
    private val isDiscoveringFlow = MutableStateFlow(false)
    private val errorsFlow = MutableSharedFlow<String>()
    
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        every { p2pController.scannedDevices } returns scannedDevicesFlow
        every { p2pController.connectedPeers } returns connectedPeersFlow
        every { p2pController.isDiscovering } returns isDiscoveringFlow
        every { p2pController.errors } returns errorsFlow
        every { p2pController.isConnected } returns MutableStateFlow(false)
        every { p2pController.messages } returns MutableStateFlow(emptyList())
        every { radioStateManager.radioStates } returns radioStatesFlow

        viewModel = BluetoothViewModel(p2pController, radioStateManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test state updates when radios enabled`() = runTest {
        radioStatesFlow.value = RadioStates(isBluetoothEnabled = true, isLocationEnabled = true)
        
        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.isBluetoothEnabled)
            assertTrue(state.isLocationEnabled)
        }
    }

    @Test
    fun `test startScan calls controller`() {
        viewModel.startScan()
        verify { p2pController.startDiscovery() }
        verify { p2pController.startAdvertising() }
    }

    @Test
    fun `test stopScan calls controller`() {
        viewModel.stopScan()
        verify { p2pController.stopDiscovery() }
        verify { p2pController.stopAdvertising() }
    }

    @Test
    fun `test error flow updates state`() = runTest {
        viewModel.state.test {
            awaitItem() // Initial
            errorsFlow.emit("Discovery failed")
            assertEquals("Discovery failed", awaitItem().errorMessage)
        }
    }
}

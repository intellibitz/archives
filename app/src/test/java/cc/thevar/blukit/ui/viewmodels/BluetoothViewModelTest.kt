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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BluetoothViewModelTest {

    private val p2pController: P2PController = mockk(relaxed = true)
    private val radioStateManager: RadioStateManager = mockk(relaxed = true)
    private lateinit var viewModel: BluetoothViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val scannedDevicesFlow = MutableStateFlow(emptyList<cc.thevar.blukit.domain.model.P2PDevice>())
    private val radioStatesFlow = MutableStateFlow(RadioStates(false, false))
    private val connectedPeersFlow = MutableStateFlow(emptySet<String>())
    private val isDiscoveringFlow = MutableStateFlow(false)
    private val isConnectedFlow = MutableStateFlow(false)
    private val errorsFlow = MutableSharedFlow<String>()
    private val messagesFlow = MutableStateFlow(emptyList<cc.thevar.blukit.domain.model.MessagePayload>())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        every { p2pController.scannedDevices } returns scannedDevicesFlow
        every { radioStateManager.radioStates } returns radioStatesFlow
        every { p2pController.connectedPeers } returns connectedPeersFlow
        every { p2pController.isDiscovering } returns isDiscoveringFlow
        every { p2pController.isConnected } returns isConnectedFlow
        every { p2pController.errors } returns errorsFlow
        every { p2pController.messages } returns messagesFlow

        viewModel = BluetoothViewModel(p2pController, radioStateManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state reflects radio states from manager`() = runTest {
        viewModel.state.test {
            assertEquals(false, awaitItem().isBluetoothEnabled)
            
            radioStatesFlow.value = RadioStates(isBluetoothEnabled = true, isLocationEnabled = true)
            assertEquals(true, awaitItem().isBluetoothEnabled)
        }
    }

    @Test
    fun `startScan triggers controller discovery and advertising`() {
        viewModel.startScan()
        verify { p2pController.startDiscovery() }
        verify { p2pController.startAdvertising() }
    }

    @Test
    fun `stopScan triggers controller stop actions`() {
        viewModel.stopScan()
        verify { p2pController.stopDiscovery() }
        verify { p2pController.stopAdvertising() }
    }
}

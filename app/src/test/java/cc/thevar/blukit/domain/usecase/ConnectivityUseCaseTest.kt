package cc.thevar.blukit.domain.usecase

import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.RadioStates
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.network.p2p.P2PController
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityUseCaseTest {

    private val p2pController: P2PController = mockk(relaxed = true)
    private val radioStateManager: RadioStateManager = mockk(relaxed = true)
    private val permissionManager: SpreadPermissionManager = mockk(relaxed = true)
    
    private val harmonyFlow = MutableStateFlow(RadioStates(false, false, false))
    private val permissionFlow = MutableStateFlow(false)
    

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any<String>(), any<String>()) } returns 0
        every { android.util.Log.w(any<String>(), any<String>()) } returns 0
        every { android.util.Log.e(any<String>(), any<String>()) } returns 0
        
        every { radioStateManager.radioStates } returns harmonyFlow
        every { permissionManager.permissionsGranted } returns permissionFlow
    }

    @After
    fun tearDown() {
        unmockkStatic(android.util.Log::class)
        clearAllMocks()
    }

    @Test
    fun `test Harmony achieved triggers discovery and advertising`() = runTest(testDispatcher) {
        val useCase = ConnectivityUseCase(p2pController, radioStateManager, permissionManager, backgroundScope)
        
        harmonyFlow.value = RadioStates(isBluetoothEnabled = true, isLocationEnabled = true, isWifiEnabled = true)
        permissionFlow.value = true
        
        runCurrent()
        
        verify { p2pController.startDiscovery() }
        verify { p2pController.startAdvertising() }
    }

    @Test
    fun `test Harmony lost stops discovery and advertising`() = runTest(testDispatcher) {
        val useCase = ConnectivityUseCase(p2pController, radioStateManager, permissionManager, backgroundScope)
        
        // First achieve harmony
        harmonyFlow.value = RadioStates(isBluetoothEnabled = true, isLocationEnabled = true, isWifiEnabled = true)
        permissionFlow.value = true
        runCurrent()
        
        // Reset mocks to clear previous calls
        clearMocks(p2pController, recordedCalls = true, answers = false)
        
        // Lose harmony
        harmonyFlow.value = RadioStates(isBluetoothEnabled = false, isLocationEnabled = true, isWifiEnabled = true)
        runCurrent()
        
        verify { p2pController.stopDiscovery() }
        verify { p2pController.stopAdvertising() }
    }
}

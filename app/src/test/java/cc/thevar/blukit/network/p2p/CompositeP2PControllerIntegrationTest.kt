package cc.thevar.blukit.network.p2p

import cc.thevar.blukit.domain.model.P2PDevice
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompositeP2PControllerIntegrationTest {

    private val nearbyController: NearbyP2PController = mockk(relaxed = true)
    private val bleController: BleFallbackController = mockk(relaxed = true)
    private lateinit var compositeController: CompositeP2PController
    private val testDispatcher = UnconfinedTestDispatcher()

    private val nearbyScannedFlow = MutableStateFlow<List<P2PDevice>>(emptyList())
    private val bleScannedFlow = MutableStateFlow<List<P2PDevice>>(emptyList())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { nearbyController.scannedDevices } returns nearbyScannedFlow
        every { bleController.scannedDevices } returns bleScannedFlow
        
        compositeController = CompositeP2PController(nearbyController, bleController)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `scanned devices are unified and distinct`() = runTest {
        val device1 = P2PDevice("id-1", "Nearby User", "👤")
        val device2 = P2PDevice("id-2", "BLE User", "👤")
        val duplicateDevice = P2PDevice("id-1", "Duplicate User", "👤")

        nearbyScannedFlow.value = listOf(device1)
        bleScannedFlow.value = listOf(device2, duplicateDevice)

        // Advance or collect to ensure the combine loop runs
        val unified = compositeController.scannedDevices.first { it.size == 2 }
        assertEquals(2, unified.size)
        val ids = unified.map { it.id }.toSet()
        assert(ids.contains("id-1"))
        assert(ids.contains("id-2"))
    }

    @Test
    fun `startDiscovery triggers both controllers`() {
        compositeController.startDiscovery()
        verify { nearbyController.startDiscovery() }
        verify { bleController.startDiscovery() }
    }

    @Test
    fun `sendMessage tries nearby then ble`() = runTest {
        coEvery { nearbyController.sendMessage(any(), any()) } returns null
        coEvery { bleController.sendMessage(any(), any()) } returns mockk()

        compositeController.sendMessage("Test", "receiver-1")
        
        coVerify { nearbyController.sendMessage("Test", "receiver-1") }
        coVerify { bleController.sendMessage("Test", "receiver-1") }
    }
}

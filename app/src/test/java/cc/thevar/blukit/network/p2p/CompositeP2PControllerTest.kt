package cc.thevar.blukit.network.p2p

import cc.thevar.blukit.domain.model.P2PDevice
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompositeP2PControllerTest {

    private val nearbyController: NearbyP2PController = mockk(relaxed = true)
    private val bleController: BleFallbackController = mockk(relaxed = true)
    private lateinit var compositeController: CompositeP2PController
    
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { nearbyController.scannedDevices } returns MutableStateFlow(emptyList())
        every { bleController.scannedDevices } returns MutableStateFlow(emptyList())
        every { nearbyController.isConnected } returns MutableStateFlow(false)
        every { bleController.isConnected } returns MutableStateFlow(false)
        every { nearbyController.connectedLinks } returns MutableStateFlow(emptySet())
        every { bleController.connectedLinks } returns MutableStateFlow(emptySet())
        every { nearbyController.incomingLinkRequests } returns MutableStateFlow(emptySet())
        every { bleController.incomingLinkRequests } returns MutableStateFlow(emptySet())
        every { nearbyController.isDiscovering } returns MutableStateFlow(false)
        every { bleController.isDiscovering } returns MutableStateFlow(false)
        every { nearbyController.isAdvertising } returns MutableStateFlow(false)
        every { bleController.isAdvertising } returns MutableStateFlow(false)
        every { nearbyController.errors } returns MutableStateFlow(null)
        every { bleController.errors } returns MutableStateFlow(null)
        every { nearbyController.messages } returns MutableStateFlow(emptyList())

        compositeController = CompositeP2PController(nearbyController, bleController)
    }

    @Test
    fun `scanned devices are combined from both controllers`() = runTest(testDispatcher) {
        val device1 = P2PDevice("id1", "Nearby", "📱")
        val device2 = P2PDevice("id2", "BLE", "📶")
        
        compositeController.scannedDevices.test {
            assertEquals(0, awaitItem().size)

            (nearbyController.scannedDevices as MutableStateFlow).value = listOf(device1)
            assertEquals(1, awaitItem().size)

            (bleController.scannedDevices as MutableStateFlow).value = listOf(device2)
            val combined = awaitItem()
            assertEquals(2, combined.size)
            assertTrue(combined.contains(device1))
            assertTrue(combined.contains(device2))
        }
    }

    @Test
    fun `startDiscovery triggers both controllers`() = runTest(testDispatcher) {
        compositeController.startDiscovery()
        runCurrent()
        verify { nearbyController.startDiscovery() }
        verify { bleController.startDiscovery() }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun assertTrue(condition: Boolean) {
        assert(condition)
    }
}

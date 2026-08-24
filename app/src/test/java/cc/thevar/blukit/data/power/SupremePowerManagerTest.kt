package cc.thevar.blukit.data.power

import app.cash.turbine.test
import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.network.p2p.P2PController
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.After
import org.junit.Test
import android.location.Location

@OptIn(ExperimentalCoroutinesApi::class)
class SupremePowerManagerTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        mockkStatic(Location::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(Location::class)
        clearAllMocks()
    }

    @Test
    fun `test report updates vibes state`() = runTest(testDispatcher) {
        val p2pController: P2PController = mockk(relaxed = true)
        val vibeStore: VibeStore = mockk(relaxed = true)
        val repository: IdentityRepository = mockk(relaxed = true)

        val scannedDevicesFlow = MutableStateFlow(emptyList<P2PDevice>())
        val connectedLinksFlow = MutableStateFlow(emptySet<String>())
        val allMessagesFlow = MutableStateFlow(emptyList<MessagePayload>())
        val lowPowerFlow = MutableStateFlow(false)

        every { p2pController.scannedDevices } returns scannedDevicesFlow
        every { p2pController.connectedLinks } returns connectedLinksFlow
        every { vibeStore.getAllMessages() } returns allMessagesFlow
        every { repository.lowPowerMode } returns lowPowerFlow

        val manager = SupremePowerManager(p2pController, vibeStore, repository, null, testDispatcher)

        manager.report.test(timeout = kotlin.time.Duration.parse("10s")) {
            // Trigger change: Found 2 vibes
            scannedDevicesFlow.value = listOf(P2PDevice("1", "A"), P2PDevice("2", "B"))
            runCurrent()
            
            var current = awaitItem()
            while (current.userCount != 2) { current = awaitItem() }
            
            assertEquals(2, current.userCount)
            
            // Trigger Link: Connect to one
            connectedLinksFlow.value = setOf("1")
            runCurrent()
            
            current = awaitItem()
            while (current.connectedLinksCount != 1) { current = awaitItem() }
            
            // Verify Harmony (Ties / Users + 0.2) => 1/2 + 0.2 = 0.7
            assertEquals(0.7f, current.harmony, 0.01f)
            
            assertTrue(current.aiInsight.isNotEmpty())
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test location based suggestions`() = runTest(testDispatcher) {
        val p2pController: P2PController = mockk(relaxed = true)
        val vibeStore: VibeStore = mockk(relaxed = true)
        val repository: IdentityRepository = mockk(relaxed = true)

        // Provide real StateFlows to ensure combine triggers
        every { p2pController.scannedDevices } returns MutableStateFlow(emptyList())
        every { p2pController.connectedLinks } returns MutableStateFlow(emptySet())
        every { p2pController.messages } returns MutableStateFlow(emptyList())
        every { vibeStore.getAllMessages() } returns MutableStateFlow(emptyList())
        every { repository.lowPowerMode } returns MutableStateFlow(false)

        val manager = SupremePowerManager(p2pController, vibeStore, repository, null, testDispatcher)

        manager.report.test(timeout = kotlin.time.Duration.parse("10s")) {
            // Initial state
            var current = awaitItem()

            // Mock location near Air Hub (12.9716 to 77.5946)
            val mockLocation = mockk<Location>()
            every { mockLocation.latitude } returns 12.9717
            every { mockLocation.longitude } returns 77.5947
            
            every { 
                Location.distanceBetween(any(), any(), any(), any(), any()) 
            } answers {
                val results = it.invocation.args[4] as FloatArray
                results[0] = 100f // 100 meters
            }

            manager.updateLocation(mockLocation)
            runCurrent()

            // Wait for the update to propagate through combine
            current = awaitItem()
            while (current.suggestedAirs.isEmpty()) { 
                current = awaitItem() 
            }

            assertTrue(current.suggestedAirs.contains("AIR HUB"))
            assertEquals("AIR HUB", current.lastLocation)

            cancelAndIgnoreRemainingEvents()
        }
    }
}

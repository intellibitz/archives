package cc.thevar.blukit.data.power

import app.cash.turbine.test
import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.network.p2p.P2PController
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SupremePowerManagerTest {

    @Test
    fun `test report updates vibes state`() = runTest {
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

        val manager = SupremePowerManager(p2pController, vibeStore, repository)

        manager.report.test {
            // Trigger change: Found 2 vibes
            scannedDevicesFlow.value = listOf(P2PDevice("1", "A"), P2PDevice("2", "B"))
            
            var current = awaitItem()
            while (current.userCount != 2) { current = awaitItem() }
            
            assertEquals(2, current.userCount)
            
            // Trigger Link: Connect to one
            connectedLinksFlow.value = setOf("1")
            
            current = awaitItem()
            while (current.connectedLinksCount != 1) { current = awaitItem() }
            
            // Verify Harmony (Ties / Users + 0.2) => 1/2 + 0.2 = 0.7
            assertEquals(0.7f, current.harmony, 0.01f)
            
            assertTrue(current.aiInsight.isNotEmpty())
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test harmony calculation with many users`() = runTest {
        val p2pController: P2PController = mockk(relaxed = true)
        val vibeStore: VibeStore = mockk(relaxed = true)
        val repository: IdentityRepository = mockk(relaxed = true)

        val scannedDevicesFlow = MutableStateFlow(List(10) { P2PDevice("id-$it", "User $it") })
        val connectedLinksFlow = MutableStateFlow(setOf("id-1", "id-2", "id-3", "id-4", "id-5"))

        every { p2pController.scannedDevices } returns scannedDevicesFlow
        every { p2pController.connectedLinks } returns connectedLinksFlow
        every { vibeStore.getAllMessages() } returns MutableStateFlow(emptyList())
        every { repository.lowPowerMode } returns MutableStateFlow(false)

        val manager = SupremePowerManager(p2pController, vibeStore, repository)

        manager.report.test {
            var current = awaitItem()
            while(current.userCount != 10) { current = awaitItem() }
            // 5 ties / 10 users + 0.2 = 0.5 + 0.2 = 0.7
            assertEquals(0.7f, current.harmony, 0.01f)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

package cc.thevar.blukit.data.power

import app.cash.turbine.test
import cc.thevar.blukit.data.local.dao.MessageDao
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
    fun `test report updates mesh state`() = runTest {
        val p2pController: P2PController = mockk(relaxed = true)
        val messageDao: MessageDao = mockk(relaxed = true)

        val scannedDevicesFlow = MutableStateFlow(emptyList<P2PDevice>())
        val connectedPeersFlow = MutableStateFlow(emptySet<String>())
        val allMessagesFlow = MutableStateFlow(emptyList<cc.thevar.blukit.data.local.entities.MessageEntity>())

        every { p2pController.scannedDevices } returns scannedDevicesFlow
        every { p2pController.connectedPeers } returns connectedPeersFlow
        every { messageDao.getAllMessages() } returns allMessagesFlow

        val manager = SupremePowerManager(p2pController, messageDao)

        manager.report.test {
            // Wait for initial combined state
            var current = awaitItem()
            while (current.userCount != 0) { current = awaitItem() }
            
            // Trigger change
            scannedDevicesFlow.value = listOf(P2PDevice("1", "A"), P2PDevice("2", "B"))
            
            // Wait for update
            current = awaitItem()
            while (current.userCount != 2) { current = awaitItem() }
            
            assertEquals(2, current.userCount)
            // Be very lenient with AI insights in tests as they are flavor text
            assertTrue("Insight was: ${current.aiInsight}", current.aiInsight.isNotEmpty())
        }
    }
}

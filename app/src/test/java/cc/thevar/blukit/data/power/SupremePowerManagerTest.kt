package cc.thevar.blukit.data.power

import app.cash.turbine.test
import cc.thevar.blukit.data.local.dao.MessageDao
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.network.p2p.P2PController
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupremePowerManagerTest {

    private val p2pController: P2PController = mockk(relaxed = true)
    private val messageDao: MessageDao = mockk(relaxed = true)

    private val scannedDevicesFlow = MutableStateFlow(emptyList<P2PDevice>())
    private val connectedPeersFlow = MutableStateFlow(emptySet<String>())
    private val allMessagesFlow = MutableStateFlow(emptyList<cc.thevar.blukit.data.local.entities.MessageEntity>())

    @Test
    fun `test report updates when mesh state changes`() = runTest {
        every { p2pController.scannedDevices } returns scannedDevicesFlow
        every { p2pController.connectedPeers } returns connectedPeersFlow
        every { messageDao.getAllMessages() } returns allMessagesFlow

        val manager = SupremePowerManager(p2pController, messageDao)

        manager.report.test {
            // Initial IDLE state
            val initial = awaitItem()
            assertEquals(0, initial.userCount)
            assertEquals("IDLE", initial.trafficDensity)

            // Simulate users appearing
            scannedDevicesFlow.value = listOf(P2PDevice("1", "A"), P2PDevice("2", "B"))
            val updated = awaitItem()
            assertEquals(2, updated.userCount)
            assertEquals("SPARSE", updated.trafficDensity)
            assertTrue(updated.aiInsight.contains("Mesh stable"))

            // Simulate high density
            scannedDevicesFlow.value = (1..60).map { P2PDevice(it.toString(), "User $it") }
            val highDensity = awaitItem()
            assertEquals(60, highDensity.userCount)
            assertEquals("CRITICAL MASS", highDensity.trafficDensity)
        }
    }
}

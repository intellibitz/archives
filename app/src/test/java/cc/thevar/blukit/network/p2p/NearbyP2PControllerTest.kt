package cc.thevar.blukit.network.p2p

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import cc.thevar.blukit.TestBlukitApplication
import cc.thevar.blukit.data.local.dao.MessageDao
import cc.thevar.blukit.data.local.dao.PeerDao
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.data.crypto.CryptoManager
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.tasks.Tasks
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestBlukitApplication::class)
class NearbyP2PControllerTest {

    private lateinit var context: Context
    private val repository: IdentityRepository = mockk(relaxed = true)
    private val contactRepository: ContactRepository = mockk(relaxed = true)
    private val messageDao: MessageDao = mockk(relaxed = true)
    private val peerDao: PeerDao = mockk(relaxed = true)
    private val hapticManager: HapticManager = mockk(relaxed = true)
    private val cryptoManager: CryptoManager = mockk(relaxed = true)
    private val connectionsClient: ConnectionsClient = mockk(relaxed = true)

    private lateinit var controller: NearbyP2PController
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        
        mockkStatic(Nearby::class)
        every { Nearby.getConnectionsClient(any<Context>()) } returns connectionsClient
        
        every { connectionsClient.startDiscovery(any<String>(), any(), any()) } returns Tasks.forResult(null)
        every { connectionsClient.startAdvertising(any<String>(), any(), any(), any()) } returns Tasks.forResult(null)
        every { connectionsClient.stopDiscovery() } returns Unit
        every { connectionsClient.stopAdvertising() } returns Unit
        
        every { messageDao.getAllMessages() } returns flowOf(emptyList())
        coEvery { repository.getNickname() } returns "Tester"
        every { repository.emojiAvatar } returns flowOf("👤")

        controller = NearbyP2PController(
            context, repository, contactRepository, messageDao, peerDao, hapticManager, cryptoManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Nearby::class)
    }

    @Test
    fun `startDiscovery calls Nearby startDiscovery`() {
        controller.startDiscovery()
        verify { connectionsClient.startDiscovery(any<String>(), any(), any()) }
    }

    @Test
    fun `stopDiscovery calls Nearby stopDiscovery`() {
        controller.stopDiscovery()
        verify { connectionsClient.stopDiscovery() }
    }

    @Test
    fun `startAdvertising calls Nearby startAdvertising`() {
        controller.startAdvertising()
        // Wait for the background coroutine to execute the call
        verify(timeout = 2000) { connectionsClient.startAdvertising(any<String>(), any(), any(), any()) }
    }
}

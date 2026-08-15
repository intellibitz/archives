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
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.tasks.Tasks
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import javax.crypto.SecretKey

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestBlukitApplication::class)
class NearbyP2PControllerTest {

    private lateinit var context: Context
    private lateinit var repository: IdentityRepository
    private val contactRepository: ContactRepository = mockk(relaxed = true)
    private val messageDao: MessageDao = mockk(relaxed = true)
    private val peerDao: PeerDao = mockk(relaxed = true)
    private val hapticManager: HapticManager = mockk(relaxed = true)
    private val cryptoManager: CryptoManager = mockk(relaxed = true)
    private val connectionsClient: ConnectionsClient = mockk(relaxed = true)

    private lateinit var controller: NearbyP2PController
    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var validPublicKeyEncoded: ByteArray

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        
        repository = mockk(relaxed = true)
        
        // Generate a valid EC key for handshakes
        val keyGen = KeyPairGenerator.getInstance("EC")
        keyGen.initialize(256)
        val pair = keyGen.generateKeyPair()
        validPublicKeyEncoded = pair.public.encoded

        mockkStatic(Nearby::class)
        every { Nearby.getConnectionsClient(any<Context>()) } returns connectionsClient
        
        every { connectionsClient.startDiscovery(any<String>(), any(), any()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.startAdvertising(any<String>(), any<String>(), any(), any()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.stopDiscovery() } returns Unit
        every { connectionsClient.stopAdvertising() } returns Unit
        every { connectionsClient.acceptConnection(any<String>(), any()) } returns Tasks.forResult<Void>(null)
        val mockTask = mockk<com.google.android.gms.tasks.Task<Void>>(relaxed = true)
        every { mockTask.addOnCompleteListener(any()) } answers {
            val listener = it.invocation.args[0] as com.google.android.gms.tasks.OnCompleteListener<Void>
            listener.onComplete(mockTask)
            mockTask
        }
        every { connectionsClient.sendPayload(any<String>(), any()) } returns mockTask
        
        every { messageDao.getAllMessages() } returns flowOf(emptyList())
        every { repository.getCurrentNickname() } returns "Tester"
        every { repository.getDeviceId() } returns "tester-id"
        every { repository.nicknameFlow } returns MutableStateFlow("Tester")
        every { repository.emojiAvatar } returns MutableStateFlow("👤")
        every { repository.stealthMode } returns MutableStateFlow(false)
        every { repository.lowPowerMode } returns MutableStateFlow(false)
        every { repository.blockedUsers } returns MutableStateFlow(emptySet())

        // Mock CryptoManager to return valid keys/secrets
        val mockKeyPair = mockk<KeyPair>(relaxed = true)
        every { mockKeyPair.public } returns pair.public
        every { cryptoManager.getLocalKeyPair() } returns mockKeyPair
        every { cryptoManager.deriveSharedSecret(any()) } returns mockk<SecretKey>(relaxed = true)
        every { cryptoManager.encrypt(any(), any()) } returns byteArrayOf(0x11)

        controller = NearbyP2PController(
            context, repository, contactRepository, messageDao, peerDao, hapticManager, cryptoManager, testDispatcher
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
        verify(timeout = 2000) { connectionsClient.startAdvertising(any<String>(), any<String>(), any<ConnectionLifecycleCallback>(), any<AdvertisingOptions>()) }
    }

    @Test
    fun `broadcastMessage sends payload to all active connections`() = runTest {
        val lifecycleCallbackSlot = slot<ConnectionLifecycleCallback>()
        val payloadCallbackSlot = slot<PayloadCallback>()
        
        every { 
            connectionsClient.startAdvertising(any<String>(), any<String>(), capture(lifecycleCallbackSlot), any<AdvertisingOptions>()) 
        } returns Tasks.forResult<Void>(null)
        
        every { 
            connectionsClient.acceptConnection(any<String>(), capture(payloadCallbackSlot)) 
        } returns Tasks.forResult<Void>(null)

        controller.startAdvertising()
        
        testDispatcher.scheduler.advanceUntilIdle()
        val lifecycleCallback = lifecycleCallbackSlot.captured

        val peerIds = listOf("peer-1", "peer-2")
        val resultSuccess = mockk<ConnectionResolution>()
        every { resultSuccess.status.isSuccess } returns true

        peerIds.forEach { peerId ->
            lifecycleCallback.onConnectionInitiated(peerId, mockk(relaxed = true))
            testDispatcher.scheduler.advanceUntilIdle()
            
            // Establish secure session via handshake with VALID key
            val handshakePayload = mockk<Payload>()
            every { handshakePayload.asBytes() } returns byteArrayOf(0x01) + validPublicKeyEncoded
            payloadCallbackSlot.captured.onPayloadReceived(peerId, handshakePayload)
            testDispatcher.scheduler.advanceUntilIdle()
            
            lifecycleCallback.onConnectionResult(peerId, resultSuccess)
            testDispatcher.scheduler.advanceUntilIdle()

            // Link Setup: Explicitly accept the link to move from activeConnections to connectedLinks
            controller.acceptLink(cc.thevar.blukit.domain.model.P2PDevice(peerId, "Vibe", "👤"))
            testDispatcher.scheduler.advanceUntilIdle()
        }

        assertEquals(2, controller.connectedLinks.value.size)

        val result = controller.broadcastMessage("Hello Vibes!")
        assertNotNull(result)
        testDispatcher.scheduler.advanceUntilIdle()

        peerIds.forEach { peerId ->
            verify(atLeast = 1, timeout = 2000) { connectionsClient.sendPayload(peerId, any()) }
        }
    }
}

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
import cc.thevar.blukit.domain.model.MessagePayload
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.tasks.Tasks
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.KeyPairGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Supreme Senior Android Expert Implementation:
 * High-Density Mesh "Stadium Surge" Test.
 * Simulates a large-scale venue environment where multiple users are "blukitting" simultaneously.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestBlukitApplication::class)
class StadiumSurgeTest {

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
        
        // Mock default successful Nearby tasks
        every { connectionsClient.startDiscovery(any<String>(), any(), any()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.startAdvertising(any<String>(), any<String>(), any(), any()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.acceptConnection(any<String>(), any()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.sendPayload(any<String>(), any()) } returns Tasks.forResult<Void>(null)
        
        every { messageDao.getAllMessages() } returns flowOf(emptyList())
        coEvery { repository.getNickname() } returns "StadiumUser"
        every { repository.emojiAvatar } returns flowOf("⚽")
        every { repository.blockedUsers } returns flowOf(emptySet())

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
    fun `stadium surge simulation - multiple users blukitting in the mesh`() = runTest {
        // 1. Capture the payload callback to simulate incoming messages
        val payloadCallbackSlot = slot<PayloadCallback>()
        every { 
            connectionsClient.acceptConnection(any<String>(), capture(payloadCallbackSlot)) 
        } returns Tasks.forResult<Void>(null)

        // Force initialize the callback by initiating a connection
        val lifecycleCallbackSlot = slot<ConnectionLifecycleCallback>()
        every { 
            connectionsClient.startAdvertising(any<String>(), any<String>(), capture(lifecycleCallbackSlot), any<AdvertisingOptions>()) 
        } returns Tasks.forResult<Void>(null)
        
        controller.startAdvertising()
        testDispatcher.scheduler.advanceUntilIdle()
        val lifecycleCallback = lifecycleCallbackSlot.captured

        // 2. Setup mock encryption/decryption
        val dummyKey: SecretKey = SecretKeySpec(ByteArray(32), "AES")
        every { cryptoManager.deriveSharedSecret(any()) } returns dummyKey
        
        // 3. Define our "Stadium Stars" and their messages
        val stadiumScenarios = listOf(
            Pair("User1", "Team A wins!"),
            Pair("User2", "Team B wins!"),
            Pair("User3", "Player 1 scores!! 🔥")
        )

        // 4. Simulate the mesh activity
        stadiumScenarios.forEachIndexed { index, (user, content) ->
            val peerId = "peer-$index"
            
            // Connection Initiated
            lifecycleCallback.onConnectionInitiated(peerId, mockk(relaxed = true))
            testDispatcher.scheduler.advanceUntilIdle()

            // Handshake logic to enable session for this peer
            val handshakePayload = mockk<Payload>()
            val keyGen = KeyPairGenerator.getInstance("EC")
            keyGen.initialize(256)
            val publicKey = keyGen.generateKeyPair().public
            every { handshakePayload.asBytes() } returns byteArrayOf(0x01) + publicKey.encoded
            
            payloadCallbackSlot.captured.onPayloadReceived(peerId, handshakePayload)
            testDispatcher.scheduler.advanceUntilIdle()

            // Simulate the peer "Blukitting" their message
            val messagePayload = MessagePayload(
                messageId = "msg-$index",
                senderId = "id-$user",
                senderName = user,
                content = content,
                timestamp = System.currentTimeMillis()
            )
            val encryptedBytes = "encrypted-$content".toByteArray() 
            val messageMsgPayload = mockk<Payload>()
            every { messageMsgPayload.asBytes() } returns encryptedBytes
            
            // Tell CryptoManager how to decrypt this specific message
            every { cryptoManager.decrypt(encryptedBytes, dummyKey) } returns Json.encodeToString(MessagePayload.serializer(), messagePayload).toByteArray()

            // Peer sends the message into the mesh
            payloadCallbackSlot.captured.onPayloadReceived(peerId, messageMsgPayload)
        }

        // 5. Verify local node processed and PERSISTED every stadium message
        testDispatcher.scheduler.advanceUntilIdle()
        
        stadiumScenarios.forEach { (_, content) ->
            coVerify(atLeast = 1) { 
                messageDao.insertMessage(match { it.content == content }) 
            }
        }

        // 6. Verify Haptic Alerts triggered for every stadium shoutout
        verify(exactly = stadiumScenarios.size) { hapticManager.triggerMessageAlert() }
    }
}

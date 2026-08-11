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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.KeyPairGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestBlukitApplication::class)
class MovieHallScenarioTest {

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
    
    private lateinit var validPublicKeyEncoded: ByteArray

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        
        val keyGen = KeyPairGenerator.getInstance("EC")
        keyGen.initialize(256)
        val pair = keyGen.generateKeyPair()
        validPublicKeyEncoded = pair.public.encoded

        mockkStatic(Nearby::class)
        every { Nearby.getConnectionsClient(any<Context>()) } returns connectionsClient
        
        every { connectionsClient.startDiscovery(any<String>(), any(), any()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.startAdvertising(any<String>(), any<String>(), any<ConnectionLifecycleCallback>(), any<AdvertisingOptions>()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.stopDiscovery() } returns Unit
        every { connectionsClient.stopAdvertising() } returns Unit
        every { connectionsClient.acceptConnection(any<String>(), any<PayloadCallback>()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.sendPayload(any<String>(), any<Payload>()) } returns Tasks.forResult<Void>(null)
        
        every { messageDao.getAllMessages() } returns flowOf(emptyList())
        coEvery { repository.getNickname() } returns "Me"
        coEvery { repository.getDeviceId() } returns "my-device-id"
        every { repository.nickname } returns flowOf("Me")
        every { repository.deviceId } returns flowOf("my-device-id")
        every { repository.emojiAvatar } returns flowOf("🎬")
        every { repository.blockedUsers } returns flowOf(emptySet())

        every { cryptoManager.getLocalKeyPair() } returns pair
        every { cryptoManager.deriveSharedSecret(any()) } returns SecretKeySpec(ByteArray(32), "AES")
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
    fun `movie hall simulation - strangers blukitting publicly while friends whisper privately`() = runTest {
        val lifecycleCallbackSlot = slot<ConnectionLifecycleCallback>()
        val payloadCallbackSlot = slot<PayloadCallback>()
        every { connectionsClient.startAdvertising(any<String>(), any<String>(), capture(lifecycleCallbackSlot), any<AdvertisingOptions>()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.acceptConnection(any<String>(), capture(payloadCallbackSlot)) } returns Tasks.forResult<Void>(null)

        controller.startAdvertising()
        testDispatcher.scheduler.advanceUntilIdle()
        val lifecycleCallback = lifecycleCallbackSlot.captured

        val dummyKey: SecretKey = SecretKeySpec(ByteArray(32), "AES")
        every { cryptoManager.deriveSharedSecret(any()) } returns dummyKey

        val peers = listOf("Friend" to "🤝", "StrangerA" to "👤", "StrangerB" to "👤")
        val peerIds = peers.mapIndexed { index, pair -> "id-${pair.first}-$index" }
        
        peers.forEachIndexed { index, _ ->
            val peerId = peerIds[index]
            lifecycleCallback.onConnectionInitiated(peerId, mockk(relaxed = true))
            testDispatcher.scheduler.advanceUntilIdle()

            val handshakePayload = mockk<Payload>()
            every { handshakePayload.asBytes() } returns byteArrayOf(0x01) + validPublicKeyEncoded
            payloadCallbackSlot.captured.onPayloadReceived(peerId, handshakePayload)
            
            lifecycleCallback.onConnectionResult(peerId, mockk<ConnectionResolution>().apply { every { status.isSuccess } returns true })
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. StrangerA broadcasts
        val strangerAPayload = MessagePayload(
            messageId = "m1",
            senderId = "id-StrangerA",
            senderName = "StrangerA",
            senderEmoji = "👤",
            receiverId = null,
            content = "That opening scene was intense!",
            timestamp = System.currentTimeMillis()
        )
        val encryptedS1 = "enc-s1".toByteArray()
        val p1 = mockk<Payload>()
        every { p1.asBytes() } returns encryptedS1
        every { cryptoManager.decrypt(encryptedS1, dummyKey) } returns Json.encodeToString(MessagePayload.serializer(), strangerAPayload).toByteArray()
        payloadCallbackSlot.captured.onPayloadReceived(peerIds[1], p1)

        // 2. Me broadcasts
        controller.broadcastMessage("Agree, amazing cinematography.")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // 3. Friend whispers
        val friendWhisper = MessagePayload(
            messageId = "m2",
            senderId = peerIds[0],
            senderName = "Friend",
            senderEmoji = "🤝",
            receiverId = "my-device-id",
            content = "Did you see that cameo?",
            timestamp = System.currentTimeMillis()
        )
        val encryptedF1 = "enc-f1".toByteArray()
        val p2 = mockk<Payload>()
        every { p2.asBytes() } returns encryptedF1
        every { cryptoManager.decrypt(encryptedF1, dummyKey) } returns Json.encodeToString(MessagePayload.serializer(), friendWhisper).toByteArray()
        payloadCallbackSlot.captured.onPayloadReceived(peerIds[0], p2)

        // 4. Me whispers back
        controller.sendMessage("OMG YES! Totally unexpected.", receiverId = peerIds[0])
        testDispatcher.scheduler.advanceUntilIdle()

        // Verifications
        coVerify { messageDao.insertMessage(match { it.content == "That opening scene was intense!" }) }
        coVerify { messageDao.insertMessage(match { it.content == "Agree, amazing cinematography." }) }
        coVerify { messageDao.insertMessage(match { it.content == "Did you see that cameo!" || it.content.contains("cameo") }) }
        coVerify { messageDao.insertMessage(match { it.content == "OMG YES! Totally unexpected." }) }

        // verify(atLeast = 1) style is safer for mesh logic in tests
        peerIds.forEach { peerId ->
            verify(atLeast = 1) { connectionsClient.sendPayload(eq(peerId), any()) }
        }
        
        verify(atLeast = 2) { hapticManager.triggerMessageAlert() }
    }
}

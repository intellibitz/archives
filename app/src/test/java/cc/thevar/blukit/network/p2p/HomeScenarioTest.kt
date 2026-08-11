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
 * "Home Mesh" Scenario Test.
 * Simulates a family environment with transition from public Lobby broadcasts to private 1-on-1 Whispers.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestBlukitApplication::class)
class HomeScenarioTest {

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
        
        // Mock successful Nearby tasks
        every { connectionsClient.startDiscovery(any<String>(), any(), any()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.startAdvertising(any<String>(), any<String>(), any<ConnectionLifecycleCallback>(), any<AdvertisingOptions>()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.stopDiscovery() } returns Unit
        every { connectionsClient.stopAdvertising() } returns Unit
        every { connectionsClient.acceptConnection(any<String>(), any<PayloadCallback>()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.sendPayload(any<String>(), any<Payload>()) } returns Tasks.forResult<Void>(null)
        
        every { messageDao.getAllMessages() } returns flowOf(emptyList())
        every { repository.nickname } returns flowOf("Mom")
        every { repository.deviceId } returns flowOf("mom-device-id")
        every { repository.emojiAvatar } returns flowOf("👩")
        every { repository.blockedUsers } returns flowOf(emptySet())
        
        coEvery { repository.getNickname() } returns "Mom"
        coEvery { repository.getDeviceId() } returns "mom-device-id"

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
    fun `home mesh simulation - from dinner broadcast to private husband chat`() = runTest {
        // 1. Setup Capture for P2P interactions
        val lifecycleCallbackSlot = slot<ConnectionLifecycleCallback>()
        val payloadCallbackSlot = slot<PayloadCallback>()
        every { connectionsClient.startAdvertising(any<String>(), any<String>(), capture(lifecycleCallbackSlot), any<AdvertisingOptions>()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.acceptConnection(any<String>(), capture(payloadCallbackSlot)) } returns Tasks.forResult<Void>(null)

        controller.startAdvertising()
        testDispatcher.scheduler.advanceUntilIdle()
        val lifecycleCallback = lifecycleCallbackSlot.captured

        val dummyKey: SecretKey = SecretKeySpec(ByteArray(32), "AES")
        every { cryptoManager.deriveSharedSecret(any()) } returns dummyKey

        // 2. Connect Family Members
        val family = listOf("Son" to "🧒", "Daughter" to "👧", "Husband" to "🧔")
        val peerIds = family.mapIndexed { index, pair -> "id-${pair.first}-$index" }
        
        family.forEachIndexed { index, _ ->
            val peerId = peerIds[index]
            lifecycleCallback.onConnectionInitiated(peerId, mockk(relaxed = true))
            
            // Handshake
            val keyGen = KeyPairGenerator.getInstance("EC")
            keyGen.initialize(256)
            val handshakePayload = mockk<Payload>()
            every { handshakePayload.asBytes() } returns byteArrayOf(0x01) + keyGen.generateKeyPair().public.encoded
            payloadCallbackSlot.captured.onPayloadReceived(peerId, handshakePayload)
            
            lifecycleCallback.onConnectionResult(peerId, mockk<ConnectionResolution>().apply { every { status.isSuccess } returns true })
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // 3. Mom Broadcasts "dinner ready"
        controller.broadcastMessage("dinner ready")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verifying it was sent to all 3 currently connected peers
        peerIds.forEach { peerId ->
            verify(atLeast = 1) { connectionsClient.sendPayload(peerId, any()) }
        }
        coVerify { messageDao.insertMessage(match { it.content == "dinner ready" && it.receiverId == null }) }

        // 4. Family Broadcasts Back
        val responses = listOf(
            peerIds[0] to ("Son" to "one min mom"),
            peerIds[1] to ("Daughter" to "ready to eat chicken"),
            peerIds[2] to ("Husband" to "movie after dinner")
        )

        responses.forEach { (peerId, data) ->
            val (name, content) = data
            val payload = MessagePayload("msg-$peerId", "id-$name", name, null, content, System.currentTimeMillis())
            val encryptedBytes = "enc-$content".toByteArray()
            val msgPayload = mockk<Payload>()
            every { msgPayload.asBytes() } returns encryptedBytes
            every { cryptoManager.decrypt(encryptedBytes, dummyKey) } returns Json.encodeToString(MessagePayload.serializer(), payload).toByteArray()
            
            payloadCallbackSlot.captured.onPayloadReceived(peerId, msgPayload)
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // 5. Mom Selects Husband for 1-on-1 Chat: "yes love"
        val husbandId = peerIds[2]
        controller.sendMessage("yes love", receiverId = husbandId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify specifically sent to husband (this would be the 2nd sendPayload call to him: 1 broadcast + 1 whisper)
        verify(exactly = 2) { connectionsClient.sendPayload(eq(husbandId), any()) }
        coVerify { messageDao.insertMessage(match { it.content == "yes love" && it.receiverId == husbandId }) }

        // 6. Husband continues 1-on-1
        val whisperResponse = MessagePayload("whisper-1", "id-Husband", "Husband", "mom-device-id", "can't wait", System.currentTimeMillis())
        val encryptedWhisper = "enc-whisper".toByteArray()
        val whisperPayload = mockk<Payload>()
        every { whisperPayload.asBytes() } returns encryptedWhisper
        every { cryptoManager.decrypt(encryptedWhisper, dummyKey) } returns Json.encodeToString(MessagePayload.serializer(), whisperResponse).toByteArray()
        
        payloadCallbackSlot.captured.onPayloadReceived(husbandId, whisperPayload)
        testDispatcher.scheduler.advanceUntilIdle()

        // 7. Final Verification of the whole thread
        coVerify { messageDao.insertMessage(match { it.content == "can't wait" && it.receiverId == "mom-device-id" }) }
    }
}

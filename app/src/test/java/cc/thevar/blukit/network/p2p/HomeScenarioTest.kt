package cc.thevar.blukit.network.p2p

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import cc.thevar.blukit.TestBlukitApplication
import cc.thevar.blukit.data.local.VibeStore
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.KeyPairGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Integration test simulating the "Home Vibes" scenario.
 * Validates the transition from public broadcasts in 'The Vibes' to secure,
 * encrypted 1-on-1 'Ties' within a family context.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestBlukitApplication::class)
class HomeScenarioTest {

    private lateinit var context: Context
    private lateinit var repository: IdentityRepository
    private val contactRepository: ContactRepository = mockk(relaxed = true)
    private val vibeStore: VibeStore = mockk(relaxed = true)
    private val hapticManager: HapticManager = mockk(relaxed = true)
    private val radioStateManager: cc.thevar.blukit.data.system.RadioStateManager = mockk(relaxed = true)
    private val cryptoManager: CryptoManager = mockk(relaxed = true)
    private val connectionsClient: ConnectionsClient = mockk(relaxed = true)

    private lateinit var controller: NearbyP2PController
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        repository = mockk(relaxed = true)
        
        mockkStatic(Nearby::class)
        every { Nearby.getConnectionsClient(any<Context>()) } returns connectionsClient
        
        every { connectionsClient.startDiscovery(any<String>(), any(), any()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.startAdvertising(any<String>(), any<String>(), any<ConnectionLifecycleCallback>(), any<AdvertisingOptions>()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.stopDiscovery() } returns Unit
        every { connectionsClient.stopAdvertising() } returns Unit
        every { connectionsClient.acceptConnection(any<String>(), any<PayloadCallback>()) } returns Tasks.forResult<Void>(null)
        val mockTask = mockk<com.google.android.gms.tasks.Task<Void>>(relaxed = true)
        every { mockTask.addOnCompleteListener(any()) } answers {
            val listener = it.invocation.args[0] as com.google.android.gms.tasks.OnCompleteListener<Void>
            listener.onComplete(mockTask)
            mockTask
        }
        every { connectionsClient.sendPayload(any<String>(), any<Payload>()) } returns mockTask
        
        every { vibeStore.getAllMessages() } returns MutableStateFlow(emptyList())
        every { repository.nicknameFlow } returns MutableStateFlow("Mom")
        every { repository.emojiAvatar } returns MutableStateFlow("👩")
        every { repository.stealthMode } returns MutableStateFlow(false)
        every { repository.lowPowerMode } returns MutableStateFlow(false)
        every { repository.blockedUsers } returns MutableStateFlow(emptySet())
        every { repository.getCurrentNickname() } returns "Mom"
        every { repository.getDeviceId() } returns "mom-device-id"
        every { radioStateManager.radioStates } returns MutableStateFlow(cc.thevar.blukit.data.system.RadioStates(true, true, true))

        controller = NearbyP2PController(
            context, repository, contactRepository, vibeStore, hapticManager, radioStateManager, cryptoManager, testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Nearby::class)
    }

    @Test
    fun `home vibes simulation - from dinner broadcast to private husband tie`() = runTest(testDispatcher) {
        val lifecycleCallbackSlot = slot<ConnectionLifecycleCallback>()
        val payloadCallbackSlot = slot<PayloadCallback>()
        every { connectionsClient.startAdvertising(any<String>(), any<String>(), capture(lifecycleCallbackSlot), any<AdvertisingOptions>()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.acceptConnection(any<String>(), capture(payloadCallbackSlot)) } returns Tasks.forResult<Void>(null)

        controller.startAdvertising()
        advanceUntilIdle()
        val lifecycleCallback = lifecycleCallbackSlot.captured

        val dummyKey: SecretKey = SecretKeySpec(ByteArray(32), "AES")
        every { cryptoManager.deriveSharedSecret(any()) } returns dummyKey

        val family = listOf("Son" to "🧒", "Daughter" to "👧", "Husband" to "🧔")
        val peerIds = family.mapIndexed { index, pair -> "id-${pair.first}-$index" }
        
        family.forEachIndexed { index, pair ->
            val peerId = peerIds[index]
            lifecycleCallback.onConnectionInitiated(peerId, mockk(relaxed = true))
            advanceUntilIdle()
            
            val keyGen = KeyPairGenerator.getInstance("EC")
            keyGen.initialize(256)
            val handshakePayload = mockk<Payload>()
            every { handshakePayload.asBytes() } returns byteArrayOf(0x01) + keyGen.generateKeyPair().public.encoded
            payloadCallbackSlot.captured.onPayloadReceived(peerId, handshakePayload)
            advanceUntilIdle()
            
            lifecycleCallback.onConnectionResult(peerId, mockk<ConnectionResolution>().apply { every { status.isSuccess } returns true })
            advanceUntilIdle()

            // Link Setup: Explicitly accept the link
            controller.acceptLink(cc.thevar.blukit.domain.model.P2PDevice(peerId, pair.first, pair.second))
            advanceUntilIdle()
        }

        advanceUntilIdle()
        assertEquals(3, controller.connectedLinks.value.size)

        controller.broadcastMessage("dinner ready")
        advanceUntilIdle()
        
        peerIds.forEach { peerId ->
            verify(atLeast = 1, timeout = 2000) { connectionsClient.sendPayload(peerId, any()) }
        }
        coVerify { vibeStore.insertMessage(match { it.content == "dinner ready" && it.receiverId == null }) }

        val responses = listOf(
            peerIds[0] to ("Son" to "one min mom"),
            peerIds[1] to ("Daughter" to "ready to eat chicken"),
            peerIds[2] to ("Husband" to "movie after dinner")
        )

        responses.forEach { (peerId, data) ->
            val (name, content) = data
            val payload = MessagePayload(
                messageId = "msg-$peerId",
                senderId = "id-$name", senderName = name, senderEmoji = "🏠",
                receiverId = null, content = content, timestamp = System.currentTimeMillis()
            )
            val encryptedBytes = "enc-$content".toByteArray()
            val msgPayload = mockk<Payload>()
            every { msgPayload.asBytes() } returns encryptedBytes
            every { cryptoManager.decrypt(encryptedBytes, dummyKey) } returns Json.encodeToString(MessagePayload.serializer(), payload).toByteArray()
            
            payloadCallbackSlot.captured.onPayloadReceived(peerId, msgPayload)
            advanceUntilIdle()
        }

        val husbandId = peerIds[2]
        controller.sendMessage("yes love", receiverId = husbandId)
        advanceUntilIdle()
        
        verify(atLeast = 2, timeout = 2000) { connectionsClient.sendPayload(eq(husbandId), any()) }
        coVerify { vibeStore.insertMessage(match { it.content == "yes love" && it.receiverId == husbandId }) }

        val whisperResponse = MessagePayload(
            messageId = "whisper-1", senderId = "id-Husband", senderName = "Husband", senderEmoji = "🧔",
            receiverId = "mom-device-id", content = "can't wait", timestamp = System.currentTimeMillis()
        )
        val encryptedWhisper = "enc-whisper".toByteArray()
        val whisperPayload = mockk<Payload>()
        every { whisperPayload.asBytes() } returns encryptedWhisper
        every { cryptoManager.decrypt(encryptedWhisper, dummyKey) } returns Json.encodeToString(MessagePayload.serializer(), whisperResponse).toByteArray()
        
        payloadCallbackSlot.captured.onPayloadReceived(husbandId, whisperPayload)
        advanceUntilIdle()

        coVerify { vibeStore.insertMessage(match { it.content == "can't wait" && it.receiverId == "mom-device-id" }) }
    }
}

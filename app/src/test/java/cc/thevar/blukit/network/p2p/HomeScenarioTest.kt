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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
        every { mockTask.isComplete } returns true
        every { mockTask.isSuccessful } returns true
        every { mockTask.addOnCompleteListener(any()) } answers {
            val listener = it.invocation.args[0] as com.google.android.gms.tasks.OnCompleteListener<Void>
            listener.onComplete(mockTask)
            mockTask
        }
        every { connectionsClient.sendPayload(any<String>(), any<Payload>()) } returns mockTask
        
        every { vibeStore.getAllMessages() } returns MutableStateFlow(emptyList())
        every { vibeStore.groups } returns MutableStateFlow(emptyList())
        every { repository.nicknameFlow } returns MutableStateFlow("Mom")
        every { repository.emojiAvatar } returns MutableStateFlow("👩")
        every { repository.stealthMode } returns MutableStateFlow(false)
        every { repository.lowPowerMode } returns MutableStateFlow(false)
        every { repository.blockedUsers } returns MutableStateFlow(emptySet())
        every { repository.getCurrentNickname() } returns "Mom"
        every { repository.getDeviceId() } returns "mom-device-id"
        every { radioStateManager.radioStates } returns MutableStateFlow(cc.thevar.blukit.data.system.RadioStates(true, true, true))

        controller = NearbyP2PController(
            context, repository, contactRepository, vibeStore, hapticManager, radioStateManager, cryptoManager, testDispatcher, testDispatcher
        )
    }

    @After
    fun tearDown() {
        controller.release()
        Dispatchers.resetMain()
        unmockkStatic(Nearby::class)
        clearAllMocks()
    }

    @Test
    fun `home vibes simulation`() = runTest(testDispatcher) {
        val lifecycleCallbackSlot = slot<ConnectionLifecycleCallback>()
        val payloadCallbackSlot = slot<PayloadCallback>()
        every { connectionsClient.startAdvertising(any<String>(), any<String>(), capture(lifecycleCallbackSlot), any<AdvertisingOptions>()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.acceptConnection(any<String>(), capture(payloadCallbackSlot)) } returns Tasks.forResult<Void>(null)

        controller.startAdvertising()
        runCurrent()
        val lifecycleCallback = lifecycleCallbackSlot.captured

        val dummyKey: SecretKey = SecretKeySpec(ByteArray(32), "AES")
        every { cryptoManager.deriveSharedSecret(any()) } returns dummyKey

        val family = listOf("Son" to "🧒", "Daughter" to "👧") // Reduced family size for test stability
        val peerIds = family.mapIndexed { index, pair -> "id-${pair.first}-$index" }
        
        family.forEachIndexed { index, pair ->
            val peerId = peerIds[index]
            lifecycleCallback.onConnectionInitiated(peerId, mockk(relaxed = true))
            runCurrent()
            
            val keyGen = KeyPairGenerator.getInstance("EC")
            keyGen.initialize(256)
            val handshakePayload = mockk<Payload>()
            every { handshakePayload.type } returns Payload.Type.BYTES
            every { handshakePayload.asBytes() } returns byteArrayOf(0x01) + keyGen.generateKeyPair().public.encoded
            payloadCallbackSlot.captured.onPayloadReceived(peerId, handshakePayload)
            runCurrent()
            
            lifecycleCallback.onConnectionResult(peerId, mockk<ConnectionResolution>().apply { every { status.isSuccess } returns true })
            runCurrent()

            // Link Setup: Explicitly accept the link
            controller.acceptLink(cc.thevar.blukit.domain.model.P2PDevice(peerId, pair.first, pair.second))
            runCurrent()
        }

        assertEquals(2, controller.connectedLinks.value.size)

        controller.broadcastMessage("dinner ready")
        runCurrent()
        
        coVerify(atLeast = 1) { vibeStore.upsertMessage(any()) }

        val responses = listOf(
            peerIds[0] to ("Son" to "one min mom"),
            peerIds[1] to ("Daughter" to "ready to eat chicken")
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
            every { msgPayload.type } returns Payload.Type.BYTES
            every { msgPayload.asBytes() } returns encryptedBytes
            every { cryptoManager.decrypt(encryptedBytes, dummyKey) } returns Json.encodeToString(MessagePayload.serializer(), payload).toByteArray()
            
            payloadCallbackSlot.captured.onPayloadReceived(peerId, msgPayload)
            runCurrent()
        }

        coVerify(atLeast = 2) { vibeStore.upsertMessage(any()) }

        val sonId = peerIds[0]
        controller.sendMessage("yes son", receiverId = sonId)
        runCurrent()
        
        coVerify(atLeast = 3) { vibeStore.upsertMessage(any()) }
    }
}

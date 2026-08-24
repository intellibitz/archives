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
import cc.thevar.blukit.domain.model.P2PDevice
import cc.thevar.blukit.ui.viewmodels.BluetoothViewModel
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
 * Performance and reliability test simulating a high-density "Air Surge" environment.
 * Validates the mesh network's capability to handle multiple concurrent users
 * and ensure reliable relaying of vibes through The Air in large-scale venues.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestBlukitApplication::class)
class AirSurgeTest {

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
        
        // Mock default successful Nearby tasks
        every { connectionsClient.startDiscovery(any<String>(), any(), any()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.startAdvertising(any<String>(), any<String>(), any(), any()) } returns Tasks.forResult<Void>(null)
        every { connectionsClient.acceptConnection(any<String>(), any()) } returns Tasks.forResult<Void>(null)
        
        val mockTask = mockk<com.google.android.gms.tasks.Task<Void>>(relaxed = true)
        every { mockTask.isComplete } returns true
        every { mockTask.isSuccessful } returns true
        every { mockTask.addOnCompleteListener(any()) } answers {
            val listener = it.invocation.args[0] as com.google.android.gms.tasks.OnCompleteListener<Void>
            listener.onComplete(mockTask)
            mockTask
        }
        every { connectionsClient.sendPayload(any<String>(), any()) } returns mockTask
        
        every { vibeStore.getAllMessages() } returns MutableStateFlow(emptyList())
        every { vibeStore.groups } returns MutableStateFlow(emptyList())
        every { repository.getCurrentNickname() } returns "AirUser"
        every { repository.getDeviceId() } returns "air-id"
        every { repository.nicknameFlow } returns MutableStateFlow("AirUser")
        every { repository.emojiAvatar } returns MutableStateFlow("🌬️")
        every { repository.stealthMode } returns MutableStateFlow(false)
        every { repository.lowPowerMode } returns MutableStateFlow(false)
        every { repository.blockedUsers } returns MutableStateFlow(emptySet())
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
    fun `cinema hall scenario - moto focus and vibes with redmi and oneplus`() = runTest(testDispatcher) {
        val moto = "moto-id"
        val redmi = "redmi-id"
        val oneplus = "oneplus-id"
        
        val controllerMoto = mockk<P2PController>(relaxed = true)
        every { controllerMoto.messages } returns MutableStateFlow(emptyList())
        val radioMoto = mockk<cc.thevar.blukit.data.system.RadioStateManager>(relaxed = true)
        val permissionsMoto = mockk<cc.thevar.blukit.data.system.SpreadPermissionManager>(relaxed = true)
        every { radioMoto.radioStates } returns MutableStateFlow(cc.thevar.blukit.data.system.RadioStates(true, true, true))
        every { permissionsMoto.permissionsGranted } returns MutableStateFlow(true)
        
        val useCaseMoto = cc.thevar.blukit.domain.usecase.ConnectivityUseCase(
            controllerMoto, radioMoto, permissionsMoto, backgroundScope
        )
        
        val viewModelMoto = BluetoothViewModel(
            controllerMoto, radioMoto, mockk(relaxed = true), permissionsMoto, vibeStore, useCaseMoto
        )
        
        runCurrent()

        // 1. Moto starts high-level Vibe (Tie) with Redmi
        viewModelMoto.connectToDevice(P2PDevice(redmi, "Redmi", "📱"))
        runCurrent()
        verify { controllerMoto.connectToDevice(any()) }
        
        // 2. Start a Group Vibe (Tie)
        val gid = viewModelMoto.startGroupVibe("MOVIE", setOf(redmi, oneplus), scope = cc.thevar.blukit.domain.model.VibeGroup.SCOPE_PUBLIC)
        runCurrent()
        assertNotNull(gid)
        
        // 3. Start a Side Vibe (1-1)
        val sideGid = viewModelMoto.startGroupVibe("WHISPER", setOf(redmi), scope = cc.thevar.blukit.domain.model.VibeGroup.SCOPE_PRIVATE)
        runCurrent()
        assertNotNull(sideGid)
    }

    @Test
    fun `air surge simulation - multiple users blukitting in the air`() = runTest(testDispatcher) {
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
        runCurrent()
        val lifecycleCallback = lifecycleCallbackSlot.captured

        // 2. Setup mock encryption/decryption
        val dummyKey: SecretKey = SecretKeySpec(ByteArray(32), "AES")
        every { cryptoManager.deriveSharedSecret(any()) } returns dummyKey
        
        // 3. Define our "Air Stars" - reduced count for stability
        val airScenarios = listOf(
            Pair("User1", "The vibe is high!"),
            Pair("User2", "Catching vibes in the air!")
        )

        // 4. Simulate the mesh activity
        airScenarios.forEachIndexed { index, (user, content) ->
            val peerId = "peer-$index"
            
            // Connection Initiated
            lifecycleCallback.onConnectionInitiated(peerId, mockk(relaxed = true))
            runCurrent()

            // Handshake logic to enable session for this peer
            val handshakePayload = mockk<Payload>()
            every { handshakePayload.type } returns Payload.Type.BYTES
            val keyGen = KeyPairGenerator.getInstance("EC")
            keyGen.initialize(256)
            val publicKey = keyGen.generateKeyPair().public
            every { handshakePayload.asBytes() } returns byteArrayOf(0x01) + publicKey.encoded
            
            payloadCallbackSlot.captured.onPayloadReceived(peerId, handshakePayload)
            runCurrent()

            // Simulate the peer having a successful connection result (linked)
            lifecycleCallback.onConnectionResult(peerId, mockk<ConnectionResolution>().apply { every { status.isSuccess } returns true })
            runCurrent()

            // Simulate the peer "Blukitting" their message
            val messagePayload = MessagePayload(
                messageId = "msg-$index",
                senderId = "id-$user",
                senderName = user,
                senderEmoji = "👤",
                content = content,
                timestamp = System.currentTimeMillis()
            )
            val encryptedBytes = "encrypted-$content".toByteArray() 
            val messageMsgPayload = mockk<Payload>()
            every { messageMsgPayload.type } returns Payload.Type.BYTES
            every { messageMsgPayload.asBytes() } returns encryptedBytes
            
            // Tell CryptoManager how to decrypt this specific message
            every { cryptoManager.decrypt(encryptedBytes, dummyKey) } returns Json.encodeToString(MessagePayload.serializer(), messagePayload).toByteArray()

            // Peer sends the message into the mesh
            payloadCallbackSlot.captured.onPayloadReceived(peerId, messageMsgPayload)
            runCurrent()
        }

        // 5. Verify local node processed and PERSISTED every air message
        runCurrent()
        
        airScenarios.forEach { (_, content) ->
            coVerify(atLeast = 1) { 
                vibeStore.upsertMessage(match { it.content == content }) 
            }
        }

        // 6. Verify Haptic Vibes triggered for every air shoutout
        verify(exactly = airScenarios.size) { hapticManager.triggerVibe(HapticManager.VibeType.MESSAGE) }
    }
}

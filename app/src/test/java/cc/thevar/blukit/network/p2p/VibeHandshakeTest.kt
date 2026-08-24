package cc.thevar.blukit.network.p2p

import android.content.Context
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.domain.model.P2PDevice
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import javax.crypto.SecretKey

@OptIn(ExperimentalCoroutinesApi::class)
class VibeHandshakeTest {

    private val context: Context = mockk(relaxed = true)
    private val repository: IdentityRepository = mockk(relaxed = true)
    private val contactRepository: ContactRepository = mockk(relaxed = true)
    private val vibeStore: VibeStore = mockk(relaxed = true)
    private val hapticManager: HapticManager = mockk(relaxed = true)
    private val radioStateManager: RadioStateManager = mockk(relaxed = true)
    private val cryptoManager: CryptoManager = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var controller: NearbyP2PController
    private val connectionsClient: ConnectionsClient = mockk(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(com.google.android.gms.nearby.Nearby::class)
        every { com.google.android.gms.nearby.Nearby.getConnectionsClient(any<Context>()) } returns connectionsClient
        
        every { repository.getDeviceId() } returns "my-device-id"
        every { repository.getCurrentNickname() } returns "Me"
        every { repository.emojiAvatar } returns MutableStateFlow("👤")
        every { repository.lowPowerMode } returns MutableStateFlow(false)
        every { vibeStore.getAllMessages() } returns MutableStateFlow(emptyList())
        every { radioStateManager.radioStates } returns MutableStateFlow(cc.thevar.blukit.data.system.RadioStates(true, true, true))

        val keyPair = KeyPairGenerator.getInstance("EC").generateKeyPair()
        every { cryptoManager.getLocalKeyPair() } returns keyPair

        every { connectionsClient.acceptConnection(any<String>(), any()) } returns Tasks.forResult<Void>(null)
        
        every { connectionsClient.sendPayload(any<String>(), any()) } answers {
            val mockTask = mockk<com.google.android.gms.tasks.Task<Void>>(relaxed = true)
            every { mockTask.addOnCompleteListener(any()) } answers {
                val listener = it.invocation.args[0] as com.google.android.gms.tasks.OnCompleteListener<Void>
                listener.onComplete(mockTask)
                mockTask
            }
            mockTask
        }

        controller = NearbyP2PController(
            context, repository, contactRepository, vibeStore, hapticManager, radioStateManager, cryptoManager, testDispatcher, testDispatcher
        )
    }

    @Test
    fun `handshake derived shared secret on connection`() = runTest(testDispatcher) {
        val endpointId = "remote-peer-1"
        val peerKeyPair = KeyPairGenerator.getInstance("EC").generateKeyPair()
        val handshakePayload = byteArrayOf(0x01.toByte()) + peerKeyPair.public.encoded

        // Capture the LifecycleCallback to simulate Nearby events
        val callbackSlot = slot<ConnectionLifecycleCallback>()
        val mockTask = mockk<Task<Void>>(relaxed = true)
        every { connectionsClient.requestConnection(any<String>(), any<String>(), capture(callbackSlot)) } returns mockTask

        controller.connectToDevice(P2PDevice(endpointId, "Friend", "🤝"))
        runCurrent()
        
        // Simulate Connection Initiated
        callbackSlot.captured.onConnectionInitiated(endpointId, mockk(relaxed = true))
        runCurrent()
        
        // Simulate Connection Success
        callbackSlot.captured.onConnectionResult(endpointId, mockk {
            every { status } returns com.google.android.gms.common.api.Status.RESULT_SUCCESS
        })
        runCurrent()

        // Capture the PayloadCallback
        val payloadCallbackSlot = slot<PayloadCallback>()
        verify { connectionsClient.acceptConnection(endpointId, capture(payloadCallbackSlot)) }

        // Simulate receiving Handshake from peer
        payloadCallbackSlot.captured.onPayloadReceived(endpointId, Payload.fromBytes(handshakePayload))
        runCurrent()

        // Verify shared secret derivation
        verify { cryptoManager.deriveSharedSecret(any()) }
    }

    @Test
    fun `link request is sent if connected`() = runTest(testDispatcher) {
        val endpointId = "peer-1"
        
        // We need to simulate that Nearby is already connected for the ID
        // The controller's internal state must know it's connected to send.
        // We'll simulate the connection cycle first.
        val callbackSlot = slot<ConnectionLifecycleCallback>()
        val mockTask = mockk<Task<Void>>(relaxed = true)
        every { connectionsClient.requestConnection(any<String>(), any<String>(), capture(callbackSlot)) } returns mockTask

        controller.connectToDevice(P2PDevice(endpointId, "Friend", "🤝"))
        runCurrent()

        callbackSlot.captured.onConnectionResult(endpointId, mockk {
            every { status } returns com.google.android.gms.common.api.Status.RESULT_SUCCESS
        })
        runCurrent()

        controller.requestLink(P2PDevice(endpointId, "Friend", "🤝"))
        runCurrent()

        coVerify(atLeast = 1) { connectionsClient.sendPayload(endpointId, any()) }
    }
}

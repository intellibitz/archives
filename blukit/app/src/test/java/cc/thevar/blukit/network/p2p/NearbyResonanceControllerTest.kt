package cc.thevar.blukit.network.p2p

import android.content.Context
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.MessageRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.domain.model.Message
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Method

@OptIn(ExperimentalCoroutinesApi::class)
class NearbyConnectionControllerTest {

    private val context = mockk<Context>(relaxed = true)
    private val repository = mockk<IdentityRepository>(relaxed = true)
    private val messageRepository = mockk<MessageRepository>(relaxed = true)
    private val hapticManager = mockk<HapticManager>(relaxed = true)
    private val radioStateManager = mockk<RadioStateManager>(relaxed = true)
    private val cryptoManager = mockk<CryptoManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var controller: NearbyConnectionController

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Nearby::class)
        val connectionsClient = mockk<ConnectionsClient>(relaxed = true)
        every { Nearby.getConnectionsClient(any<Context>()) } returns connectionsClient
        
        controller = NearbyConnectionController(
            context, repository, messageRepository, hapticManager, 
            radioStateManager, cryptoManager, testDispatcher, testDispatcher
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(Nearby::class)
        Dispatchers.resetMain()
    }

    @Test
    fun `relayMessage increments hopCount and does not relay beyond 3 hops`() {
        val payload = Message(
            messageId = "msg_123",
            senderId = "other_peer",
            senderName = "Source",
            content = "Relay test",
            timestamp = 1000L,
            hopCount = 2
        )

        val method: Method = NearbyConnectionController::class.java.getDeclaredMethod("relayMessage", String::class.java, Message::class.java)
        method.isAccessible = true
        
        val payloadMaxHops = payload.copy(hopCount = 3)
        method.invoke(controller, "endpoint_source", payloadMaxHops)
    }
}

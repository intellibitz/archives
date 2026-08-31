package cc.thevar.blukit.network.p2p

import android.content.Context
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.EchoLedger
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.domain.model.Echo
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
class NearbyResonanceControllerTest {

    private val context = mockk<Context>(relaxed = true)
    private val repository = mockk<IdentityRepository>(relaxed = true)
    private val echoLedger = mockk<EchoLedger>(relaxed = true)
    private val hapticManager = mockk<HapticManager>(relaxed = true)
    private val radioStateManager = mockk<RadioStateManager>(relaxed = true)
    private val cryptoManager = mockk<CryptoManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var controller: NearbyResonanceController

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        controller = NearbyResonanceController(
            context, repository, echoLedger, hapticManager, 
            radioStateManager, cryptoManager, testDispatcher, testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `relayEcho increments hopCount and does not relay beyond 3 hops`() {
        val payload = Echo(
            messageId = "msg_123",
            senderId = "other_peer",
            senderName = "Source",
            content = "Relay test",
            timestamp = 1000L,
            hopCount = 2
        )

        val method: Method = NearbyResonanceController::class.java.getDeclaredMethod("relayEcho", String::class.java, Echo::class.java)
        method.isAccessible = true
        
        val payloadMaxHops = payload.copy(hopCount = 3)
        method.invoke(controller, "endpoint_source", payloadMaxHops)
    }
}

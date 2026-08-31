package cc.thevar.blukit.data.local

import android.content.Context
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.domain.model.Echo
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EchoLedgerTest {

    private val context = mockk<Context>(relaxed = true)
    private val cryptoManager = mockk<CryptoManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var echoLedger: EchoLedger

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        echoLedger = EchoLedger(context, cryptoManager, testDispatcher)
    }

    @Test
    fun `upsertEcho resolves record updates using LWW-CRDT`() {
        val recordId = "task_123"
        val initialEcho = Echo(
            messageId = recordId,
            senderId = "user1",
            senderName = "User 1",
            content = "Initial Task",
            timestamp = 1000L,
            type = Echo.TYPE_ASSIGNMENT_TASK,
            noteVersion = 1,
            taskStatus = Echo.TASK_PENDING
        )

        echoLedger.upsertEcho(initialEcho)
        assertEquals(1, echoLedger.echoes.value.size)
        assertEquals(Echo.TASK_PENDING, echoLedger.echoes.value.first().taskStatus)

        // Concurrent update from another Source with higher version
        val updatedEcho = initialEcho.copy(
            content = "Updated Task",
            timestamp = 1100L,
            noteVersion = 2,
            taskStatus = Echo.TASK_COMPLETED
        )

        echoLedger.upsertEcho(updatedEcho)
        assertEquals(1, echoLedger.echoes.value.size)
        assertEquals("Updated Task", echoLedger.echoes.value.first().content)
        assertEquals(Echo.TASK_COMPLETED, echoLedger.echoes.value.first().taskStatus)

        // Outdated update should be ignored
        val outdatedEcho = initialEcho.copy(
            content = "Old Task",
            timestamp = 900L,
            noteVersion = 1,
            taskStatus = Echo.TASK_PENDING
        )

        echoLedger.upsertEcho(outdatedEcho)
        assertEquals(1, echoLedger.echoes.value.size)
        assertEquals("Updated Task", echoLedger.echoes.value.first().content)
    }

    @Test
    fun `upsertEcho sorts echoes by timestamp`() {
        val p1 = createEcho("First", 1000L)
        val p2 = createEcho("Second", 500L)
        val p3 = createEcho("Third", 1500L)

        echoLedger.upsertEcho(p1)
        echoLedger.upsertEcho(p2)
        echoLedger.upsertEcho(p3)

        val echoes = echoLedger.echoes.value
        assertEquals(3, echoes.size)
        assertEquals("Second", echoes[0].content)
        assertEquals("First", echoes[1].content)
        assertEquals("Third", echoes[2].content)
    }

    private fun createEcho(content: String, timestamp: Long) = Echo(
        messageId = java.util.UUID.randomUUID().toString(),
        senderId = "sender",
        senderName = "Sender",
        content = content,
        timestamp = timestamp
    )
}

package cc.thevar.blukit.data.local

import android.content.Context
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.domain.model.MessagePayload
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PulseStoreTest {

    private val context = mockk<Context>(relaxed = true)
    private val cryptoManager = mockk<CryptoManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var pulseStore: PulseStore

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        pulseStore = PulseStore(context, cryptoManager, testDispatcher)
    }

    @Test
    fun `upsertMessage resolves assignment updates using LWW-CRDT`() {
        val assignmentId = "task_123"
        val initialPulse = MessagePayload(
            messageId = assignmentId,
            senderId = "user1",
            senderName = "User 1",
            content = "Initial Task",
            timestamp = 1000L,
            type = MessagePayload.TYPE_ASSIGNMENT_TASK,
            noteVersion = 1,
            taskStatus = MessagePayload.TASK_PENDING
        )

        pulseStore.upsertMessage(initialPulse)
        assertEquals(1, pulseStore.messages.value.size)
        assertEquals(MessagePayload.TASK_PENDING, pulseStore.messages.value.first().taskStatus)

        // Concurrent update from another peer with higher version
        val updatedPulse = initialPulse.copy(
            content = "Updated Task",
            timestamp = 1100L,
            noteVersion = 2,
            taskStatus = MessagePayload.TASK_COMPLETED
        )

        pulseStore.upsertMessage(updatedPulse)
        assertEquals(1, pulseStore.messages.value.size)
        assertEquals("Updated Task", pulseStore.messages.value.first().content)
        assertEquals(MessagePayload.TASK_COMPLETED, pulseStore.messages.value.first().taskStatus)

        // Outdated update should be ignored
        val outdatedPulse = initialPulse.copy(
            content = "Old Task",
            timestamp = 900L,
            noteVersion = 1,
            taskStatus = MessagePayload.TASK_PENDING
        )

        pulseStore.upsertMessage(outdatedPulse)
        assertEquals(1, pulseStore.messages.value.size)
        assertEquals("Updated Task", pulseStore.messages.value.first().content)
    }

    @Test
    fun `upsertMessage sorts pulses by timestamp`() {
        val p1 = createPulse("First", 1000L)
        val p2 = createPulse("Second", 500L)
        val p3 = createPulse("Third", 1500L)

        pulseStore.upsertMessage(p1)
        pulseStore.upsertMessage(p2)
        pulseStore.upsertMessage(p3)

        val messages = pulseStore.messages.value
        assertEquals(3, messages.size)
        assertEquals("Second", messages[0].content)
        assertEquals("First", messages[1].content)
        assertEquals("Third", messages[2].content)
    }

    private fun createPulse(content: String, timestamp: Long) = MessagePayload(
        messageId = java.util.UUID.randomUUID().toString(),
        senderId = "sender",
        senderName = "Sender",
        content = content,
        timestamp = timestamp
    )
}

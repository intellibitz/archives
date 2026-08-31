package cc.thevar.blukit.data.local

import android.content.Context
import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.domain.model.Message
import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MessageRepositoryTest {

    private val context = mockk<Context>(relaxed = true)
    private val cryptoManager = mockk<CryptoManager>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var messageRepository: MessageRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val filesDir = File("build/tmp/test").apply { mkdirs() }
        every { context.filesDir } returns filesDir
        messageRepository = MessageRepository(context, cryptoManager, testDispatcher)
    }

    @Test
    fun `upsertMessage resolves record updates using LWW-CRDT`() {
        val recordId = "task_123"
        val initialMessage = Message(
            messageId = recordId,
            senderId = "user1",
            senderName = "User 1",
            content = "Initial Task",
            timestamp = 1000L,
            type = Message.TYPE_ASSIGNMENT_TASK,
            noteVersion = 1,
            taskStatus = Message.TASK_PENDING
        )

        messageRepository.upsertMessage(initialMessage)
        assertEquals(1, messageRepository.echoes.value.size)
        assertEquals(Message.TASK_PENDING, messageRepository.echoes.value.first().taskStatus)

        // Concurrent update from another Source with higher version
        val updatedMessage = initialMessage.copy(
            content = "Updated Task",
            timestamp = 1100L,
            noteVersion = 2,
            taskStatus = Message.TASK_COMPLETED
        )

        messageRepository.upsertMessage(updatedMessage)
        assertEquals(1, messageRepository.echoes.value.size)
        assertEquals("Updated Task", messageRepository.echoes.value.first().content)
        assertEquals(Message.TASK_COMPLETED, messageRepository.echoes.value.first().taskStatus)

        // Outdated update should be ignored
        val outdatedMessage = initialMessage.copy(
            content = "Old Task",
            timestamp = 900L,
            noteVersion = 1,
            taskStatus = Message.TASK_PENDING
        )

        messageRepository.upsertMessage(outdatedMessage)
        assertEquals(1, messageRepository.echoes.value.size)
        assertEquals("Updated Task", messageRepository.echoes.value.first().content)
    }

    @Test
    fun `upsertMessage sorts echoes by timestamp`() {
        val p1 = createMessage("First", 1000L)
        val p2 = createMessage("Second", 500L)
        val p3 = createMessage("Third", 1500L)

        messageRepository.upsertMessage(p1)
        messageRepository.upsertMessage(p2)
        messageRepository.upsertMessage(p3)

        val echoes = messageRepository.echoes.value
        assertEquals(3, echoes.size)
        assertEquals("Second", echoes[0].content)
        assertEquals("First", echoes[1].content)
        assertEquals("Third", echoes[2].content)
    }

    private fun createMessage(content: String, timestamp: Long) = Message(
        messageId = java.util.UUID.randomUUID().toString(),
        senderId = "sender",
        senderName = "Sender",
        content = content,
        timestamp = timestamp
    )
}

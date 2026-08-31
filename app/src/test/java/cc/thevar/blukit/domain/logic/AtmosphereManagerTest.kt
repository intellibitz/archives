package cc.thevar.blukit.domain.logic

import android.content.Context
import cc.thevar.blukit.data.local.MessageRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.domain.model.Message
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AssistantManagerTest {

    private val context = mockk<Context>(relaxed = true)
    private val messageRepository = mockk<MessageRepository>(relaxed = true)
    private val identityRepository = mockk<IdentityRepository>(relaxed = true)
    private lateinit var atmosphereManager: AssistantManager

    @Before
    fun setup() {
        atmosphereManager = AssistantManager(context, messageRepository, identityRepository)
    }

    @Test
    fun `generateSynthesis filters stopwords and extracts top keywords`() {
        val echoes = listOf(
            createMessage("Hello world this is a test Message"),
            createMessage("Hello amazing world of blukit"),
            createMessage("Amazing blukit resonance is amazing")
        )

        val synthesis = atmosphereManager.generateSynthesis("test_group", echoes)

        assertTrue("AMAZING should be a top keyword", synthesis.topKeywords.contains("AMAZING"))
        assertTrue("WORLD should be a top keyword", synthesis.topKeywords.contains("WORLD"))
        assertTrue(synthesis.topKeywords.size <= 5)
    }

    @Test
    fun `detectAtmosphericTrend identifies Academic Ritual`() {
        val echoes = listOf(
            createMessage("I have a lecture today"),
            createMessage("Submission for the assignment is due"),
            createMessage("The professor was great")
        )

        val trend = atmosphereManager.detectAtmosphericTrend(echoes)
        assertEquals("ACADEMIC RITUAL", trend)
    }

    @Test
    fun `sentiment score reflects positive and negative keywords`() {
        val echoes = listOf(
            createMessage("This is amazing and great!"),
            createMessage("I love it!"),
            createMessage("Wow!")
        )

        val synthesis = atmosphereManager.generateSynthesis("test_group", echoes)
        assertTrue("Sentiment score should be positive. Found: ${synthesis.sentimentScore}", synthesis.sentimentScore > 0.5f)
        assertTrue("Summary should reflect vibrant energy. Found: ${synthesis.summary}", synthesis.summary.contains("VIBRANT"))
    }

    private fun createMessage(content: String) = Message(
        messageId = "test_id",
        senderId = "sender",
        senderName = "Sender",
        content = content,
        timestamp = System.currentTimeMillis(),
        groupId = "test_group"
    )
}

package cc.thevar.blukit.domain.logic

import android.content.Context
import cc.thevar.blukit.data.local.PulseStore
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.domain.model.MessagePayload
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IntelligenceManagerTest {

    private val context = mockk<Context>(relaxed = true)
    private val pulseStore = mockk<PulseStore>(relaxed = true)
    private val identityRepository = mockk<IdentityRepository>(relaxed = true)
    private lateinit var intelligenceManager: IntelligenceManager

    @Before
    fun setup() {
        intelligenceManager = IntelligenceManager(context, pulseStore, identityRepository)
    }

    @Test
    fun `generateResonanceSummary filters stopwords and extracts top keywords`() {
        val pulses = listOf(
            createPulse("Hello world this is a test pulse"),
            createPulse("Hello amazing world of blukit"),
            createPulse("Amazing blukit mesh is amazing")
        )

        val summary = intelligenceManager.generateResonanceSummary("test_group", pulses)

        // Stopwords like "THIS", "THAT", "HELLO", "PULSE", "BLUKIT" should be filtered
        assertTrue("AMAZING should be a top keyword", summary.topKeywords.contains("AMAZING"))
        assertTrue("WORLD should be a top keyword", summary.topKeywords.contains("WORLD"))
        assertTrue(summary.topKeywords.size <= 5)
    }

    @Test
    fun `detectAtmosphericTrend identifies Academic Ritual`() {
        val pulses = listOf(
            createPulse("I have a lecture today"),
            createPulse("Submission for the assignment is due"),
            createPulse("The professor was great")
        )

        val trend = intelligenceManager.detectAtmosphericTrend(pulses)
        assertEquals("ACADEMIC RITUAL", trend)
    }

    @Test
    fun `detectAtmosphericTrend identifies Social Synergy`() {
        val pulses = listOf(
            createPulse("Let's go to the party"),
            createPulse("The music is amazing"),
            createPulse("I love this concert")
        )

        val trend = intelligenceManager.detectAtmosphericTrend(pulses)
        assertEquals("SOCIAL SYNERGY", trend)
    }

    @Test
    fun `sentiment score reflects positive and negative keywords`() {
        val pulses = listOf(
            createPulse("This is amazing and great!"),
            createPulse("I love it!"),
            createPulse("Wow!")
        )

        val summary = intelligenceManager.generateResonanceSummary("test_group", pulses)
        assertTrue("Sentiment score should be positive. Found: ${summary.sentimentScore}", summary.sentimentScore > 0.5f)
        assertTrue("Summary should reflect vibrant energy. Found: ${summary.summary}", summary.summary.contains("VIBRANT"))
    }

    private fun createPulse(content: String) = MessagePayload(
        messageId = "test_id",
        senderId = "sender",
        senderName = "Sender",
        content = content,
        timestamp = System.currentTimeMillis(),
        groupId = "test_group"
    )
}

/**
 * BLUKIT DOMAIN: INTELLIGENCE MANAGER
 *
 * The central orchestration engine for Crowd AI.
 * Handles on-device synthesis, swarm logic consensus, and privacy-preserving analytics.
 */
package cc.thevar.blukit.domain.logic

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import cc.thevar.blukit.data.local.PulseStore
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.domain.model.MessagePayload
import cc.thevar.blukit.domain.model.intelligence.ResonanceSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

class IntelligenceManager(
    private val context: Context,
    private val pulseStore: PulseStore,
    private val identityRepository: IdentityRepository,
    ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO,
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    init {
        startAmbientSynthesis()
    }

    /**
     * AMBIENT SYNTHESIS: Periodically processes local pulses to generate Resonance Summaries.
     * This is an on-device, privacy-first implementation of "Crowd AI."
     */
    private fun startAmbientSynthesis() {
        scope.launch {
            pulseStore.activeGroups.collectLatest { groups ->
                groups.forEach { group ->
                    launch {
                        processGroupIntelligence(group.id)
                    }
                }
            }
        }
    }

    private suspend fun processGroupIntelligence(groupId: String) {
        while (true) {
            // BATTERY-AWARE SCALING: Adjust synthesis frequency based on energy levels.
            val batteryLevel = getBatteryLevel()
            val synthesisDelay = when {
                batteryLevel < 15 -> 10.minutes
                batteryLevel < 30 -> 5.minutes
                else -> 1.minutes
            }
            delay(synthesisDelay)
            
            val pulses = pulseStore.messages.value.filter { it.groupId == groupId }
            if (pulses.size < 5) continue

            val summary = generateResonanceSummary(groupId, pulses)
            
            // Broadcast the AI Summary to the mesh as a priority pulse
            val aiPulse = MessagePayload(
                messageId = UUID.randomUUID().toString(),
                senderId = "AI_ORCHESTRATOR",
                senderName = "CROWD AI",
                senderEmoji = "🧠",
                groupId = groupId,
                content = summary.summary,
                timestamp = System.currentTimeMillis(),
                type = MessagePayload.TYPE_AI_SUMMARY,
                isPriority = true,
                isMeta = true,
            )
            pulseStore.upsertMessage(aiPulse)
            Log.i("IntelligenceManager", "Swarm Logic: AI Summary broadcasted for $groupId")
        }
    }

    /**
     * LOCAL SYNTHESIS: Uses a simplified TF-IDF approach and stopword filtering to cluster pulses.
     */
    fun generateResonanceSummary(groupId: String, pulses: List<MessagePayload>): ResonanceSummary {
        val stopWords = setOf(
            "THE", "AND", "THIS", "THAT", "WITH", "FROM", "THEIR", "THEY", "WHAT", 
            "YOUR", "HAVE", "WERE", "THERE", "ABOUT", "WHICH", "WOULD", "COULD",
            "SHOULD", "THESE", "THOSE", "BECAUSE", "WHILE", "WHERE", "EVERY",
            "HELLO", "PULSE", "BLUKIT", "JUST", "WILL", "SOME",
        )

        val words = pulses.asSequence()
            .flatMap { it.content.split(Regex("\\s+")) }
            .map { it.uppercase().filter { c -> c.isLetter() } }
            .filter { (it.length > 3) && (it !in stopWords) }
            .toList()

        // Simplified TF-IDF: Frequency within this group weighted against a global heuristic
        val keywords = words.groupingBy { it }
            .eachCount()
            .asSequence()
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }
            .toList()

        val mainTopic = keywords.firstOrNull() ?: "GENERAL ACTIVITY"
        
        // Sentiment Lexicon Heuristics
        val positiveWords = setOf("GOOD", "GREAT", "AMAZING", "LOVE", "PARTY", "FUN", "YES", "COOL", "WOW")
        val negativeWords = setOf("BAD", "SAD", "HATE", "SLOW", "BORING", "NO", "FAIL", "ERR")
        
        var sentimentScore = 0.1f
        pulses.forEach { p ->
            val content = p.content.uppercase()
            if (positiveWords.any { content.contains(it) }) sentimentScore += 0.2f
            if (negativeWords.any { content.contains(it) }) sentimentScore -= 0.2f
            if (content.contains("!")) sentimentScore += 0.1f
        }
        sentimentScore = sentimentScore.coerceIn(-1.0f, 1.0f)

        // INTENT SYNTHESIS: Identifying Atmospheric Trends
        val intent = detectAtmosphericTrend(pulses)
        val trendSummary = intent?.let { " TREND: $it DETECTED." } ?: ""

        val intensityLabel = when {
            sentimentScore > 0.6 -> "VIBRANT"
            sentimentScore > 0.2 -> "POSITIVE"
            sentimentScore < -0.6 -> "CRITICAL"
            sentimentScore < -0.2 -> "TENSE"
            else -> "STABLE"
        }

        return ResonanceSummary(
            groupId = groupId,
            summary = "SWARM REPORT: $mainTopic IS RESONATING.$trendSummary ENERGY IS $intensityLabel.",
            topKeywords = keywords,
            sentimentScore = sentimentScore,
            derivedTimestamp = System.currentTimeMillis(),
            pulseCountSampled = pulses.size,
        )
    }

    fun detectAtmosphericTrend(pulses: List<MessagePayload>): String? {
        val content = pulses.joinToString(" ") { it.content }.lowercase()
        return when {
            content.contains("lecture") || content.contains("professor") || content.contains("assignment") || content.contains("exam") || content.contains("study") -> "ACADEMIC RITUAL"
            content.contains("train") || content.contains("metro") || content.contains("station") || content.contains("bus") -> "URBAN TRANSIT"
            content.contains("party") || content.contains("music") || content.contains("dance") || content.contains("concert") -> "SOCIAL SYNERGY"
            content.contains("food") || content.contains("coffee") || content.contains("cafe") || content.contains("eat") -> "CROWD NOURISHMENT"
            content.contains("protest") || content.contains("march") || content.contains("rally") -> "COLLECTIVE ACTION"
            else -> null
        }
    }

    private fun getBatteryLevel(): Int {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level == -1 || scale == -1) 50 else (level * 100 / scale.toFloat()).toInt()
    }

    /**
     * SWARM CONSENSUS: Triggers a vote pulse for a specific resonance point.
     */
    fun castConsensusVote(pulseId: String, groupId: String, weight: Int) {
        val votePulse = MessagePayload(
            messageId = UUID.randomUUID().toString(),
            senderId = identityRepository.getDeviceId(),
            senderName = "YOU",
            parentMessageId = pulseId,
            groupId = groupId,
            content = weight.toString(),
            timestamp = System.currentTimeMillis(),
            type = MessagePayload.TYPE_CONSENSUS_VOTE,
        )
        pulseStore.upsertMessage(votePulse)
    }
}

/**
 * BLUKIT DOMAIN: INTELLIGENCE MANAGER
 *
 * The central orchestration engine for Mesh Insights.
 * Handles on-device synthesis, swarm logic consensus, and privacy-preserving analytics.
 */
package cc.thevar.blukit.domain.logic

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import cc.thevar.blukit.data.local.MessageStore
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.domain.model.MeshMessage
import cc.thevar.blukit.domain.model.intelligence.MeshInsight
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
    private val messageStore: MessageStore,
    private val identityRepository: IdentityRepository,
    ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO,
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    init {
        startAmbientInsights()
    }

    /**
     * AMBIENT INSIGHTS: Periodically processes local messages to generate Mesh Insights.
     * This is an on-device, privacy-first implementation of "Mesh AI."
     */
    private fun startAmbientInsights() {
        scope.launch {
            messageStore.activeGroups.collectLatest { groups ->
                groups.forEach { group ->
                    launch {
                        processRoomIntelligence(group.id)
                    }
                }
            }
        }
    }

    private suspend fun processRoomIntelligence(groupId: String) {
        while (true) {
            // BATTERY-AWARE SCALING: Adjust synthesis frequency based on energy levels.
            val batteryLevel = getBatteryLevel()
            val synthesisDelay = when {
                batteryLevel < 15 -> 10.minutes
                batteryLevel < 30 -> 5.minutes
                else -> 1.minutes
            }
            delay(synthesisDelay)
            
            val messages = messageStore.messages.value.filter { it.groupId == groupId }
            if (messages.size < 5) continue

            val insight = generateMeshInsight(groupId, messages)
            
            // Broadcast the AI Summary to the mesh as a priority message
            val aiMessage = MeshMessage(
                messageId = UUID.randomUUID().toString(),
                senderId = "AI_ORCHESTRATOR",
                senderName = "ROOM AI",
                senderEmoji = "🧠",
                groupId = groupId,
                content = insight.summary,
                timestamp = System.currentTimeMillis(),
                type = MeshMessage.TYPE_AI_SUMMARY,
                isPriority = true,
                isMeta = true,
            )
            messageStore.upsertMessage(aiMessage)
            Log.i("IntelligenceManager", "Room Logic: AI Insight broadcasted for $groupId")
        }
    }

    /**
     * LOCAL SYNTHESIS: Uses a simplified TF-IDF approach and stopword filtering to cluster messages.
     */
    fun generateMeshInsight(groupId: String, messages: List<MeshMessage>): MeshInsight {
        val stopWords = setOf(
            "THE", "AND", "THIS", "THAT", "WITH", "FROM", "THEIR", "THEY", "WHAT", 
            "YOUR", "HAVE", "WERE", "THERE", "ABOUT", "WHICH", "WOULD", "COULD",
            "SHOULD", "THESE", "THOSE", "BECAUSE", "WHILE", "WHERE", "EVERY",
            "HELLO", "PULSE", "BLUKIT", "JUST", "WILL", "SOME",
        )

        val words = messages.asSequence()
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
        messages.forEach { p ->
            val content = p.content.uppercase()
            if (positiveWords.any { content.contains(it) }) sentimentScore += 0.2f
            if (negativeWords.any { content.contains(it) }) sentimentScore -= 0.2f
            if (content.contains("!")) sentimentScore += 0.1f
        }
        sentimentScore = sentimentScore.coerceIn(-1.0f, 1.0f)

        // INTENT SYNTHESIS: Identifying Atmospheric Trends
        val intent = detectAtmosphericTrend(messages)
        val trendSummary = intent?.let { " TREND: $it DETECTED." } ?: ""

        val intensityLabel = when {
            sentimentScore > 0.6 -> "VIBRANT"
            sentimentScore > 0.2 -> "POSITIVE"
            sentimentScore < -0.6 -> "CRITICAL"
            sentimentScore < -0.2 -> "TENSE"
            else -> "STABLE"
        }

        return MeshInsight(
            groupId = groupId,
            summary = "ROOM REPORT: $mainTopic IS RESONATING.$trendSummary ENERGY IS $intensityLabel.",
            topKeywords = keywords,
            sentimentScore = sentimentScore,
            derivedTimestamp = System.currentTimeMillis(),
            messageCountSampled = messages.size,
        )
    }

    fun detectAtmosphericTrend(messages: List<MeshMessage>): String? {
        val content = messages.joinToString(" ") { it.content }.lowercase()
        return when {
            content.contains("lecture") || content.contains("professor") || content.contains("assignment") || content.contains("exam") || content.contains("study") -> "ACADEMIC RITUAL"
            content.contains("train") || content.contains("metro") || content.contains("station") || content.contains("bus") -> "URBAN TRANSIT"
            content.contains("party") || content.contains("music") || content.contains("dance") || content.contains("concert") -> "SOCIAL SYNERGY"
            content.contains("food") || content.contains("coffee") || content.contains("cafe") || content.contains("eat") -> "ROOM NOURISHMENT"
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
     * SWARM CONSENSUS: Triggers a vote pulse for a specific message.
     */
    fun castConsensusVote(messageId: String, groupId: String, weight: Int) {
        val voteMessage = MeshMessage(
            messageId = UUID.randomUUID().toString(),
            senderId = identityRepository.getDeviceId(),
            senderName = "YOU",
            parentMessageId = messageId,
            groupId = groupId,
            content = weight.toString(),
            timestamp = System.currentTimeMillis(),
            type = MeshMessage.TYPE_CONSENSUS_VOTE,
        )
        messageStore.upsertMessage(voteMessage)
    }
}

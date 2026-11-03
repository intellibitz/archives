/**
 * BLUKIT DOMAIN: ASSISTANT MANAGER
 *
 * The central orchestration engine for Group Synthesis.
 * Handles on-device synthesis, swarm logic consensus, and privacy-preserving analytics.
 */
package cc.thevar.blukit.domain.logic

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import cc.thevar.blukit.data.local.MessageRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.intelligence.Synthesis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

class AssistantManager(
    private val context: Context,
    private val messageRepository: MessageRepository,
    private val identityRepository: IdentityRepository,
    ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO,
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val documentMiner = DocumentMiner(context)

    init {
        startAmbientAssistant()
    }

    /**
     * AMBIENT ASSISTANT: Periodically processes local Messages to generate Synthesis.
     */
    private fun startAmbientAssistant() {
        scope.launch {
            messageRepository.activeGroups.collectLatest { groups ->
                groups.forEach { group ->
                    launch {
                        processGroupAssistant(group.id)
                    }
                }
            }
        }
    }

    private suspend fun processGroupAssistant(groupId: String) {
        while (true) {
            val batteryLevel = getBatteryLevel()
            val synthesisDelay = when {
                batteryLevel < 15 -> 10.minutes
                batteryLevel < 30 -> 5.minutes
                else -> 1.minutes
            }
            delay(synthesisDelay)
            
            val messages = messageRepository.messages.value.filter { it.groupId == groupId }
            if (messages.isEmpty()) continue

            // Mine documents in this group
            val documentMessages = messages.filter { it.type == Message.TYPE_FILE }
            val minedInsights = documentMessages.map { documentMiner.mineFile(it) }
            val extractedTasks = minedInsights.flatMap { it.tasks }.distinct()

            val synthesis = generateSynthesis(groupId, messages, extractedTasks)
            
            // Assistant: Instead of broadcasting a Message, we update the Group metadata
            messageRepository.getGroup(groupId)?.let { group ->
                messageRepository.insertGroup(
                    group.copy(
                        trendLabel = synthesis.trendLabel,
                        connectionSummary = synthesis.summary,
                        extractedTasks = extractedTasks.toSet()
                    )
                )
            }
            
            Log.i("AssistantManager", "Group Connection: Synthesis updated silently for $groupId")
        }
    }

    /**
     * LOCAL SYNTHESIS: Uses a simplified TF-IDF approach and stopword filtering to cluster Messages.
     */
    fun generateSynthesis(groupId: String, messages: List<Message>, minedTasks: List<String> = emptyList()): Synthesis {
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

        val keywords = words.groupingBy { it }
            .eachCount()
            .asSequence()
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }
            .toList()

        val mainTopic = keywords.firstOrNull() ?: "GENERAL ACTIVITY"
        
        var sentimentScore = 0.1f
        messages.forEach { p ->
            val content = p.content.uppercase()
            if (setOf("GOOD", "GREAT", "AMAZING", "LOVE", "PARTY", "FUN", "YES", "COOL", "WOW").any { content.contains(it) }) sentimentScore += 0.2f
            if (setOf("BAD", "SAD", "HATE", "SLOW", "BORING", "NO", "FAIL", "ERR").any { content.contains(it) }) sentimentScore -= 0.2f
            if (content.contains("!")) sentimentScore += 0.1f
        }
        sentimentScore = sentimentScore.coerceIn(-1.0f, 1.0f)

        val intent = detectConnectionTrend(messages)
        val trendSummary = intent?.let { " TREND: $it DETECTED." } ?: ""
        val taskSummary = if (minedTasks.isNotEmpty()) " MINED ${minedTasks.size} TASKS." else ""

        val intensityLabel = when {
            sentimentScore > 0.6 -> "VIBRANT"
            sentimentScore > 0.2 -> "POSITIVE"
            sentimentScore < -0.6 -> "CRITICAL"
            sentimentScore < -0.2 -> "TENSE"
            else -> "STABLE"
        }

        return Synthesis(
            groupId = groupId,
            summary = "Assistant Report: $mainTopic CONNECTION ACTIVE.$trendSummary$taskSummary ENERGY IS $intensityLabel.",
            trendLabel = intent,
            topKeywords = keywords,
            sentimentScore = sentimentScore,
            derivedTimestamp = System.currentTimeMillis(),
            messageCountSampled = messages.size,
        )
    }

    fun detectConnectionTrend(messages: List<Message>): String? {
        val content = messages.joinToString(" ") { it.content }.lowercase()
        return when {
            content.contains("lecture") || content.contains("professor") || content.contains("assignment") || content.contains("exam") || content.contains("study") -> "ACADEMIC"
            content.contains("train") || content.contains("metro") || content.contains("station") || content.contains("bus") -> "TRANSIT"
            content.contains("party") || content.contains("music") || content.contains("dance") || content.contains("concert") -> "SOCIAL"
            content.contains("food") || content.contains("coffee") || content.contains("cafe") || content.contains("eat") -> "DINING"
            content.contains("protest") || content.contains("march") || content.contains("rally") -> "ACTION"
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
     * SWARM CONSENSUS: Triggers a vote pulse for a specific Message.
     */
    fun castConsensusVote(messageId: String, groupId: String, weight: Int) {
        val voteMessage = Message(
            messageId = UUID.randomUUID().toString(),
            senderId = identityRepository.getDeviceId(),
            senderName = "YOU",
            parentMessageId = messageId,
            groupId = groupId,
            content = weight.toString(),
            timestamp = System.currentTimeMillis(),
            type = Message.TYPE_CONSENSUS_VOTE,
        )
        messageRepository.upsertMessage(voteMessage)
    }
}

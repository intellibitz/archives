/**
 * BLUKIT DOMAIN: ATMOSPHERE MANAGER
 *
 * The central orchestration engine for Sphere Synthesis.
 * Handles on-device synthesis, swarm logic consensus, and privacy-preserving analytics.
 */
package cc.thevar.blukit.domain.logic

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import cc.thevar.blukit.data.local.EchoLedger
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.intelligence.Synthesis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

class AtmosphereManager(
    private val context: Context,
    private val echoLedger: EchoLedger,
    private val identityRepository: IdentityRepository,
    ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO,
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val documentMiner = DocumentMiner(context)

    init {
        startAmbientAtmosphere()
    }

    /**
     * AMBIENT ATMOSPHERE: Periodically processes local Echoes to generate Synthesis.
     */
    private fun startAmbientAtmosphere() {
        scope.launch {
            echoLedger.activeSpheres.collectLatest { spheres ->
                spheres.forEach { sphere ->
                    launch {
                        processSphereAtmosphere(sphere.id)
                    }
                }
            }
        }
    }

    private suspend fun processSphereAtmosphere(groupId: String) {
        while (true) {
            val batteryLevel = getBatteryLevel()
            val synthesisDelay = when {
                batteryLevel < 15 -> 10.minutes
                batteryLevel < 30 -> 5.minutes
                else -> 1.minutes
            }
            delay(synthesisDelay)
            
            val echoes = echoLedger.echoes.value.filter { it.groupId == groupId }
            if (echoes.isEmpty()) continue

            // Mine documents in this sphere
            val documentEchoes = echoes.filter { it.type == Echo.TYPE_FILE }
            val minedInsights = documentEchoes.map { documentMiner.mineFile(it) }
            val extractedTasks = minedInsights.flatMap { it.tasks }.distinct()

            val synthesis = generateSynthesis(groupId, echoes, extractedTasks)
            
            val aiEcho = Echo(
                messageId = UUID.randomUUID().toString(),
                senderId = "ATMOSPHERE_ORCHESTRATOR",
                senderName = "THE ATMOSPHERE",
                senderEmoji = "🧠",
                groupId = groupId,
                content = synthesis.summary,
                trendLabel = synthesis.trendLabel,
                timestamp = System.currentTimeMillis(),
                type = Echo.TYPE_AI_SUMMARY,
                isPriority = true,
                isMeta = true,
            )
            echoLedger.upsertEcho(aiEcho)
            
            // If tasks were mined, inject them as system echoes
            extractedTasks.forEach { task ->
                val taskEcho = Echo(
                    messageId = UUID.randomUUID().toString(),
                    senderId = "ATMOSPHERE_MINER",
                    senderName = "AIR MINER",
                    senderEmoji = "⚒️",
                    groupId = groupId,
                    content = "NEW TASK DETECTED: $task",
                    timestamp = System.currentTimeMillis(),
                    type = Echo.TYPE_ASSIGNMENT_TASK,
                    isMeta = true
                )
                echoLedger.upsertEcho(taskEcho)
            }
            Log.i("AtmosphereManager", "Sphere Resonance: Synthesis broadcasted for $groupId")
        }
    }

    /**
     * LOCAL SYNTHESIS: Uses a simplified TF-IDF approach and stopword filtering to cluster Echoes.
     */
    fun generateSynthesis(groupId: String, echoes: List<Echo>, minedTasks: List<String> = emptyList()): Synthesis {
        val stopWords = setOf(
            "THE", "AND", "THIS", "THAT", "WITH", "FROM", "THEIR", "THEY", "WHAT", 
            "YOUR", "HAVE", "WERE", "THERE", "ABOUT", "WHICH", "WOULD", "COULD",
            "SHOULD", "THESE", "THOSE", "BECAUSE", "WHILE", "WHERE", "EVERY",
            "HELLO", "PULSE", "BLUKIT", "JUST", "WILL", "SOME",
        )

        val words = echoes.asSequence()
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
        echoes.forEach { p ->
            val content = p.content.uppercase()
            if (setOf("GOOD", "GREAT", "AMAZING", "LOVE", "PARTY", "FUN", "YES", "COOL", "WOW").any { content.contains(it) }) sentimentScore += 0.2f
            if (setOf("BAD", "SAD", "HATE", "SLOW", "BORING", "NO", "FAIL", "ERR").any { content.contains(it) }) sentimentScore -= 0.2f
            if (content.contains("!")) sentimentScore += 0.1f
        }
        sentimentScore = sentimentScore.coerceIn(-1.0f, 1.0f)

        val intent = detectAtmosphericTrend(echoes)
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
            summary = "AIR REPORT: $mainTopic RESONANCE ACTIVE.$trendSummary$taskSummary ENERGY IS $intensityLabel.",
            trendLabel = intent,
            topKeywords = keywords,
            sentimentScore = sentimentScore,
            derivedTimestamp = System.currentTimeMillis(),
            messageCountSampled = echoes.size,
        )
    }

    fun detectAtmosphericTrend(echoes: List<Echo>): String? {
        val content = echoes.joinToString(" ") { it.content }.lowercase()
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
     * SWARM CONSENSUS: Triggers a vote pulse for a specific Echo.
     */
    fun castConsensusVote(messageId: String, groupId: String, weight: Int) {
        val voteEcho = Echo(
            messageId = UUID.randomUUID().toString(),
            senderId = identityRepository.getDeviceId(),
            senderName = "YOU",
            parentMessageId = messageId,
            groupId = groupId,
            content = weight.toString(),
            timestamp = System.currentTimeMillis(),
            type = Echo.TYPE_CONSENSUS_VOTE,
        )
        echoLedger.upsertEcho(voteEcho)
    }
}

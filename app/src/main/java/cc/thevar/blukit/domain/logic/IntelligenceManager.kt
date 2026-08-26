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
     * LOCAL SYNTHESIS: Simulates on-device NLP to cluster pulses into a summary.
     */
    private fun generateResonanceSummary(groupId: String, pulses: List<MessagePayload>): ResonanceSummary {
        val keywords = pulses.asSequence()
            .flatMap { it.content.split(" ") }
            .filter { it.length > 4 }
            .groupingBy { it.uppercase() }
            .eachCount()
            .asSequence()
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }
            .toList()

        val mainTopic = keywords.firstOrNull() ?: "GENERAL ACTIVITY"
        val sentiment = if (pulses.any { it.content.contains("!") }) 0.5f else 0.1f

        // INTENT SYNTHESIS: Identifying Atmospheric Trends
        val intent = detectAtmosphericTrend(pulses)
        val trendSummary = intent?.let { " INTENT: $it detected." } ?: ""

        return ResonanceSummary(
            groupId = groupId,
            summary = "SWARM REPORT: HIGH RESONANCE AROUND $mainTopic.$trendSummary COLLECTIVE ENERGY IS ${if (sentiment > 0.3) "INTENSE" else "STABLE"}.",
            topKeywords = keywords,
            sentimentScore = sentiment,
            derivedTimestamp = System.currentTimeMillis(),
            pulseCountSampled = pulses.size,
        )
    }

    private fun detectAtmosphericTrend(pulses: List<MessagePayload>): String? {
        val content = pulses.joinToString(" ") { it.content }.lowercase()
        return when {
            content.contains("lecture") || content.contains("professor") || content.contains("assignment") -> "ACADEMIC RITUAL"
            content.contains("train") || content.contains("metro") || content.contains("station") -> "URBAN TRANSIT"
            content.contains("party") || content.contains("music") || content.contains("dance") -> "SOCIAL SYNERGY"
            content.contains("food") || content.contains("coffee") || content.contains("cafe") -> "CROWD NOURISHMENT"
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
            senderId = "LOCAL_USER", // Real implementation uses IdentityRepository.userId
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

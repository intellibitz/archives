package cc.thevar.blukit.data.power

import cc.thevar.blukit.data.local.dao.MessageDao
import cc.thevar.blukit.domain.power.SupremePowerReport
import cc.thevar.blukit.network.p2p.P2PController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * The Supreme Power: Background Intelligence Service.
 * Monitors mesh health, crowd density, and provides AI-driven insights.
 */
class SupremePowerManager(
    private val p2pController: P2PController,
    private val messageDao: MessageDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _report = MutableStateFlow(SupremePowerReport())
    val report: StateFlow<SupremePowerReport> = _report.asStateFlow()

    init {
        startIntelligenceGathering()
    }

    private fun startIntelligenceGathering() {
        scope.launch {
            combine(
                p2pController.scannedDevices,
                p2pController.connectedPeers,
                messageDao.getAllMessages()
            ) { scanned, connected, messages ->
                val userCount = scanned.size
                val peerCount = connected.size
                val msgCount = messages.size
                
                // Logic for Mesh Health
                val health = if (userCount > 0) {
                    min(1.0f, (peerCount.toFloat() / userCount.toFloat()) + 0.2f)
                } else 0f

                // Logic for Traffic Density
                val density = when {
                    userCount > 50 -> "CRITICAL MASS"
                    userCount > 20 -> "HIGH DENSITY"
                    userCount > 5 -> "ACTIVE"
                    userCount > 0 -> "SPARSE"
                    else -> "IDLE"
                }

                // AI Insight Generation (Heuristic-based)
                val insight = generateAiInsight(userCount, peerCount, msgCount, health)

                SupremePowerReport(
                    userCount = userCount,
                    connectedPeerCount = peerCount,
                    totalMessages = msgCount,
                    meshHealth = health,
                    trafficDensity = density,
                    aiInsight = insight,
                    signalStability = if (health > 0.7f) "STABLE" else "FLUCTUATING"
                )
            }.collect {
                _report.value = it
            }
        }
    }

    private fun generateAiInsight(users: Int, peers: Int, msgs: Int, health: Float): String {
        return when {
            users == 0 -> "Listening for nearby vibes..."
            health < 0.3f -> "The connection is faint. Vibes may take time to travel."
            users > 10 && health > 0.8f -> "The atmosphere is vibrant. Everyone is connected."
            peers == 0 && users > 0 -> "Vibes are near. Form a tie to bridge the distance."
            msgs > 100 -> "The air is full of life. Our invisible web is strong."
            else -> "The air is steady. Watching for new arrivals."
        }
    }
}

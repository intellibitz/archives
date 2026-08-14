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
import kotlin.time.Duration.Companion.seconds

/**
 * The Supreme Power: Intelligence Service.
 * Monitors vibes, ties, and provides human-centric insights.
 */
class SupremePowerManager(
    private val p2pController: P2PController,
    private val messageDao: MessageDao,
    private val hapticManager: cc.thevar.blukit.data.system.HapticManager? = null
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _report = MutableStateFlow(SupremePowerReport())
    val report: StateFlow<SupremePowerReport> = _report.asStateFlow()

    private val _breezeFlow = MutableSharedFlow<String>(replay = 1)

    init {
        startIntelligenceGathering()
    }

    private fun startIntelligenceGathering() {
        scope.launch {
            combine(
                p2pController.scannedDevices,
                p2pController.connectedTies,
                messageDao.getAllMessages(),
                _breezeFlow.onStart { emit("") }
            ) { args ->
                val scanned = args[0] as List<*>
                val connected = args[1] as Set<*>
                val messages = args[2] as List<*>
                
                val userCount = scanned.size
                val tiesCount = connected.size
                val msgCount = messages.size
                
                // Logic for Vibe Harmony
                val vibeHarmony = if (userCount > 0) {
                    min(1.0f, (tiesCount.toFloat() / userCount.toFloat()) + 0.2f)
                } else 0f

                // AI Insight Generation (Heuristic-based)
                val insight = generateAiInsight(userCount, tiesCount, msgCount, vibeHarmony)
                val breeze = args.getOrNull(3) as? String

                SupremePowerReport(
                    userCount = userCount,
                    connectedTiesCount = tiesCount,
                    totalMessages = msgCount,
                    harmony = vibeHarmony,
                    aiInsight = insight,
                    currentBreeze = breeze,
                )
            }.collect {
                _report.value = it
            }
        }

        observeEventsForBreezes()
    }

    private fun observeEventsForBreezes() {
        // Vibe Detected
        p2pController.scannedDevices
            .map { it.size }
            .distinctUntilChanged()
            .scan(0 to 0) { acc, new -> acc.second to new }
            .onEach { (old, new) ->
                if (new > old) emitBreeze("A new vibe joined The Vibes")
            }.launchIn(scope)

        // Tie Formed
        p2pController.connectedTies
            .map { it.size }
            .distinctUntilChanged()
            .scan(0 to 0) { acc, new -> acc.second to new }
            .onEach { (old, new) ->
                if (new > old) emitBreeze("A new tie has been formed")
            }.launchIn(scope)

        // Messages Relayed
        p2pController.messages
            .onEach { msgs ->
                if (msgs.isNotEmpty()) {
                    val last = msgs.last()
                    if (System.currentTimeMillis() - last.timestamp < 1000) {
                        emitBreeze("Vibe relayed through the web")
                    }
                }
            }.launchIn(scope)
    }

    private suspend fun emitBreeze(text: String) {
        _breezeFlow.emit(text)
        hapticManager?.triggerVibe(cc.thevar.blukit.data.system.HapticManager.VibeType.CONNECTION)
        delay(5.seconds)
        if (_breezeFlow.replayCache.firstOrNull() == text) {
            _breezeFlow.emit("")
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

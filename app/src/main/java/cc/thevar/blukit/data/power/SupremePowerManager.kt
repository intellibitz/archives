package cc.thevar.blukit.data.power

import cc.thevar.blukit.data.local.VibeStore
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
    private val vibeStore: VibeStore,
    private val identityRepository: cc.thevar.blukit.data.repository.IdentityRepository,
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
                p2pController.connectedLinks,
                vibeStore.getAllMessages(),
                identityRepository.lowPowerMode,
                _breezeFlow.onStart { emit("") }
            ) { args: Array<Any?> ->
                val scanned = args[0] as List<cc.thevar.blukit.domain.model.P2PDevice>
                val connected = args[1] as Set<String>
                val messages = args[2] as List<cc.thevar.blukit.domain.model.MessagePayload>
                val lowPower = args[3] as Boolean
                
                val userCount = scanned.size
                val linksCount = connected.size
                val msgCount = messages.size
                
                // Logic for Vibe Harmony
                val vibeHarmony = if (userCount > 0) {
                    min(1.0f, (linksCount.toFloat() / userCount.toFloat()) + 0.2f)
                } else 0f

                // AI Insight Generation (Heuristic-based)
                val insight = generateAiInsight(userCount, linksCount, msgCount, vibeHarmony, lowPower)
                val breeze = args.getOrNull(4) as? String

                SupremePowerReport(
                    userCount = userCount,
                    connectedLinksCount = linksCount,
                    totalMessages = msgCount,
                    harmony = vibeHarmony,
                    aiInsight = insight,
                    currentBreeze = breeze,
                    lowPowerMode = lowPower
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
                if (new > old) emitBreeze("VIBE PROXIMITY")
            }.launchIn(scope)

        // Link Formed
        p2pController.connectedLinks
            .map { it.size }
            .distinctUntilChanged()
            .scan(0 to 0) { acc, new -> acc.second to new }
            .onEach { (old, new) ->
                if (new > old) emitBreeze("PEOPLE ENERGY")
            }.launchIn(scope)

        // Messages Relayed
        p2pController.messages
            .onEach { msgs ->
                if (msgs.isNotEmpty()) {
                    val last = msgs.last()
                    if (System.currentTimeMillis() - last.timestamp < 1000) {
                        emitBreeze("VIBE SPREAD")
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

    private fun generateAiInsight(users: Int, links: Int, msgs: Int, harmony: Float, lowPower: Boolean): String {
        if (lowPower) return "ENERGY SAVER ACTIVE"
        
        return when {
            users == 0 -> "MAKE PEOPLE VIBE"
            users > 15 -> "VIBE RESONANCE: MESH DENSE"
            harmony < 0.3f -> "BLUKIT NEARBY: SPREAD VIBES"
            users > 10 && harmony > 0.8f -> "VIBE RESONANCE"
            links == 0 && users > 0 -> "CROWD ENERGY"
            msgs > 100 -> "VIBE FLOW"
            else -> "MAKE PEOPLE VIBE"
        }
    }
}

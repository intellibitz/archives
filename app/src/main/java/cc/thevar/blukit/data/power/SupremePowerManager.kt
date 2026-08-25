package cc.thevar.blukit.data.power

import cc.thevar.blukit.data.local.PulseStore
import cc.thevar.blukit.domain.power.SupremePowerReport
import cc.thevar.blukit.network.p2p.P2PController
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

/**
 * The Supreme Power: Intelligence Service.
 * Monitors vibes, chains, and provides human-centric insights.
 */
class SupremePowerManager(
    private val p2pController: P2PController,
    private val pulseStore: PulseStore,
    private val identityRepository: cc.thevar.blukit.data.repository.IdentityRepository,
    private val hapticManager: cc.thevar.blukit.data.system.HapticManager? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _report = MutableStateFlow(SupremePowerReport())
    val report: StateFlow<SupremePowerReport> = _report.asStateFlow()

    private val _breezeFlow = MutableSharedFlow<String>(replay = 1)
    private val _lastLocation = MutableStateFlow<android.location.Location?>(null)

    // Geofencing coordinates (Campus landmarks)
    private val landmarks = mapOf(
        "AIR HUB" to (12.9716 to 77.5946),
        "LIBRARY" to (12.9724 to 77.5937)
    )

    init {
        startIntelligenceGathering()
    }

    fun updateLocation(location: android.location.Location) {
        _lastLocation.value = location
    }

    private fun startIntelligenceGathering() {
        scope.launch {
            combine(
                p2pController.scannedDevices,
                p2pController.connectedTies,
                pulseStore.getAllMessages(),
                identityRepository.lowPowerMode,
                _breezeFlow.onStart { emit("") },
                _lastLocation
            ) { args: Array<Any?> ->
                val scanned = args[0] as List<cc.thevar.blukit.domain.model.P2PDevice>
                val connected = args[1] as Set<String>
                val messages = args[2] as List<cc.thevar.blukit.domain.model.MessagePayload>
                val lowPower = args[3] as Boolean
                
                val userCount = scanned.size
                val radioCount = connected.size
                val msgCount = messages.size
                
                // Logic for Pulse Harmony
                val pulseHarmony = if (userCount > 0) {
                    min(1.0f, (radioCount.toFloat() / userCount.toFloat()) + 0.2f)
                } else 0f

                // AI Insight Generation (Heuristic-based)
                val insight = generateAiInsight(userCount, radioCount, msgCount, pulseHarmony, lowPower)
                val breeze = args.getOrNull(4) as? String
                val location = args.getOrNull(5) as? android.location.Location

                val suggestions = if (location != null) {
                    landmarks.filter { (_, coords) ->
                        val results = FloatArray(1)
                        android.location.Location.distanceBetween(location.latitude, location.longitude, coords.first, coords.second, results)
                        results[0] < 500 // 500 meters
                    }.keys.toList()
                } else emptyList()

                val locationLabel = suggestions.firstOrNull() ?: if (location != null) "NEARBY" else null

                SupremePowerReport(
                    userCount = userCount,
                    connectedTiesCount = radioCount,
                    totalMessages = msgCount,
                    harmony = pulseHarmony,
                    aiInsight = insight,
                    currentBreeze = breeze,
                    lowPowerMode = lowPower,
                    suggestedAirs = suggestions,
                    lastLocation = locationLabel
                )
            }.collect {
                _report.value = it
            }
        }

        observeEventsForBreezes()
    }

    private fun observeEventsForBreezes() {
        // Pulse Detected
        p2pController.scannedDevices
            .map { it.size }
            .distinctUntilChanged()
            .scan(0 to 0) { acc, new -> acc.second to new }
            .onEach { (old, new) ->
                if (new > old) emitBreeze("PULSE PROXIMITY")
            }.launchIn(scope)

        // Radio Formed
        p2pController.connectedTies
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
                        emitBreeze("PULSE SPREAD")
                    }
                }
            }.launchIn(scope)
    }

    private suspend fun emitBreeze(text: String) {
        _breezeFlow.emit(text)
        hapticManager?.triggerPulse(cc.thevar.blukit.data.system.HapticManager.PulseType.CONNECTION)
        delay(5.seconds)
        if (_breezeFlow.replayCache.firstOrNull() == text) {
            _breezeFlow.emit("")
        }
    }

    private fun generateAiInsight(users: Int, radios: Int, msgs: Int, harmony: Float, lowPower: Boolean): String {
        if (lowPower) return "ENERGY SAVER ACTIVE"
        
        return when {
            users == 0 -> "MAKE PEOPLE PULSE"
            users > 15 -> "PULSE PULSE: MESH DENSE"
            harmony < 0.3f -> "BLUKIT NEARBY: SPREAD PULSES"
            users > 10 && harmony > 0.8f -> "PULSE PULSE"
            radios == 0 && users > 0 -> "CROWD ENERGY"
            msgs > 100 -> "PULSE FLOW"
            else -> "MAKE PEOPLE PULSE"
        }
    }
}

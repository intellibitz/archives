package cc.thevar.blukit.data.power

import cc.thevar.blukit.data.local.MessageStore
import cc.thevar.blukit.domain.power.SupremePowerReport
import cc.thevar.blukit.network.p2p.P2PController
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

/**
 * The Supreme Power: Intelligence Service.
 * Monitors rooms, groups, and provides human-centric insights.
 */
class SupremePowerManager(
    private val p2pController: P2PController,
    private val messageStore: MessageStore,
    private val identityRepository: cc.thevar.blukit.data.repository.IdentityRepository,
    private val hapticManager: cc.thevar.blukit.data.system.HapticManager? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _report = MutableStateFlow(SupremePowerReport())
    val report: StateFlow<SupremePowerReport> = _report.asStateFlow()

    private val _breezeFlow = MutableSharedFlow<String>(replay = 1)
    private val _lastLocation = MutableStateFlow<android.location.Location?>(null)

    // Geofencing coordinates (Campus landmarks)
    private val landmarks = mapOf(
        "HOME HUB" to (12.9716 to 77.5946),
        "LIBRARY" to (12.9724 to 77.5937),
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
                p2pController.connectedGroups,
                messageStore.messages,
                identityRepository.lowPowerMode,
                _breezeFlow.onStart { emit("") },
                _lastLocation
            ) { args: Array<Any?> ->
                @Suppress("UNCHECKED_CAST")
                val scanned = args[0] as List<cc.thevar.blukit.domain.model.P2PDevice>
                @Suppress("UNCHECKED_CAST")
                val connected = args[1] as Set<String>
                @Suppress("UNCHECKED_CAST")
                val messages = args[2] as List<cc.thevar.blukit.domain.model.MeshMessage>
                val lowPower = args[3] as Boolean
                
                val userCount = scanned.size
                val radioCount = connected.size
                val msgCount = messages.size
                
                // Logic for Mesh Harmony
                val meshHarmony = if (userCount > 0) {
                    min(1.0f, (radioCount.toFloat() / userCount.toFloat()) + 0.2f)
                } else 0f

                // AI Insight Generation (Heuristic-based)
                val insight = generateAiInsight(userCount, radioCount, msgCount, meshHarmony, lowPower)
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
                    harmony = meshHarmony,
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
        // Person Detected
        p2pController.scannedDevices
            .map { it.size }
            .distinctUntilChanged()
            .scan(0 to 0) { acc, new -> acc.second to new }
            .onEach { (old, new) ->
                if (new > old) emitBreeze("PEOPLE PROXIMITY")
            }.launchIn(scope)

        // Radio Formed
        p2pController.connectedGroups
            .map { it.size }
            .distinctUntilChanged()
            .scan(0 to 0) { acc, new -> acc.second to new }
            .onEach { (old, new) ->
                if (new > old) emitBreeze("SOCIAL ENERGY")
            }.launchIn(scope)

        // Messages Relayed
        p2pController.messages
            .onEach { msgs ->
                if (msgs.isNotEmpty()) {
                    val last = msgs.last()
                    if ((System.currentTimeMillis() - last.timestamp) < 1000) {
                        emitBreeze("MESSAGE SPREAD")
                    }
                }
            }.launchIn(scope)
    }

    private suspend fun emitBreeze(text: String) {
        _breezeFlow.emit(text)
        hapticManager?.triggerMessage(cc.thevar.blukit.data.system.HapticManager.MessageType.CONNECTION)
        delay(5.seconds)
        if (_breezeFlow.replayCache.firstOrNull() == text) {
            _breezeFlow.emit("")
        }
    }

    private fun generateAiInsight(users: Int, radios: Int, msgs: Int, harmony: Float, lowPower: Boolean): String {
        if (lowPower) return "ENERGY SAVER ACTIVE"
        
        return when {
            users == 0 -> "CONNECT WITH PEOPLE"
            users > 15 -> "VIBRANT ROOM DETECTED"
            harmony < 0.3f -> "PEOPLE NEARBY: SAY HELLO"
            (users > 10) && (harmony > 0.8f) -> "ROOM HARMONY"
            (radios == 0) && (users > 0) -> "SOCIAL ENERGY"
            msgs > 100 -> "CONVERSATION FLOWING"
            else -> "SAY HELLO TO THE ROOM"
        }
    }
}

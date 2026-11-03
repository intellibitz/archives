package cc.thevar.blukit.data.power

import cc.thevar.blukit.data.local.MessageRepository
import cc.thevar.blukit.domain.power.HarmonyReport
import cc.thevar.blukit.network.p2p.ConnectionController
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
 * The Harmony Service.
 * Monitors Groups, Sources, and provides human-centric Synthesis.
 */
class HarmonyManager(
    private val connectionController: ConnectionController,
    private val messageLedger: MessageRepository,
    private val identityRepository: cc.thevar.blukit.data.repository.IdentityRepository,
    private val hapticManager: cc.thevar.blukit.data.system.HapticManager? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _report = MutableStateFlow(HarmonyReport())
    val report: StateFlow<HarmonyReport> = _report.asStateFlow()

    private val _breezeFlow = MutableSharedFlow<String>(replay = 1)
    private val _lastLocation = MutableStateFlow<android.location.Location?>(null)

    // Geofencing coordinates (Campus landmarks)
    private val landmarks = mapOf(
        "HOME HUB" to (12.9716 to 77.5946),
        "LIBRARY" to (12.9724 to 77.5937),
    )

    init {
        startHarmonyGathering()
    }

    fun updateLocation(location: android.location.Location) {
        _lastLocation.value = location
    }

    private fun startHarmonyGathering() {
        scope.launch {
            combine(
                connectionController.scannedDevices,
                connectionController.connectedGroups,
                messageLedger.messages,
                identityRepository.lowPowerMode,
                _breezeFlow.onStart { emit("") },
                _lastLocation
            ) { args: Array<Any?> ->
                @Suppress("UNCHECKED_CAST")
                val scanned = args[0] as List<cc.thevar.blukit.domain.model.Source>
                @Suppress("UNCHECKED_CAST")
                val connected = args[1] as Set<String>
                @Suppress("UNCHECKED_CAST")
                val messages = args[2] as List<cc.thevar.blukit.domain.model.Message>
                val lowPower = args[3] as Boolean
                
                val userCount = scanned.size
                val connectionCount = connected.size
                val messageCount = messages.size
                
                val meshHarmony = if (userCount > 0) {
                    min(1.0f, (connectionCount.toFloat() / userCount.toFloat()) + 0.2f)
                } else 0f

                val synthesis = generateSynthesis(userCount, connectionCount, messageCount, meshHarmony, lowPower)
                val latestSynthesis = messages.findLast { it.type == cc.thevar.blukit.domain.model.Message.TYPE_AI_SUMMARY }
                val trend = latestSynthesis?.trendLabel
                
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

                HarmonyReport(
                    userCount = userCount,
                    connectedTiesCount = connectionCount,
                    totalMessages = messageCount,
                    harmony = meshHarmony,
                    synthesis = synthesis,
                    trendLabel = trend,
                    currentBreeze = breeze,
                    lowPowerMode = lowPower,
                    suggestedGroups = suggestions,
                    lastLocation = locationLabel
                )
            }.collect {
                _report.value = it
            }
        }

        observeEventsForBreezes()
    }

    private fun observeEventsForBreezes() {
        // Source Detected
        connectionController.scannedDevices
            .map { it.size }
            .distinctUntilChanged()
            .scan(0 to 0) { acc, new -> acc.second to new }
            .onEach { (old, new) ->
                if (new > old) emitBreeze("PERSON NEARBY")
            }.launchIn(scope)

        // Connection Formed
        connectionController.connectedGroups
            .map { it.size }
            .distinctUntilChanged()
            .scan(0 to 0) { acc, new -> acc.second to new }
            .onEach { (old, new) ->
                if (new > old) emitBreeze("CONNECTED")
            }.launchIn(scope)

        // Messages Relayed
        connectionController.messages
            .onEach { msgs ->
                if (msgs.isNotEmpty()) {
                    val last = msgs.last()
                    if ((System.currentTimeMillis() - last.timestamp) < 1000) {
                        emitBreeze("MESSAGE SENT")
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

    private fun generateSynthesis(users: Int, connections: Int, messages: Int, harmony: Float, lowPower: Boolean): String {
        if (lowPower) return "POWER SAVING"
        
        return when {
            users == 0 -> "START YOUR HISTORY"
            users > 15 -> "MANY PEOPLE NEARBY"
            harmony < 0.3f -> "PEOPLE NEARBY: START CHATTING"
            (users > 10) && (harmony > 0.8f) -> "STABLE CONNECTIONS"
            (connections == 0) && (users > 0) -> "READY TO CONNECT"
            messages > 100 -> "ACTIVE CHAT"
            else -> "BLUKIT: CONNECTED"
        }
    }
}

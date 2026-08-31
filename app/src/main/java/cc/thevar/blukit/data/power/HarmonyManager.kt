package cc.thevar.blukit.data.power

import cc.thevar.blukit.data.local.EchoLedger
import cc.thevar.blukit.domain.power.HarmonyReport
import cc.thevar.blukit.network.p2p.ResonanceController
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
 * Monitors Spheres, Sources, and provides human-centric Synthesis.
 */
class HarmonyManager(
    private val resonanceController: ResonanceController,
    private val echoLedger: EchoLedger,
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
                resonanceController.scannedDevices,
                resonanceController.connectedGroups,
                echoLedger.echoes,
                identityRepository.lowPowerMode,
                _breezeFlow.onStart { emit("") },
                _lastLocation
            ) { args: Array<Any?> ->
                @Suppress("UNCHECKED_CAST")
                val scanned = args[0] as List<cc.thevar.blukit.domain.model.Source>
                @Suppress("UNCHECKED_CAST")
                val connected = args[1] as Set<String>
                @Suppress("UNCHECKED_CAST")
                val echoes = args[2] as List<cc.thevar.blukit.domain.model.Echo>
                val lowPower = args[3] as Boolean
                
                val userCount = scanned.size
                val resonanceCount = connected.size
                val echoCount = echoes.size
                
                val meshHarmony = if (userCount > 0) {
                    min(1.0f, (resonanceCount.toFloat() / userCount.toFloat()) + 0.2f)
                } else 0f

                val synthesis = generateSynthesis(userCount, resonanceCount, echoCount, meshHarmony, lowPower)
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
                    connectedTiesCount = resonanceCount,
                    totalMessages = echoCount,
                    harmony = meshHarmony,
                    synthesis = synthesis,
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
        // Source Detected
        resonanceController.scannedDevices
            .map { it.size }
            .distinctUntilChanged()
            .scan(0 to 0) { acc, new -> acc.second to new }
            .onEach { (old, new) ->
                if (new > old) emitBreeze("SOURCE PROXIMITY")
            }.launchIn(scope)

        // Resonance Formed
        resonanceController.connectedGroups
            .map { it.size }
            .distinctUntilChanged()
            .scan(0 to 0) { acc, new -> acc.second to new }
            .onEach { (old, new) ->
                if (new > old) emitBreeze("RESONANCE ENERGY")
            }.launchIn(scope)

        // Echoes Relayed
        resonanceController.messages
            .onEach { msgs ->
                if (msgs.isNotEmpty()) {
                    val last = msgs.last()
                    if ((System.currentTimeMillis() - last.timestamp) < 1000) {
                        emitBreeze("ECHO SPREAD")
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

    private fun generateSynthesis(users: Int, resonances: Int, echoes: Int, harmony: Float, lowPower: Boolean): String {
        if (lowPower) return "ENERGY SAVER ACTIVE"
        
        return when {
            users == 0 -> "RECORD YOUR LIFE"
            users > 15 -> "VIBRANT SPHERE DETECTED"
            harmony < 0.3f -> "SOURCES NEARBY: RESONATE"
            (users > 10) && (harmony > 0.8f) -> "SPHERE HARMONY"
            (resonances == 0) && (users > 0) -> "RESONANCE ENERGY"
            echoes > 100 -> "ECHOES FLOWING"
            else -> "YOUR LIFE, RESONATING"
        }
    }
}

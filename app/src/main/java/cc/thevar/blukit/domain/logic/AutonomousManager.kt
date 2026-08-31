package cc.thevar.blukit.domain.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import android.util.Log
import cc.thevar.blukit.data.repository.IdentityRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

/**
 * AUTONOMOUS MANAGER: The intelligent background engine of Blukit.
 * Handles Zero-Configuration automation for Eco and Stealth modes.
 */
class AutonomousManager(
    private val context: Context,
    private val repository: IdentityRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private var stealthTimeoutJob: Job? = null
    private val STEALTH_TIMEOUT = 5.minutes

    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            evaluateEcoState()
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        }
        context.registerReceiver(powerReceiver, filter)
        
        // Initial state evaluation
        evaluateEcoState()
        
        // Start the stealth timer
        resetStealthTimer()
    }

    /**
     * ECO INTELLIGENCE: Automatically throttles the mesh engine.
     * Triggers when battery is < 15% or System Battery Saver is active.
     */
    private fun evaluateEcoState() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isPowerSaveMode = powerManager.isPowerSaveMode
        
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = level * 100 / scale.toFloat()

        val shouldBeEco = isPowerSaveMode || batteryPct < 15
        
        if (repository.lowPowerMode.value != shouldBeEco) {
            Log.i("AutonomousManager", "Silent Eco: ${if (shouldBeEco) "Activating" else "Deactivating"} Low Power Mode")
            repository.toggleLowPowerMode(shouldBeEco)
        }
    }

    /**
     * SILENT STEALTH: Automatically hides identity when the user is inactive.
     * Activity is reset by UI interactions or sending messages.
     */
    fun onUserActivity() {
        if (repository.stealthMode.value) {
            Log.i("AutonomousManager", "Silent Stealth: Resurfacing due to activity")
            repository.toggleStealth(false)
        }
        resetStealthTimer()
    }

    private fun resetStealthTimer() {
        stealthTimeoutJob?.cancel()
        stealthTimeoutJob = scope.launch {
            delay(STEALTH_TIMEOUT)
            if (!repository.stealthMode.value) {
                Log.i("AutonomousManager", "Silent Stealth: Entering background mode due to inactivity")
                repository.toggleStealth(true)
            }
        }
    }
    
    fun stop() {
        try {
            context.unregisterReceiver(powerReceiver)
        } catch (e: Exception) {
            // Receiver not registered
        }
        stealthTimeoutJob?.cancel()
    }
}

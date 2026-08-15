package cc.thevar.blukit.data.system

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Elite Senior Architect Implementation:
 * Haptic Silent Alerts for discrete venue messaging.
 */
class HapticManager(context: Context) {

    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    enum class VibeType {
        MESSAGE, // Short double vibe
        CONNECTION, // Strong single vibe
        ERROR, // Long intense vibe
        SHOUT, // Rippling vibe
        STADIUM_WAVE // Traveling wave feeling
    }

    /**
     * Triggers a discrete haptic alert based on system events.
     */
    fun triggerVibe(type: VibeType) {
        if (!vibrator.hasVibrator()) return

        val effect = when (type) {
            VibeType.MESSAGE -> VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 40), -1)
            VibeType.CONNECTION -> VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
            VibeType.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200), -1)
            VibeType.SHOUT -> VibrationEffect.createWaveform(longArrayOf(0, 30, 30, 30, 30, 30), -1)
            VibeType.STADIUM_WAVE -> {
                // Wave: Increasing duration pulses to simulate outward motion
                VibrationEffect.createWaveform(longArrayOf(0, 20, 40, 30, 40, 50, 40, 70), -1)
            }
        }
        vibrator.vibrate(effect)
    }

    /**
     * Legacy compatibility wrapper.
     */
    fun triggerMessageAlert() = triggerVibe(VibeType.MESSAGE)

    /**
     * Triggers a proximity-based vibe for incoming surges.
     * High Fidelity implementation: Stronger vibes for closer devices.
     */
    fun triggerProximityVibe(proximity: Float) {
        if (!vibrator.hasVibrator()) return

        val effect = when {
            proximity > 0.8f -> {
                // Strong, sharp double vibe
                VibrationEffect.createWaveform(longArrayOf(0, 60, 40, 60), -1)
            }
            proximity > 0.4f -> {
                // Medium single vibe
                VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE)
            }
            else -> {
                // Subtle, faint ripple
                VibrationEffect.createWaveform(longArrayOf(0, 20, 30, 20, 30, 20), -1)
            }
        }
        vibrator.vibrate(effect)
    }
}

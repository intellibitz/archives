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

    enum class PulseType {
        MESSAGE, // Short double pulse
        CONNECTION, // Strong single pulse
        ERROR, // Long intense pulse
        SHOUT, // Rippling pulse
        AIR_WAVE // Traveling wave feeling
    }

    /**
     * Triggers a discrete haptic alert based on system events.
     */
    fun triggerPulse(type: PulseType) {
        if (!vibrator.hasVibrator()) return

        val effect = when (type) {
            PulseType.MESSAGE -> VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 40), -1)
            PulseType.CONNECTION -> VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
            PulseType.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200), -1)
            PulseType.SHOUT -> VibrationEffect.createWaveform(longArrayOf(0, 30, 30, 30, 30, 30), -1)
            PulseType.AIR_WAVE -> {
                // Wave: Increasing duration pulses to simulate outward motion
                VibrationEffect.createWaveform(longArrayOf(0, 20, 40, 30, 40, 50, 40, 70), -1)
            }
        }
        vibrator.vibrate(effect)
    }
}

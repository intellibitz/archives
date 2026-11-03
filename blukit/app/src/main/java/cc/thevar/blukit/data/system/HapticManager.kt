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

    enum class MessageType {
        MESSAGE, // Short double message
        CONNECTION, // Strong single message
        ERROR, // Long intense message
        SHOUT, // Rippling message
        WAVE, // Traveling wave feeling
        SYNC // Life recording pulse
    }

    /**
     * Triggers a discrete haptic alert based on system events.
     */
    fun triggerMessage(type: MessageType) {
        if (!vibrator.hasVibrator()) return

        val effect = when (type) {
            MessageType.MESSAGE -> VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 40), -1)
            MessageType.CONNECTION -> VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
            MessageType.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200), -1)
            MessageType.SHOUT -> VibrationEffect.createWaveform(longArrayOf(0, 30, 30, 30, 30, 30), -1)
            MessageType.WAVE -> {
                VibrationEffect.createWaveform(longArrayOf(0, 20, 40, 30, 40, 50, 40, 70), -1)
            }
            MessageType.SYNC -> {
                // A strong, growing pulse feeling for recording life
                VibrationEffect.createWaveform(longArrayOf(0, 40, 40, 80, 40, 150), -1)
            }
        }
        vibrator.vibrate(effect)
    }
}

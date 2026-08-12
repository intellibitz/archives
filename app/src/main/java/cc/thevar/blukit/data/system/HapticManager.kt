package cc.thevar.blukit.data.system

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Supreme Senior Architect Implementation:
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

    /**
     * Triggers a discrete double-pulse haptic alert for incoming messages.
     */
    fun triggerMessageAlert() {
        if (vibrator.hasVibrator()) {
            // Short double pulse: [wait, vibrate, wait, vibrate]
            val effect = VibrationEffect.createWaveform(
                longArrayOf(0, 100, 50, 100),
                -1 // No repeat
            )
            vibrator.vibrate(effect)
        }
    }
}

package cc.thevar.blukit

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import cc.thevar.blukit.data.worker.PurgeWorker
import java.util.concurrent.TimeUnit

/**
 * Blukit Application class — entry point for global system initialization.
 */
class BlukitApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupPurgeWorker()
    }

    /**
     * Supreme Senior Architect Implementation:
     * Schedules a background worker to periodically purge ephemeral chat data (12h TTL).
     */
    private fun setupPurgeWorker() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val purgeRequest = PeriodicWorkRequestBuilder<PurgeWorker>(
            12, TimeUnit.HOURS
        ).setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "purge_messages",
            ExistingPeriodicWorkPolicy.KEEP,
            purgeRequest
        )
    }
}

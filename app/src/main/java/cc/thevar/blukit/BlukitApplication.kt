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
     * Schedules a periodic background worker to enforce ephemeral data retention policies.
     * Chat logs are purged every 12 hours to maintain "Vibing Persistence" and ensure user privacy.
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

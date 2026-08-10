package cc.thevar.blukit

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import cc.thevar.blukit.data.worker.PurgeWorker
import java.util.concurrent.TimeUnit

class BlukitApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupPurgeWorker()
    }

    private fun setupPurgeWorker() {
        val purgeRequest = PeriodicWorkRequestBuilder<PurgeWorker>(
            12, TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "purge_messages",
            ExistingPeriodicWorkPolicy.KEEP,
            purgeRequest
        )
    }
}

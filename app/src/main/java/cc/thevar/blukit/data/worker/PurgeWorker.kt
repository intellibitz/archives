package cc.thevar.blukit.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cc.thevar.blukit.data.local.ChatDatabase

/**
 * Background worker responsible for the automated 12-hour cleanup of the local Room database.
 * Ensures that "Vibing Persistence" is maintained by deleting messages older than the TTL threshold.
 */
class PurgeWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = ChatDatabase.getInstance(applicationContext)
        val threshold = System.currentTimeMillis() - (12 * 60 * 60 * 1000) // 12 hours
        
        return try {
            database.messageDao.deleteOldMessages(threshold)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

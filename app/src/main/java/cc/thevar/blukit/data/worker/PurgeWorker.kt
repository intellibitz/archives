package cc.thevar.blukit.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cc.thevar.blukit.data.local.ChatDatabase

/**
 * Supreme Senior Android Expert Implementation:
 * Background worker to enforce the 12-hour TTL (Time-To-Live) for chat logs.
 * Uses the ChatDatabase singleton for efficient data purging.
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

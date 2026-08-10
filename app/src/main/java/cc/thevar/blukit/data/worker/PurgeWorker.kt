package cc.thevar.blukit.data.worker

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cc.thevar.blukit.data.local.ChatDatabase

class PurgeWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = Room.databaseBuilder(
            applicationContext,
            ChatDatabase::class.java,
            "chat.db"
        ).build()

        val threshold = System.currentTimeMillis() - (12 * 60 * 60 * 1000) // 12 hours
        database.messageDao.deleteOldMessages(threshold)

        return Result.success()
    }
}

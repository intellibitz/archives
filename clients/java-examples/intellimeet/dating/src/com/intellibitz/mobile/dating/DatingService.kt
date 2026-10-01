package com.intellibitz.mobile.dating

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Parcel

class DatingService : Service() {

    private var mNM: NotificationManager? = null
    var thr: Thread? = null

    override fun onCreate() {
        mNM = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val contentIntent = Intent()
        val appIntent = Intent()
        mNM!!.notify(
            MOOD_NOTIFICATIONS,
            Notification(
                this,
                R.drawable.stat_sample,
                "Matching service started",
                System.currentTimeMillis(),
                "Matching service started",
                "Matching service started",
                contentIntent,
                R.drawable.no_picture,
                getText(R.string.userinfo),
                appIntent
            )
        )
        thr = Thread(null, mTask, "NotifyingService")
        thr!!.start()
    }

    private val mTask = Runnable {
        try {
            while (true) {
                showNotification(R.drawable.stat_happy, R.string.status_bar_chennai)
                Thread.sleep(3000)
                showNotification(R.drawable.stat_neutral, R.string.status_bar_coimbatore)
                Thread.sleep(3000)
                showNotification(R.drawable.stat_sad, R.string.status_bar_salem)
                Thread.sleep(3000)
                showNotification(R.drawable.stat_sad, R.string.status_bar_erode)
                Thread.sleep(3000)
                Thread.sleep(3000)
                showNotification(R.drawable.stat_sad, R.string.status_bar_nellai)
                Thread.sleep(3000)
                Thread.sleep(3000)
                showNotification(R.drawable.stat_sad, R.string.status_bar_madurai)
                Thread.sleep(3000)
            }
        } catch (ex: Exception) {
        }
        this@DatingService.stopSelf()
    }

    @Suppress("DEPRECATION")
    override fun onDestroy() {
        mNM!!.cancel(MOOD_NOTIFICATIONS)
        thr!!.stop()
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    @Suppress("DEPRECATION")
    private fun showNotification(moodId: Int, textId: Int) {
        val contentIntent = Intent()
        val appIntent = Intent()
        val text = getText(textId)
        mNM!!.notify(
            MOOD_NOTIFICATIONS,
            Notification(
                this,
                moodId,
                null,
                System.currentTimeMillis(),
                getText(R.string.status_bar_notifications_mood_title),
                text,
                contentIntent,
                R.drawable.no_picture,
                getText(R.string.userinfo),
                appIntent
            )
        )
        thr!!.stop()
    }

    private val mBinder = object : Binder() {
        override fun onTransact(code: Int, data: Parcel, reply: Parcel, flags: Int): Boolean {
            return super.onTransact(code, data, reply, flags)
        }
    }

    companion object {
        private var MOOD_NOTIFICATIONS = R.layout.status_bar_notifications
    }
}

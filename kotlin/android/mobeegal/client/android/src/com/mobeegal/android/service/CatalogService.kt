package com.mobeegal.android.service

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.DeadObjectException
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Process
import android.os.RemoteCallbackList
import android.widget.Toast
import com.mobeegal.android.R

class CatalogService : Service() {

    val mCallbacks =
        RemoteCallbackList<ICatalogServiceCallback>()

    var mValue = 0
    var mNM: NotificationManager? = null

    override fun onCreate() {
        mNM = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Display a notification about us starting.
        showNotification()

        // While this service is running, it will continually increment a
        // number.  Send the first message that is used to perform the
        // increment.
        mHandler.sendEmptyMessage(REPORT_MSG)
    }

    override fun onDestroy() {
        // Cancel the persistent notification.
        mNM!!.cancel(R.string.remote_service_started)

        // Tell the user we stopped.
        Toast.makeText(
            this, R.string.remote_service_stopped,
            Toast.LENGTH_SHORT
        ).show()

        // Unregister all callbacks.
        mCallbacks.kill()

        // Remove the next pending message to increment the counter, stopping
        // the increment loop.
        mHandler.removeMessages(REPORT_MSG)
    }

    override fun onBind(intent: Intent): IBinder? {
        // Select the interface to return.  If your service only implements
        // a single interface, you can just return it here without checking
        // the Intent.
        if (ICatalogService::class.java.name == intent.action) {
            return mBinder
        }
        if (ISecondary::class.java.name == intent.action) {
            return mSecondaryBinder
        }
        return null
    }

    /**
     * The IRemoteInterface is defined through IDL
     */
    private val mBinder: ICatalogService.Stub = object : ICatalogService.Stub() {
        override fun registerCallback(cb: ICatalogServiceCallback?) {
            if (cb != null) {
                mCallbacks.register(cb)
            }
        }

        override fun unregisterCallback(cb: ICatalogServiceCallback?) {
            if (cb != null) {
                mCallbacks.unregister(cb)
            }
        }
    }

    /**
     * A secondary interface to the service.
     */
    private val mSecondaryBinder: ISecondary.Stub = object : ISecondary.Stub() {
        override fun getPid(): Int {
            return Process.myPid()
        }

        override fun basicTypes(
            anInt: Int, aLong: Long, aBoolean: Boolean,
            aFloat: Float, aDouble: Double, aString: String?
        ) {
        }
    }

    /**
     * Our Handler used to execute operations on the main thread.  This is used
     * to schedule increments of our value.
     */
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                // It is time to bump the value!
                REPORT_MSG -> {
                    // Up it goes.
                    val value = ++mValue

                    // Broadcast to all clients the new value.
                    val N = mCallbacks.beginBroadcast()
                    for (i in 0 until N) {
                        try {
                            mCallbacks.getBroadcastItem(i).valueChanged(value)
                        } catch (e: DeadObjectException) {
                            // The RemoteCallbackList will take care of removing
                            // the dead object for us.
                        }
                    }
                    mCallbacks.finishBroadcast()

                    // Repeat every 1 second.
                    sendMessageDelayed(obtainMessage(REPORT_MSG), (1 * 1000).toLong())
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    /**
     * Show a notification while this service is running.
     */
    private fun showNotification() {
        // This is who should be launched if the user selects our notification.
        val contentIntent = Intent()

        // This is who should be launched if the user selects the app icon in the notification,
        // (in this case, we launch the same activity for both)
        val appIntent = Intent()

        // In this sample, we'll use the same text for the ticker and the expanded notification
        val text = getText(R.string.remote_service_started)

        mNM!!.notify(
            R.string.remote_service_started,
            // we use a string id because it is a unique
            // number.  we use it later to cancel the
            // notification
            Notification(
                this, // our context
                R.drawable.stat_sample,
                // the icon for the status bar
                text,
                // the text to display in the ticker
                System.currentTimeMillis(),
                // the timestamp for the notification
                getText(R.string.remote_service_label),
                // the title for the notification
                text,
                // the details to display in the notification
                contentIntent
            )
        ) // the appIntent (see above)
    }

    companion object {
        private const val REPORT_MSG = 1
    }
}

package com.androidworks.navsys.wuffit.service

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log
import com.androidworks.navsys.wuffit.content.Tracker

class SMSHandler : Service() {

    private val mBinder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val service: SMSHandler
            get() = this@SMSHandler
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { performSMSParsing(it) }
        return START_NOT_STICKY
    }

    private fun performSMSParsing(intent: Intent) {
        Log.d(TAG, "Service started $intent")
        val bundle = intent.getBundleExtra("SMS_BUNDLE") ?: return
        val format = bundle.getString("format")

        @Suppress("DEPRECATION")
        val pdus = bundle.get("pdus") as? Array<*> ?: return

        for (pdu in pdus) {
            val bytes = pdu as? ByteArray ?: continue
            val msg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && format != null) {
                SmsMessage.createFromPdu(bytes, format)
            } else {
                @Suppress("DEPRECATION")
                SmsMessage.createFromPdu(bytes)
            }
            if (msg != null) {
                Log.d(TAG, "Message ==> $msg")
                parseSMSMessage(msg)
            }
        }
        stopSelf()
        Log.d(TAG, "Service stopped")
    }

    private fun parseSMSMessage(sms: SmsMessage) {
        val msg = sms.displayMessageBody ?: return
        val num = sms.originatingAddress ?: return
        val formatNum = PhoneNumberUtils.formatNumber(num) ?: num

        val cursor = contentResolver.query(
            Tracker.Details.CONTENT_URI,
            arrayOf(Tracker.Details._ID, Tracker.Details.NUMBER),
            null,
            null,
            null
        ) ?: return

        cursor.use { c ->
            val sz = c.count
            if (sz > 0) {
                c.moveToFirst()
                for (i in 0 until sz) {
                    val dbNum = c.getString(1)
                    if (PhoneNumberUtils.compare(formatNum, dbNum)) {
                        Log.d(TAG, "Phone numbers are equal: $formatNum and: $dbNum")
                        val id = c.getString(0)
                        if (msg.contains("\$GPRMC")) {
                            updateTrackerLocationReply(msg, id)
                        } else {
                            updateTrackerSetupReply(id, msg)
                        }
                        break
                    }
                    c.moveToNext()
                }
            }
        }
    }

    private fun updateTrackerLocationReply(msg: String, fid: String) {
        val contentValues = ContentValues(2).apply {
            put(Tracker.Locations.UPDATE_TIME, System.currentTimeMillis())
            put(Tracker.Locations.LOCATION, msg)
        }
        val where = "${Tracker.Locations._FID} = ? "
        val whereArgs = arrayOf(fid)
        contentResolver.update(Tracker.Locations.CONTENT_URI, contentValues, where, whereArgs)
        Log.d(TAG, "Updated: ${Tracker.Locations.CONTENT_URI}")
    }

    private fun updateTrackerSetupReply(fid: String, reply: String) {
        val contentValues = ContentValues(2).apply {
            put(Tracker.Setup.REPLY_TIME, System.currentTimeMillis())
            put(Tracker.Setup.REPLY, reply)
        }
        val where = "${Tracker.Setup._FID} = ? "
        val whereArgs = arrayOf(fid)
        contentResolver.update(Tracker.Setup.CONTENT_URI, contentValues, where, whereArgs)
        Log.d(TAG, "Updated: ${Tracker.Setup.CONTENT_URI}")
    }

    override fun onBind(intent: Intent?): IBinder = mBinder

    private fun getSmsManager(): SmsManager {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSystemService(SmsManager::class.java) ?: SmsManager.getDefault()
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
    }

    fun setupWuffIT(cmd: String) {
        val cursor = contentResolver.query(
            Tracker.Details.CONTENT_URI,
            arrayOf(Tracker.Details._ID, Tracker.Details.NUMBER),
            null,
            null,
            null
        ) ?: return

        cursor.use { c ->
            val sz = c.count
            if (0 == sz) {
                Log.w(TAG, "No Records found in database: ${Tracker.Details.CONTENT_URI}")
            } else {
                c.moveToFirst()
                for (i in 0 until sz) {
                    setupWuffIT(cmd, c.getString(1), c.getString(0))
                    c.moveToNext()
                }
            }
        }
    }

    fun setupWuffIT(cmd: String, num: String, id: String) {
        getSmsManager().sendTextMessage(num, null, cmd, null, null)
        val contentValues = ContentValues(1).apply {
            put(Tracker.Setup.SETUP_TIME, System.currentTimeMillis())
        }
        val where = "${Tracker.Setup._FID} = ? "
        val whereArgs = arrayOf(id)
        contentResolver.update(Tracker.Setup.CONTENT_URI, contentValues, where, whereArgs)
    }

    fun requestLocationUpdates(cmd: String) {
        val cursor = contentResolver.query(
            Tracker.Details.CONTENT_URI,
            arrayOf(Tracker.Details._ID, Tracker.Details.NUMBER),
            null,
            null,
            null
        ) ?: return

        cursor.use { c ->
            val sz = c.count
            if (0 == sz) {
                Log.w(TAG, "No Records found in database: ${Tracker.Details.CONTENT_URI}")
            } else {
                c.moveToFirst()
                for (i in 0 until sz) {
                    requestLocationUpdates(cmd, c.getString(1), c.getString(0))
                    c.moveToNext()
                }
            }
        }
    }

    fun requestLocationUpdates(cmd: String, num: String, id: String) {
        getSmsManager().sendTextMessage(num, null, cmd, null, null)
        val contentValues = ContentValues(1).apply {
            put(Tracker.Locations.REQUEST_TIME, System.currentTimeMillis())
        }
        val where = "${Tracker.Locations._FID} = ? "
        val whereArgs = arrayOf(id)
        contentResolver.update(Tracker.Locations.CONTENT_URI, contentValues, where, whereArgs)
    }

    companion object {
        private const val TAG = "SMSHandler"
    }
}

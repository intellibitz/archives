/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobeegal.android.activity

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import com.mobeegal.android.content.MstuffQuery
import java.util.ArrayList

/**
 * @author jyothsna
 */
class TimeSettings : Activity() {

    private var querystatusString: String? = null
    private var secondvalue: String? = null
    private var firstvalue: Int = 0
    private var interval: Array<String>? = null
    private var getTimeInterval: String? = null
    private var res: String? = null
    private var gettime1: String? = null
    private var timename1: String? = null
    private var servicename2: String? = null
    private var viewname1: String? = null
    private var myDatabase: SQLiteDatabase? = null
    var results: ArrayList<Any?> = ArrayList()
    private val mHandler = Handler()
    var intobject2: Intent? = null
    var firstTime2: Long = 0

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        try {
            myDatabase = this.openOrCreateDatabase(
                "Mobeegal",
                Context.MODE_PRIVATE, null
            )
            val col = arrayOf("views", "service", "time", "settime")
            val c1 = myDatabase!!.query(
                "Preferences", col, null, null,
                null, null, null
            )
            val viewnamepref = c1.getColumnIndexOrThrow("views")
            val servicenamepref = c1.getColumnIndexOrThrow("service")
            val timenamepref = c1.getColumnIndexOrThrow("time")
            val gettimepref = c1.getColumnIndexOrThrow("settime")
            if (c1 != null) {
                if (c1.isFirst) {
                    do {
                        viewname1 = c1.getString(viewnamepref)
                        servicename2 = c1.getString(servicenamepref)
                        timename1 = c1.getString(timenamepref)
                        gettime1 = c1.getString(gettimepref)
                        results.add(viewname1)
                        results.add(servicename2)
                        results.add(timename1)
                        results.add(gettime1)
                    } while (c1.moveToNext())
                }
            }
            res = results.toString()
        } catch (ne: NullPointerException) {
        }
        try {
            getTimeInterval = gettime1
            Log.i("gettime", getTimeInterval)
            // splitting the time
            interval = getTimeInterval!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (interval != null) {
                firstvalue = Integer.parseInt(interval!![0])
                secondvalue = interval!![1].toString()
            }
            if (interval != null && servicename2 == "Auto" &&
                secondvalue == "Seconds"
            ) {
                intobject2 = Intent(this@TimeSettings, MstuffQuery::class.java)
                firstTime2 = SystemClock.elapsedRealtime()
                firstTime2 += (10 * 1000).toLong()
                val pi = PendingIntent.getActivity(
                    applicationContext, 0, intobject2,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
                mHandler.post {
                    val alarmmanager =
                        getSystemService(ALARM_SERVICE) as AlarmManager
                    alarmmanager.setRepeating(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        firstTime2, (10 * 1000).toLong(), pi
                    )
                }
            }
            if (interval != null && servicename2 == "Manual") {
                startService(
                    Intent(
                        "com.mobeegal.android.service.REMOTE_SERVICE"
                    )
                )
                if (secondvalue == "Minutes") {
                    intobject2 =
                        Intent(this@TimeSettings, MstuffQuery::class.java)
                    firstTime2 = SystemClock.elapsedRealtime()
                    firstTime2 += (firstvalue * 60 * 1000).toLong()
                    val pi = PendingIntent.getActivity(
                        applicationContext, 0, intobject2,
                        PendingIntent.FLAG_CANCEL_CURRENT
                    )
                    mHandler.post {
                        val alarmmanager =
                            getSystemService(
                                ALARM_SERVICE
                            ) as AlarmManager
                        alarmmanager.setRepeating(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            firstTime2, (firstvalue * 60 * 1000).toLong(),
                            pi
                        )
                    }
                } else if (secondvalue == "Hours") {
                    intobject2 =
                        Intent(this@TimeSettings, MstuffQuery::class.java)
                    firstTime2 = SystemClock.elapsedRealtime()
                    firstTime2 += (firstvalue * 60 * 60 * 1000).toLong()
                    val pi = PendingIntent.getActivity(
                        applicationContext, 0, intobject2,
                        PendingIntent.FLAG_CANCEL_CURRENT
                    )
                    mHandler.post {
                        val alarmmanager =
                            getSystemService(
                                ALARM_SERVICE
                            ) as AlarmManager
                        alarmmanager.setRepeating(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            firstTime2, (firstvalue * 60 * 60 * 1000).toLong(),
                            pi
                        )
                    }
                } else if (secondvalue == "Day") {
                    intobject2 =
                        Intent(this@TimeSettings, MstuffQuery::class.java)
                    firstTime2 = SystemClock.elapsedRealtime()
                    firstTime2 += (firstvalue * 24 * 60 * 60 * 1000).toLong()
                    val pi = PendingIntent.getActivity(
                        applicationContext, 0, intobject2,
                        PendingIntent.FLAG_CANCEL_CURRENT
                    )
                    mHandler.post {
                        val alarmmanager =
                            getSystemService(
                                ALARM_SERVICE
                            ) as AlarmManager
                        alarmmanager.setRepeating(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            firstTime2,
                            (firstvalue * 24 * 60 * 60 * 1000).toLong(),
                            pi
                        )
                    }
                }
            }
        } catch (e: NullPointerException) {
            Log.i("gettime1", getTimeInterval)
        } catch (ae: ArrayIndexOutOfBoundsException) {
        }
        val intentObj = Intent(this@TimeSettings, Settings::class.java)
        startActivityForResult(intentObj, 0)
        finish()
    }
}

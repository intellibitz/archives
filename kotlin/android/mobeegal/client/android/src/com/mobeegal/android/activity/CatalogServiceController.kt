package com.mobeegal.android.activity

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageButton
import android.widget.Toast
import com.mobeegal.android.R
import com.mobeegal.android.content.MstuffQuery

class CatalogServiceController : Activity() {

    var count = 0
    var gettingcategory: String? = null
    //public Catalogs welcome1;
    var result1: String? = null
    var result: String? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setTheme(android.R.style.Theme_Dialog)
        setContentView(R.layout.catalogservice_controller)
        val bundles = this.intent.extras
        if (bundles != null) {
            gettingcategory = bundles.getString("passingcategoryActivation")
        }
        var imageButton =
            findViewById(R.id.buttonActivate) as ImageButton
        imageButton.setOnClickListener(mStartListener)
        imageButton = findViewById(R.id.buttonBack) as ImageButton
        imageButton.setOnClickListener(mStopListener)
    }

    private val mStartListener = OnClickListener {
        if (count == 0) {
            startService(
                Intent(
                    "com.mobeegal.android.service.REMOTE_SERVICE"
                )
            )
            count++
            try {
                val intobject1 = Intent(
                    this@CatalogServiceController, MstuffQuery::class.java
                )
                val bundle = Bundle()
                bundle.putString("passingcategory", gettingcategory)
                intobject1.putExtras(bundle)
                var firstTime = SystemClock.elapsedRealtime()
                firstTime += (10 * 1000).toLong()
                val alarmmanager =
                    getSystemService(ALARM_SERVICE) as AlarmManager
                val pi = PendingIntent.getActivity(
                    applicationContext, 0, intobject1,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
                alarmmanager
                    .setRepeating(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        firstTime, (10 * 1000).toLong(), pi
                    )
            } catch (e: NullPointerException) {
            }
        } else {
            Toast.makeText(
                this@CatalogServiceController,
                "Service already activated", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val mStopListener = OnClickListener {
        stopService(
            Intent(
                "com.mobeegal.android.service.REMOTE_SERVICE"
            )
        )
        val intent = Intent(
            this@CatalogServiceController,
            MstuffQuery::class.java
        )
        val alarmmanager =
            getSystemService(ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        alarmmanager.cancel(pi)
        finish()
    }
}

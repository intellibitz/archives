package com.intellibitz.mobile.dating

import android.app.Activity
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class DatingServiceController : Activity() {

    var butActivate: Button? = null
    var butDeactivate: Button? = null
    var storedValues: String? = null
    var partAge: TextView? = null
    var partHeight: TextView? = null
    var partWeight: TextView? = null
    var partLocation: TextView? = null
    var count = 0
    var mNM: NotificationManager? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setTheme(android.R.style.Theme_Dialog)
        setContentView(R.layout.dating_controller)
        partAge = TextView(this)
        mNM = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        butActivate = findViewById(R.id.buttonActivate) as Button
        butDeactivate = findViewById(R.id.buttonBack) as Button
        butDeactivate!!.visibility = Button.INVISIBLE
        butActivate!!.setOnClickListener(OnClickListener { v ->
            if (count == 0) {
                startService(Intent(this@DatingServiceController, DatingService::class.java), null)
                finish()
                count++
                //butDeactivate.setEnabled(true);
                butActivate!!.visibility = Button.INVISIBLE
                val intobject = Intent(this@DatingServiceController, Dating::class.java)
                startActivity(intobject)
            } else {
                Toast.makeText(
                    this@DatingServiceController,
                    "Service already activated",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        butDeactivate!!.setOnClickListener(object : OnClickListener {
            override fun onClick(v: View) {
                stopService(Intent(this@DatingServiceController, DatingService::class.java))
                Toast.makeText(
                    this@DatingServiceController,
                    "Service Deactivated",
                    Toast.LENGTH_SHORT
                ).show()
            }

            @Suppress("UNUSED")
            protected fun onDestroy() {
                // Cancel the persistent notification.
                mNM!!.cancel(R.string.local_service_started)
            }
        })
    }
}

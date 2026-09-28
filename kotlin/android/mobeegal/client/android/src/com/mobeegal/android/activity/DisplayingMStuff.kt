/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobeegal.android.activity

import android.app.Activity
import android.app.NotificationManager
import android.os.Bundle
import com.mobeegal.android.R

/**
 * @author jyothsna
 */
class DisplayingMStuff : Activity() {
    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        val nm =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(R.string.notification_message)
        finish()
    }
}

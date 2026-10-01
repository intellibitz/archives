package com.androidworks.navsys.wuffit.content

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.androidworks.navsys.wuffit.service.SMSHandler

class WuffITSMSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, SMSHandler::class.java).apply {
            action = "com.androidworks.navsys.wuffit.SERVICE_SMS_HANDLER"
            putExtra("SMS_BUNDLE", intent.extras)
        }
        context.startService(serviceIntent)
    }
}

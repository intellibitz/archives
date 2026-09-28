/**
 *
 */
package com.uc.dca

import android.app.Application
import android.content.Intent

/**
 * @author muthu
 *
 */
class IRApplication : Application() {

    /* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
    override fun onCreate() {
        // TODO Auto-generated method stub
        super.onCreate()
//		kicks start the service
        val serviceIntent = Intent("com.uc.dca.SERVICE_INCIDENT_REPORT")
        applicationContext.startService(serviceIntent)
    }

    companion object {
        const val TAG = "IRApplication"
    }
}

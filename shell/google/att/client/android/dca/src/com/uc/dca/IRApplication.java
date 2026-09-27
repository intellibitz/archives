/**
 * 
 */
package com.uc.dca;

import android.app.Application;
import android.content.Intent;

/**
 * @author muthu
 *
 */
public class IRApplication extends Application {

    static final String TAG = "IRApplication";

    /* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
//		kicks start the service
        Intent serviceIntent = new Intent("com.uc.dca.SERVICE_INCIDENT_REPORT");
        getApplicationContext().startService(serviceIntent);
	}

}

package com.mobeegal.android

/*
<!--
$Id:: MobeegalApplication.java 14 2008-08-19 06:36:45Z muthu.ramadoss           $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.LocationProvider
import android.provider.Settings
import android.util.Log

/**
 * MobeegalApplication<br> Application class to maintain Global state.. will be
 * invoked once during mobeegal startup by Android
 */
class MobeegalApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initLocation()
        //        openOrCreateDatabase();
    }

    fun initLocation() {
        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        Settings.System.putString(
            contentResolver,
            Settings.System.LOCATION_PROVIDERS_ALLOWED, PROVIDER_NAME
        )
        locationManager.updateProviders()
        val prov = locationManager.getProvider(PROVIDER_NAME)
        Log.i(LOG_TAG, prov.toString())

        /*
                Location loc = locationManager.getCurrentLocation(PROVIDER_NAME);
                if (loc == null)
                {
                    Log.e(LOG_TAG, "current location null");
                }
                else
                {
                    Log.d(LOG_TAG, "location: " + loc.toString());
                }
        */

        val loc = locationManager.getLastKnownLocation(PROVIDER_NAME)
        if (loc == null) {
            Log.e(LOG_TAG, "last known location null")
        } else {
            Log.d(LOG_TAG, "last known location: " + loc.toString())
        }
    }

    /*
        void openOrCreateDatabase()
        {
            ... large commented block preserved in Java original ...
        }
    */

    companion object {
        const val PROVIDER_NAME = "gps"
        const val LOG_TAG = "MobeegalApplication"
    }
}

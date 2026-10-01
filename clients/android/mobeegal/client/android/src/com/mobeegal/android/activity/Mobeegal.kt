package com.mobeegal.android.activity

/*
<!--
$Id:: Mobeegal.java 14 2008-08-19 06:36:45Z muthu.ramadoss                      $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import com.mobeegal.android.R

class Mobeegal : Activity() {

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.mobeegal)
        showResultsView()
        // don't show this again.. remove this activity from history
        finish()
    }

    fun showResultsView() {
        // If this were my app's main activity, I would load the default values so they're set even if
        // the user does not go into the preferences screen.
        PreferenceManager
            .setDefaultValues(this, R.xml.preferences, false)
        // Since we're in the same package, we can use this context to get
        // the default shared preferences
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(this)
        val mapview =
            sharedPref.getBoolean("mapview_preference", true)

        if (mapview) {
            startActivity(
                Intent(this@Mobeegal, MapResults::class.java)
            )
        } else {
            startActivity(
                Intent(this@Mobeegal, MStuffTextView::class.java)
            )
        }
        /*
                SQLiteDatabase myDB;
                try
                {
                    ... large commented block ...
                }
        */
    }
}

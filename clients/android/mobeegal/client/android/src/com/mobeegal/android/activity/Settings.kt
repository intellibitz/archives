package com.mobeegal.android.activity

/*
<!--
$Id:: Settings.java 14 2008-08-19 06:36:45Z muthu.ramadoss                   $: Id of last commit
$Rev:: 14                                                                       $: Revision of last commit
$Author:: muthu.ramadoss                                                        $: Author of last commit
$Date:: 2008-08-19 12:06:45 +0530 (Tue, 19 Aug 2008)                            $: Date of last commit
$HeadURL:: http://svn.assembla.com/svn/mobeegal/trunk/client/android/src/com/mo#$: Head URL of last commit
-->
*/

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import com.mobeegal.android.R
import com.mobeegal.android.util.ViewMenu

class Settings : Activity() {

    var activateButton1: Button? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.settings)
        val activate =
            findViewById(R.id.activate_deactivate) as ImageButton
        val preference = findViewById(R.id.preference) as ImageButton
        val helpsettings =
            findViewById(R.id.helpsettings) as ImageButton
        //Button activateButton1 = (Button) findViewById(R.id.buttonActivate);
        activate.setOnClickListener {
            // TODO Auto-generated method stub
            //	activateButton1.setEnabled(false);
            val intent = Intent(
                this@Settings,
                ServiceActivateDeactivate::class.java
            )
            startActivityForResult(intent, 0)
        }
        preference.setOnClickListener {
            val intent = Intent(this@Settings, Preferences::class.java)
            startActivityForResult(intent, 0)
        }
        helpsettings.setOnClickListener {
            // TODO Auto-generated method stub
            //	activateButton1.setEnabled(false);
            val intent = Intent(this@Settings, Help::class.java)
            startActivityForResult(intent, 0)
        }
    }

    //	MenuView
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsMenu(menu)
        return true
    }

    //Menu Item
    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            //		mStuff Menu
            1 -> {
                val stuffCheckintent =
                    Intent(this@Settings, MapResults::class.java)
                startActivityForResult(stuffCheckintent, 0)
                finish()
            }
            2 -> {
                val intent1 =
                    Intent(this@Settings, FindandInstall::class.java)
                startActivityForResult(intent1, 0)
                finish()
            }
            3 -> {
                val settings = Intent(this@Settings, Settings::class.java)
                startActivityForResult(settings, 0)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

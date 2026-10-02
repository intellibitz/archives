package com.mobeegal.android.activity

/*
<!--
$Id:: Help.java 14 2008-08-19 06:36:45Z muthu.ramadoss                       $: Id of last commit
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
import com.mobeegal.android.R
import com.mobeegal.android.util.ViewMenu

class Help : Activity() {

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.help)
    }

    //	MenuView
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        ViewMenu.onCreateOptionsMenu(menu)
        return true
    }

    //	Menu Item
    override fun onMenuItemSelected(i: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                //			mStuff Menu
                val stuffCheckintent =
                    Intent(this@Help, MapResults::class.java)
                startActivityForResult(stuffCheckintent, 0)
                finish()
            }
            2 -> {
                val intent1 = Intent(this@Help, FindandInstall::class.java)
                startActivityForResult(intent1, 0)
                finish()
            }
            3 -> {
                val settings = Intent(this@Help, Settings::class.java)
                startActivity(settings)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

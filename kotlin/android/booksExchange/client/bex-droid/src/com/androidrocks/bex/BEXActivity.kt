/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 */

package com.androidrocks.bex

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.androidrocks.bex.activity.ConnectActivity

class BEXActivity : Activity() {
    /** Called when the activity is first created. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        setupViews()
    }

    private fun setupViews() {
        // setup click listeners for buttons
        findViewById<android.view.View>(R.id.list_stuff).setOnClickListener {
            // Go to List stuff screen
            val intent = Intent("com.androidworks.bex.intent.action.VIEW_SHELVES")
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.type = "vnd.android.cursor.dir/vnd.com.androidrocks.bex.provider.list"
            startActivity(intent)
        }

        // setup click listeners for buttons
        findViewById<android.view.View>(R.id.connect_friends).setOnClickListener {
            // Go to Connect screen
            val intent = Intent(this, ConnectActivity::class.java)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            startActivity(intent)
        }
    }
}

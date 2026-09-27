package com.androidworks.navsys.wuffit.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.androidworks.navsys.wuffit.R

class WuffITTracker : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        findViewById<View>(R.id.select_tracker)?.setOnClickListener {
            val intent = Intent("com.androidworks.navsys.wuffit.ACTION_SELECT_TRACKER").apply {
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            startActivity(intent)
        }

        findViewById<View>(R.id.setup_wuffit)?.setOnClickListener {
            val intent = Intent("com.androidworks.navsys.wuffit.ACTION_SETUP_WUFFIT").apply {
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            startActivity(intent)
        }

        findViewById<View>(R.id.request_location)?.setOnClickListener {
            val intent = Intent("com.androidworks.navsys.wuffit.ACTION_REQUEST_LOCATION").apply {
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            startActivity(intent)
        }

        findViewById<View>(R.id.display_location)?.setOnClickListener {
            val intent = Intent("com.androidworks.navsys.wuffit.ACTION_DISPLAY_LOCATION").apply {
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            startActivity(intent)
        }

        findViewById<View>(R.id.exit)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            startActivity(intent)
        }
    }
}

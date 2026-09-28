package com.intellibitz.mobile.dating

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ViewFlipper

class Dating : Activity() {

    private var mFlipper: ViewFlipper? = null
    private var mContainer: ViewGroup? = null
    private var intent: Intent? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setTheme(android.R.style.Theme_ContextMenu)
        setContentView(R.layout.welcome)
        mFlipper = findViewById(R.id.flipper) as ViewFlipper
        mFlipper!!.startFlipping()
        val icon1 = findViewById(R.id.icon1) as ImageView
        val icon2 = findViewById(R.id.icon2) as ImageView
        val icon3 = findViewById(R.id.icon3) as ImageView
        val icon4 = findViewById(R.id.icon4) as ImageView
        val icon5 = findViewById(R.id.icon5) as ImageView
        val icon6 = findViewById(R.id.icon6) as ImageView
        mContainer = findViewById(R.id.container) as ViewGroup
        (mContainer!!.parent as ViewGroup).setKeepAnimations(true)

        icon1.setOnClickListener {
            intent = Intent(this@Dating, Seeker::class.java)
            startActivity(intent)
        }
        icon2.setOnClickListener {
            val intent1 = Intent(this@Dating, DatingServiceController::class.java)
            startActivity(intent1)
        }
        icon3.setOnClickListener {
            val intent2 = Intent(this@Dating, Map::class.java)
            startActivity(intent2)
        }
        icon4.setOnClickListener {
            val intent3 = Intent(this@Dating, MatchingData::class.java)
            startActivity(intent3)
        }
        icon5.setOnClickListener {
            val intent4 = Intent(this@Dating, Help::class.java)
            startActivity(intent4)
        }

        icon6.setOnClickListener {
            finish()
        }
    }
}

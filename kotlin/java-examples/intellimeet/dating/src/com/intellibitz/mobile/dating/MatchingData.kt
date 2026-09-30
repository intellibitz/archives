package com.intellibitz.mobile.dating

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ViewFlipper
import com.google.android.maps.MapView
import com.google.android.maps.Point

/**
 *
 * @author
 */
class MatchingData : Activity() {

    private var mMapView: MapView? = null
    private var mFlipper: ViewFlipper? = null
    private var p1: Point? = null
    private var p2: Point? = null
    private var p3: Point? = null
    private var p4: Point? = null
    private var p5: Point? = null
    private var bubbleIcon: Bitmap? = null
    private var bubbleIcon1: Bitmap? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.matchingprofile)
        mFlipper = findViewById(R.id.flipper) as ViewFlipper
        mFlipper!!.startFlipping()
        bubbleIcon = BitmapFactory.decodeResource(resources, R.drawable.bubble)
        bubbleIcon1 = BitmapFactory.decodeResource(resources, R.drawable.bubble1)
        val button = findViewById(R.id.chating) as Button
        val showmap = findViewById(R.id.viewmap) as Button
        button.setOnClickListener {
            val intobject = Intent(this@MatchingData, MatchingProfile::class.java)
            startActivity(intobject)
        }

        showmap.setOnClickListener {
            val intobject1 = Intent(this@MatchingData, Map::class.java)
            startActivity(intobject1)
        }
    }
}

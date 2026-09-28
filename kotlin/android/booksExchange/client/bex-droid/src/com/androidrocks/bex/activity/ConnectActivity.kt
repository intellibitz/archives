/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 */

package com.androidrocks.bex.activity

import android.os.Bundle
import com.androidrocks.bex.R
import com.google.android.maps.MapActivity
import com.google.android.maps.MapView

class ConnectActivity : MapActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connect)
        setupViews()
    }

    override fun isRouteDisplayed(): Boolean {
        return false // To change body of implemented methods use File | Settings | File Templates.
    }

    private fun setupViews() {
        val mapView = findViewById<MapView>(R.id.mapview)
        mapView.setBuiltInZoomControls(true)
        mapView.controller.setZoom(18)
    }
}

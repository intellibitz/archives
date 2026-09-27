/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 */

package com.androidrocks.bex.activity;

import android.app.Activity;
import android.os.Bundle;

import com.androidrocks.bex.R;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

/**
 * Created by IntelliJ IDEA.
 * User: muthu
 * Date: Aug 7, 2009
 * Time: 7:33:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectActivity extends MapActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect);
		setupViews();
    }

	protected boolean isRouteDisplayed() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

	private void setupViews() {
		// TODO Auto-generated method stub
		MapView mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(18);
	}

}

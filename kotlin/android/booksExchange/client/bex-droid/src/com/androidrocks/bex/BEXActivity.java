package com.androidrocks.bex;

/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 */

import com.androidrocks.bex.R;
import com.androidrocks.bex.activity.ConnectActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class BEXActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		setupViews();
	}

	private void setupViews() {
		// setup click listeners for buttons
		findViewById(R.id.list_stuff).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Go to List stuff screen
				Intent intent = new Intent(
						"com.androidworks.bex.intent.action.VIEW_SHELVES");
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent
						.setType("vnd.android.cursor.dir/vnd.com.androidrocks.bex.provider.list");
				startActivity(intent);
			}
		});

		// setup click listeners for buttons
		findViewById(R.id.connect_friends).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						// Go to Connect screen
						Intent intent = new Intent(ConnectActivity.class
								.getName());
						intent.addCategory(Intent.CATEGORY_DEFAULT);
						startActivity(intent);
					}
				});

	}
}

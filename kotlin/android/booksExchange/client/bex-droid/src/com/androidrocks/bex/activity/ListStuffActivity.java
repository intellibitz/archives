/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 */

/**
 * 
 */
package com.androidrocks.bex.activity;

import com.androidrocks.bex.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @author muthu
 *
 */
public class ListStuffActivity extends Activity {

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_stuff);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_stuff, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_add_search:
                onAddSearch();
                return true;
            case R.id.menu_item_add:
                onAdd();
                return true;
            case R.id.menu_item_check:
                onCheck();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    private void onAddSearch() {
//        AddBookActivity.show(this);
    }

    private void onAdd() {
//        startScan(REQUEST_SCAN_FOR_ADD);
    }

    private void onCheck() {
//        startScan(REQUEST_SCAN_FOR_CHECK);
    }


}

/*
 * Copyright (C) 2009 Muthu Ramadoss. All rights reserved.
 *
 */

package com.androidrocks.bex.activity

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.androidrocks.bex.R

class ListStuffActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_stuff)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.list_stuff, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_add_search -> {
                onAddSearch()
                return true
            }
            R.id.menu_item_add -> {
                onAdd()
                return true
            }
            R.id.menu_item_check -> {
                onCheck()
                return true
            }
        }
        return super.onMenuItemSelected(featureId, item)
    }

    private fun onAddSearch() {
        // AddBookActivity.show(this);
    }

    private fun onAdd() {
        // startScan(REQUEST_SCAN_FOR_ADD);
    }

    private fun onCheck() {
        // startScan(REQUEST_SCAN_FOR_CHECK);
    }
}

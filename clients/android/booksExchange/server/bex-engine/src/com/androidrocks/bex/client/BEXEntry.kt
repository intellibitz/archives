package com.androidrocks.bex.client

import com.google.gwt.core.client.EntryPoint
import com.google.gwt.user.client.ui.RootPanel

/**
 * The entry point class which performs the initial loading of the DynaTable
 * application.
 */
class BEXEntry : EntryPoint {

    override fun onModuleLoad() {
        // Find the slot for the calendar widget.
        //
        val slot = RootPanel.get("reports")
        if (slot != null) {
        }
    }
}

package com.androidrocks.bex.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * The entry point class which performs the initial loading of the DynaTable
 * application.
 */
public class BEXEntry implements EntryPoint {

  public void onModuleLoad() {
    // Find the slot for the calendar widget.
    //
    RootPanel slot = RootPanel.get("reports");
    if (slot != null) {
    }
  }
}

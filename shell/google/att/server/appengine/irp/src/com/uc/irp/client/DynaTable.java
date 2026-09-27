package com.uc.irp.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * The entry point class which performs the initial loading of the DynaTable
 * application.
 */
public class DynaTable implements EntryPoint {

  public void onModuleLoad() {
    // Find the slot for the calendar widget.
    //
    RootPanel slot = RootPanel.get("reports");
    if (slot != null) {
      IncidentReportsWidget reports = new IncidentReportsWidget(15);
      slot.add(reports);

      // Find the slot for the days filter widget.
      //
      slot = RootPanel.get("events");
      if (slot != null) {
        EventFilterWidget filter = new EventFilterWidget(reports);
        slot.add(filter);
      }
    }
  }
}

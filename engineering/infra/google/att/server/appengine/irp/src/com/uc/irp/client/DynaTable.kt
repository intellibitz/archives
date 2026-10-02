package com.uc.irp.client

import com.google.gwt.core.client.EntryPoint
import com.google.gwt.user.client.ui.RootPanel

/**
 * The entry point class which performs the initial loading of the DynaTable
 * application.
 */
class DynaTable : EntryPoint {

    override fun onModuleLoad() {
        // Find the slot for the calendar widget.
        //
        var slot = RootPanel.get("reports")
        if (slot != null) {
            val reports = IncidentReportsWidget(15)
            slot.add(reports)

            // Find the slot for the days filter widget.
            //
            slot = RootPanel.get("events")
            if (slot != null) {
                val filter = EventFilterWidget(reports)
                slot.add(filter)
            }
        }
    }
}

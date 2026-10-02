package com.uc.irp.client

import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.event.dom.client.ClickHandler
import com.google.gwt.user.client.ui.Button
import com.google.gwt.user.client.ui.CheckBox
import com.google.gwt.user.client.ui.Composite
import com.google.gwt.user.client.ui.HasAlignment
import com.google.gwt.user.client.ui.HorizontalPanel
import com.google.gwt.user.client.ui.VerticalPanel
import com.google.gwt.user.client.ui.Widget

/**
 * A UI Widget that allows a user to filter the days being displayed in the
 * dynamic table.
 */
class EventFilterWidget(private val reports: IncidentReportsWidget) : Composite() {

    private inner class EventCheckBox(caption: String, val event: Int) : CheckBox(caption) {
        init {
            // Use a shared handler to save memory.
            addClickHandler(eventCheckBoxHandler)

            // Initialize based on the reports's current value.
            value = reports.getEventIncluded(event)
        }
    }

    private inner class EventCheckBoxHandler : ClickHandler {
        override fun onClick(event: ClickEvent) {
            onClick(event.source as EventCheckBox)
        }

        fun onClick(eventCheckBox: EventCheckBox) {
            reports.setEventIncluded(eventCheckBox.event, eventCheckBox.value)
        }
    }

    private val outer = VerticalPanel()

    private val eventCheckBoxHandler = EventCheckBoxHandler()

    init {
        initWidget(outer)
        setStyleName("DynaTable-EventFilterWidget")
        outer.add(EventCheckBox("Service State", 0))
        outer.add(EventCheckBox("Call State", 1))
        outer.add(EventCheckBox("Signal State", 2))
        outer.add(EventCheckBox("DataConnection State", 3))

        val buttonAll = Button("All", ClickHandler { setAllCheckBoxes(true) })

        val buttonNone = Button("None", ClickHandler { setAllCheckBoxes(false) })

        val hp = HorizontalPanel()
        hp.setHorizontalAlignment(HasAlignment.ALIGN_CENTER)
        hp.add(buttonAll)
        hp.add(buttonNone)

        outer.add(hp)
        outer.setCellVerticalAlignment(hp, HasAlignment.ALIGN_BOTTOM)
        outer.setCellHorizontalAlignment(hp, HasAlignment.ALIGN_CENTER)
    }

    private fun setAllCheckBoxes(checked: Boolean) {
        var i = 0
        val n = outer.widgetCount
        while (i < n) {
            val w = outer.getWidget(i)
            if (w is EventCheckBox) {
                w.value = checked
                eventCheckBoxHandler.onClick(w)
            }
            ++i
        }
    }
}

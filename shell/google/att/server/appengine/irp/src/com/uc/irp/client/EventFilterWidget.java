package com.uc.irp.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A UI Widget that allows a user to filter the days being displayed in the
 * dynamic table.
 */
public class EventFilterWidget extends Composite {

  private class EventCheckBox extends CheckBox {
    public final int event;

    public EventCheckBox(String caption, int event) {
      super(caption);

      // Remember custom data for this widget.
      this.event = event;

      // Use a shared handler to save memory.
      addClickHandler(eventCheckBoxHandler);

      // Initialize based on the reports's current value.
      setValue(reports.getEventIncluded(event));
    }
  }

  private class EventCheckBoxHandler implements ClickHandler {
    public void onClick(ClickEvent event) {
      onClick((EventCheckBox) event.getSource());
    }

    public void onClick(EventCheckBox eventCheckBox) {
      reports.setEventIncluded(eventCheckBox.event, eventCheckBox.getValue());
    }
  }

  private final IncidentReportsWidget reports;

  private final VerticalPanel outer = new VerticalPanel();

  private final EventCheckBoxHandler eventCheckBoxHandler = new EventCheckBoxHandler();

  public EventFilterWidget(IncidentReportsWidget reports) {
    this.reports = reports;
    initWidget(outer);
    setStyleName("DynaTable-EventFilterWidget");
    outer.add(new EventCheckBox("Service State", 0));
    outer.add(new EventCheckBox("Call State", 1));
    outer.add(new EventCheckBox("Signal State", 2));
    outer.add(new EventCheckBox("DataConnection State", 3));

    Button buttonAll = new Button("All", new ClickHandler() {
      public void onClick(ClickEvent event) {
        setAllCheckBoxes(true);
      }
    });

    Button buttonNone = new Button("None", new ClickHandler() {
      public void onClick(ClickEvent event) {
        setAllCheckBoxes(false);
      }
    });

    HorizontalPanel hp = new HorizontalPanel();
    hp.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
    hp.add(buttonAll);
    hp.add(buttonNone);

    outer.add(hp);
    outer.setCellVerticalAlignment(hp, HasAlignment.ALIGN_BOTTOM);
    outer.setCellHorizontalAlignment(hp, HasAlignment.ALIGN_CENTER);
  }

  private void setAllCheckBoxes(boolean checked) {
    for (int i = 0, n = outer.getWidgetCount(); i < n; ++i) {
      Widget w = outer.getWidget(i);
      if (w instanceof EventCheckBox) {
        ((EventCheckBox) w).setValue(checked);
        eventCheckBoxHandler.onClick((EventCheckBox) w);
      }
    }
  }
}

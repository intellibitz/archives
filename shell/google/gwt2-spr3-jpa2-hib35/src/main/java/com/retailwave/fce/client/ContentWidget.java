package com.retailwave.fce.client;
/**
 * $Id: ContentWidget.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/ContentWidget.java $
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p/>
 * A widget used to display FCE content view.
 */
public abstract class ContentWidget
        extends ResizeComposite {

    protected TabLayoutPanel tabLayoutPanel;
    protected ArrayList<HashMap<String, Widget>> childWidgets = new ArrayList<HashMap<String, Widget>>(5);

    @UiField
    DockLayoutPanel dockLayoutPanel;
    @UiField
    Label name;
    @UiField
    Label description;
    @UiField
    protected DockLayoutPanel contentDockLayoutPanel;

    @UiTemplate("com.retailwave.fce.client.ContentWidget.ui.xml")
    interface Binder extends UiBinder<Widget, ContentWidget> {
    }

    private static final Binder binder = GWT.create(Binder.class);


    /**
     * Constructor.
     */
    public ContentWidget() {
        binder.createAndBindUi(this);
// hide the tabs.. todo: revisit for better design       
        tabLayoutPanel = new TabLayoutPanel(0, Style.Unit.PX);
        initWidget(tabLayoutPanel);
    }

    public TabLayoutPanel getTabLayoutPanel() {
        return tabLayoutPanel;
    }

    /**
     * Get the description of this example.
     *
     * @return a description for this example
     */
    public abstract String getDescription();

    /**
     * Get the name of this example to use as a title.
     *
     * @return a name for this example
     */
    public abstract String getName();

    /**
     * When the widget is first initialized, this method is called. If it returns
     * a Widget, the widget will be added as the first tab. Return null to disable
     * the first tab.
     *
     * @return the widget to add to the first tab
     */
    public abstract Widget onInitialize();

    /**
     * called when the menu for this content is selected
     */
    public void onMenuSelection() {
    }

    /**
     * @return String[] the history tokens to be mapped to menu items
     */
    abstract public String[] getHistoryTokens();

    public void add(Widget widget, String name) {
        final HashMap<String, Widget> widgetHashMap = new HashMap<String, Widget>(1);
        widgetHashMap.put(name, widget);
        childWidgets.add(widgetHashMap);
    }

    /**
     * Initialize this widget by creating the elements that should be added to the page.
     *
     * @return Widget the current content
     */
    protected final Widget createWidget() {
        name.setText(getName());
        description.setText(getDescription());
        dockLayoutPanel.setTitle(name.getText());

        tabLayoutPanel.add(dockLayoutPanel, name.getText());

// add the child widgets
        for (HashMap<String, Widget> widgets : childWidgets) {
            tabLayoutPanel.add(widgets.values().iterator().next(), widgets.keySet().iterator().next());
        }
// remove the local reference to widgets, since they are already contained in this widget now
        childWidgets.clear();

        contentDockLayoutPanel.add(onInitialize());

        return this;
    }

    @Override
    protected void onLoad() {
        ensureWidget();
        // Select the first tab, if no history
        // if history available, select the correct tab
        String hist = History.getToken();
        int count = tabLayoutPanel.getWidgetCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                final Label tabWidget = (Label) tabLayoutPanel.getTabWidget(i);
                if (tabWidget.getText().equals(hist)) {
                    tabLayoutPanel.selectTab(i);
                    return;
                }
            }
            tabLayoutPanel.selectTab(0);
        }
    }

    // from LazyPanel

    /**
     * Ensures that the widget has been created by calling {@link #createWidget}
     * if {@link #getWidget} returns <code>null</code>. Typically it is not
     * necessary to call this directly, as it is called as a side effect of a
     * <code>setVisible(true)</code> call.
     */
    public void ensureWidget() {
        Widget widget = null;
        if (tabLayoutPanel.getWidgetCount() > 0) {
            widget = tabLayoutPanel.getWidget(0);
        }
// if either no content, or the content is from the sub widgets.. create this content       
        if (null == widget) {
            createWidget();
        }
    }

}

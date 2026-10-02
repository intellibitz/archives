package com.retailwave.fce.client
/**
 * $Id: ContentWidget.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/ContentWidget.java $
 */

import com.google.gwt.core.client.GWT
import com.google.gwt.dom.client.Style
import com.google.gwt.uibinder.client.UiBinder
import com.google.gwt.uibinder.client.UiField
import com.google.gwt.uibinder.client.UiTemplate
import com.google.gwt.user.client.History
import com.google.gwt.user.client.ui.*

import java.util.ArrayList
import java.util.HashMap

/**
 * <p/>
 * A widget used to display FCE content view.
 */
open class ContentWidget : ResizeComposite() {

    protected var tabLayoutPanel: TabLayoutPanel? = null
    protected var childWidgets: ArrayList<HashMap<String, Widget>> = ArrayList<HashMap<String, Widget>>(5)

    @UiField
    var dockLayoutPanel: DockLayoutPanel? = null
    @UiField
    var name: Label? = null
    @UiField
    var description: Label? = null
    @UiField
    var var: protected? = null contentDockLayoutPanel: DockLayoutPanel? = null

    @UiTemplate("com.retailwave.fce.client.ContentWidget.ui.xml")
    interface Binder : UiBinder<Widget, ContentWidget> {
    }

    private var binder: Binder  = GWT.create(Binder::class.java)

    /**
     * Constructor.
     */
    constructor() {
        binder.createAndBindUi(this)
// hide the tabs.. todo: revisit for better design       
        tabLayoutPanel = TabLayoutPanel(0, Style.Unit.PX)
        initWidget(tabLayoutPanel)
    }

    fun getTabLayoutPanel(): TabLayoutPanel {
        return tabLayoutPanel
    }

    /**
     * Get the description of this example.
     *
     * @return a description for this example
     */
    public abstract String getDescription()

    /**
     * Get the name of this example to use as a title.
     *
     * @return a name for this example
     */
    public abstract String getName()

    /**
     * When the widget is first initialized, this method is called. If it returns
     * a Widget, the widget will be added as the first tab. Return null to disable
     * the first tab.
     *
     * @return the widget to add to the first tab
     */
    public abstract Widget onInitialize()

    /**
     * called when the menu for this content is selected
     */
    fun onMenuSelection() {
    }

    /**
     * @return String[] the history tokens to be mapped to menu items
     */
    abstract public String[] getHistoryTokens()

    fun add(widget: Widget, name: String) {
        val widgetHashMap = HashMap<String, Widget>(1)
        widgetHashMap.put(name, widget)
        childWidgets.add(widgetHashMap)
    }

    /**
     * Initialize this widget by creating the elements that should be added to the page.
     *
     * @return Widget the current content
     */
    fun createWidget(): Widget {
        name.setText(getName())
        description.setText(getDescription())
        dockLayoutPanel.setTitle(name.getText())

        tabLayoutPanel.add(dockLayoutPanel, name.getText())

// add the child widgets
        for (widgets in childWidgets) {
            tabLayoutPanel.add(widgets.values().iterator().next(), widgets.keySet().iterator().next())
        }
// remove the local reference to widgets, since they are already contained in this widget now
        childWidgets.clear()

        contentDockLayoutPanel.add(onInitialize())

        return this
    }

        protected fun onLoad(): override fun {
        ensureWidget()
        // Select the first tab, if no history
        // if history available, select the correct tab
        val hist = History.getToken()
        var count = tabLayoutPanel.getWidgetCount()
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                val tabWidget = (Label) tabLayoutPanel.getTabWidget(i)
                if (tabWidget.getText().equals(hist)) {
                    tabLayoutPanel.selectTab(i)
                    return
                }
            }
            tabLayoutPanel.selectTab(0)
        }
    }

    // from LazyPanel

    /**
     * Ensures that the widget has been created by calling {@link #createWidget}
     * if {@link #getWidget} returns <code>null</code>. Typically it is not
     * necessary to call this directly, as it is called as a side effect of a
     * <code>setVisible(true)</code> call.
     */
    fun ensureWidget() {
        val widget = null
        if (tabLayoutPanel.getWidgetCount() > 0) {
            widget = tabLayoutPanel.getWidget(0)
        }
// if either no content, or the content is from the sub widgets.. create this content       
        if (null == widget) {
            createWidget()
        }
    }

}
package com.retailwave.fce.client
/**
 * $Id: Application.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/Application.java $
 */

import com.google.gwt.core.client.GWT
import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.event.logical.shared.HasSelectionHandlers
import com.google.gwt.event.logical.shared.SelectionHandler
import com.google.gwt.event.shared.HandlerRegistration
import com.google.gwt.uibinder.client.UiBinder
import com.google.gwt.uibinder.client.UiField
import com.google.gwt.uibinder.client.UiHandler
import com.google.gwt.user.client.Timer
import com.google.gwt.user.client.ui.*
import com.google.gwt.widgetideas.client.ProgressBar
import com.retailwave.fce.client.data.CommandResult

/**
 * Application
 * <p/>
 * The FCE application that includes a titlePanel bar, main menu, content area, and footer.
 */
open class Application : ResizeComposite(), HasSelectionHandlers<TreeItem> {

    private var commandResult: CommandResult? = null

    @UiField
    var layout: DockLayoutPanel? = null
    @UiField
    var hide: Button? = null
    @UiField
    var userName: Label? = null
    @UiField
    var messageLabel: Label? = null
    @UiField
    var messagePanel: DockLayoutPanel? = null
    @UiField
    var mainMenu: Tree? = null
    @UiField
    var progressBar: ProgressBar? = null

    interface Binder : UiBinder<Widget, Application> {
    }

    private var binder: Binder  = GWT.create(Binder::class.java)

    /**
     * Constructor.
     */
    constructor() {
        initWidget(binder.createAndBindUi(this))
        commandResult = CommandResult()
    }

    var progress = 0
    val progressTimer = object : Timer() {
            override fun run() {
            progress = progress + 10
            progressBar.setProgress(progress)
            if (100 == progress) {
                progress = 0
            }
        }
    }

    fun showProgress(msg: String, ms: Int) {
        progress = 0
        progressBar.setTitle(msg)
        progressTimer.scheduleRepeating(ms)
        val txt = messageLabel.getText()
        if (null == txt || 0 == txt.length) {
            messageLabel.setText(msg)
        }
        messagePanel.setVisible(true)
    }

    fun cancelProgress() {
        progressTimer.cancel()
        progress = 0
        progressBar.setProgress(100)
        hideMessage()
    }

    fun addSelectionHandler(handler: SelectionHandler<TreeItem>): HandlerRegistration {
        return mainMenu.addSelectionHandler(handler)
    }

    /**
     * @return the main menu.
     */
    fun getMainMenu(): Tree {
        return mainMenu
    }

    fun welcome(t: String) {
        userName.setText(t)
    }

    fun showMessage(msg: String) {
        messageLabel.setText(msg)
        messagePanel.setVisible(true)
    }

    fun hideMessage() {
        messageLabel.setText("")
        messagePanel.setVisible(false)
    }

    fun getCommandResult(): CommandResult {
        return commandResult
    }

    fun setCommandResult(commandResult: CommandResult) {
        this.commandResult = commandResult
    }

    @UiHandler("hide")
    void handleClick(ClickEvent e) {
        hideMessage()
    }

    fun getContentWidget(): ContentWidget {
        if (layout.getWidgetCount() == 4) {
            return (ContentWidget) layout.getWidget(3)
        }
        return null
    }

    /**
     * Set the {@link Widget} to display in the content area.
     *
     * @param contentWidget the content widget
     */
    fun setContentWidget(contentWidget: ContentWidget) {
        if (layout.getWidgetCount() == 4) {
            layout.remove(3)
        }
        layout.add(contentWidget)
    }

    companion object {
        private val binder: Binder = GWT.create(Binder::class.java)
    }

}
package com.retailwave.fce.client;
/**
 * $Id: Application.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/Application.java $
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.widgetideas.client.ProgressBar;
import com.retailwave.fce.client.data.CommandResult;

/**
 * Application
 * <p/>
 * The FCE application that includes a titlePanel bar, main menu, content area, and footer.
 */
public class Application
        extends ResizeComposite
        implements HasSelectionHandlers<TreeItem> {

    private CommandResult commandResult;

    @UiField
    DockLayoutPanel layout;
    @UiField
    Button hide;
    @UiField
    Label userName;
    @UiField
    Label messageLabel;
    @UiField
    DockLayoutPanel messagePanel;
    @UiField
    Tree mainMenu;
    @UiField
    ProgressBar progressBar;


    interface Binder extends UiBinder<Widget, Application> {
    }

    private static final Binder binder = GWT.create(Binder.class);

    /**
     * Constructor.
     */
    public Application() {
        initWidget(binder.createAndBindUi(this));
        commandResult = new CommandResult();
    }

    int progress = 0;
    Timer progressTimer = new Timer() {
        @Override
        public void run() {
            progress = progress + 10;
            progressBar.setProgress(progress);
            if (100 == progress) {
                progress = 0;
            }
        }
    };

    public void showProgress(String msg, int ms) {
        progress = 0;
        progressBar.setTitle(msg);
        progressTimer.scheduleRepeating(ms);
        final String txt = messageLabel.getText();
        if (null == txt || 0 == txt.length()) {
            messageLabel.setText(msg);
        }
        messagePanel.setVisible(true);
    }

    public void cancelProgress() {
        progressTimer.cancel();
        progress = 0;
        progressBar.setProgress(100);
        hideMessage();
    }

    public HandlerRegistration addSelectionHandler(
            SelectionHandler<TreeItem> handler) {
        return mainMenu.addSelectionHandler(handler);
    }

    /**
     * @return the main menu.
     */
    public Tree getMainMenu() {
        return mainMenu;
    }

    public void welcome(String t) {
        userName.setText(t);
    }


    public void showMessage(String msg) {
        messageLabel.setText(msg);
        messagePanel.setVisible(true);
    }

    public void hideMessage() {
        messageLabel.setText("");
        messagePanel.setVisible(false);
    }

    public CommandResult getCommandResult() {
        return commandResult;
    }

    public void setCommandResult(CommandResult commandResult) {
        this.commandResult = commandResult;
    }

    @UiHandler("hide")
    void handleClick(ClickEvent e) {
        hideMessage();
    }

    public ContentWidget getContentWidget() {
        if (layout.getWidgetCount() == 4) {
            return (ContentWidget) layout.getWidget(3);
        }
        return null;
    }

    /**
     * Set the {@link Widget} to display in the content area.
     *
     * @param contentWidget the content widget
     */
    public void setContentWidget(ContentWidget contentWidget) {
        if (layout.getWidgetCount() == 4) {
            layout.remove(3);
        }
        layout.add(contentWidget);
    }

}

package com.retailwave.fce.client.content.user;
/**
 * $Id: AddUser.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/content/user/AddUser.java $
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.*;
import com.retailwave.fce.client.ContentWidget;
import com.retailwave.fce.client.content.i18n.AddUserConstants;
import com.retailwave.fce.client.content.i18n.UIConstants;
import com.retailwave.fce.client.presenter.AddUserPresenter;
import com.retailwave.fce.client.util.UIHelper;
import com.retailwave.fce.client.validator.PersonNameValidator;
import eu.maydu.gwt.validation.client.DefaultValidationProcessor;
import eu.maydu.gwt.validation.client.ValidationProcessor;
import eu.maydu.gwt.validation.client.validators.standard.NotEmptyValidator;
import eu.maydu.gwt.validation.client.validators.strings.EmailValidator;

/**
 * AddUser
 * <p/>
 * Screen to add a user
 */
public class AddUser extends ContentWidget
        implements ClickHandler, KeyUpHandler, AddUserPresenter.Display {

    static final UIConstants uiConstants = UIHelper.getUiConstants();
    static final AddUserConstants myConstants = GWT.create(AddUserConstants.class);

    public static final String ID_PREFIX = myConstants.idPrefix();

    public static String shortName = myConstants.shortName();
    public static String fullName = myConstants.fullName();
    public static String email = myConstants.email();
    public static String active = myConstants.active();
    public static String role = myConstants.role();
    public static String program = myConstants.program();
    public static String country = myConstants.country();

    public static String shortNameId = ID_PREFIX + shortName;
    public static String fullNameId = ID_PREFIX + fullName;
    public static String emailId = ID_PREFIX + email;
    public static String activeId = ID_PREFIX + active;
    public static String roleId = ID_PREFIX + role;
    public static String programId = ID_PREFIX + program;
    public static String countryId = ID_PREFIX + country;

    static String[] commandButtons = uiConstants.saveClear();
    static String[] okCancel = uiConstants.okCancel();
    ClickHandler hideHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            yesNoDialog.hide();
        }
    };
    DialogBox yesNoDialog = UIHelper.createDialogBox
            (myConstants.title(), uiConstants.saveChangesDialog(), okCancel, hideHandler);
    private HasClickHandlers ok;
    private HasClickHandlers cancel;

    ValidationProcessor validationProcessor = new DefaultValidationProcessor();
    private FlowPanel flowPanel = new FlowPanel();


    /**
     * Constructor.
     */
    public AddUser() {
        super();
    }

    @Override
    public String[] getHistoryTokens() {
        return new String[]{getName()};
    }

    @Override
    public String getDescription() {
        return myConstants.description();
    }

    @Override
    public String getName() {
        return myConstants.title();
    }

    /**
     * Creates widget, to display in parent
     */
    @Override
    public Widget onInitialize() {
        flowPanel.setTitle(getName());

        flowPanel.add(UIHelper.createTextInput(shortName, shortNameId, 12, false, ID_PREFIX,
                this, validationProcessor, NotEmptyValidator.class.getName(), PersonNameValidator.class.getName()));
        flowPanel.add(UIHelper.createTextInput(fullName, fullNameId, 50, false, ID_PREFIX,
                this, validationProcessor, NotEmptyValidator.class.getName(), PersonNameValidator.class.getName()));
        flowPanel.add(UIHelper.createTextInput(email, emailId, 50, false, ID_PREFIX,
                this, validationProcessor, EmailValidator.class.getName()));
        flowPanel.add(UIHelper.createCheckInput(active, activeId, false, ID_PREFIX));
        ((CheckBox) UIHelper.getFromInputCache(activeId, ID_PREFIX)).setValue(true);
        flowPanel.add(UIHelper.createListInput(program, programId, false, null, false, ID_PREFIX));
        flowPanel.add(UIHelper.createListInput(role, roleId, false, null, false, ID_PREFIX));
        flowPanel.add(UIHelper.createListInput(country, countryId, false, null, false, ID_PREFIX));
        flowPanel.add(UIHelper.createCommands(commandButtons, this));

        final FlowPanel p = (FlowPanel) ((FlowPanel) yesNoDialog.getWidget()).getWidget(1);
        ok = (HasClickHandlers) p.getWidget(0);
        cancel = (HasClickHandlers) p.getWidget(1);

        return flowPanel;
    }

    private void saveAction() {
        if (validationProcessor.validate()) {
            UIHelper.confirm(yesNoDialog, (Button) cancel);
        }
    }

    private void clearAction() {
        UIHelper.clearInputs(flowPanel);
        validationProcessor.reset();
        UIHelper.setFocus(flowPanel);
    }


    @Override
    public void onClick(ClickEvent clickEvent) {
        // note that in general, events can have sources that are not Widgets.
        Widget sender = (Widget) clickEvent.getSource();
        if (Button.class == sender.getClass()) {
            if (commandButtons[0].equals(sender.getTitle())) {
                saveAction();
            } else if (commandButtons[1].equals(sender.getTitle())) {
                clearAction();
            }
        }
    }

    /**
     * Called when KeyUpEvent is fired.
     *
     * @param event the {@link com.google.gwt.event.dom.client.KeyUpEvent} that was fired
     */
    @Override
    public void onKeyUp(KeyUpEvent event) {
        validationProcessor.validate(((Widget) event.getSource()).getTitle());
    }

    @Override
    public HasClickHandlers getSaveButton() {
        return ok;
    }

}

package com.retailwave.fce.client.content.user;
/**
 * $Id: SearchModifyUser.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/content/user/SearchModifyUser.java $
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.retailwave.fce.client.content.i18n.SearchModifyUserConstants;
import com.retailwave.fce.client.content.i18n.UIConstants;
import com.retailwave.fce.shared.dto.UserDTO;
import com.retailwave.fce.client.util.UIHelper;
import com.retailwave.fce.client.validator.PersonNameValidator;
import com.retailwave.fce.shared.rpc.UserServiceRemote;
import com.retailwave.fce.shared.rpc.UserServiceRemoteAsync;
import eu.maydu.gwt.validation.client.DefaultValidationProcessor;
import eu.maydu.gwt.validation.client.ValidationProcessor;
import eu.maydu.gwt.validation.client.validators.standard.NotEmptyValidator;
import eu.maydu.gwt.validation.client.validators.strings.EmailValidator;

public class SearchModifyUser
        extends ResizeComposite
        implements ClickHandler {

    static final UIConstants uiConstants = UIHelper.getUiConstants();
    static final SearchModifyUserConstants myConstants = GWT.create(SearchModifyUserConstants.class);
    static final String ID_PREFIX = myConstants.idPrefix();

    private Button edit;
    private Button activate;
    private Button deActivate;
    private Button save;
    private Button clear;
    private Button backToSearch;
    ValidationProcessor validationProcessor = new DefaultValidationProcessor();
    UserServiceRemoteAsync userServiceAsync = UserServiceRemote.App.getInstance();

    ClickHandler[] handlers = new ClickHandler[]{
            new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    save(modifiedUserDTO);
                    afterOkClicked();
                }
            },
            new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    yesNoDialog.hide();
                    afterCancelClicked();
                }
            }
    };

    String[] commandButtons = myConstants.commands();
    String[] okCancel = uiConstants.okCancel();
    DialogBox yesNoDialog = UIHelper.createDialogBox(myConstants.title(), uiConstants.saveChangesDialog(), okCancel, handlers);
    private HasClickHandlers ok;
    private HasClickHandlers cancel;

    private UserDTO savedUserDTO;
    private UserDTO modifiedUserDTO;

    private enum ACTION_STATE {
        VIEW_ACTION, EDIT_ACTION, ACTIVATE_ACTION, CANCEL_ACTION, SAVE_ACTION, DEACTIVATE_ACTION, BACKTOSEARCH_ACTION;
    }

    private ACTION_STATE actionState = ACTION_STATE.VIEW_ACTION;

    @UiField
    Label name;
    @UiField
    Label description;
    @UiField
    DockLayoutPanel dockLayoutPanel;
    @UiField
    DockLayoutPanel contentDockLayoutPanel;

    private FlowPanel flowPanel = new FlowPanel();

    @UiTemplate("com.retailwave.fce.client.ContentWidget.ui.xml")
    interface Binder extends UiBinder<Widget, SearchModifyUser> {
    }

    private static final Binder binder = GWT.create(Binder.class);

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

    /**
     * Constructor.
     */
    public SearchModifyUser() {
        super();
        initWidget(binder.createAndBindUi(this));
    }

    public String getDescription() {
        return myConstants.description();
    }

    public String getName() {
        return myConstants.title();
    }

    /**
     * @return Widget Creates widget, to display in parent
     */
    public Widget onInitialize() {

        UIHelper.clearInputCache(ID_PREFIX);
        name.setText(getName());
        description.setText(getDescription());
        setTitle(getName());

        flowPanel.add(UIHelper.createTextInput(shortName, shortNameId, 12, false, ID_PREFIX,
                null, validationProcessor, NotEmptyValidator.class.getName(), PersonNameValidator.class.getName()));
        flowPanel.add(UIHelper.createTextInput(fullName, fullNameId, 50, false, ID_PREFIX,
                null, validationProcessor, NotEmptyValidator.class.getName(), PersonNameValidator.class.getName()));
        flowPanel.add(UIHelper.createTextInput(email, emailId, 50, false, ID_PREFIX,
                null, validationProcessor, EmailValidator.class.getName()));
        flowPanel.add(UIHelper.createCheckInput(active, activeId, false, ID_PREFIX));
        flowPanel.add(UIHelper.createListInput(program, programId, false, null, false, ID_PREFIX));
        flowPanel.add(UIHelper.createListInput(role, roleId, false, null, false, ID_PREFIX));
        flowPanel.add(UIHelper.createListInput(country, countryId, false, null, false, ID_PREFIX));

        flowPanel.add(addCommands());

        final FlowPanel p = (FlowPanel) ((FlowPanel) yesNoDialog.getWidget()).getWidget(1);
        ok = (HasClickHandlers) p.getWidget(0);
        cancel = (HasClickHandlers) p.getWidget(1);

// present in view mode
        viewAction();
// add the flowPanel panel which contains inputs and okCancel to the outer dockLayoutPanel panel
        contentDockLayoutPanel.add(flowPanel);
        return this;
    }

    private Widget addCommands() {
        FlowPanel commandPanel = new FlowPanel();
        commandPanel.addStyleName(UIHelper.style.commandPanel());
        for (String text : commandButtons) {
            Button b = new Button();
            /**
             * HorizontalPanel is a bit trickier. In some cases, you can simply replace it with a DockLayoutPanel,
             * but that requires that you specify its childrens' widths explicitly. The most common alternative is to use
             * FlowPanel, and to use the float: left; CSS property on its children.
             * And of course, you can continue to use HorizontalPanel itself, as long as you take the caveats above into account.
             */
            b.addStyleName(UIHelper.style.commandPanelButtonDefWidth());
            b.addStyleName(UIHelper.style.floatLeft());
            b.setTitle(text);
            b.setText(text);
            b.addClickHandler(this);
            commandPanel.add(b);
        }
        edit = (Button) commandPanel.getWidget(0);
        save = (Button) commandPanel.getWidget(1);
        clear = (Button) commandPanel.getWidget(2);
        activate = (Button) commandPanel.getWidget(3);
        deActivate = (Button) commandPanel.getWidget(4);
        backToSearch = (Button) commandPanel.getWidget(5);

        activate.setVisible(false);
        deActivate.setVisible(false);

        return commandPanel;
    }

    private void populateUI(UserDTO userDTO) {
        ((TextBox) UIHelper.getFromInputCache(shortNameId, ID_PREFIX)).setText(userDTO.getName());
        ((TextBox) UIHelper.getFromInputCache(fullNameId, ID_PREFIX)).setText(userDTO.getFullName());
        ((TextBox) UIHelper.getFromInputCache(emailId, ID_PREFIX)).setText(userDTO.getEmailAddress());
        ((CheckBox) UIHelper.getFromInputCache(activeId, ID_PREFIX)).setValue(userDTO.isActive());
        UIHelper.setListBoxValue(userDTO.getRole(), (ListBox) UIHelper.getFromInputCache(roleId, ID_PREFIX));
        UIHelper.setListBoxValue(userDTO.getProgram(), (ListBox) UIHelper.getFromInputCache(programId, ID_PREFIX));
        UIHelper.setListBoxValue(userDTO.getCountry(), (ListBox) UIHelper.getFromInputCache(countryId, ID_PREFIX));
    }

    private UserDTO populateUser() {
        modifiedUserDTO = new UserDTO();
        modifiedUserDTO.setUserId(this.savedUserDTO.getUserId());
        modifiedUserDTO.setName(((TextBox) UIHelper.getFromInputCache(shortNameId, ID_PREFIX)).getText());
        modifiedUserDTO.setFullName(((TextBox) UIHelper.getFromInputCache(fullNameId, ID_PREFIX)).getText());
        modifiedUserDTO.setEmailAddress(((TextBox) UIHelper.getFromInputCache(emailId, ID_PREFIX)).getText());
        modifiedUserDTO.setActive(((CheckBox) UIHelper.getFromInputCache(activeId, ID_PREFIX)).getValue());
        ListBox listBox = (ListBox) UIHelper.getFromInputCache(roleId, ID_PREFIX);
        modifiedUserDTO.setRole(listBox.getItemText(listBox.getSelectedIndex()));
        ListBox listBox1 = (ListBox) UIHelper.getFromInputCache(programId, ID_PREFIX);
        modifiedUserDTO.setProgram(listBox1.getItemText(listBox1.getSelectedIndex()));
        ListBox listBox2 = (ListBox) UIHelper.getFromInputCache(countryId, ID_PREFIX);
        modifiedUserDTO.setCountry(listBox2.getItemText(listBox2.getSelectedIndex()));
        return modifiedUserDTO;
    }

    private void cancelAction() {
        actionState = ACTION_STATE.CANCEL_ACTION;
        if (savedUserDTO != null) {
            populateUI(savedUserDTO);
        }
        viewAction();
    }

    private void editAction() {
        actionState = ACTION_STATE.EDIT_ACTION;
        edit.setVisible(false);
        activate.setVisible(false);
        deActivate.setVisible(false);
        save.setVisible(true);
        clear.setVisible(true);

        UIHelper.enableInputs(flowPanel);
    }

    private void viewAction() {
        actionState = ACTION_STATE.VIEW_ACTION;
        edit.setVisible(true);
        if (savedUserDTO != null) {
            if (savedUserDTO.isActive()) {
                activate.setVisible(false);
                deActivate.setVisible(true);
            } else {
                activate.setVisible(true);
                deActivate.setVisible(false);
            }
        }
        save.setVisible(false);
        clear.setVisible(false);

        UIHelper.disableInputs(flowPanel);
    }

    private void saveAction() {
        actionState = ACTION_STATE.SAVE_ACTION;
        if (validationProcessor.validate()) {
            populateUser();
            UIHelper.confirm(yesNoDialog, (Button) cancel);
        }
    }

    private void activateAction() {
        actionState = ACTION_STATE.ACTIVATE_ACTION;
// only in view mode, so do a quick swap
        modifiedUserDTO = savedUserDTO;
        modifiedUserDTO.setActive(true);
        UIHelper.confirm(yesNoDialog, (Button) cancel);
    }

    private void deActivateAction() {
        actionState = ACTION_STATE.DEACTIVATE_ACTION;
// only in view mode, so do a quick swap
        modifiedUserDTO = savedUserDTO;
        modifiedUserDTO.setActive(false);
        UIHelper.confirm(yesNoDialog, (Button) cancel);
    }

    private void backToSearchAction() {
        actionState = ACTION_STATE.BACKTOSEARCH_ACTION;
//        UIHelper.getApplication().getMainMenu().getSelectedItem().setState(true, true);
        History.fireCurrentHistoryState();
    }


    private void afterOkClicked() {
// update the ui for the activate, deactivate action since no user interaction of clicking the checkbox is required       
        if (ACTION_STATE.ACTIVATE_ACTION.equals(actionState)) {
            ((CheckBox) UIHelper.getFromInputCache(activeId, ID_PREFIX)).setValue(modifiedUserDTO.isActive());
        } else if (ACTION_STATE.DEACTIVATE_ACTION.equals(actionState)) {
            ((CheckBox) UIHelper.getFromInputCache(activeId, ID_PREFIX)).setValue(modifiedUserDTO.isActive());
        }
    }

    private void afterCancelClicked() {
    }


    @Override
    public void onClick(ClickEvent clickEvent) {
        // note that in general, events can have sources that are not Widgets.
        Widget sender = (Widget) clickEvent.getSource();
        if (Button.class == sender.getClass()) {
            if (commandButtons[0].equals(sender.getTitle())) {
                editAction();
            } else if (commandButtons[1].equals(sender.getTitle())) {
                saveAction();
            } else if (commandButtons[2].equals(sender.getTitle())) {
//                clearAction();
                cancelAction();
            } else if (commandButtons[3].equals(sender.getTitle())) {
                activateAction();
            } else if (commandButtons[4].equals(sender.getTitle())) {
                deActivateAction();
            } else if (commandButtons[5].equals(sender.getTitle())) {
                backToSearchAction();
            }
        }
    }

    private void save(final UserDTO userDTO) {
        yesNoDialog.hide();
        UIHelper.scheduleProgress(uiConstants.saveProgressWait());
        userServiceAsync.updateUser(userDTO, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
                UIHelper.cancelProgress();
//                UIHelper.showStatus(userDTO + myConstants.modifyUserFailed() + throwable.getLocalizedMessage());
                UIHelper.showStatus(UIHelper.getUiConstants().saveFail());
            }

            @Override
            public void onSuccess(Void aVoid) {
                SearchModifyUser.this.savedUserDTO = userDTO;
                viewAction();
                UIHelper.cancelProgress();
                UIHelper.showStatus(myConstants.modifyUserSuccess() + userDTO.getName());
            }
        });
    }

    public void view(final UserDTO userDTO) {
        UIHelper.scheduleProgress();
// lazy loading might have happened, so load the collections here
        userServiceAsync.getUser(userDTO.getUserId(), new AsyncCallback<UserDTO>() {
            @Override
            public void onFailure(Throwable throwable) {
                UIHelper.cancelProgress();
//                UIHelper.showStatus(userDTO + myConstants.viewUserFailed() + throwable.getLocalizedMessage());
                UIHelper.showStatus(UIHelper.getUiConstants().fetchFail());
            }

            @Override
            public void onSuccess(UserDTO result) {
                SearchModifyUser.this.savedUserDTO = result;
// cache is cleared by now, get from the widgets themselves
                populateUI(result);
                viewAction();
                UIHelper.cancelProgress();
            }
        });
    }

}
package com.retailwave.fce.client.presenter;
/**
 * $Id: AddUserPresenter.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/presenter/AddUserPresenter.java $
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.retailwave.fce.client.content.i18n.AddUserConstants;
import com.retailwave.fce.client.content.i18n.UIConstants;
import com.retailwave.fce.client.content.user.AddUser;
import com.retailwave.fce.shared.dto.UserDTO;
import com.retailwave.fce.shared.rpc.UserServiceRemoteAsync;
import com.retailwave.fce.client.util.UIHelper;

/**
 * AddUser
 * <p/>
 * Screen to add a user
 */
public class AddUserPresenter
        implements Presenter.ContentPresenter {

    public interface Display {
        HasClickHandlers getSaveButton();
    }

    private final UserServiceRemoteAsync rpcService;
    private final HandlerManager eventBus;
// ideally the type should be Display as defined above, but content widget dependency is an issue
// todo: remove content widget dependency and refactor to 'Display' interface type   
    private final AddUser display;

    static final UIConstants uiConstants = UIHelper.getUiConstants();
    static final AddUserConstants myConstants = GWT.create(AddUserConstants.class);
    public static final String ID_PREFIX = myConstants.idPrefix();

    Timer programTimer;
    Timer rolesTimer;
    Timer countriesTimer;

    private HandlerRegistration handlerRegistration;

    /**
     * Constructor.
     *
     * @param userServiceAsync
     * @param eventBus
     * @param display
     */
    public AddUserPresenter(UserServiceRemoteAsync userServiceAsync, HandlerManager eventBus, AddUser display) {
        this.eventBus = eventBus;
        this.display = display;
        this.rpcService = userServiceAsync;
    }


    @Override
    public void go(HasWidgets container) {
// called every time this view is shown
// initialize the display as required
// bind handlers and events       
// do the registration only once
        if (null == handlerRegistration) {
// code to be called only once goes here
// todo: handle once execution logic outside this condition and remove dependency on null check           
//            scheduleTimers();

            handlerRegistration = display.getSaveButton().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    doSave();
                }
            });
        }
    }

    @Override
    public String getDescription() {
        return myConstants.description();
    }

    @Override
    public String getName() {
        return myConstants.title();
    }

    @Override
    public String[] getHistoryTokens() {
        return new String[]{myConstants.title()};
    }

    @Override
    public AddUser getContentView() {
        return display;
    }


    private void closeStatus() {
        UIHelper.cancelProgress();
/*
        if (VSCHelper.isUserDataLoadSuccess()) {
// hide the rpc status, since data have been loaded successfully now
            UIHelper.hideStatus();
        }
*/
    }


    private UserDTO fillUserFromDisplay() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName(((TextBox) UIHelper.getFromInputCache(AddUser.shortNameId, ID_PREFIX)).getText());
        userDTO.setFullName(((TextBox) UIHelper.getFromInputCache(AddUser.fullNameId, ID_PREFIX)).getText());
        userDTO.setEmailAddress(((TextBox) UIHelper.getFromInputCache(AddUser.emailId, ID_PREFIX)).getText());
        userDTO.setActive(((CheckBox) UIHelper.getFromInputCache(AddUser.activeId, ID_PREFIX)).getValue());
        ListBox listBox = (ListBox) UIHelper.getFromInputCache(AddUser.roleId, ID_PREFIX);
        userDTO.setRole(listBox.getItemText(listBox.getSelectedIndex()));
        ListBox listBox1 = (ListBox) UIHelper.getFromInputCache(AddUser.programId, ID_PREFIX);
        userDTO.setProgram(listBox1.getItemText(listBox1.getSelectedIndex()));
        ListBox listBox2 = (ListBox) UIHelper.getFromInputCache(AddUser.countryId, ID_PREFIX);
        userDTO.setCountry(listBox2.getItemText(listBox2.getSelectedIndex()));
        return userDTO;
    }

    private void doSave() {
        UIHelper.scheduleProgress(uiConstants.saveProgressWait());
        final UserDTO modifiedUserDTO = fillUserFromDisplay();
        rpcService.saveUser(modifiedUserDTO, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
                UIHelper.cancelProgress();
//                UIHelper.showStatus(myConstants.saveUserFailed() + throwable.getMessage() + " == " + throwable);
                UIHelper.showStatus(UIHelper.getUiConstants().saveFail());
            }

            @Override
            public void onSuccess(Void aVoid) {
                UIHelper.cancelProgress();
                UIHelper.showStatus(myConstants.saveUserSuccess() + modifiedUserDTO.getName());
            }
        });
    }

}
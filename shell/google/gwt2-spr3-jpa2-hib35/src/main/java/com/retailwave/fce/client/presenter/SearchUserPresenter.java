package com.retailwave.fce.client.presenter;
/**
 * $Id: SearchUserPresenter.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/presenter/SearchUserPresenter.java $
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.ListBox;
import com.retailwave.fce.client.content.i18n.SearchUserConstants;
import com.retailwave.fce.client.content.i18n.UIConstants;
import com.retailwave.fce.client.content.user.SearchModifyUser;
import com.retailwave.fce.client.content.user.SearchUser;
import com.retailwave.fce.client.data.CommandResult;
import com.retailwave.fce.shared.dto.UserDTO;
import com.retailwave.fce.shared.rpc.UserServiceRemoteAsync;
import com.retailwave.fce.client.util.UIHelper;

/**
 * AddUser
 * <p/>
 * Screen to add a user
 */
public class SearchUserPresenter
        implements Presenter.ContentPresenter {
    private HandlerRegistration handlerRegistration;

    public interface Display {
        HasSelectionHandlers getSelectionHandlers();
    }

    static final UIConstants uiConstants = UIHelper.getUiConstants();
    static final SearchUserConstants myConstants = GWT.create(SearchUserConstants.class);
    static final String ID_PREFIX = myConstants.idPrefix();

    private final HandlerManager eventBus;
    private final SearchUser display;

    private final UserServiceRemoteAsync rpcService;

    Timer programTimer;
    Timer rolesTimer;
    Timer countriesTimer;

    private SearchModifyUser searchModifyUser;

    /**
     * Constructor.
     *
     * @param userServiceAsync
     * @param eventBus
     * @param display
     */
    public SearchUserPresenter(UserServiceRemoteAsync userServiceAsync, HandlerManager eventBus, SearchUser display) {
        this.eventBus = eventBus;
        this.display = display;
        this.rpcService = userServiceAsync;
        searchModifyUser = new SearchModifyUser();

// sub widgets need to be initialized here
//        display.add(searchModifyUser.onInitialize(), searchModifyUserPresenter.getName());
//        display.add(searchModifyPartnerUser.onInitialize(), searchModifyPartnerUserPresenter.getName());
    }


    @Override
    public void go(HasWidgets container) {
// called every time this view is shown
// initialize the display as required
// bind handlers and events
// do the registration only once
// sub presenters need to be initialized here
//        searchModifyUserPresenter.go(container);
//        searchModifyPartnerUserPresenter.go(container);

        CommandResult commandResult = UIHelper.getApplication().getCommandResult();
        Object result = commandResult.getResult();
        if (null == handlerRegistration) {
// code to be called only once goes here
// todo: handle once execution logic outside this condition and remove dependency on null check
            scheduleTimers();

            handlerRegistration = display.getSelectionHandlers().addSelectionHandler(new SelectionHandler<Integer>() {
                @Override
                public void onSelection(SelectionEvent<Integer> selectionEvent) {
//                    only if user is available in result.. otherwise ignore since select partner workflow might trigger this
                    CommandResult commandResult = UIHelper.getApplication().getCommandResult();
                    Object result = commandResult.getResult();
                    if (null != result && UserDTO.class == result.getClass()) {
                        UserDTO userDTO = (UserDTO) result;
                        switch (selectionEvent.getSelectedItem()) {
                            case 1:
                                searchModifyUser.view(userDTO);
                                commandResult.clear();
                                break;
                        }
                    }
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
    public SearchUser getContentView() {
        return display;
    }

    private void createCountriesTimer() {
        if (null == countriesTimer) {
            countriesTimer = new Timer() {
                @Override
                public void run() {
//                    final String[] vals = VSCHelper.getSearchCountries();
                    final String[] vals = null;
                    if (vals != null) {
                        setupCountries(vals);
                    }
                }
            };
        }
    }

    private void scheduleTimers() {
        createCountriesTimer();
        countriesTimer.scheduleRepeating(1000);
    }

    private void setupRoles(String[] vals) {
        rolesTimer.cancel();
        rolesTimer = null;
        UIHelper.setDefaults(vals, (ListBox) UIHelper.getFromInputCache(SearchUser.roleId, ID_PREFIX));
        closeStatus();
    }

    private void setupPrograms(String[] vals) {
        programTimer.cancel();
        programTimer = null;
        UIHelper.setDefaults(vals, (ListBox) UIHelper.getFromInputCache(SearchUser.programId, ID_PREFIX));
        closeStatus();
    }

    private void setupCountries(String[] vals) {
        countriesTimer.cancel();
        countriesTimer = null;
        UIHelper.setDefaults(vals, (ListBox) UIHelper.getFromInputCache(SearchUser.countryId, ID_PREFIX));
        closeStatus();
    }

    private void closeStatus() {
        UIHelper.cancelProgress();
//        if (VSCHelper.isUserDataLoadSuccess()) {
// hide the rpc status, since data have been loaded successfully now
            UIHelper.hideStatus();
//        }
    }

}
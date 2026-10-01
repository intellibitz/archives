package com.retailwave.fce.client.presenter
/**
 * $Id: SearchUserPresenter.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/presenter/SearchUserPresenter.java $
 */

import com.google.gwt.core.client.GWT
import com.google.gwt.event.logical.shared.HasSelectionHandlers
import com.google.gwt.event.logical.shared.SelectionEvent
import com.google.gwt.event.logical.shared.SelectionHandler
import com.google.gwt.event.shared.HandlerManager
import com.google.gwt.event.shared.HandlerRegistration
import com.google.gwt.user.client.Timer
import com.google.gwt.user.client.ui.HasWidgets
import com.google.gwt.user.client.ui.ListBox
import com.retailwave.fce.client.content.i18n.SearchUserConstants
import com.retailwave.fce.client.content.i18n.UIConstants
import com.retailwave.fce.client.content.user.SearchModifyUser
import com.retailwave.fce.client.content.user.SearchUser
import com.retailwave.fce.client.data.CommandResult
import com.retailwave.fce.shared.dto.UserDTO
import com.retailwave.fce.shared.rpc.UserServiceRemoteAsync
import com.retailwave.fce.client.util.UIHelper

/**
 * AddUser
 * <p/>
 * Screen to add a user
 */
open class SearchUserPresenter : Presenter.ContentPresenter {
    private var handlerRegistration: HandlerRegistration? = null

    interface Display {
        HasSelectionHandlers getSelectionHandlers()
    }

    val uiConstants = UIHelper.getUiConstants()
    val myConstants = GWT.create(SearchUserConstants::class.java)
    val ID_PREFIX = myConstants.idPrefix()

    private var eventBus: final HandlerManager? = null
    private var display: final SearchUser? = null

    private var rpcService: final UserServiceRemoteAsync? = null

    Timer programTimer
    Timer rolesTimer
    Timer countriesTimer

    private var searchModifyUser: SearchModifyUser? = null

    /**
     * Constructor.
     *
     * @param userServiceAsync
     * @param eventBus
     * @param display
     */
    constructor(userServiceAsync: UserServiceRemoteAsync, eventBus: HandlerManager, display: SearchUser) {
        this.eventBus = eventBus
        this.display = display
        this.rpcService = userServiceAsync
        searchModifyUser = SearchModifyUser()

// sub widgets need to be initialized here
//        display.add(searchModifyUser.onInitialize(), searchModifyUserPresenter.getName())
//        display.add(searchModifyPartnerUser.onInitialize(), searchModifyPartnerUserPresenter.getName())
    }

        override fun go(container: HasWidgets) {
// called every time this view is shown
// initialize the display as required
// bind handlers and events
// do the registration only once
// sub presenters need to be initialized here
//        searchModifyUserPresenter.go(container)
//        searchModifyPartnerUserPresenter.go(container)

        val commandResult = UIHelper.getApplication().getCommandResult()
        val result = commandResult.getResult()
        if (null == handlerRegistration) {
// code to be called only once goes here
// todo: handle once execution logic outside this condition and remove dependency on null check
            scheduleTimers()

            handlerRegistration = display.getSelectionHandlers().addSelectionHandler(object : SelectionHandler<Integer> {
                    override fun onSelection(selectionEvent: SelectionEvent<Integer>) {
//                    only if user is available in result.. otherwise ignore since select partner workflow might trigger this
                    val commandResult = UIHelper.getApplication().getCommandResult()
                    val result = commandResult.getResult()
                    if (null != result && UserDTO::class.java == result.getClass()) {
                        val userDTO = (UserDTO) result
                        switch (selectionEvent.getSelectedItem()) {
                            case 1:
                                searchModifyUser.view(userDTO)
                                commandResult.clear()
                                break
                        }
                    }
                }
            })
        }
    }

        override fun getDescription(): String {
        return myConstants.description()
    }

        override fun getName(): String {
        return myConstants.title()
    }

        override fun getHistoryTokens(): Array<String> {
        return new String[]{myConstants.title()}
    }

        override fun getContentView(): SearchUser {
        return display
    }

    private fun createCountriesTimer() {
        if (null == countriesTimer) {
            countriesTimer = object : Timer() {
                    override fun run() {
//                    val vals = VSCHelper.getSearchCountries()
                    val vals = null
                    if (vals != null) {
                        setupCountries(vals)
                    }
                }
            }
        }
    }

    private fun scheduleTimers() {
        createCountriesTimer()
        countriesTimer.scheduleRepeating(1000)
    }

    private fun setupRoles(vals: Array<String>) {
        rolesTimer.cancel()
        rolesTimer = null
        UIHelper.setDefaults(vals, (ListBox) UIHelper.getFromInputCache(SearchUser.roleId, ID_PREFIX))
        closeStatus()
    }

    private fun setupPrograms(vals: Array<String>) {
        programTimer.cancel()
        programTimer = null
        UIHelper.setDefaults(vals, (ListBox) UIHelper.getFromInputCache(SearchUser.programId, ID_PREFIX))
        closeStatus()
    }

    private fun setupCountries(vals: Array<String>) {
        countriesTimer.cancel()
        countriesTimer = null
        UIHelper.setDefaults(vals, (ListBox) UIHelper.getFromInputCache(SearchUser.countryId, ID_PREFIX))
        closeStatus()
    }

    private fun closeStatus() {
        UIHelper.cancelProgress()
//        if (VSCHelper.isUserDataLoadSuccess()) {
// hide the rpc status, since data have been loaded successfully now
            UIHelper.hideStatus()
//        }
    }

}
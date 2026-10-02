package com.retailwave.fce.client.content.user
/**
 * $Id: SearchUser.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/content/user/SearchUser.java $
 */

import com.google.gwt.core.client.GWT
import com.google.gwt.dom.client.Style
import com.google.gwt.event.dom.client.*
import com.google.gwt.event.logical.shared.HasSelectionHandlers
import com.google.gwt.user.client.ui.*
import com.retailwave.fce.client.ContentWidget
import com.retailwave.fce.client.content.i18n.SearchUserConstants
import com.retailwave.fce.client.content.i18n.UIConstants
import com.retailwave.fce.client.data.CommandResult
import com.retailwave.fce.shared.dto.UserDTO
import com.retailwave.fce.client.presenter.SearchUserPresenter
import com.retailwave.fce.client.ui.UserResultsTable
import com.retailwave.fce.client.util.SearchHelper
import com.retailwave.fce.client.util.UIHelper

/**
 * SearchUser
 * <p/>
 * Screen to search a user
 */
open class SearchUser : ContentWidget(), ClickHandler, KeyUpHandler, SearchUserPresenter.Display {

    public var uiConstants: UIConstants  = UIHelper.getUiConstants()
    public var myConstants: SearchUserConstants  = GWT.create(SearchUserConstants::class.java)
    public var ID_PREFIX: String  = myConstants.idPrefix()
    public var actions: Array<String> = myConstants.actions()

    protected var userResultsTable: UserResultsTable = UserResultsTable()

    public var shortName: String  = myConstants.shortName()
    public var fullName: String  = myConstants.fullName()
    public var email: String  = myConstants.email()
    public var active: String  = myConstants.active()
    public var role: String  = myConstants.role()
    public var program: String  = myConstants.program()
    public var country: String  = myConstants.country()
    public var type: String  = myConstants.type()

    public var shortNameId: String  = ID_PREFIX + shortName
    public var fullNameId: String  = ID_PREFIX + fullName
    public var emailId: String  = ID_PREFIX + email
    public var activeId: String  = ID_PREFIX + active
    public var roleId: String  = ID_PREFIX + role
    public var programId: String  = ID_PREFIX + program
    public var countryId: String  = ID_PREFIX + country
    public var typeId: String  = ID_PREFIX + type

    val USER_TYPES = myConstants.typeValues()
    val ACTIVE_VALUES = myConstants.activeValues()
    val TRUE_FALSE = uiConstants.trueFalse()
    val YES_NO = uiConstants.yesNo()

    // Create a panel to layout the widgets
    protected var searchLayoutPanel: DockLayoutPanel = DockLayoutPanel(Style.Unit.EM)
    protected var buttons: Array<String> = uiConstants.searchClear()
    private var flowPanel: FlowPanel = FlowPanel()

    /**
     * Constructor.
     */
    constructor() {
        super()
    }

        override fun getHistoryTokens(): Array<String> {
        return new String[]{getName()}
    }

        override fun getDescription(): String {
        return myConstants.description()
    }

        override fun getName(): String {
        return myConstants.title()
    }

    /**
     * Creates widget, to display in parent
     */
        override fun onInitialize(): Widget {

        searchLayoutPanel.setTitle(getName())

// input and commands can go into flow panel
        val p1 = FlowPanel()
        p1.add(UIHelper.createTextInput(shortName, shortNameId, 12, false, ID_PREFIX, this, null))
        p1.add(UIHelper.createTextInput(fullName, fullNameId, 50, false, ID_PREFIX, this, null))
        p1.add(UIHelper.createTextInput(email, emailId, 50, false, ID_PREFIX, this, null))

        val p2 = FlowPanel()
//        p2.add(UIHelper.createListInput(role, roleId, false, null, false, ID_PREFIX))
//        p2.add(UIHelper.createListInput(program, programId, false, null, false, ID_PREFIX))
//        p2.add(UIHelper.createListInput(country, countryId, false, null, false, ID_PREFIX))
        p2.add(UIHelper.createListInput(type, typeId, false, USER_TYPES, false, ID_PREFIX))
        p2.add(UIHelper.createListInput(active, activeId, false, ACTIVE_VALUES, false, ID_PREFIX))

        p1.addStyleName(UIHelper.style.floatLeft())
        p2.addStyleName(UIHelper.style.floatLeft())
        flowPanel.add(p1)
        flowPanel.add(p2)

        val scrollPanel = ScrollPanel(flowPanel)

        searchLayoutPanel.addNorth(scrollPanel, 6.5)
        searchLayoutPanel.addNorth(UIHelper.createCommands(buttons, this), 3)

//todo: results layout needs to be fixed (investigate incubator widgets for gwt 2.0 uibinder compatability)
        userResultsTable.init(this)
        searchLayoutPanel.add(userResultsTable)

        return searchLayoutPanel
    }

        override fun onClick(clickEvent: ClickEvent) {
        // note that in general, events can have sources that are not Widgets.
        val sender = (Widget) clickEvent.getSource()
        if (Button::class.java == sender.getClass()) {
            if (buttons[0].equals(sender.getTitle())) {
                searchAction()
            } else if (buttons[1].equals(sender.getTitle())) {
                UIHelper.clearInputs(flowPanel)
                clearAction()
            }
        }
    }

        override fun onKeyUp(keyUpEvent: KeyUpEvent) {
        if (keyUpEvent.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            searchAction()
        }
    }

    /**
     * called when the menu for this content is selected
     */
        override fun onMenuSelection() {
        if (tabLayoutPanel.getWidgetCount() >= 0) {
            tabLayoutPanel.selectTab(0)
        }
    }

    fun selectAction(userDTO: UserDTO) {
        viewAction(userDTO)
    }

    fun actionPerformed(userDTO: UserDTO) {
        viewAction(userDTO)
    }

    fun viewAction(userDTO: UserDTO) {
        val commandResult = UIHelper.getApplication().getCommandResult()
        commandResult.clear()
        commandResult.setResult(userDTO)
        if (userDTO.isPartnerUser()) {
            tabLayoutPanel.selectTab(2)
        } else {
            tabLayoutPanel.selectTab(1)
        }
    }

    protected fun searchAction() {
        val criteria = populateUser()
        if (criteria.isWildcard()) {
            SearchHelper.enableLike(criteria)
        }
        userResultsTable.search(criteria)
    }

    fun clearAction() {
        if (userResultsTable.isVisible()) {
            userResultsTable.setVisible(false)
        }
    }

    private fun populateUser(): UserDTO {
        val userDTO = UserDTO()
        userDTO.setName(UIHelper.getTextBoxValueFromInputCache(shortNameId, ID_PREFIX))
        userDTO.setFullName(UIHelper.getTextBoxValueFromInputCache(fullNameId, ID_PREFIX))
        userDTO.setEmailAddress(UIHelper.getTextBoxValueFromInputCache(emailId, ID_PREFIX))
        userDTO.setActiveSearch(UIHelper.getListBoxValueFromInputCache(activeId, ID_PREFIX))
        val activ = userDTO.getActiveSearch()
        if (TRUE_FALSE[0].equalsIgnoreCase(activ) || YES_NO[0].equalsIgnoreCase(activ)) {
            userDTO.setActive(true)
        } else if (TRUE_FALSE[1].equalsIgnoreCase(activ) || YES_NO[1].equalsIgnoreCase(activ)) {
            userDTO.setActive(false)
        }
//        userDTO.setRole(UIHelper.getListBoxValueFromInputCache(roleId, ID_PREFIX))
//        userDTO.setProgram(UIHelper.getListBoxValueFromInputCache(programId, ID_PREFIX))
//        userDTO.setCountry(UIHelper.getListBoxValueFromInputCache(countryId, ID_PREFIX))
        userDTO.setTypeSearch(UIHelper.getListBoxValueFromInputCache(typeId, ID_PREFIX))
        userDTO.setPartnerUser(USER_TYPES[2].equalsIgnoreCase(userDTO.getTypeSearch()))
        return userDTO
    }

        override fun getSelectionHandlers(): HasSelectionHandlers<Integer> {
        return getTabLayoutPanel()
    }
}
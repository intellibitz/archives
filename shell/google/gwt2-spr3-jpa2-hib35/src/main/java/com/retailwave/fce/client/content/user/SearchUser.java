package com.retailwave.fce.client.content.user;
/**
 * $Id: SearchUser.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/content/user/SearchUser.java $
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.user.client.ui.*;
import com.retailwave.fce.client.ContentWidget;
import com.retailwave.fce.client.content.i18n.SearchUserConstants;
import com.retailwave.fce.client.content.i18n.UIConstants;
import com.retailwave.fce.client.data.CommandResult;
import com.retailwave.fce.shared.dto.UserDTO;
import com.retailwave.fce.client.presenter.SearchUserPresenter;
import com.retailwave.fce.client.ui.UserResultsTable;
import com.retailwave.fce.client.util.SearchHelper;
import com.retailwave.fce.client.util.UIHelper;

/**
 * SearchUser
 * <p/>
 * Screen to search a user
 */
public class SearchUser
        extends ContentWidget
        implements ClickHandler, KeyUpHandler, SearchUserPresenter.Display {

    public static final UIConstants uiConstants = UIHelper.getUiConstants();
    public static final SearchUserConstants myConstants = GWT.create(SearchUserConstants.class);
    public static final String ID_PREFIX = myConstants.idPrefix();
    public static final String[] actions = myConstants.actions();

    protected UserResultsTable userResultsTable = new UserResultsTable();

    public static final String shortName = myConstants.shortName();
    public static final String fullName = myConstants.fullName();
    public static final String email = myConstants.email();
    public static final String active = myConstants.active();
    public static final String role = myConstants.role();
    public static final String program = myConstants.program();
    public static final String country = myConstants.country();
    public static final String type = myConstants.type();

    public static final String shortNameId = ID_PREFIX + shortName;
    public static final String fullNameId = ID_PREFIX + fullName;
    public static final String emailId = ID_PREFIX + email;
    public static final String activeId = ID_PREFIX + active;
    public static final String roleId = ID_PREFIX + role;
    public static final String programId = ID_PREFIX + program;
    public static final String countryId = ID_PREFIX + country;
    public static final String typeId = ID_PREFIX + type;

    final String[] USER_TYPES = myConstants.typeValues();
    final String[] ACTIVE_VALUES = myConstants.activeValues();
    final String[] TRUE_FALSE = uiConstants.trueFalse();
    final String[] YES_NO = uiConstants.yesNo();

    // Create a panel to layout the widgets
    protected DockLayoutPanel searchLayoutPanel = new DockLayoutPanel(Style.Unit.EM);
    protected String[] buttons = uiConstants.searchClear();
    private FlowPanel flowPanel = new FlowPanel();


    /**
     * Constructor.
     */
    public SearchUser() {
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

        searchLayoutPanel.setTitle(getName());

// input and commands can go into flow panel
        FlowPanel p1 = new FlowPanel();
        p1.add(UIHelper.createTextInput(shortName, shortNameId, 12, false, ID_PREFIX, this, null));
        p1.add(UIHelper.createTextInput(fullName, fullNameId, 50, false, ID_PREFIX, this, null));
        p1.add(UIHelper.createTextInput(email, emailId, 50, false, ID_PREFIX, this, null));

        FlowPanel p2 = new FlowPanel();
//        p2.add(UIHelper.createListInput(role, roleId, false, null, false, ID_PREFIX));
//        p2.add(UIHelper.createListInput(program, programId, false, null, false, ID_PREFIX));
//        p2.add(UIHelper.createListInput(country, countryId, false, null, false, ID_PREFIX));
        p2.add(UIHelper.createListInput(type, typeId, false, USER_TYPES, false, ID_PREFIX));
        p2.add(UIHelper.createListInput(active, activeId, false, ACTIVE_VALUES, false, ID_PREFIX));

        p1.addStyleName(UIHelper.style.floatLeft());
        p2.addStyleName(UIHelper.style.floatLeft());
        flowPanel.add(p1);
        flowPanel.add(p2);

        ScrollPanel scrollPanel = new ScrollPanel(flowPanel);

        searchLayoutPanel.addNorth(scrollPanel, 6.5);
        searchLayoutPanel.addNorth(UIHelper.createCommands(buttons, this), 3);

//todo: results layout needs to be fixed (investigate incubator widgets for gwt 2.0 uibinder compatability)
        userResultsTable.init(this);
        searchLayoutPanel.add(userResultsTable);

        return searchLayoutPanel;
    }


    @Override
    public void onClick(ClickEvent clickEvent) {
        // note that in general, events can have sources that are not Widgets.
        Widget sender = (Widget) clickEvent.getSource();
        if (Button.class == sender.getClass()) {
            if (buttons[0].equals(sender.getTitle())) {
                searchAction();
            } else if (buttons[1].equals(sender.getTitle())) {
                UIHelper.clearInputs(flowPanel);
                clearAction();
            }
        }
    }

    @Override
    public void onKeyUp(KeyUpEvent keyUpEvent) {
        if (keyUpEvent.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            searchAction();
        }
    }

    /**
     * called when the menu for this content is selected
     */
    @Override
    public void onMenuSelection() {
        if (tabLayoutPanel.getWidgetCount() >= 0) {
            tabLayoutPanel.selectTab(0);
        }
    }

    public void selectAction(UserDTO userDTO) {
        viewAction(userDTO);
    }

    public void actionPerformed(UserDTO userDTO) {
        viewAction(userDTO);
    }

    public void viewAction(UserDTO userDTO) {
        final CommandResult commandResult = UIHelper.getApplication().getCommandResult();
        commandResult.clear();
        commandResult.setResult(userDTO);
        if (userDTO.isPartnerUser()) {
            tabLayoutPanel.selectTab(2);
        } else {
            tabLayoutPanel.selectTab(1);
        }
    }

    protected void searchAction() {
        UserDTO criteria = populateUser();
        if (criteria.isWildcard()) {
            SearchHelper.enableLike(criteria);
        }
        userResultsTable.search(criteria);
    }

    public void clearAction() {
        if (userResultsTable.isVisible()) {
            userResultsTable.setVisible(false);
        }
    }

    private UserDTO populateUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName(UIHelper.getTextBoxValueFromInputCache(shortNameId, ID_PREFIX));
        userDTO.setFullName(UIHelper.getTextBoxValueFromInputCache(fullNameId, ID_PREFIX));
        userDTO.setEmailAddress(UIHelper.getTextBoxValueFromInputCache(emailId, ID_PREFIX));
        userDTO.setActiveSearch(UIHelper.getListBoxValueFromInputCache(activeId, ID_PREFIX));
        String activ = userDTO.getActiveSearch();
        if (TRUE_FALSE[0].equalsIgnoreCase(activ) || YES_NO[0].equalsIgnoreCase(activ)) {
            userDTO.setActive(true);
        } else if (TRUE_FALSE[1].equalsIgnoreCase(activ) || YES_NO[1].equalsIgnoreCase(activ)) {
            userDTO.setActive(false);
        }
//        userDTO.setRole(UIHelper.getListBoxValueFromInputCache(roleId, ID_PREFIX));
//        userDTO.setProgram(UIHelper.getListBoxValueFromInputCache(programId, ID_PREFIX));
//        userDTO.setCountry(UIHelper.getListBoxValueFromInputCache(countryId, ID_PREFIX));
        userDTO.setTypeSearch(UIHelper.getListBoxValueFromInputCache(typeId, ID_PREFIX));
        userDTO.setPartnerUser(USER_TYPES[2].equalsIgnoreCase(userDTO.getTypeSearch()));
        return userDTO;
    }


    @Override
    public HasSelectionHandlers<Integer> getSelectionHandlers() {
        return getTabLayoutPanel();
    }
}
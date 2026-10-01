package com.retailwave.fce.client.content.user
/**
 * $Id: SearchModifyUser.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/content/user/SearchModifyUser.java $
 */

import com.google.gwt.core.client.GWT
import com.google.gwt.event.dom.client.ClickEvent
import com.google.gwt.event.dom.client.ClickHandler
import com.google.gwt.event.dom.client.HasClickHandlers
import com.google.gwt.uibinder.client.UiBinder
import com.google.gwt.uibinder.client.UiField
import com.google.gwt.uibinder.client.UiTemplate
import com.google.gwt.user.client.History
import com.google.gwt.user.client.rpc.AsyncCallback
import com.google.gwt.user.client.ui.*
import com.retailwave.fce.client.content.i18n.SearchModifyUserConstants
import com.retailwave.fce.client.content.i18n.UIConstants
import com.retailwave.fce.shared.dto.UserDTO
import com.retailwave.fce.client.util.UIHelper
import com.retailwave.fce.client.validator.PersonNameValidator
import com.retailwave.fce.shared.rpc.UserServiceRemote
import com.retailwave.fce.shared.rpc.UserServiceRemoteAsync
import eu.maydu.gwt.validation.client.DefaultValidationProcessor
import eu.maydu.gwt.validation.client.ValidationProcessor
import eu.maydu.gwt.validation.client.validators.standard.NotEmptyValidator
import eu.maydu.gwt.validation.client.validators.strings.EmailValidator

open class SearchModifyUser : ResizeComposite(), ClickHandler {

    val uiConstants = UIHelper.getUiConstants()
    val myConstants = GWT.create(SearchModifyUserConstants::class.java)
    val ID_PREFIX = myConstants.idPrefix()

    private var edit: Button? = null
    private var activate: Button? = null
    private var deActivate: Button? = null
    private var save: Button? = null
    private var clear: Button? = null
    private var backToSearch: Button? = null
    val validationProcessor = DefaultValidationProcessor()
    val userServiceAsync = UserServiceRemote.App.getInstance()

    val handlers = new ClickHandler[]{
            object : ClickHandler() {
                    override fun onClick(event: ClickEvent) {
                    save(modifiedUserDTO)
                    afterOkClicked()
                }
            },
            object : ClickHandler() {
                    override fun onClick(event: ClickEvent) {
                    yesNoDialog.hide()
                    afterCancelClicked()
                }
            }
    }

    val commandButtons = myConstants.commands()
    val okCancel = uiConstants.okCancel()
    val yesNoDialog = UIHelper.createDialogBox(myConstants.title(), uiConstants.saveChangesDialog(), okCancel, handlers)
    private var ok: HasClickHandlers? = null
    private var cancel: HasClickHandlers? = null

    private var savedUserDTO: UserDTO? = null
    private var modifiedUserDTO: UserDTO? = null

    private enum ACTION_STATE {
        VIEW_ACTION, EDIT_ACTION, ACTIVATE_ACTION, CANCEL_ACTION, SAVE_ACTION, DEACTIVATE_ACTION, BACKTOSEARCH_ACTION
    }

    private var actionState: ACTION_STATE = ACTION_STATE.VIEW_ACTION

    @UiField
    var name: Label? = null
    @UiField
    var description: Label? = null
    @UiField
    var dockLayoutPanel: DockLayoutPanel? = null
    @UiField
    var contentDockLayoutPanel: DockLayoutPanel? = null

    private var flowPanel: FlowPanel = FlowPanel()

    @UiTemplate("com.retailwave.fce.client.ContentWidget.ui.xml")
    interface Binder : UiBinder<Widget, SearchModifyUser> {
    }

    private var binder: Binder  = GWT.create(Binder::class.java)

    public var shortName: String = myConstants.shortName()
    public var fullName: String = myConstants.fullName()
    public var email: String = myConstants.email()
    public var active: String = myConstants.active()
    public var role: String = myConstants.role()
    public var program: String = myConstants.program()
    public var country: String = myConstants.country()

    public var shortNameId: String = ID_PREFIX + shortName
    public var fullNameId: String = ID_PREFIX + fullName
    public var emailId: String = ID_PREFIX + email
    public var activeId: String = ID_PREFIX + active
    public var roleId: String = ID_PREFIX + role
    public var programId: String = ID_PREFIX + program
    public var countryId: String = ID_PREFIX + country

    /**
     * Constructor.
     */
    constructor() {
        super()
        initWidget(binder.createAndBindUi(this))
    }

    fun getDescription(): String {
        return myConstants.description()
    }

    fun getName(): String {
        return myConstants.title()
    }

    /**
     * @return Widget Creates widget, to display in parent
     */
    fun onInitialize(): Widget {

        UIHelper.clearInputCache(ID_PREFIX)
        name.setText(getName())
        description.setText(getDescription())
        setTitle(getName())

        flowPanel.add(UIHelper.createTextInput(shortName, shortNameId, 12, false, ID_PREFIX,
                null, validationProcessor, NotEmptyValidator::class.java.getName(), PersonNameValidator::class.java.getName()))
        flowPanel.add(UIHelper.createTextInput(fullName, fullNameId, 50, false, ID_PREFIX,
                null, validationProcessor, NotEmptyValidator::class.java.getName(), PersonNameValidator::class.java.getName()))
        flowPanel.add(UIHelper.createTextInput(email, emailId, 50, false, ID_PREFIX,
                null, validationProcessor, EmailValidator::class.java.getName()))
        flowPanel.add(UIHelper.createCheckInput(active, activeId, false, ID_PREFIX))
        flowPanel.add(UIHelper.createListInput(program, programId, false, null, false, ID_PREFIX))
        flowPanel.add(UIHelper.createListInput(role, roleId, false, null, false, ID_PREFIX))
        flowPanel.add(UIHelper.createListInput(country, countryId, false, null, false, ID_PREFIX))

        flowPanel.add(addCommands())

        val p = (FlowPanel) ((FlowPanel) yesNoDialog.getWidget()).getWidget(1)
        ok = (HasClickHandlers) p.getWidget(0)
        cancel = (HasClickHandlers) p.getWidget(1)

// present in view mode
        viewAction()
// add the flowPanel panel which contains inputs and okCancel to the outer dockLayoutPanel panel
        contentDockLayoutPanel.add(flowPanel)
        return this
    }

    private fun addCommands(): Widget {
        val commandPanel = FlowPanel()
        commandPanel.addStyleName(UIHelper.style.commandPanel())
        for (text in commandButtons) {
            val b = Button()
            /**
             * HorizontalPanel is a bit trickier. In some cases, you can simply replace it with a DockLayoutPanel,
             * but that requires that you specify its childrens' widths explicitly. The most common alternative is to use
             * FlowPanel, and to use the float: left; CSS property on its children.
             * And of course, you can continue to use HorizontalPanel itself, as long as you take the caveats above into account.
             */
            b.addStyleName(UIHelper.style.commandPanelButtonDefWidth())
            b.addStyleName(UIHelper.style.floatLeft())
            b.setTitle(text)
            b.setText(text)
            b.addClickHandler(this)
            commandPanel.add(b)
        }
        edit = (Button) commandPanel.getWidget(0)
        save = (Button) commandPanel.getWidget(1)
        clear = (Button) commandPanel.getWidget(2)
        activate = (Button) commandPanel.getWidget(3)
        deActivate = (Button) commandPanel.getWidget(4)
        backToSearch = (Button) commandPanel.getWidget(5)

        activate.setVisible(false)
        deActivate.setVisible(false)

        return commandPanel
    }

    private fun populateUI(userDTO: UserDTO) {
        ((TextBox) UIHelper.getFromInputCache(shortNameId, ID_PREFIX)).setText(userDTO.getName())
        ((TextBox) UIHelper.getFromInputCache(fullNameId, ID_PREFIX)).setText(userDTO.getFullName())
        ((TextBox) UIHelper.getFromInputCache(emailId, ID_PREFIX)).setText(userDTO.getEmailAddress())
        ((CheckBox) UIHelper.getFromInputCache(activeId, ID_PREFIX)).setValue(userDTO.isActive())
        UIHelper.setListBoxValue(userDTO.getRole(), (ListBox) UIHelper.getFromInputCache(roleId, ID_PREFIX))
        UIHelper.setListBoxValue(userDTO.getProgram(), (ListBox) UIHelper.getFromInputCache(programId, ID_PREFIX))
        UIHelper.setListBoxValue(userDTO.getCountry(), (ListBox) UIHelper.getFromInputCache(countryId, ID_PREFIX))
    }

    private fun populateUser(): UserDTO {
        modifiedUserDTO = UserDTO()
        modifiedUserDTO.setUserId(this.savedUserDTO.getUserId())
        modifiedUserDTO.setName(((TextBox) UIHelper.getFromInputCache(shortNameId, ID_PREFIX)).getText())
        modifiedUserDTO.setFullName(((TextBox) UIHelper.getFromInputCache(fullNameId, ID_PREFIX)).getText())
        modifiedUserDTO.setEmailAddress(((TextBox) UIHelper.getFromInputCache(emailId, ID_PREFIX)).getText())
        modifiedUserDTO.setActive(((CheckBox) UIHelper.getFromInputCache(activeId, ID_PREFIX)).getValue())
        val listBox = (ListBox) UIHelper.getFromInputCache(roleId, ID_PREFIX)
        modifiedUserDTO.setRole(listBox.getItemText(listBox.getSelectedIndex()))
        val listBox1 = (ListBox) UIHelper.getFromInputCache(programId, ID_PREFIX)
        modifiedUserDTO.setProgram(listBox1.getItemText(listBox1.getSelectedIndex()))
        val listBox2 = (ListBox) UIHelper.getFromInputCache(countryId, ID_PREFIX)
        modifiedUserDTO.setCountry(listBox2.getItemText(listBox2.getSelectedIndex()))
        return modifiedUserDTO
    }

    private fun cancelAction() {
        actionState = ACTION_STATE.CANCEL_ACTION
        if (savedUserDTO != null) {
            populateUI(savedUserDTO)
        }
        viewAction()
    }

    private fun editAction() {
        actionState = ACTION_STATE.EDIT_ACTION
        edit.setVisible(false)
        activate.setVisible(false)
        deActivate.setVisible(false)
        save.setVisible(true)
        clear.setVisible(true)

        UIHelper.enableInputs(flowPanel)
    }

    private fun viewAction() {
        actionState = ACTION_STATE.VIEW_ACTION
        edit.setVisible(true)
        if (savedUserDTO != null) {
            if (savedUserDTO.isActive()) {
                activate.setVisible(false)
                deActivate.setVisible(true)
            } else {
                activate.setVisible(true)
                deActivate.setVisible(false)
            }
        }
        save.setVisible(false)
        clear.setVisible(false)

        UIHelper.disableInputs(flowPanel)
    }

    private fun saveAction() {
        actionState = ACTION_STATE.SAVE_ACTION
        if (validationProcessor.validate()) {
            populateUser()
            UIHelper.confirm(yesNoDialog, (Button) cancel)
        }
    }

    private fun activateAction() {
        actionState = ACTION_STATE.ACTIVATE_ACTION
// only in view mode, so do a quick swap
        modifiedUserDTO = savedUserDTO
        modifiedUserDTO.setActive(true)
        UIHelper.confirm(yesNoDialog, (Button) cancel)
    }

    private fun deActivateAction() {
        actionState = ACTION_STATE.DEACTIVATE_ACTION
// only in view mode, so do a quick swap
        modifiedUserDTO = savedUserDTO
        modifiedUserDTO.setActive(false)
        UIHelper.confirm(yesNoDialog, (Button) cancel)
    }

    private fun backToSearchAction() {
        actionState = ACTION_STATE.BACKTOSEARCH_ACTION
//        UIHelper.getApplication().getMainMenu().getSelectedItem().setState(true, true)
        History.fireCurrentHistoryState()
    }

    private fun afterOkClicked() {
// update the ui for the activate, deactivate action since no user interaction of clicking the checkbox is required       
        if (ACTION_STATE.ACTIVATE_ACTION.equals(actionState)) {
            ((CheckBox) UIHelper.getFromInputCache(activeId, ID_PREFIX)).setValue(modifiedUserDTO.isActive())
        } else if (ACTION_STATE.DEACTIVATE_ACTION.equals(actionState)) {
            ((CheckBox) UIHelper.getFromInputCache(activeId, ID_PREFIX)).setValue(modifiedUserDTO.isActive())
        }
    }

    private fun afterCancelClicked() {
    }

        override fun onClick(clickEvent: ClickEvent) {
        // note that in general, events can have sources that are not Widgets.
        val sender = (Widget) clickEvent.getSource()
        if (Button::class.java == sender.getClass()) {
            if (commandButtons[0].equals(sender.getTitle())) {
                editAction()
            } else if (commandButtons[1].equals(sender.getTitle())) {
                saveAction()
            } else if (commandButtons[2].equals(sender.getTitle())) {
//                clearAction()
                cancelAction()
            } else if (commandButtons[3].equals(sender.getTitle())) {
                activateAction()
            } else if (commandButtons[4].equals(sender.getTitle())) {
                deActivateAction()
            } else if (commandButtons[5].equals(sender.getTitle())) {
                backToSearchAction()
            }
        }
    }

    private fun save(userDTO: UserDTO) {
        yesNoDialog.hide()
        UIHelper.scheduleProgress(uiConstants.saveProgressWait())
        userServiceAsync.updateUser(userDTO, object : AsyncCallback<Void> {
                override fun onFailure(throwable: Throwable) {
                UIHelper.cancelProgress()
//                UIHelper.showStatus(userDTO + myConstants.modifyUserFailed() + throwable.getLocalizedMessage())
                UIHelper.showStatus(UIHelper.getUiConstants().saveFail())
            }

                override fun onSuccess(aVoid: Void) {
                SearchModifyUser.this.savedUserDTO = userDTO
                viewAction()
                UIHelper.cancelProgress()
                UIHelper.showStatus(myConstants.modifyUserSuccess() + userDTO.getName())
            }
        })
    }

    fun view(userDTO: UserDTO) {
        UIHelper.scheduleProgress()
// lazy loading might have happened, so load the collections here
        userServiceAsync.getUser(userDTO.getUserId(), object : AsyncCallback<UserDTO> {
                override fun onFailure(throwable: Throwable) {
                UIHelper.cancelProgress()
//                UIHelper.showStatus(userDTO + myConstants.viewUserFailed() + throwable.getLocalizedMessage())
                UIHelper.showStatus(UIHelper.getUiConstants().fetchFail())
            }

                override fun onSuccess(result: UserDTO) {
                SearchModifyUser.this.savedUserDTO = result
// cache is cleared by now, get from the widgets themselves
                populateUI(result)
                viewAction()
                UIHelper.cancelProgress()
            }
        })
    }

}
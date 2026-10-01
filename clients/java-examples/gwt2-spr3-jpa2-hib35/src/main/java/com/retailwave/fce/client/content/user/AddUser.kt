package com.retailwave.fce.client.content.user
/**
 * $Id: AddUser.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/content/user/AddUser.java $
 */

import com.google.gwt.core.client.GWT
import com.google.gwt.event.dom.client.*
import com.google.gwt.user.client.ui.*
import com.retailwave.fce.client.ContentWidget
import com.retailwave.fce.client.content.i18n.AddUserConstants
import com.retailwave.fce.client.content.i18n.UIConstants
import com.retailwave.fce.client.presenter.AddUserPresenter
import com.retailwave.fce.client.util.UIHelper
import com.retailwave.fce.client.validator.PersonNameValidator
import eu.maydu.gwt.validation.client.DefaultValidationProcessor
import eu.maydu.gwt.validation.client.ValidationProcessor
import eu.maydu.gwt.validation.client.validators.standard.NotEmptyValidator
import eu.maydu.gwt.validation.client.validators.strings.EmailValidator

/**
 * AddUser
 * <p/>
 * Screen to add a user
 */
open class AddUser : ContentWidget(), ClickHandler, KeyUpHandler, AddUserPresenter.Display {

    val uiConstants = UIHelper.getUiConstants()
    val myConstants = GWT.create(AddUserConstants::class.java)

    public var ID_PREFIX: String  = myConstants.idPrefix()

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

    String[] commandButtons = uiConstants.saveClear()
    String[] okCancel = uiConstants.okCancel()
    val hideHandler = object : ClickHandler() {
            override fun onClick(event: ClickEvent) {
            yesNoDialog.hide()
        }
    }
    val yesNoDialog = UIHelper.createDialogBox
            (myConstants.title(), uiConstants.saveChangesDialog(), okCancel, hideHandler)
    private var ok: HasClickHandlers? = null
    private var cancel: HasClickHandlers? = null

    val validationProcessor = DefaultValidationProcessor()
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
        flowPanel.setTitle(getName())

        flowPanel.add(UIHelper.createTextInput(shortName, shortNameId, 12, false, ID_PREFIX,
                this, validationProcessor, NotEmptyValidator::class.java.getName(), PersonNameValidator::class.java.getName()))
        flowPanel.add(UIHelper.createTextInput(fullName, fullNameId, 50, false, ID_PREFIX,
                this, validationProcessor, NotEmptyValidator::class.java.getName(), PersonNameValidator::class.java.getName()))
        flowPanel.add(UIHelper.createTextInput(email, emailId, 50, false, ID_PREFIX,
                this, validationProcessor, EmailValidator::class.java.getName()))
        flowPanel.add(UIHelper.createCheckInput(active, activeId, false, ID_PREFIX))
        ((CheckBox) UIHelper.getFromInputCache(activeId, ID_PREFIX)).setValue(true)
        flowPanel.add(UIHelper.createListInput(program, programId, false, null, false, ID_PREFIX))
        flowPanel.add(UIHelper.createListInput(role, roleId, false, null, false, ID_PREFIX))
        flowPanel.add(UIHelper.createListInput(country, countryId, false, null, false, ID_PREFIX))
        flowPanel.add(UIHelper.createCommands(commandButtons, this))

        val p = (FlowPanel) ((FlowPanel) yesNoDialog.getWidget()).getWidget(1)
        ok = (HasClickHandlers) p.getWidget(0)
        cancel = (HasClickHandlers) p.getWidget(1)

        return flowPanel
    }

    private fun saveAction() {
        if (validationProcessor.validate()) {
            UIHelper.confirm(yesNoDialog, (Button) cancel)
        }
    }

    private fun clearAction() {
        UIHelper.clearInputs(flowPanel)
        validationProcessor.reset()
        UIHelper.setFocus(flowPanel)
    }

        override fun onClick(clickEvent: ClickEvent) {
        // note that in general, events can have sources that are not Widgets.
        val sender = (Widget) clickEvent.getSource()
        if (Button::class.java == sender.getClass()) {
            if (commandButtons[0].equals(sender.getTitle())) {
                saveAction()
            } else if (commandButtons[1].equals(sender.getTitle())) {
                clearAction()
            }
        }
    }

    /**
     * Called when KeyUpEvent is fired.
     *
     * @param event the {@link com.google.gwt.event.dom.client.KeyUpEvent} that was fired
     */
        override fun onKeyUp(event: KeyUpEvent) {
        validationProcessor.validate(((Widget) event.getSource()).getTitle())
    }

        override fun getSaveButton(): HasClickHandlers {
        return ok
    }

}
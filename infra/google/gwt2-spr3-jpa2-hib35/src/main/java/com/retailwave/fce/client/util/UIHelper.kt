package com.retailwave.fce.client.util
/**
 * $Id: UIHelper.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/util/UIHelper.java $
 */

import com.google.gwt.core.client.GWT
import com.google.gwt.core.client.JavaScriptObject
import com.google.gwt.dom.client.DivElement
import com.google.gwt.event.dom.client.ClickHandler
import com.google.gwt.event.dom.client.KeyUpHandler
import com.google.gwt.event.shared.EventHandler
import com.google.gwt.resources.client.CssResource
import com.google.gwt.uibinder.client.UiBinder
import com.google.gwt.uibinder.client.UiField
import com.google.gwt.user.client.ui.*
import com.retailwave.fce.client.Application
import com.retailwave.fce.client.content.i18n.UIConstants
import eu.maydu.gwt.validation.client.ValidationProcessor
import eu.maydu.gwt.validation.client.Validator
import eu.maydu.gwt.validation.client.actions.FocusAction
import eu.maydu.gwt.validation.client.actions.LabelTextAction
import eu.maydu.gwt.validation.client.actions.StyleAction

import java.util.HashMap

/**
 * UIHelper
 * <p/>
 * Helper methods for FCE
 */
open class UIHelper {

    interface UIHelperStyle : CssResource {
        String inputPanel()

        String validationFailedBorder()

        String validationFailedText()

        String inputLabel()

        String inputLabelSearch()

        String inputLabelDisabled()

        String floatLeft()

        String commandPanel()

        String commandPanelButton()

        String commandPanelButtonDefWidth()
    }

    interface Binder : UiBinder<DivElement, UIHelper> {
    }

    private var binder: Binder  = GWT.create(Binder::class.java)

    {
        binder.createAndBindUi(null)
    }

    private var CACHE_KEY_DEFAULT: String  = "UIHelper_"
    public var inputCache: HashMap<String, HashMap<String, UIObject>>  = HashMap<String, HashMap<String, UIObject>>()
    private var application: Application? = null

    private var uiConstants: UIConstants = GWT.create(UIConstants::class.java)

    @UiField
    var var: public? = null style: UIHelperStyle? = null

    private UIHelper() {
    }

    @JvmStatic
    fun getUiConstants(): UIConstants {
        return uiConstants
    }

    @JvmStatic
    fun addToInputCache(key: String, input: UIObject) {
        addToInputCache(key, input, CACHE_KEY_DEFAULT)
    }

    @JvmStatic
    fun clearInputCache(cacheName: String) {
        if (null == cacheName) {
            cacheName = CACHE_KEY_DEFAULT
        }
        val cache = inputCache.get(cacheName)
        if (cache != null) {
            cache.clear()
        }
    }

    @JvmStatic
    fun clearInputCache() {
        clearInputCache(CACHE_KEY_DEFAULT)
    }

    @JvmStatic
    fun clearAllInputCache() {
        inputCache.clear()
    }

    @JvmStatic
    fun getFromInputCache(id: String): UIObject {
        return getFromInputCache(id, CACHE_KEY_DEFAULT)
    }

    @JvmStatic
    fun addToInputCache(key: String, input: UIObject, cacheName: String) {
        if (null == cacheName) {
            cacheName = CACHE_KEY_DEFAULT
        }
        val cache = inputCache.get(cacheName)
// create a new cache, if previous cache was not found
        if (cache == null) {
            cache = HashMap()
            inputCache.put(cacheName, cache)
        }
        cache.put(key, input)
    }

    @JvmStatic
    fun getFromInputCache(id: String, cacheName: String): UIObject {
        if (null == cacheName) {
            cacheName = CACHE_KEY_DEFAULT
        }
        val cache = inputCache.get(cacheName)
        val val = null
        if (cache != null) {
            val = cache.get(id)
        }
        return val
    }

    @JvmStatic
    fun getTextBoxValueFromInputCache(id: String): String {
        return getTextBoxValueFromInputCache(id, CACHE_KEY_DEFAULT)
    }

    @JvmStatic
    fun getTextBoxValueFromInputCache(id: String, cacheName: String): String {
        val box = getTextBoxFromInputCache(id, cacheName)
        val val = null
        if (box != null) {
            val = box.getText()
//            ignore empty, and reset to null
            if (val.length == 0) {
                val = null
            }
        }
        return val
    }

    @JvmStatic
    fun getTextBoxFromInputCache(id: String, cacheName: String): TextBox {
        return (TextBox) getFromInputCache(id, cacheName)
    }

/*
    @JvmStatic
    fun getCheckBoxValueFromInputCache(name: String): Boolean {
        val box = (CheckBox) getFromInputCache(name)
        return box != null && box.isEnabled()
    }
*/

    @JvmStatic
    fun getListBoxValueFromInputCache(id: String): String {
        return getListBoxValueFromInputCache(id, CACHE_KEY_DEFAULT)
    }

    @JvmStatic
    fun getListBoxValueFromInputCache(id: String, cacheName: String): String {
        val box = (ListBox) UIHelper.getFromInputCache(id, cacheName)
        val val = null
        if (box != null) {
            val selectedIndex = box.getSelectedIndex()
            if (selectedIndex != -1) {
                val = box.getItemText(selectedIndex)
//            ignore empty, and reset to null
                if ("".equals(val)) {
                    val = null
                }
            }
        }
        return val
    }

    @JvmStatic
    fun setListBoxValue(value: String, listBox: ListBox): Boolean {
        var sz = listBox.getItemCount()
        for (int i = 0; i < sz; i++) {
            if (listBox.getItemText(i).equalsIgnoreCase(value)) {
                listBox.setSelectedIndex(i)
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun buildInputPanel(label: Widget, input: Widget, validatorNames: Array<String>, validationProcessor: ValidationProcessor): FlowPanel {
        val panel = FlowPanel()
        panel.addStyleName(style.inputPanel())

        label.addStyleName(style.floatLeft())
        panel.add(label)
        panel.add(input)
        if (null != validatorNames && null != validationProcessor && validatorNames.length > 0) {

            val error = Label("")
            error.setStylePrimaryName(style.validationFailedText())
            panel.add(error)

            val validators = new Validator[validatorNames.length]
            var i = 0
            for (validatorName in validatorNames) {
                val validator = ValidatorHelper.createValidator(validatorName, input)
                validator.addActionForFailure(FocusAction())
                validator.addActionForFailure(StyleAction(style.validationFailedBorder()))
                validator.addActionForFailure(LabelTextAction(error, false))
                validators[i++] = validator
            }
            validationProcessor.addValidators(input.getTitle(), validators)
        }
        return panel
    }

    @JvmStatic
    fun createTextInput(name: String, id: String, inputLen: Int, readOnly: Boolean, cacheName: String, handler: EventHandler, validationProcessor: ValidationProcessor, validatorNames: String...): FlowPanel {
        id = id.trim()
        val label = Label(name)
        label.setTitle(name)
        label.addStyleName(style.inputLabel())
        label.addStyleName(style.inputLabelDisabled())

        val input = TextBox()
        input.setTitle(name)
        input.ensureDebugId(id)
        input.setVisibleLength(inputLen)
        input.setMaxLength(inputLen)
        input.setReadOnly(readOnly)
        input.setEnabled(!readOnly)
        if (input.isEnabled() && input.isVisible()) {
            label.removeStyleName(style.inputLabelDisabled())
        }
        if (null != handler) {
            input.addKeyUpHandler((KeyUpHandler) handler)
        }
        if (null == cacheName) {
            addToInputCache(id, input)
        } else {
            addToInputCache(id, input, cacheName)
        }
        return buildInputPanel(label, input, validatorNames, validationProcessor)
    }

    @JvmStatic
    fun createTextArea(name: String, id: String, inputLen: Int, charWidth: Int, readOnly: Boolean, cacheName: String, handler: EventHandler, validationProcessor: ValidationProcessor, validatorNames: String...): FlowPanel {
        id = id.trim()

        val label = Label(name)
        label.addStyleName(style.inputLabel())
        label.addStyleName(style.inputLabelDisabled())

        val input = TextArea()
        input.ensureDebugId(id)
        input.setCharacterWidth(charWidth)
        input.setVisibleLines(inputLen)
        input.setReadOnly(readOnly)
        input.setEnabled(!readOnly)
        if (input.isEnabled() && input.isVisible()) {
            label.removeStyleName(style.inputLabelDisabled())
        }
        if (null != handler) {
            input.addKeyUpHandler((KeyUpHandler) handler)
        }
        if (null == cacheName) {
            addToInputCache(id, input)
        } else {
            addToInputCache(id, input, cacheName)
        }

        return buildInputPanel(label, input, validatorNames, validationProcessor)
    }

    @JvmStatic
    fun createCheckInput(name: String, id: String, readOnly: Boolean, cacheName: String): FlowPanel {
        id = id.trim()
        val label = Label(name)
        label.addStyleName(style.inputLabel())
        label.addStyleName(style.inputLabelDisabled())

        val input = CheckBox()
        input.ensureDebugId(id)
        input.setEnabled(!readOnly)
        if (input.isEnabled() && input.isVisible()) {
            label.removeStyleName(style.inputLabelDisabled())
        }
        if (null == cacheName) {
            addToInputCache(id, input)
        } else {
            addToInputCache(id, input, cacheName)
        }

        return buildInputPanel(label, input, null, null)
    }

    @JvmStatic
    fun createListInput(name: String, id: String, isMultipleSelect: Boolean, listTypes: Array<String>, readOnly: Boolean, cacheName: String): FlowPanel {
        id = id.trim()
        val label = Label(name)
        label.addStyleName(style.inputLabel())
        label.addStyleName(style.inputLabelDisabled())

        val input = ListBox(isMultipleSelect)
        input.ensureDebugId(id)
        input.setEnabled(!readOnly)
        setDefaults(listTypes, input)
        if (input.isEnabled() && input.isVisible()) {
            label.removeStyleName(style.inputLabelDisabled())
        }
        if (null == cacheName) {
            addToInputCache(id, input)
        } else {
            addToInputCache(id, input, cacheName)
        }

        return buildInputPanel(label, input, null, null)
    }

    @JvmStatic
    fun setDefaults(items: Array<String>, input: ListBox): Boolean {
        if (null != input && null != items) {
// clear previous defaults
            input.clear()
            for (item in items) {
                input.addItem(item)
            }
            return true
        }
        return false
    }

    @JvmStatic
    fun createCommands(buttons: Array<String>, handlers: EventHandler...): FlowPanel {
        val panel = FlowPanel()
        panel.addStyleName(style.commandPanel())
        var i = 0
        var sz = buttons.length
        for (text in buttons) {
            val b = Button()
            /**
             * HorizontalPanel is a bit trickier. In some cases, you can simply replace it with a DockLayoutPanel,
             * but that requires that you specify its childrens' widths explicitly. The most common alternative is to use
             * FlowPanel, and to use the float: left; CSS property on its children.
             * And of course, you can continue to use HorizontalPanel itself, as long as you take the caveats above into account.
             */
            b.addStyleName(style.commandPanelButton())
            b.addStyleName(style.floatLeft())
            b.setTitle(text)
            b.setText(text)
            if (null != handlers) {
                if (sz == handlers.length) {
                    b.addClickHandler((ClickHandler) handlers[i++])
                } else {
// todo: handle variable length handlers between 1 and button size
                    b.addClickHandler((ClickHandler) handlers[0])
                }
            }
            panel.add(b)
        }
        return panel
    }

    /**
     * Creates a dialog box with a message.
     *
     * @param title    the title of the dialog box
     * @param msg      the message to display
     * @param commands the command buttons to be displayed
     * @param handlers the handler to be invoked when command button is clicked
     * @return the new dialog box
     */
    @JvmStatic
    fun createDialogBox(title: String, msg: String, commands: Array<String>, handlers: EventHandler...): DialogBox {

        val dialogContents = FlowPanel()
//        the dialog contents.. msg at top, and command buttons at bottom
        dialogContents.add(HTML(msg))
        dialogContents.add(createCommands(commands, handlers))

        // Create a dialog box and set the caption text
        val dialogBox = DialogBox(false, true)
        dialogBox.setText(title)
        dialogBox.setWidget(dialogContents)
        dialogBox.setAnimationEnabled(true)
        dialogBox.setGlassEnabled(true)
        return dialogBox
    }

    @JvmStatic
    fun enableInputs(complexPanel: ComplexPanel) {
        toggleInputs(complexPanel, true)
    }

    @JvmStatic
    fun disableInputs(complexPanel: ComplexPanel) {
        toggleInputs(complexPanel, false)
    }

    @JvmStatic
    fun toggleInputs(complexPanel: ComplexPanel, flag: Boolean) {
        for (widget in complexPanel) {
            val widgetClass = widget.getClass()
            if (FlowPanel::class.java == widgetClass) {
                toggleInputs((ComplexPanel) widget, flag)
            } else if (
                    TextBox::class.java == widgetClass
                            || TextArea::class.java == widgetClass
                            || CheckBox::class.java == widgetClass
                            || ListBox::class.java == widgetClass
                            || Label::class.java == widgetClass
                    ) {
                if (TextBox::class.java == widgetClass || TextArea::class.java == widgetClass) {
                    val box = (TextBoxBase) widget
                    box.setEnabled(flag)
                    box.setReadOnly(!flag)
                } else if (CheckBox::class.java == widgetClass) {
                    ((CheckBox) widget).setEnabled(flag)
                } else if (ListBox::class.java == widgetClass) {
                    ((ListBox) widget).setEnabled(flag)
                } else if (Label::class.java == widgetClass) {
                    val l = ((Label) widget)
                    if (flag) {
                        l.removeStyleName(style.inputLabelDisabled())
                    } else {
                        l.addStyleName(style.inputLabelDisabled())
                    }
                }
            }
        }
    }

    @JvmStatic
    fun clearInputs(complexPanel: ComplexPanel) {
        for (widget in complexPanel) {
            val widgetClass = widget.getClass()
            if (FlowPanel::class.java == widgetClass) {
                clearInputs((ComplexPanel) widget)
            } else if (
                    TextBox::class.java == widgetClass
                            || TextArea::class.java == widgetClass
                            || CheckBox::class.java == widgetClass
                            || ListBox::class.java == widgetClass
                    ) {
                if (TextBox::class.java == widgetClass || TextArea::class.java == widgetClass) {
                    ((TextBoxBase) widget).setText("")
                } else if (CheckBox::class.java == widgetClass) {
                    ((CheckBox) widget).setValue(false)
                } else if (ListBox::class.java == widgetClass) {
                    ((ListBox) widget).setSelectedIndex(0)
                }
            }
        }
    }

    @JvmStatic
    fun setFocus(complexPanel: ComplexPanel): Boolean {
        for (widget in complexPanel) {
            val widgetClass = widget.getClass()
            if (TextBox::class.java == widgetClass || TextArea::class.java == widgetClass) {
                ((TextBoxBase) widget).setFocus(true)
                return true
            } else if (FlowPanel::class.java == widgetClass) {
                return setFocus((ComplexPanel) widget)
            }
        }
        return false
    }

    @JvmStatic
    fun setApplication(app: Application) {
        application = app
    }

    @JvmStatic
    fun getApplication(): Application {
        return application
    }

    @JvmStatic
    fun scheduleProgress() {
        scheduleProgress(uiConstants.loadProgressWait(), 1000)
    }

    @JvmStatic
    fun scheduleProgress(s: String) {
        getApplication().showProgress(s, 1000)
    }

    @JvmStatic
    fun scheduleProgress(s: String, ms: Int) {
        getApplication().showProgress(s, ms)
    }

    @JvmStatic
    fun cancelProgress() {
        getApplication().cancelProgress()
    }

    @JvmStatic
    fun showStatus(msg: String) {
        getApplication().showMessage(msg)
    }

    @JvmStatic
    fun hideStatus() {
        getApplication().hideMessage()
    }

    public native void nativeFocus(JavaScriptObject o)/*-{
        try{o.focus();}catch(e){}
     }-*/

    @JvmStatic
    fun hackRootLayoutPanelNotBlank() {
        val rootWidth = RootLayoutPanel.get().getElement().getStyle().getWidth()
        RootLayoutPanel.get().setWidth(rootWidth.equals("100%") ? "" : "100%")
    }

    @JvmStatic
    fun confirm(dialogBox: DialogBox, button: Button) {
        dialogBox.center()
        nativeFocus(button.getElement())
        hackRootLayoutPanelNotBlank()
    }

}
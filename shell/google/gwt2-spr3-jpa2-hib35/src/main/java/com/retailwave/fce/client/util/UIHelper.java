package com.retailwave.fce.client.util;
/**
 * $Id: UIHelper.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/util/UIHelper.java $
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.retailwave.fce.client.Application;
import com.retailwave.fce.client.content.i18n.UIConstants;
import eu.maydu.gwt.validation.client.ValidationProcessor;
import eu.maydu.gwt.validation.client.Validator;
import eu.maydu.gwt.validation.client.actions.FocusAction;
import eu.maydu.gwt.validation.client.actions.LabelTextAction;
import eu.maydu.gwt.validation.client.actions.StyleAction;

import java.util.HashMap;

/**
 * UIHelper
 * <p/>
 * Helper methods for FCE
 */
public class UIHelper {

    public interface UIHelperStyle extends CssResource {
        String inputPanel();

        String validationFailedBorder();

        String validationFailedText();

        String inputLabel();

        String inputLabelSearch();

        String inputLabelDisabled();

        String floatLeft();

        String commandPanel();

        String commandPanelButton();

        String commandPanelButtonDefWidth();
    }

    interface Binder extends UiBinder<DivElement, UIHelper> {
    }

    private static final Binder binder = GWT.create(Binder.class);

    static {
        binder.createAndBindUi(null);
    }

    private static final String CACHE_KEY_DEFAULT = "UIHelper_";
    public static final HashMap<String, HashMap<String, UIObject>> inputCache
            = new HashMap<String, HashMap<String, UIObject>>();
    private static Application application;

    private static UIConstants uiConstants = GWT.create(UIConstants.class);

    @UiField
    public static UIHelperStyle style;


    private UIHelper() {
    }

    public static UIConstants getUiConstants() {
        return uiConstants;
    }

    private static void addToInputCache(String key, UIObject input) {
        addToInputCache(key, input, CACHE_KEY_DEFAULT);
    }

    public static void clearInputCache(String cacheName) {
        if (null == cacheName) {
            cacheName = CACHE_KEY_DEFAULT;
        }
        HashMap cache = inputCache.get(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    public static void clearInputCache() {
        clearInputCache(CACHE_KEY_DEFAULT);
    }

    public static void clearAllInputCache() {
        inputCache.clear();
    }

    public static UIObject getFromInputCache(String id) {
        return getFromInputCache(id, CACHE_KEY_DEFAULT);
    }

    public static void addToInputCache(String key, UIObject input, String cacheName) {
        if (null == cacheName) {
            cacheName = CACHE_KEY_DEFAULT;
        }
        HashMap<String, UIObject> cache = inputCache.get(cacheName);
// create a new cache, if previous cache was not found
        if (cache == null) {
            cache = new HashMap<String, UIObject>();
            inputCache.put(cacheName, cache);
        }
        cache.put(key, input);
    }

    public static UIObject getFromInputCache(String id, String cacheName) {
        if (null == cacheName) {
            cacheName = CACHE_KEY_DEFAULT;
        }
        HashMap<String, UIObject> cache = inputCache.get(cacheName);
        UIObject val = null;
        if (cache != null) {
            val = cache.get(id);
        }
        return val;
    }

    public static String getTextBoxValueFromInputCache(String id) {
        return getTextBoxValueFromInputCache(id, CACHE_KEY_DEFAULT);
    }

    public static String getTextBoxValueFromInputCache(String id, String cacheName) {
        TextBox box = getTextBoxFromInputCache(id, cacheName);
        String val = null;
        if (box != null) {
            val = box.getText();
//            ignore empty, and reset to null
            if (val.length() == 0) {
                val = null;
            }
        }
        return val;
    }

    public static TextBox getTextBoxFromInputCache(String id, String cacheName) {
        return (TextBox) getFromInputCache(id, cacheName);
    }

/*
    public static boolean getCheckBoxValueFromInputCache(String name) {
        CheckBox box = (CheckBox) getFromInputCache(name);
        return box != null && box.isEnabled();
    }
*/

    public static String getListBoxValueFromInputCache(String id) {
        return getListBoxValueFromInputCache(id, CACHE_KEY_DEFAULT);
    }

    public static String getListBoxValueFromInputCache(String id, String cacheName) {
        ListBox box = (ListBox) UIHelper.getFromInputCache(id, cacheName);
        String val = null;
        if (box != null) {
            final int selectedIndex = box.getSelectedIndex();
            if (selectedIndex != -1) {
                val = box.getItemText(selectedIndex);
//            ignore empty, and reset to null
                if ("".equals(val)) {
                    val = null;
                }
            }
        }
        return val;
    }

    public static boolean setListBoxValue(String value, ListBox listBox) {
        int sz = listBox.getItemCount();
        for (int i = 0; i < sz; i++) {
            if (listBox.getItemText(i).equalsIgnoreCase(value)) {
                listBox.setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }

    public static FlowPanel buildInputPanel(Widget label, Widget input,
                                            String[] validatorNames, ValidationProcessor validationProcessor) {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(style.inputPanel());

        label.addStyleName(style.floatLeft());
        panel.add(label);
        panel.add(input);
        if (null != validatorNames && null != validationProcessor && validatorNames.length > 0) {

            Label error = new Label("");
            error.setStylePrimaryName(style.validationFailedText());
            panel.add(error);

            Validator[] validators = new Validator[validatorNames.length];
            int i = 0;
            for (String validatorName : validatorNames) {
                Validator<? extends Validator> validator = ValidatorHelper.createValidator(validatorName, input);
                validator.addActionForFailure(new FocusAction());
                validator.addActionForFailure(new StyleAction(style.validationFailedBorder()));
                validator.addActionForFailure(new LabelTextAction(error, false));
                validators[i++] = validator;
            }
            validationProcessor.addValidators(input.getTitle(), validators);
        }
        return panel;
    }

    public static FlowPanel createTextInput
            (String name, String id, int inputLen, boolean readOnly, String cacheName,
             EventHandler handler, ValidationProcessor validationProcessor, String... validatorNames) {
        id = id.trim();
        final Label label = new Label(name);
        label.setTitle(name);
        label.addStyleName(style.inputLabel());
        label.addStyleName(style.inputLabelDisabled());

        final TextBox input = new TextBox();
        input.setTitle(name);
        input.ensureDebugId(id);
        input.setVisibleLength(inputLen);
        input.setMaxLength(inputLen);
        input.setReadOnly(readOnly);
        input.setEnabled(!readOnly);
        if (input.isEnabled() && input.isVisible()) {
            label.removeStyleName(style.inputLabelDisabled());
        }
        if (null != handler) {
            input.addKeyUpHandler((KeyUpHandler) handler);
        }
        if (null == cacheName) {
            addToInputCache(id, input);
        } else {
            addToInputCache(id, input, cacheName);
        }
        return buildInputPanel(label, input, validatorNames, validationProcessor);
    }

    public static FlowPanel createTextArea(String name, String id, int inputLen, int charWidth, boolean readOnly,
                                           String cacheName, EventHandler handler, ValidationProcessor validationProcessor, String... validatorNames) {
        id = id.trim();

        final Label label = new Label(name);
        label.addStyleName(style.inputLabel());
        label.addStyleName(style.inputLabelDisabled());

        final TextArea input = new TextArea();
        input.ensureDebugId(id);
        input.setCharacterWidth(charWidth);
        input.setVisibleLines(inputLen);
        input.setReadOnly(readOnly);
        input.setEnabled(!readOnly);
        if (input.isEnabled() && input.isVisible()) {
            label.removeStyleName(style.inputLabelDisabled());
        }
        if (null != handler) {
            input.addKeyUpHandler((KeyUpHandler) handler);
        }
        if (null == cacheName) {
            addToInputCache(id, input);
        } else {
            addToInputCache(id, input, cacheName);
        }

        return buildInputPanel(label, input, validatorNames, validationProcessor);
    }

    public static FlowPanel createCheckInput(String name, String id,
                                             boolean readOnly, String cacheName) {
        id = id.trim();
        final Label label = new Label(name);
        label.addStyleName(style.inputLabel());
        label.addStyleName(style.inputLabelDisabled());

        final CheckBox input = new CheckBox();
        input.ensureDebugId(id);
        input.setEnabled(!readOnly);
        if (input.isEnabled() && input.isVisible()) {
            label.removeStyleName(style.inputLabelDisabled());
        }
        if (null == cacheName) {
            addToInputCache(id, input);
        } else {
            addToInputCache(id, input, cacheName);
        }

        return buildInputPanel(label, input, null, null);
    }

    public static FlowPanel createListInput(String name, String id,
                                            boolean isMultipleSelect, String[] listTypes,
                                            boolean readOnly, String cacheName) {
        id = id.trim();
        final Label label = new Label(name);
        label.addStyleName(style.inputLabel());
        label.addStyleName(style.inputLabelDisabled());

        final ListBox input = new ListBox(isMultipleSelect);
        input.ensureDebugId(id);
        input.setEnabled(!readOnly);
        setDefaults(listTypes, input);
        if (input.isEnabled() && input.isVisible()) {
            label.removeStyleName(style.inputLabelDisabled());
        }
        if (null == cacheName) {
            addToInputCache(id, input);
        } else {
            addToInputCache(id, input, cacheName);
        }

        return buildInputPanel(label, input, null, null);
    }

    public static boolean setDefaults(String[] items, ListBox input) {
        if (null != input && null != items) {
// clear previous defaults
            input.clear();
            for (String item : items) {
                input.addItem(item);
            }
            return true;
        }
        return false;
    }

    public static FlowPanel createCommands(String[] buttons, EventHandler... handlers) {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName(style.commandPanel());
        int i = 0;
        int sz = buttons.length;
        for (String text : buttons) {
            Button b = new Button();
            /**
             * HorizontalPanel is a bit trickier. In some cases, you can simply replace it with a DockLayoutPanel,
             * but that requires that you specify its childrens' widths explicitly. The most common alternative is to use
             * FlowPanel, and to use the float: left; CSS property on its children.
             * And of course, you can continue to use HorizontalPanel itself, as long as you take the caveats above into account.
             */
            b.addStyleName(style.commandPanelButton());
            b.addStyleName(style.floatLeft());
            b.setTitle(text);
            b.setText(text);
            if (null != handlers) {
                if (sz == handlers.length) {
                    b.addClickHandler((ClickHandler) handlers[i++]);
                } else {
// todo: handle variable length handlers between 1 and button size
                    b.addClickHandler((ClickHandler) handlers[0]);
                }
            }
            panel.add(b);
        }
        return panel;
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
    public static DialogBox createDialogBox(String title, String msg,
                                            String[] commands, EventHandler... handlers) {

        FlowPanel dialogContents = new FlowPanel();
//        the dialog contents.. msg at top, and command buttons at bottom
        dialogContents.add(new HTML(msg));
        dialogContents.add(createCommands(commands, handlers));

        // Create a dialog box and set the caption text
        final DialogBox dialogBox = new DialogBox(false, true);
        dialogBox.setText(title);
        dialogBox.setWidget(dialogContents);
        dialogBox.setAnimationEnabled(true);
        dialogBox.setGlassEnabled(true);
        return dialogBox;
    }

    public static void enableInputs(ComplexPanel complexPanel) {
        toggleInputs(complexPanel, true);
    }

    public static void disableInputs(ComplexPanel complexPanel) {
        toggleInputs(complexPanel, false);
    }

    private static void toggleInputs(ComplexPanel complexPanel, boolean flag) {
        for (Widget widget : complexPanel) {
            final Class<? extends Widget> widgetClass = widget.getClass();
            if (FlowPanel.class == widgetClass) {
                toggleInputs((ComplexPanel) widget, flag);
            } else if (
                    TextBox.class == widgetClass
                            || TextArea.class == widgetClass
                            || CheckBox.class == widgetClass
                            || ListBox.class == widgetClass
                            || Label.class == widgetClass
                    ) {
                if (TextBox.class == widgetClass || TextArea.class == widgetClass) {
                    TextBoxBase box = (TextBoxBase) widget;
                    box.setEnabled(flag);
                    box.setReadOnly(!flag);
                } else if (CheckBox.class == widgetClass) {
                    ((CheckBox) widget).setEnabled(flag);
                } else if (ListBox.class == widgetClass) {
                    ((ListBox) widget).setEnabled(flag);
                } else if (Label.class == widgetClass) {
                    Label l = ((Label) widget);
                    if (flag) {
                        l.removeStyleName(style.inputLabelDisabled());
                    } else {
                        l.addStyleName(style.inputLabelDisabled());
                    }
                }
            }
        }
    }

    public static void clearInputs(ComplexPanel complexPanel) {
        for (Widget widget : complexPanel) {
            final Class<? extends Widget> widgetClass = widget.getClass();
            if (FlowPanel.class == widgetClass) {
                clearInputs((ComplexPanel) widget);
            } else if (
                    TextBox.class == widgetClass
                            || TextArea.class == widgetClass
                            || CheckBox.class == widgetClass
                            || ListBox.class == widgetClass
                    ) {
                if (TextBox.class == widgetClass || TextArea.class == widgetClass) {
                    ((TextBoxBase) widget).setText("");
                } else if (CheckBox.class == widgetClass) {
                    ((CheckBox) widget).setValue(false);
                } else if (ListBox.class == widgetClass) {
                    ((ListBox) widget).setSelectedIndex(0);
                }
            }
        }
    }

    public static boolean setFocus(ComplexPanel complexPanel) {
        for (Widget widget : complexPanel) {
            final Class<? extends Widget> widgetClass = widget.getClass();
            if (TextBox.class == widgetClass || TextArea.class == widgetClass) {
                ((TextBoxBase) widget).setFocus(true);
                return true;
            } else if (FlowPanel.class == widgetClass) {
                return setFocus((ComplexPanel) widget);
            }
        }
        return false;
    }

    public static void setApplication(Application app) {
        application = app;
    }

    public static Application getApplication() {
        return application;
    }

    public static void scheduleProgress() {
        scheduleProgress(uiConstants.loadProgressWait(), 1000);
    }

    public static void scheduleProgress(String s) {
        getApplication().showProgress(s, 1000);
    }

    public static void scheduleProgress(String s, int ms) {
        getApplication().showProgress(s, ms);
    }

    public static void cancelProgress() {
        getApplication().cancelProgress();
    }

    public static void showStatus(String msg) {
        getApplication().showMessage(msg);
    }

    public static void hideStatus() {
        getApplication().hideMessage();
    }

    public static native void nativeFocus(JavaScriptObject o)/*-{
        try{o.focus();}catch(e){}
     }-*/;

    public static void hackRootLayoutPanelNotBlank() {
        String rootWidth = RootLayoutPanel.get().getElement().getStyle().getWidth();
        RootLayoutPanel.get().setWidth(rootWidth.equals("100%") ? "" : "100%");
    }

    public static void confirm(DialogBox dialogBox, Button button) {
        dialogBox.center();
        nativeFocus(button.getElement());
        hackRootLayoutPanelNotBlank();
    }

}

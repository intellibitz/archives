package com.retailwave.fce.client.util;
/**
 * $Id: ValidatorHelper.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/util/ValidatorHelper.java $
 */

import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.UIObject;
import com.retailwave.fce.client.validator.PersonNameValidator;
import eu.maydu.gwt.validation.client.Validator;
import eu.maydu.gwt.validation.client.validators.standard.NotEmptyValidator;
import eu.maydu.gwt.validation.client.validators.strings.EmailValidator;
import eu.maydu.gwt.validation.client.validators.strings.NameValidator;
import eu.maydu.gwt.validation.client.validators.strings.StringLengthValidator;

public class ValidatorHelper {

    private ValidatorHelper() {
    }

    public static Validator<? extends Validator> createValidator(String name, UIObject uiObject) {
        if (StringLengthValidator.class.getName().equals(name)) {
            return createStringLengthValidator(uiObject);
        } else if (NotEmptyValidator.class.getName().equals(name)) {
            return createNotEmptyValidator(uiObject);
        } else if (NameValidator.class.getName().equals(name)) {
            return createNameValidator(uiObject);
        } else if (PersonNameValidator.class.getName().equals(name)) {
            return createPersonNameValidator(uiObject);
        } else if (EmailValidator.class.getName().equals(name)) {
            return createEmailValidator(uiObject);
        }
        return null;
    }

    private static PersonNameValidator createPersonNameValidator(UIObject uiObject) {
        return new PersonNameValidator((TextBox) uiObject);
    }

    public static StringLengthValidator createStringLengthValidator(UIObject uiObject) {
        StringLengthValidator stringLengthValidator = new StringLengthValidator((TextBoxBase) uiObject);
// todo: set min and max for length validator
        stringLengthValidator.setMax(50);
        return stringLengthValidator;
    }

    public static NotEmptyValidator createNotEmptyValidator(UIObject uiObject) {
        return new NotEmptyValidator((TextBoxBase) uiObject);
    }

    public static NameValidator createNameValidator(UIObject uiObject) {
        return new NameValidator((TextBox) uiObject);
    }

    public static EmailValidator createEmailValidator(UIObject uiObject) {
        return new EmailValidator((TextBox) uiObject);
    }

}
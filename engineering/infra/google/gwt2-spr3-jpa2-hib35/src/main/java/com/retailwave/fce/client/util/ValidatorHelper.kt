package com.retailwave.fce.client.util
/**
 * $Id: ValidatorHelper.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/util/ValidatorHelper.java $
 */

import com.google.gwt.user.client.ui.TextBox
import com.google.gwt.user.client.ui.TextBoxBase
import com.google.gwt.user.client.ui.UIObject
import com.retailwave.fce.client.validator.PersonNameValidator
import eu.maydu.gwt.validation.client.Validator
import eu.maydu.gwt.validation.client.validators.standard.NotEmptyValidator
import eu.maydu.gwt.validation.client.validators.strings.EmailValidator
import eu.maydu.gwt.validation.client.validators.strings.NameValidator
import eu.maydu.gwt.validation.client.validators.strings.StringLengthValidator

class ValidatorHelper private constructor() {
    companion object {
        @JvmStatic
        fun createValidator(name: String, uiObject: UIObject): Validator<out Validator<*>>? {
            if (StringLengthValidator::class.java.name == name) {
                return createStringLengthValidator(uiObject)
            } else if (NotEmptyValidator::class.java.name == name) {
                return createNotEmptyValidator(uiObject)
            } else if (NameValidator::class.java.name == name) {
                return createNameValidator(uiObject)
            } else if (PersonNameValidator::class.java.name == name) {
                return createPersonNameValidator(uiObject)
            } else if (EmailValidator::class.java.name == name) {
                return createEmailValidator(uiObject)
            }
            return null
        }

        @JvmStatic
        private fun createPersonNameValidator(uiObject: UIObject): PersonNameValidator {
            return PersonNameValidator(uiObject as TextBox)
        }

        @JvmStatic
        fun createStringLengthValidator(uiObject: UIObject): StringLengthValidator {
            val stringLengthValidator = StringLengthValidator(uiObject as TextBoxBase)
// todo: set min and max for length validator
            stringLengthValidator.setMax(50)
            return stringLengthValidator
        }

        @JvmStatic
        fun createNotEmptyValidator(uiObject: UIObject): NotEmptyValidator {
            return NotEmptyValidator(uiObject as TextBoxBase)
        }

        @JvmStatic
        fun createNameValidator(uiObject: UIObject): NameValidator {
            return NameValidator(uiObject as TextBox)
        }

        @JvmStatic
        fun createEmailValidator(uiObject: UIObject): EmailValidator {
            return EmailValidator(uiObject as TextBox)
        }
    }
}

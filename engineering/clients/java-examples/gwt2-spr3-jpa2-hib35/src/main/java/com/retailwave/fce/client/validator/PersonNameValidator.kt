package com.retailwave.fce.client.validator
/**
 * $Id: PersonNameValidator.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/validator/PersonNameValidator.java $
 */

import com.google.gwt.user.client.ui.HasText
import com.google.gwt.user.client.ui.SuggestBox
import com.google.gwt.user.client.ui.TextBox
import eu.maydu.gwt.validation.client.ValidationResult
import eu.maydu.gwt.validation.client.i18n.ValidationMessages
import eu.maydu.gwt.validation.client.validators.ValidatorAlgorithmResult
import eu.maydu.gwt.validation.client.validators.strings.NameValidator
import eu.maydu.gwt.validation.client.validators.strings.algorithms.CharacterValidatorAlgorithm

/**
 * PersonNameValidator
 */
class PersonNameValidator : NameValidator {

    var hasText: HasText? = null

    constructor(text: TextBox) : super(text) {
        hasText = text
    }

    constructor(text: SuggestBox) : super(text) {
        hasText = text
    }

    override fun <V : ValidationMessages> validate(messages: V): ValidationResult? {
        var result = super.validate(messages)
        if (null == result && null != hasText) {
            val text = hasText!!.text
            if (null != text && text.contains(".")) {
                val res = ValidatorAlgorithmResult(
                    CharacterValidatorAlgorithm.NOT_A_VALID_CHARACTER, "."
                )
                result = ValidationResult(
                    getErrorMessage(
                        messages, messages
                            .standardMessages.notAValidCharacter('.'),
                        res.parameters
                    )
                )
            }
        }
        return result
    }
}

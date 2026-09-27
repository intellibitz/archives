package com.retailwave.fce.client.validator;
/**
 * $Id: PersonNameValidator.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/validator/PersonNameValidator.java $
 */

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import eu.maydu.gwt.validation.client.ValidationResult;
import eu.maydu.gwt.validation.client.i18n.ValidationMessages;
import eu.maydu.gwt.validation.client.validators.ValidatorAlgorithmResult;
import eu.maydu.gwt.validation.client.validators.strings.NameValidator;
import eu.maydu.gwt.validation.client.validators.strings.algorithms.CharacterValidatorAlgorithm;

/**
 * PersonNameValidator
 */
public class PersonNameValidator
        extends NameValidator {

    HasText hasText = null;

    public PersonNameValidator(TextBox text) {
        super(text);
        hasText = text;
    }

    public PersonNameValidator(SuggestBox text) {
        super(text);
        hasText = text;
    }

    @Override
    public <V extends ValidationMessages> ValidationResult validate(V messages) {
        ValidationResult result = super.validate(messages);
        if (null == result && null != hasText) {
            String text = hasText.getText();
            if (null != text && text.contains(".")) {
                ValidatorAlgorithmResult res = new ValidatorAlgorithmResult
                        (CharacterValidatorAlgorithm.NOT_A_VALID_CHARACTER, ".");
                result = new ValidationResult(getErrorMessage(messages, messages
                        .getStandardMessages().notAValidCharacter('.'),
                        res.getParameters()));
            }
        }
        return result;
    }
}
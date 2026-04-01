package com.group.book_selling.validators;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;

import com.group.book_selling.dto.PersonalInfoForm;

class PersonalInfoFormValidatorTest {

    private final PersonalInfoFormValidator validator = new PersonalInfoFormValidator();

    @Test
    void validate_withBlankFirstName_rejects() {
        PersonalInfoForm form = new PersonalInfoForm();
        form.setFirstName("   ");
        form.setLastName("Nguyen");
        form.setPhone("0912345678");

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(form, "personalInfoForm");
        validator.validate(form, errors);

        assertTrue(errors.hasFieldErrors("firstName"));
    }

    @Test
    void validate_withInvalidPhone_rejects() {
        PersonalInfoForm form = new PersonalInfoForm();
        form.setFirstName("Van");
        form.setLastName("A");
        form.setPhone("12");

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(form, "personalInfoForm");
        validator.validate(form, errors);

        assertTrue(errors.hasFieldErrors("phone"));
    }

    @Test
    void validate_withValidData_hasNoErrors() {
        PersonalInfoForm form = new PersonalInfoForm();
        form.setFirstName("Van");
        form.setLastName("A");
        form.setPhone("0912345678");

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(form, "personalInfoForm");
        validator.validate(form, errors);

        assertFalse(errors.hasErrors());
    }
}

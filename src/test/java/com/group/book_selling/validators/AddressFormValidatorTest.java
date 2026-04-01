package com.group.book_selling.validators;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;

import com.group.book_selling.dto.AddressForm;

class AddressFormValidatorTest {

    private final AddressFormValidator validator = new AddressFormValidator();

    @Test
    void validate_withBlankStreet_rejects() {
        AddressForm form = validForm();
        form.setStreetDetails(" ");

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(form, "addressForm");
        validator.validate(form, errors);

        assertTrue(errors.hasFieldErrors("streetDetails"));
    }

    @Test
    void validate_withInvalidPhone_rejects() {
        AddressForm form = validForm();
        form.setPhoneNumber("abc");

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(form, "addressForm");
        validator.validate(form, errors);

        assertTrue(errors.hasFieldErrors("phoneNumber"));
    }

    @Test
    void validate_withValidData_hasNoErrors() {
        AddressForm form = validForm();

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(form, "addressForm");
        validator.validate(form, errors);

        assertFalse(errors.hasErrors());
    }

    private AddressForm validForm() {
        AddressForm form = new AddressForm();
        form.setRecipientName("Nguyen Van A");
        form.setPhoneNumber("0912345678");
        form.setProvinceOrCity("1");
        form.setDistrict("2");
        form.setWard("3");
        form.setStreetDetails("123 Nguyen Trai");
        form.setDefaultAddress(false);
        return form;
    }
}

package com.group.book_selling.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.group.book_selling.dto.PersonalInfoForm;

@Component
public class PersonalInfoFormValidator implements Validator {

    private static final String PHONE_REGEX = "^[0-9+\\-\\s]{8,}$";

    @Override
    public boolean supports(Class<?> clazz) {
        return PersonalInfoForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PersonalInfoForm form = (PersonalInfoForm) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "firstName.blank",
                "Họ không được để trống!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "lastName.blank",
                "Tên không được để trống!");

        if (errors.hasFieldErrors("phone")) {
            return;
        }

        String phone = form.getPhone();
        if (phone == null || phone.isBlank()) {
            return;
        }

        if (!phone.matches(PHONE_REGEX)) {
            errors.rejectValue("phone", "phone.invalid", "Số điện thoại không hợp lệ!");
        }
    }
}

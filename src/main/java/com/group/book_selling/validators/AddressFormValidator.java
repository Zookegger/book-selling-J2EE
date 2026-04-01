package com.group.book_selling.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.group.book_selling.dto.AddressForm;

@Component
public class AddressFormValidator implements Validator {

    private static final String PHONE_REGEX = "^[0-9+\\-\\s]{8,}$";

    @Override
    public boolean supports(Class<?> clazz) {
        return AddressForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AddressForm form = (AddressForm) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "recipientName", "recipientName.blank",
                "Tên người nhận không được để trống!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "phoneNumber", "phoneNumber.blank",
                "Số điện thoại không được để trống!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "provinceOrCity", "provinceOrCity.blank",
                "Tỉnh/Thành phố không được để trống!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "district", "district.blank",
                "Quận/Huyện không được để trống!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ward", "ward.blank",
                "Phường/Xã không được để trống!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "streetDetails", "streetDetails.blank",
                "Địa chỉ chi tiết không được để trống!");

        if (errors.hasFieldErrors("phoneNumber")) {
            return;
        }

        String phone = form.getPhoneNumber();
        if (phone == null || !phone.matches(PHONE_REGEX)) {
            errors.rejectValue("phoneNumber", "phoneNumber.invalid", "Số điện thoại không hợp lệ!");
        }
    }
}

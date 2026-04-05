package com.group.book_selling.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.group.book_selling.dto.ChangePasswordForm;

@Component
public class ChangePasswordValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ChangePasswordForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ChangePasswordForm form = (ChangePasswordForm) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentPassword", "currentPassword.blank",
                "Mật khẩu hiện tại không được trống!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "newPassword", "newPassword.blank",
                "Mật khẩu mới không được trống!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "confirmPassword", "confirmPassword.blank",
                "Mật khẩu xác nhận không được trống!");

        if (errors.hasFieldErrors("newPassword") || errors.hasFieldErrors("confirmPassword")) {
            return;
        }

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            errors.rejectValue("confirmPassword", "confirmPassword.mismatch", "Mật khẩu xác nhận không khớp!");
        }

        if (!isStrongPassword(form.getNewPassword())) {
            errors.rejectValue("newPassword", "newPassword.weak",
                    "Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ hoa, chữ thường và số!");
        }
    }

    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }

        boolean hasUpperCase = password.matches(".*[A-Z].*");
        boolean hasLowerCase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");

        return hasUpperCase && hasLowerCase && hasDigit;
    }
}

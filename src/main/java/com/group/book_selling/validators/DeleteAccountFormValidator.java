package com.group.book_selling.validators;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.group.book_selling.dto.DeleteAccountForm;
import com.group.book_selling.models.CustomUserDetail;
import com.group.book_selling.models.User;
import com.group.book_selling.services.UserServices;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeleteAccountFormValidator implements Validator {

    private final UserServices userService;

    @Override
    public boolean supports(Class<?> clazz) {
        return DeleteAccountForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        DeleteAccountForm form = (DeleteAccountForm) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "password.blank",
                "Mật khẩu không được để trống!");

        if (errors.hasFieldErrors("password")) {
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetail userDetail)) {
            errors.reject("auth.invalid", "Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại.");
            return;
        }

        User user = userService.findByEmail(userDetail.getEmail());
        if (user == null || !userService.checkPassword(form.getPassword(), user.getPassword())) {
            errors.rejectValue("password", "password.invalid", "Sai mật khẩu!");
        }
    }
}

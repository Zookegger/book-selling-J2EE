package com.group.book_selling.validators;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BeanPropertyBindingResult;

import com.group.book_selling.dto.DeleteAccountForm;
import com.group.book_selling.models.CustomUserDetail;
import com.group.book_selling.models.User;
import com.group.book_selling.models.UserRole;
import com.group.book_selling.services.UserServices;

@ExtendWith(MockitoExtension.class)
class DeleteAccountFormValidatorTest {

    @Mock
    private UserServices userService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validate_withWrongPassword_rejects() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .isEmailVerified(true)
                .build();

        CustomUserDetail principal = new CustomUserDetail(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "encoded", principal.getAuthorities()));

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(userService.checkPassword("wrong", "encoded")).thenReturn(false);

        DeleteAccountForm form = new DeleteAccountForm();
        form.setPassword("wrong");

        DeleteAccountFormValidator validator = new DeleteAccountFormValidator(userService);
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(form, "deleteAccountForm");

        validator.validate(form, errors);

        assertTrue(errors.hasFieldErrors("password"));
    }
}

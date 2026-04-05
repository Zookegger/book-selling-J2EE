package com.group.book_selling.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.group.book_selling.dto.PersonalInfoForm;
import com.group.book_selling.models.CustomUserDetail;
import com.group.book_selling.models.User;
import com.group.book_selling.models.UserRole;
import com.group.book_selling.services.UserServices;
import com.group.book_selling.validators.AddressFormValidator;
import com.group.book_selling.validators.DeleteAccountFormValidator;
import com.group.book_selling.validators.PersonalInfoFormValidator;

class UserControllerTest {

    @Test
    void updatePersonalInfo_withValidationErrors_returnsProfileView() {
        UserServices userService = Mockito.mock(UserServices.class);
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded")
                .firstName("Old")
                .lastName("Name")
                .role(UserRole.USER)
                .isEmailVerified(true)
                .build();

        when(userService.findByEmail("test@example.com")).thenReturn(user);

        UserController controller = new UserController(
                userService,
                new PersonalInfoFormValidator(),
                new AddressFormValidator(),
                new DeleteAccountFormValidator(userService));

        PersonalInfoForm form = new PersonalInfoForm();
        form.setFirstName(" ");
        form.setLastName("Name");
        form.setPhone("0912345678");

        BeanPropertyBindingResult result = new BeanPropertyBindingResult(form, "personalInfoForm");
        new PersonalInfoFormValidator().validate(form, result);

        ExtendedModelMap model = new ExtendedModelMap();
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.updatePersonalInfo(
                new CustomUserDetail(user),
                form,
                result,
                model,
                redirect);

        assertEquals("profile/profile", view);
        assertEquals("personal", model.getAttribute("activeTab"));
        assertFalse(result.getFieldErrors("firstName").isEmpty());
        verify(userService, never()).save(user);
    }
}

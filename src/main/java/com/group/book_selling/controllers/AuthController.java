/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.group.book_selling.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.group.book_selling.dto.AddressForm;
import com.group.book_selling.dto.ChangePasswordForm;
import com.group.book_selling.dto.DeleteAccountForm;
import com.group.book_selling.dto.PersonalInfoForm;
import com.group.book_selling.models.CustomUserDetail;
import com.group.book_selling.models.User;
import com.group.book_selling.models.UserRole;
import com.group.book_selling.services.EmailService;
import com.group.book_selling.services.UserServices;
import com.group.book_selling.template.EmailTemplate;
import com.group.book_selling.validators.ChangePasswordValidator;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Nguyen Duc Trung
 */
@Slf4j // Sử dụng Lombok để tạo logger

@Controller
@RequiredArgsConstructor
public class AuthController {
    private static final String PROFILE_VIEW = "profile/profile";

    private final UserServices userService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ChangePasswordValidator changePasswordValidator;

    @InitBinder("changePasswordForm")
    protected void initChangePasswordBinder(WebDataBinder binder) {
        binder.addValidators(changePasswordValidator);
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user, BindingResult result, Model model,
            HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            List<FieldError> errors = result.getFieldErrors();
            for (FieldError error : errors) {
                model.addAttribute(error.getField() + "_error", error.getDefaultMessage());
            }

            return "auth/register";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(UserRole.USER);
        user.setEmailVerified(false);
        userService.prepareEmailVerification(user);

        String verificationUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath("/verify-email")
                .replaceQuery(null)
                .queryParam("token", user.getEmailVerificationToken())
                .build()
                .toUriString();

        String fullName = (user.getFirstName() == null ? "" : user.getFirstName().trim())
                + (user.getLastName() == null ? "" : " " + user.getLastName().trim());
        String message = EmailTemplate.verificationEmailTemplate(fullName.trim(), verificationUrl);
        emailService.sendHtmlMessage(user.getEmail(), "Xác thực email", message)
                .exceptionally(ex -> {
                    log.error("Lỗi khi gửi email xác thực cho {}", user.getEmail(), ex);
                    return null;
                });

        redirectAttributes.addFlashAttribute("registrationMessage",
                "Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.");
        return "redirect:/login";
    }

    @PostMapping("/resend-verification")
    public String handleResendVerification(@RequestParam("email") String email, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        if (email == null || email.isBlank()) {
            redirectAttributes.addFlashAttribute("resetErrorMessage", "Vui lòng nhập email.");
            return "redirect:/login";
        }

        User user = userService.findByEmail(email);
        if (user != null) {
            userService.prepareEmailVerification(user);
            String verificationUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                    .replacePath("/verify-email")
                    .replaceQuery(null)
                    .queryParam("token", user.getEmailVerificationToken())
                    .build()
                    .toUriString();

            String fullName = (user.getFirstName() == null ? "" : user.getFirstName().trim())
                    + (user.getLastName() == null ? "" : " " + user.getLastName().trim());
            String message = EmailTemplate.verificationEmailTemplate(fullName.trim(), verificationUrl);

            emailService.sendHtmlMessage(user.getEmail(), "Xác thực email", message)
                    .exceptionally(ex -> {
                        log.error("Lỗi khi gửi email xác thực lại cho {}: {}", email, ex);
                        return null;
                    });
        }

        redirectAttributes.addFlashAttribute("resetRequestMessage",
                "Nếu email tồn tại trong hệ thống, chúng tôi đã gửi lại liên kết xác thực. Vui lòng kiểm tra email của bạn.");

        return "redirect:/login";
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        boolean verified = userService.verifyEmailToken(token);
        if (verified) {
            redirectAttributes.addFlashAttribute("verifyMessage", "Email của bạn đã được xác thực thành công.");
        } else {
            redirectAttributes.addFlashAttribute("verifyMessage", "Liên kết xác thực không hợp lệ hoặc đã hết hạn.");
        }
        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam("email") String email, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        if (email == null || email.isBlank()) {
            redirectAttributes.addFlashAttribute("resetErrorMessage", "Vui lòng nhập email.");
            return "redirect:/forgot-password";
        }

        User user = userService.findByEmail(email);
        if (user == null) {
            redirectAttributes.addFlashAttribute("resetRequestMessage",
                    "Nếu email tồn tại, bạn sẽ nhận được hướng dẫn thay đổi mật khẩu trong vài phút.");
            return "redirect:/login";
        }

        userService.preparePasswordReset(user);
        String resetUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath("/reset-password")
                .replaceQuery(null)
                .queryParam("token", user.getPasswordResetToken())
                .build()
                .toUriString();

        String fullName = (user.getFirstName() == null ? "" : user.getFirstName().trim())
                + (user.getLastName() == null ? "" : " " + user.getLastName().trim());
        String message = EmailTemplate.passwordResetEmailTemplate(fullName.trim(), resetUrl);

        emailService.sendHtmlMessage(user.getEmail(), "Yêu cầu đặt lại mật khẩu", message)
                .exceptionally(ex -> {
                    // log the error — you can't redirect from here
                    log.error("Lỗi khi gửi email xác thực lại cho {}: {}", email, ex);
                    return null;
                });

        redirectAttributes.addFlashAttribute("resetRequestMessage",
                "Nếu email tồn tại, bạn sẽ nhận được hướng dẫn thay đổi mật khẩu trong vài phút.");
        return "redirect:/login";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("token") String token, Model model,
            RedirectAttributes redirectAttributes) {
        if (token == null || token.isBlank()) {
            redirectAttributes.addFlashAttribute("resetErrorMessage", "Liên kết không hợp lệ.");
            return "redirect:/login";
        }

        User user = userService.findByPasswordResetToken(token);
        if (user == null || user.getPasswordResetExpires() == null
                || user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("resetErrorMessage",
                    "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            return "redirect:/login";
        }

        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("token") String token,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model, RedirectAttributes redirectAttributes) {
        if (token == null || token.isBlank()) {
            redirectAttributes.addFlashAttribute("resetErrorMessage", "Liên kết không hợp lệ.");
            return "redirect:/login";
        }
        if (password == null || password.isBlank() || !password.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("passwordError", "Mật khẩu và xác nhận phải khớp và không được để trống.");
            return "auth/reset-password";
        }

        boolean resetSuccessful = userService.resetPassword(token, password);
        if (!resetSuccessful) {
            redirectAttributes.addFlashAttribute("resetErrorMessage",
                    "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            return "redirect:/login";
        }

        redirectAttributes.addFlashAttribute("resetPasswordMessage",
                "Mật khẩu đã được đặt lại thành công. Vui lòng đăng nhập với mật khẩu mới.");
        return "redirect:/login";
    }
    

    /**
     * Đổi mật khẩu người dùng
     */
    @PostMapping("/profile/change-password")
    public String changePassword(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Validated @ModelAttribute("changePasswordForm") ChangePasswordForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (userDetail == null) {
            return "redirect:/login";
        }

        User user = userService.findByEmail(userDetail.getEmail());
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng.");
            return "redirect:/profile";
        }

        if (!result.hasFieldErrors("currentPassword") && !user.comparePassword(form.getCurrentPassword())) {
            result.rejectValue("currentPassword", "currentPassword.invalid", "Mật khẩu hiện tại không đúng!");
        }

        if (result.hasErrors()) {
            populateProfileModelForSecurity(user, form, model);
            return PROFILE_VIEW;
        }

        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userService.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");

        return "redirect:/profile";
    }

    private void populateProfileModelForSecurity(User user, ChangePasswordForm form, Model model) {
        model.addAttribute("user", user);

        if (!model.containsAttribute("personalInfoForm")) {
            PersonalInfoForm personalInfoForm = new PersonalInfoForm();
            personalInfoForm.setFirstName(user.getFirstName());
            personalInfoForm.setLastName(user.getLastName());
            personalInfoForm.setPhone(user.getPhoneNumber());
            model.addAttribute("personalInfoForm", personalInfoForm);
        }

        if (!model.containsAttribute("addAddressForm")) {
            model.addAttribute("addAddressForm", new AddressForm());
        }

        if (!model.containsAttribute("editAddressForm")) {
            model.addAttribute("editAddressForm", new AddressForm());
        }

        if (!model.containsAttribute("deleteAccountForm")) {
            model.addAttribute("deleteAccountForm", new DeleteAccountForm());
        }

        model.addAttribute("changePasswordForm", form);
        model.addAttribute("activeTab", "security");
    }
}

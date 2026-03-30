/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.group.book_selling.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.group.book_selling.models.User;
import com.group.book_selling.models.UserRole;
import com.group.book_selling.services.EmailService;
import com.group.book_selling.services.UserServices;
import com.group.book_selling.template.EmailTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 *
 * @author Nguyen Duc Trung
 */
@Controller
public class AuthController {
    @Autowired
    private UserServices userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

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
        userService.save(user);

        String verificationUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath("/verify-email")
                .replaceQuery(null)
                .queryParam("token", user.getEmailVerificationToken())
                .build()
                .toUriString();

        String fullName = (user.getFirstName() == null ? "" : user.getFirstName().trim())
                + (user.getLastName() == null ? "" : " " + user.getLastName().trim());
        String message = EmailTemplate.verificationEmailTemplate(fullName.trim(), verificationUrl);
        try {
            emailService.sendHtmlMessage(user.getEmail(), "Xác thực email", message);
            redirectAttributes.addFlashAttribute("registrationMessage", "Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.");
            return "redirect:/login";
        } catch (RuntimeException ex) {
            model.addAttribute("emailError", "Không gửi được email xác thực. Vui lòng thử lại sau.");
            return "auth/register";
        }
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
}
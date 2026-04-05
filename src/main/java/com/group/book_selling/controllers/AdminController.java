package com.group.book_selling.controllers;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group.book_selling.dto.AdminCreateUserForm;
import com.group.book_selling.models.CustomUserDetail;
import com.group.book_selling.models.User;
import com.group.book_selling.models.UserRole;
import com.group.book_selling.repositories.IUserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String overview() {
        return "admin/overview";
    }

    @GetMapping("/books")
    public String manageBooks() {
        return "books/list";
    }

    @GetMapping("/users")
    public String users(
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "q", required = false) String keyword,
            Model model) {
        List<User> allUsers = userRepository.findAll();

        long totalUsers = allUsers.size();
        long adminCount = allUsers.stream().filter(user -> user.getRole() == UserRole.ADMIN).count();
        long unverifiedCount = allUsers.stream().filter(user -> !user.isEmailVerified()).count();

        UserRole selectedRole = parseRole(role);
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        String keywordLower = normalizedKeyword.toLowerCase(Locale.ROOT);

        List<User> users = allUsers.stream()
                .filter(user -> selectedRole == null || user.getRole() == selectedRole)
                .filter(user -> keywordLower.isBlank() || matchesKeyword(user, keywordLower))
                .collect(Collectors.toList());

        model.addAttribute("users", users);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("unverifiedCount", unverifiedCount);
        model.addAttribute("selectedRole", selectedRole == null ? "ALL" : selectedRole.name());
        model.addAttribute("keyword", normalizedKeyword);

        return "admin/user/list";
    }

    @GetMapping("/users/create")
    public String createUserPage(Model model) {
        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new AdminCreateUserForm());
        }
        return "admin/user/form";
    }

    @PostMapping("/users")
    public String createUser(
            @Valid @ModelAttribute("userForm") AdminCreateUserForm userForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (userRepository.existsByEmail(userForm.getEmail())) {
            bindingResult.rejectValue("email", "email.exists", "Email này đã được sử dụng");
        }

        if (bindingResult.hasErrors()) {
            String firstError = bindingResult.getFieldErrors().isEmpty()
                    ? "Dữ liệu tạo người dùng không hợp lệ."
                    : bindingResult.getFieldErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", firstError);
            return "redirect:/admin/users";
        }

        User user = User.builder()
                .firstName(userForm.getFirstName().trim())
                .lastName(userForm.getLastName().trim())
                .email(userForm.getEmail().trim().toLowerCase(Locale.ROOT))
                .phoneNumber(userForm.getPhoneNumber() == null ? null : userForm.getPhoneNumber().trim())
                .password(passwordEncoder.encode(userForm.getPassword()))
                .role(userForm.getRole() == null ? UserRole.USER : userForm.getRole())
                .isEmailVerified(userForm.isEmailVerified())
                .isAccountLocked(false)
                .build();

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo tài khoản người dùng thành công.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/update")
    public String updateUser(
            @PathVariable("id") Long id,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam(name = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(name = "password", required = false) String password,
            @RequestParam("role") String role,
            @RequestParam(name = "emailVerified", defaultValue = "false") boolean emailVerified,
            @RequestParam(name = "q", required = false) String keyword,
            @RequestParam(name = "selectedRole", required = false) String selectedRole,
            RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng.");
            appendFilterParams(redirectAttributes, selectedRole, keyword);
            return "redirect:/admin/users";
        }

        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (normalizedEmail.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email không được để trống.");
            appendFilterParams(redirectAttributes, selectedRole, keyword);
            return "redirect:/admin/users";
        }

        User duplicate = userRepository.findByEmail(normalizedEmail);
        if (duplicate != null && !duplicate.getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email này đã được sử dụng.");
            appendFilterParams(redirectAttributes, selectedRole, keyword);
            return "redirect:/admin/users";
        }

        if (firstName == null || firstName.trim().isBlank() || lastName == null || lastName.trim().isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Họ và tên không được để trống.");
            appendFilterParams(redirectAttributes, selectedRole, keyword);
            return "redirect:/admin/users";
        }

        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(normalizedEmail);
        user.setPhoneNumber(phoneNumber == null ? null : phoneNumber.trim());
        user.setEmailVerified(emailVerified);

        try {
            user.setRole(UserRole.valueOf(role.trim().toUpperCase(Locale.ROOT)));
        } catch (Exception ex) {
            user.setRole(UserRole.USER);
        }

        if (password != null && !password.trim().isBlank()) {
            user.setPassword(passwordEncoder.encode(password.trim()));
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật người dùng thành công: " + user.getEmail());
        appendFilterParams(redirectAttributes, selectedRole, keyword);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}")
    public String userDetail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng.");
            return "redirect:/admin/users";
        }

        model.addAttribute("userDetail", user);
        return "admin/user/detail";
    }

    @PostMapping("/users/{id}/toggle-lock")
    public String toggleUserLock(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetail currentUser,
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "q", required = false) String keyword,
            RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng.");
            appendFilterParams(redirectAttributes, role, keyword);
            return "redirect:/admin/users";
        }

        if (currentUser != null && currentUser.getEmail().equalsIgnoreCase(user.getEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không thể tự khóa tài khoản của chính mình.");
            appendFilterParams(redirectAttributes, role, keyword);
            return "redirect:/admin/users";
        }

        user.setAccountLocked(!user.isAccountLocked());
        userRepository.save(user);

        if (user.isAccountLocked()) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã khóa tài khoản: " + user.getEmail());
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Đã mở khóa tài khoản: " + user.getEmail());
        }

        appendFilterParams(redirectAttributes, role, keyword);
        return "redirect:/admin/users";
    }

    private UserRole parseRole(String role) {
        if (role == null || role.isBlank() || "ALL".equalsIgnoreCase(role)) {
            return null;
        }
        try {
            return UserRole.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean matchesKeyword(User user, String keywordLower) {
        String fullName = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).toLowerCase(Locale.ROOT);
        String email = user.getEmail() == null ? "" : user.getEmail().toLowerCase(Locale.ROOT);
        String phone = user.getPhoneNumber() == null ? "" : user.getPhoneNumber().toLowerCase(Locale.ROOT);
        return fullName.contains(keywordLower) || email.contains(keywordLower) || phone.contains(keywordLower);
    }

    private void appendFilterParams(RedirectAttributes redirectAttributes, String role, String keyword) {
        if (role != null && !role.isBlank()) {
            redirectAttributes.addAttribute("role", role);
        }
        if (keyword != null && !keyword.isBlank()) {
            redirectAttributes.addAttribute("q", keyword.trim());
        }
    }
}

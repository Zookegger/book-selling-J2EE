package com.group.book_selling.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.group.book_selling.models.CustomUserDetail;
import com.group.book_selling.models.User;
import com.group.book_selling.services.UserServices;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Controller để xử lý các chức năng liên quan đến hồ sơ người dùng
 */
@Controller
public class UserController {

    @Autowired
    private UserServices userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/avatar";

    /**
     * Hiển thị trang hồ sơ người dùng
     */
    @GetMapping("/profile")
    public String profilePage(
        @AuthenticationPrincipal CustomUserDetail userDetail,
        Model model) {
            if (userDetail == null) {
                return "redirect:/login";
            }
        User user = userService.findByEmail(userDetail.getEmail());
        model.addAttribute("user", user);
        return "profile";
    }

    /**
     * Cập nhật thông tin cá nhân người dùng
     */
    @PostMapping("/profile/update-personal")
    public String updatePersonalInfo(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam(value = "phone", required = false) String phone,
            RedirectAttributes redirectAttributes) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof CustomUserDetail) {
                CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();
                User user = userService.findByEmail(userDetail.getEmail());
                
                if (user != null) {
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setPhoneNumber(phone != null ? phone : "");
                    userService.save(user);
                    redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
                }
            }
        }
        
        return "redirect:/profile";
    }

    /**
     * Upload ảnh đại diện
     */
    @PostMapping("/profile/upload-avatar")
    public String uploadAvatar(
            @RequestParam("avatarFile") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        if (authentication.getPrincipal() instanceof CustomUserDetail) {
            CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();
            User user = userService.findByEmail(userDetail.getEmail());
            
            if (user != null && !file.isEmpty()) {
                try {
                    // Validate file type
                    String contentType = file.getContentType();
                    if (!contentType.startsWith("image/")) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn file ảnh!");
                        return "redirect:/profile";
                    }
                    
                    // Create upload directory if not exists
                    Path uploadPath = Paths.get(UPLOAD_DIR);
                    Files.createDirectories(uploadPath);
                    
                    // Generate unique filename
                    String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                    Path filepath = uploadPath.resolve(filename);
                    
                    // Save file
                    Files.copy(file.getInputStream(), filepath);
                    
                    // Update user profile picture URL
                    user.setProfilePictureUrl("/uploads/avatar/" + filename);
                    userService.save(user);
                    
                    redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ảnh đại diện thành công!");
                } catch (IOException e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tải ảnh: " + e.getMessage());
                }
            }
        }
        
        return "redirect:/profile";
    }

    /**
     * Đổi mật khẩu người dùng
     */
    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        if (authentication.getPrincipal() instanceof CustomUserDetail) {
            CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();
            User user = userService.findByEmail(userDetail.getEmail());
            
            if (user != null) {
                // Validate current password
                if (!user.comparePassword(currentPassword)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không đúng!");
                    return "redirect:/profile";
                }
                
                // Validate new password
                if (newPassword == null || newPassword.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới không được trống!");
                    return "redirect:/profile";
                }
                
                if (!newPassword.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
                    return "redirect:/profile";
                }
                
                // Validate password strength with regex
                if (!isStrongPassword(newPassword)) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ hoa, chữ thường và số!");
                    return "redirect:/profile";
                }
                
                // Update password
                user.setPassword(passwordEncoder.encode(newPassword));
                userService.save(user);
                redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
            }
        }
        
        return "redirect:/profile";
    }

    /**
     * Kiểm tra độ mạnh của mật khẩu
     * Phải có: ít nhất 6 ký tự, chữ hoa, chữ thường, và số
     */
    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        boolean hasUpperCase = password.matches(".*[A-Z].*");
        boolean hasLowerCase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        return hasUpperCase && hasLowerCase && hasDigit;
    }

    /**
     * Thêm địa chỉ mới
     */
    @PostMapping("/profile/add-address")
    public String addAddress(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestParam("recipientName") String recipientName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("provinceOrCity") String provinceOrCity,
            @RequestParam("district") String district,
            @RequestParam("ward") String ward,
            @RequestParam("streetDetails") String streetDetails,
            @RequestParam(value = "isDefault", required = false) String isDefault,
            RedirectAttributes redirectAttributes) {
        
        try {
            User user = userService.findByEmail(userDetail.getEmail());
            if (user != null) {
                User.Address newAddress = User.Address.builder()
                        .recipientName(recipientName)
                        .phoneNumber(phoneNumber)
                        .provinceOrCity(provinceOrCity)
                        .district(district)
                        .ward(ward)
                        .streetDetails(streetDetails)
                        .isDefault(isDefault != null && isDefault.equals("on"))
                        .country("Vietnam")
                        .build();
                
                user.getAddresses().add(newAddress);
                userService.save(user);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm địa chỉ thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm địa chỉ: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }

    /**
     * Cập nhật địa chỉ
     */
    @PostMapping("/profile/update-address")
    public String updateAddress(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestParam("addressIndex") int addressIndex,
            @RequestParam("recipientName") String recipientName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("provinceOrCity") String provinceOrCity,
            @RequestParam("district") String district,
            @RequestParam("ward") String ward,
            @RequestParam("streetDetails") String streetDetails,
            @RequestParam(value = "isDefault", required = false) String isDefault,
            RedirectAttributes redirectAttributes) {
        
        try {
            User user = userService.findByEmail(userDetail.getEmail());
            if (user != null && addressIndex >= 0 && addressIndex < user.getAddresses().size()) {
                User.Address address = user.getAddresses().get(addressIndex);
                address.setRecipientName(recipientName);
                address.setPhoneNumber(phoneNumber);
                address.setProvinceOrCity(provinceOrCity);
                address.setDistrict(district);
                address.setWard(ward);
                address.setStreetDetails(streetDetails);
                address.setDefault(isDefault != null && isDefault.equals("on"));
                
                userService.save(user);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật địa chỉ thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật địa chỉ: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }

    /**
     * Xóa địa chỉ
     */
    @PostMapping("/profile/delete-address")
    public String deleteAddress(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestParam("addressIndex") int addressIndex,
            RedirectAttributes redirectAttributes) {
        
        try {
            User user = userService.findByEmail(userDetail.getEmail());
            if (user != null && addressIndex >= 0 && addressIndex < user.getAddresses().size()) {
                user.getAddresses().remove(addressIndex);
                userService.save(user);
                redirectAttributes.addFlashAttribute("successMessage", "Xóa địa chỉ thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa địa chỉ: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }

    /**
     * Đặt địa chỉ mặc định
     */
    @PostMapping("/profile/set-default-address")
    public String setDefaultAddress(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestParam("addressIndex") int addressIndex,
            RedirectAttributes redirectAttributes) {
        
        try {
            User user = userService.findByEmail(userDetail.getEmail());
            if (user != null && addressIndex >= 0 && addressIndex < user.getAddresses().size()) {
                // Reset all addresses to not default
                for (User.Address addr : user.getAddresses()) {
                    addr.setDefault(false);
                }
                // Set the selected one as default
                user.getAddresses().get(addressIndex).setDefault(true);
                
                userService.save(user);
                redirectAttributes.addFlashAttribute("successMessage", "Đặt địa chỉ mặc định thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi đặt địa chỉ mặc định: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }

    /**
    * Xóa tài khoản người dùng
    */
    @PostMapping("/profile/delete-account")
    public String deleteAccount(
    @RequestParam("password") String password,
    RedirectAttributes redirectAttributes) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth.getPrincipal() instanceof CustomUserDetail userDetail) {
        User user = userService.findByEmail(userDetail.getEmail());
        // ✅ check password
        if (!userService.checkPassword(password, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sai mật khẩu!");
            return "redirect:/profile";
        }
        // ✅ delete user
        userService.deleteUser(user.getId());
        // ✅ logout
        SecurityContextHolder.clearContext();
        return "redirect:/login?deleted";
    }
    return "redirect:/login";
}
}


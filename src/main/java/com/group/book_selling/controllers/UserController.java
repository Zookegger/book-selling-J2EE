package com.group.book_selling.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group.book_selling.dto.AddressForm;
import com.group.book_selling.dto.ChangePasswordForm;
import com.group.book_selling.dto.DeleteAccountForm;
import com.group.book_selling.dto.PersonalInfoForm;
import com.group.book_selling.models.CustomUserDetail;
import com.group.book_selling.models.User;
import com.group.book_selling.services.UserServices;
import com.group.book_selling.validators.AddressFormValidator;
import com.group.book_selling.validators.DeleteAccountFormValidator;
import com.group.book_selling.validators.PersonalInfoFormValidator;

import lombok.RequiredArgsConstructor;

/**
 * Controller để xử lý các chức năng liên quan đến hồ sơ người dùng
 */
@Controller
@RequiredArgsConstructor
public class UserController {

    private static final String PROFILE_VIEW = "profile/profile";

    private final UserServices userService;
    private final PersonalInfoFormValidator personalInfoFormValidator;
    private final AddressFormValidator addressFormValidator;
    private final DeleteAccountFormValidator deleteAccountFormValidator;

    @InitBinder("personalInfoForm")
    protected void initPersonalInfoBinder(WebDataBinder binder) {
        binder.addValidators(personalInfoFormValidator);
    }

    @InitBinder("addAddressForm")
    protected void initAddAddressBinder(WebDataBinder binder) {
        binder.addValidators(addressFormValidator);
    }

    @InitBinder("editAddressForm")
    protected void initEditAddressBinder(WebDataBinder binder) {
        binder.addValidators(addressFormValidator);
    }

    @InitBinder("deleteAccountForm")
    protected void initDeleteAccountBinder(WebDataBinder binder) {
        binder.addValidators(deleteAccountFormValidator);
    }

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
        if (user == null) {
            return "redirect:/login";
        }

        prepareProfileModel(user, model);
        return PROFILE_VIEW;
    }

    /**
     * Cập nhật thông tin cá nhân người dùng
     */
    @PostMapping("/profile/update-personal")
    public String updatePersonalInfo(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Validated @ModelAttribute("personalInfoForm") PersonalInfoForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (userDetail == null) {
            return "redirect:/login";
        }

        User user = userService.findByEmail(userDetail.getEmail());
        if (user == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            prepareProfileModel(user, model);
            model.addAttribute("activeTab", "personal");
            return PROFILE_VIEW;
        }

        user.setFirstName(form.getFirstName().trim());
        user.setLastName(form.getLastName().trim());
        user.setPhoneNumber(form.getPhone() == null ? "" : form.getPhone().trim());
        userService.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");

        return "redirect:/profile";
    }

    /**
     * Thêm địa chỉ mới
     */
    @PostMapping("/profile/add-address")
    public String addAddress(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Validated @ModelAttribute("addAddressForm") AddressForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (userDetail == null) {
            return "redirect:/login";
        }

        User user = userService.findByEmail(userDetail.getEmail());
        if (user == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            prepareProfileModel(user, model);
            model.addAttribute("activeTab", "addresses");
            model.addAttribute("openModal", "addAddressModal");
            return PROFILE_VIEW;
        }

        if (form.isDefaultAddress()) {
            clearDefaultAddressFlag(user);
        }

        User.Address newAddress = User.Address.builder()
                .recipientName(form.getRecipientName().trim())
                .phoneNumber(form.getPhoneNumber().trim())
                .provinceOrCity(form.getProvinceOrCity().trim())
                .district(form.getDistrict().trim())
                .ward(form.getWard().trim())
                .streetDetails(form.getStreetDetails().trim())
                .isDefault(form.isDefaultAddress())
                .country("Vietnam")
                .build();

        user.getAddresses().add(newAddress);
        userService.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm địa chỉ thành công!");

        return "redirect:/profile";
    }

    /**
     * Cập nhật địa chỉ
     */
    @PostMapping("/profile/update-address")
    public String updateAddress(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Validated @ModelAttribute("editAddressForm") AddressForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (userDetail == null) {
            return "redirect:/login";
        }

        User user = userService.findByEmail(userDetail.getEmail());
        if (user == null) {
            return "redirect:/login";
        }

        if (form.getAddressIndex() == null) {
            result.rejectValue("addressIndex", "addressIndex.required", "Không tìm thấy địa chỉ cần cập nhật!");
        }

        if (!result.hasFieldErrors("addressIndex")
                && (form.getAddressIndex() < 0 || form.getAddressIndex() >= user.getAddresses().size())) {
            result.rejectValue("addressIndex", "addressIndex.invalid", "Địa chỉ không hợp lệ!");
        }

        if (result.hasErrors()) {
            prepareProfileModel(user, model);
            model.addAttribute("activeTab", "addresses");
            model.addAttribute("openModal", "editAddressModal");
            return PROFILE_VIEW;
        }

        if (form.isDefaultAddress()) {
            clearDefaultAddressFlag(user);
        }

        User.Address address = user.getAddresses().get(form.getAddressIndex());
        address.setRecipientName(form.getRecipientName().trim());
        address.setPhoneNumber(form.getPhoneNumber().trim());
        address.setProvinceOrCity(form.getProvinceOrCity().trim());
        address.setDistrict(form.getDistrict().trim());
        address.setWard(form.getWard().trim());
        address.setStreetDetails(form.getStreetDetails().trim());
        address.setDefault(form.isDefaultAddress());

        userService.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật địa chỉ thành công!");

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
            if (userDetail == null) {
                return "redirect:/login";
            }

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
            if (userDetail == null) {
                return "redirect:/login";
            }

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
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Validated @ModelAttribute("deleteAccountForm") DeleteAccountForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (userDetail == null) {
            return "redirect:/login";
        }

        User user = userService.findByEmail(userDetail.getEmail());
        if (user == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            prepareProfileModel(user, model);
            model.addAttribute("activeTab", "security");
            model.addAttribute("openModal", "deleteAccountModal");
            return PROFILE_VIEW;
        }

        userService.deleteUser(user.getId());
        SecurityContextHolder.clearContext();
        return "redirect:/login?deleted";
    }

    private void prepareProfileModel(User user, Model model) {
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

        if (!model.containsAttribute("changePasswordForm")) {
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
        }

        if (!model.containsAttribute("activeTab")) {
            model.addAttribute("activeTab", "personal");
        }
    }

    private void clearDefaultAddressFlag(User user) {
        for (User.Address address : user.getAddresses()) {
            address.setDefault(false);
        }
    }
}

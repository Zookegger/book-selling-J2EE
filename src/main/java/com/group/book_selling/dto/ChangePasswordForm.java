package com.group.book_selling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordForm {

    @NotBlank(message = "Mật khẩu hiện tại không được trống!")
    @Size(max = 128, message = "Mật khẩu hiện tại không hợp lệ!")
    private String currentPassword;

    @NotBlank(message = "Mật khẩu mới không được trống!")
    @Size(max = 128, message = "Mật khẩu mới không hợp lệ!")
    private String newPassword;

    @NotBlank(message = "Mật khẩu xác nhận không được trống!")
    @Size(max = 128, message = "Mật khẩu xác nhận không hợp lệ!")
    private String confirmPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}

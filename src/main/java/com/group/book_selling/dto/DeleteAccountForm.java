package com.group.book_selling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DeleteAccountForm {

    @NotBlank(message = "Mật khẩu không được để trống!")
    @Size(max = 128, message = "Mật khẩu không hợp lệ!")
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

package com.group.book_selling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PersonalInfoForm {

    @NotBlank(message = "Họ không được để trống!")
    @Size(max = 100, message = "Họ không được vượt quá 100 ký tự!")
    private String firstName;

    @NotBlank(message = "Tên không được để trống!")
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự!")
    private String lastName;

    @Pattern(regexp = "^[0-9+\\-\\s]{8,}$|^$", message = "Số điện thoại không hợp lệ!")
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự!")
    private String phone;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

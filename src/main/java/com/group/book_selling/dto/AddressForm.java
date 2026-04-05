package com.group.book_selling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AddressForm {

    private Integer addressIndex;

    @NotBlank(message = "Tên người nhận không được để trống!")
    @Size(max = 120, message = "Tên người nhận không được vượt quá 120 ký tự!")
    private String recipientName;

    @NotBlank(message = "Số điện thoại không được để trống!")
    @Pattern(regexp = "^[0-9+\\-\\s]{8,}$", message = "Số điện thoại không hợp lệ!")
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự!")
    private String phoneNumber;

    @NotBlank(message = "Tỉnh/Thành phố không được để trống!")
    private String provinceOrCity;

    @NotBlank(message = "Quận/Huyện không được để trống!")
    private String district;

    @NotBlank(message = "Phường/Xã không được để trống!")
    private String ward;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống!")
    @Size(max = 255, message = "Địa chỉ chi tiết không được vượt quá 255 ký tự!")
    private String streetDetails;

    private boolean defaultAddress;

    public Integer getAddressIndex() {
        return addressIndex;
    }

    public void setAddressIndex(Integer addressIndex) {
        this.addressIndex = addressIndex;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProvinceOrCity() {
        return provinceOrCity;
    }

    public void setProvinceOrCity(String provinceOrCity) {
        this.provinceOrCity = provinceOrCity;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getStreetDetails() {
        return streetDetails;
    }

    public void setStreetDetails(String streetDetails) {
        this.streetDetails = streetDetails;
    }

    public boolean isDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }
}

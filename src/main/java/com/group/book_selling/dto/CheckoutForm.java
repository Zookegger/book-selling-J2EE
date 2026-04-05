package com.group.book_selling.dto;

import com.group.book_selling.models.PaymentMethod;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutForm {

    @NotNull(message = "Vui lòng chọn địa chỉ giao hàng")
    private Integer selectedAddressIndex;

    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
}

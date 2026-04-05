package com.group.book_selling.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @NotNull
    @Column(nullable = false)
    private Long bookId;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String sku;

    @NotBlank
    @Column(nullable = false, length = 220)
    private String title;

    @Column(length = 500)
    private String coverImage;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookFormatType formatType;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal vatRate;

    @Min(1)
    @Column(nullable = false)
    private int quantity;

    @NotBlank
    @Column(nullable = false, length = 3)
    private String currency;

    public BigDecimal getSubTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getTaxAmount() {
        return getSubTotal().multiply(vatRate);
    }

    public BigDecimal getGrandTotal() {
        return getSubTotal().add(getTaxAmount());
    }
}

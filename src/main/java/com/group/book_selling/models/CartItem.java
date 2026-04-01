package com.group.book_selling.models;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class CartItem implements Serializable {

    private final Long bookId;
    private final String sku; // Mã định danh duy nhất cho mỗi phiên bản sách (có thể là kết hợp của bookId + formatType)
    private final String title;
    private final String coverImage;
    private final BookFormatType formatType;
    private final BigDecimal unitPrice;
    private final String currency;
    private int qty;

    public CartItem(
            Long bookId,
            String sku,
            String title,
            String coverImage,
            BookFormatType formatType,
            BigDecimal unitPrice,
            String currency,
            int qty) {
        if (qty < 1) throw new IllegalArgumentException("Số lượng phải ít nhất là 1.");
        this.bookId = bookId;
        this.sku = sku;
        this.title = title;
        this.coverImage = coverImage;
        this.formatType = formatType;
        this.unitPrice = unitPrice;
        this.currency = currency;
        // Digital/audiobook: Số lượng luôn là 1 để tránh việc mua bản điện tư nhiều lần
        this.qty = (formatType == BookFormatType.PHYSICAL) ? qty : 1;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(qty));
    }

    
    public void setQty(int qty) {
        if (qty < 1) throw new IllegalArgumentException("Số lượng phải ít nhất là 1.");
        this.qty = (formatType == BookFormatType.PHYSICAL) ? qty : 1;
    }

    public boolean isDigital() {
        return formatType == BookFormatType.DIGITAL || formatType == BookFormatType.AUDIOBOOK;
    }
}
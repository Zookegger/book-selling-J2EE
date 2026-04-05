package com.group.book_selling.models;

import java.io.Serializable;
import java.math.BigDecimal;

import com.group.book_selling.utils.SlugUtils;

import lombok.Getter;

@Getter
public class CartItem implements Serializable {

    private final Long bookId;
    private final String sku; // Mã định danh duy nhất cho mỗi phiên bản sách (có thể là kết hợp của bookId +
                              // formatType)
    private final String slug; // Slug được tạo từ tiêu đề sách, dùng để hiển thị và liên kết đến trang chi
                               // tiết
    private final String title;
    private final String coverImage;
    private final BookFormatType formatType;
    private final BigDecimal unitPrice;
    private final BigDecimal vatRate;
    private final String currency;
    private int qty;

    public CartItem(
            Long bookId,
            String sku,
            String title,
            String slug,
            String coverImage,
            BookFormatType formatType,
            BigDecimal unitPrice,
            BigDecimal vatRate,
            String currency,
            int qty) {
        if (qty < 1)
            throw new IllegalArgumentException("Số lượng phải ít nhất là 1.");
        this.bookId = bookId;
        this.slug = slug != null ? slug : SlugUtils.slugify(title);
        this.sku = sku;
        this.title = title;
        this.coverImage = coverImage;
        this.formatType = formatType;
        this.unitPrice = unitPrice;
        this.vatRate = (vatRate == null) ? BigDecimal.ZERO : vatRate;
        this.currency = currency;
        // Digital/audiobook: Số lượng luôn là 1 để tránh việc mua bản điện tư nhiều lần
        this.qty = (formatType == BookFormatType.PHYSICAL) ? qty : 1;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(qty));
    }

    public BigDecimal getTaxAmount() {
        return getSubtotal().multiply(vatRate);
    }

    public BigDecimal getGrandTotal() {
        return getSubtotal().add(getTaxAmount());
    }

    public void setQty(int qty) {
        if (qty < 1)
            throw new IllegalArgumentException("Số lượng phải ít nhất là 1.");
        this.qty = (formatType == BookFormatType.PHYSICAL) ? qty : 1;
    }

    public boolean isDigital() {
        return formatType == BookFormatType.DIGITAL || formatType == BookFormatType.AUDIOBOOK;
    }
}
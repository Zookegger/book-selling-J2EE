package com.group.book_selling.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

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
public class BookFormat {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "format_type", nullable = false, length = 20)
    private BookFormatType formatType;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String sku;

    @Column(name = "format_isbn", length = 32)
    private String isbn;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(precision = 12, scale = 2)
    private BigDecimal discountedPrice;

    @Builder.Default
    @NotBlank
    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    private LocalDate releaseDate;

    @Min(0)
    private Integer stockQuantity;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(precision = 8, scale = 3)
    private BigDecimal weight;

    private String dimensions;

    @Column(name = "file_path")
    private String file;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_format", length = 10)
    private DigitalFileFormat fileFormat;

    @Min(0)
    private Long fileSize;

    @Min(1)
    private Integer downloadLimit;

    private String sampleFile;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    void markCreated(LocalDateTime now) {
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
        normalizeCurrency();
    }

    void markUpdated(LocalDateTime now) {
        this.updatedAt = now;
        normalizeCurrency();
    }

    void validateBusinessRules() {
        if (this.formatType == BookFormatType.PHYSICAL && this.file != null && !this.file.isBlank()) {
            throw new IllegalStateException("Physical format cannot have a digital file.");
        }

        if (this.formatType == BookFormatType.DIGITAL && this.stockQuantity != null) {
            throw new IllegalStateException("Digital format should not have stock quantity.");
        }

        if (this.discountedPrice != null && this.discountedPrice.compareTo(this.price) > 0) {
            throw new IllegalStateException("Discounted price cannot be greater than price.");
        }
    }

    private void normalizeCurrency() {
        if (this.currency == null || this.currency.isBlank()) {
            this.currency = "USD";
            return;
        }

        this.currency = this.currency.toUpperCase(Locale.ROOT);
    }
}

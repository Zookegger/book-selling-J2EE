package com.group.book_selling.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
class Address {

    @NotBlank
    private String recipientName;

    @NotBlank
    @Pattern(regexp = "^[0-9+\\-\\s]{8,}$", message = "Not a valid phone number!")
    private String phoneNumber;

    @NotBlank
    private String provinceOrCity;
    @NotBlank
    private String district;
    @NotBlank
    private String ward;
    @NotBlank
    private String streetDetails;

    @Builder.Default
    private String country = "Vietnam";

    @Builder.Default
    private boolean isDefault = false;
}

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class WishlistItem {

    @NotNull
    private Long bookId;

    @Builder.Default
    private LocalDateTime addedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private BookFormatType desiredFormat;
}

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class DigitalLibraryItem {

    @NotNull
    private Long bookId;

    @Min(0)
    private Integer formatIndex;

    @Builder.Default
    private LocalDateTime purchasedAt = LocalDateTime.now();
}

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String firstName;

    @NotBlank
    @Column(nullable = false)
    private String lastName;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private UserRole role = UserRole.CUSTOMER;

    @Builder.Default
    private boolean isEmailVerified = false;

    private String emailVerificationToken;
    private LocalDateTime emailVerificationExpires;

    private String passwordResetToken;
    private LocalDateTime passwordResetExpires;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_addresses", joinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_wishlist", joinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    private List<WishlistItem> wishList = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_digital_library", joinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    private List<DigitalLibraryItem> digitalLibrary = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        hashPasswordIfNeeded();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        hashPasswordIfNeeded();
    }

    public boolean comparePassword(String candidate) {
        return PASSWORD_ENCODER.matches(candidate, this.password);
    }

    private void hashPasswordIfNeeded() {
        if (this.password == null || this.password.isBlank()) {
            return;
        }

        if (isBcryptHash(this.password)) {
            return;
        }

        this.password = PASSWORD_ENCODER.encode(this.password);
    }

    private boolean isBcryptHash(String value) {
        return value.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    }
}
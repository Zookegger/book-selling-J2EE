package com.group.book_selling.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.group.book_selling.utils.SlugUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "authors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank
    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Builder.Default
    @Column(columnDefinition = "TEXT")
    private String bio = "";

    private LocalDate birthDate;

    @Column(length = 255)
    private String website;

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        applyDerivedFields(true);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        applyDerivedFields(false);
    }

    private void applyDerivedFields(boolean createFallbackEmail) {
        this.slug = SlugUtils.slugify(this.name);

        if (this.bio == null) {
            this.bio = "";
        }

        if (createFallbackEmail && (this.email == null || this.email.isBlank())) {
            String base = SlugUtils.slugify(this.name);
            String suffix = UUID.randomUUID().toString().substring(0, 8);
            this.email = base + "-" + suffix + "@example.com";
        }
    }
}

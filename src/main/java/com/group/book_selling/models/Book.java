package com.group.book_selling.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.group.book_selling.utils.SlugUtils;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books", indexes = {
        @Index(name = "idx_books_title", columnList = "title")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_books_slug", columnNames = "slug"),
        @UniqueConstraint(name = "uk_books_isbn", columnNames = "isbn")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 220)
    private String title;

    @NotBlank
    @Column(nullable = false, unique = true, length = 260)
    private String slug;

    @Builder.Default
    @Column(length = 220)
    private String subtitle = "";

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(unique = true, length = 32)
    private String isbn;

    @NotNull
    @Column(nullable = false)
    private LocalDate publicationDate;

    @Builder.Default
    @NotBlank
    @Column(nullable = false, length = 10)
    private String language = "en";

    @Min(1)
    private Integer pageCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "book_authors", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "author_id"))
    @Builder.Default
    private List<Author> authors = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "book_categories", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    @Builder.Default
    private List<Category> categories = new ArrayList<>();

    @Column(length = 500)
    private String coverImage;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "book_formats", joinColumns = @JoinColumn(name = "book_id"), indexes = {
            @Index(name = "idx_book_formats_sku", columnList = "sku")
    }, uniqueConstraints = {
            @UniqueConstraint(name = "uk_book_formats_sku", columnNames = "sku"),
            @UniqueConstraint(name = "uk_book_formats_isbn", columnNames = "format_isbn")
    })
    @Builder.Default
    private List<BookFormat> formats = new ArrayList<>();

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        applyDerivedFieldsAndValidate(now, true);
    }

    @PreUpdate
    protected void onUpdate() {
        LocalDateTime now = LocalDateTime.now();
        this.updatedAt = now;
        applyDerivedFieldsAndValidate(now, false);
    }

    private void applyDerivedFieldsAndValidate(LocalDateTime now, boolean create) {
        String slugSource = this.title;

        if (this.authors != null && !this.authors.isEmpty()) {
            Author primaryAuthor = this.authors.get(0);
            if (primaryAuthor != null && primaryAuthor.getName() != null && !primaryAuthor.getName().isBlank()) {
                slugSource = this.title + "-" + primaryAuthor.getName();
            }
        }

        this.slug = SlugUtils.slugify(slugSource);

        if (this.subtitle == null) {
            this.subtitle = "";
        }

        if (this.language == null || this.language.isBlank()) {
            this.language = "en";
        }

        if (this.formats == null) {
            this.formats = new ArrayList<>();
            return;
        }

        for (BookFormat format : this.formats) {
            if (format == null) {
                continue;
            }

            format.validateBusinessRules();
            if (create) {
                format.markCreated(now);
            } else {
                format.markUpdated(now);
            }
        }
    }
}

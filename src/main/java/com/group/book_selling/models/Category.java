package com.group.book_selling.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.group.book_selling.utils.SlugUtils;

import jakarta.persistence.Column;
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
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_categories_slug", columnList = "slug"),
        @Index(name = "idx_categories_parent", columnList = "parent_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank
    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @Builder.Default
    @Column(columnDefinition = "TEXT")
    private String description = "";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "category_ancestors", joinColumns = @JoinColumn(name = "category_id"), inverseJoinColumns = @JoinColumn(name = "ancestor_id"))
    @Builder.Default
    private List<Category> ancestors = new ArrayList<>();

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private int orderIndex = 0;

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        applyDerivedFields();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        applyDerivedFields();
    }

    private void applyDerivedFields() {
        this.slug = SlugUtils.slugify(this.name);

        if (this.description == null) {
            this.description = "";
        }
    }
}

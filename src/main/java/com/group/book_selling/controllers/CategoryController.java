package com.group.book_selling.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.group.book_selling.models.Category;
import com.group.book_selling.repository.ICategoryRepository;
import com.group.book_selling.utils.SlugUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * API CRUD co ban cho danh muc sach.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryRepository categoryRepository;

    /** Lay danh sach danh muc. */
    @GetMapping
    public List<Category> findAll() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "orderIndex").and(Sort.by("id")));
    }

    /** Lay chi tiet danh muc. */
    @GetMapping("/{id}")
    public Category findById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay danh muc"));
    }

    /** Tao danh muc moi. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category create(@Valid @RequestBody Category request) {
        request.setId(null);
        request.setSlug(SlugUtils.slugify(request.getName()));
        if (request.getAncestors() == null) {
            request.setAncestors(new ArrayList<>());
        }
        return categoryRepository.save(request);
    }

    /** Cap nhat danh muc. */
    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @Valid @RequestBody Category request) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay danh muc"));

        existing.setName(request.getName());
        existing.setSlug(SlugUtils.slugify(request.getName()));
        existing.setDescription(request.getDescription());
        existing.setParent(request.getParent());
        existing.setAncestors(request.getAncestors() == null ? new ArrayList<>() : request.getAncestors());
        existing.setOrderIndex(request.getOrderIndex());

        return categoryRepository.save(existing);
    }

    /** Xoa danh muc theo id. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay danh muc");
        }
        categoryRepository.deleteById(id);
    }
}

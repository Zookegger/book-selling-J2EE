package com.group.book_selling.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.group.book_selling.models.Category;
import com.group.book_selling.repository.ICategoryRepository;
import com.group.book_selling.utils.SlugUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ICategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "orderIndex").and(Sort.by("name")));
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay danh muc"));
    }

    @Transactional(readOnly = true)
    public Category findBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay danh muc"));
    }

    @Transactional(readOnly = true)
    public List<Category> findAllExceptId(Long id) {
        return categoryRepository.findAll().stream()
                .filter(category -> !category.getId().equals(id))
                .toList();
    }

    @Transactional
    public Category create(Category request) {
        request.setId(null);
        request.setSlug(SlugUtils.slugify(request.getName()));
        request.setAncestors(
                request.getAncestors() == null ? new ArrayList<>() : new ArrayList<>(request.getAncestors()));
        return categoryRepository.save(request);
    }

    @Transactional
    public Category update(Long id, Category request) {
        Category existing = findById(id);

        existing.setName(request.getName());
        existing.setSlug(SlugUtils.slugify(request.getName()));
        existing.setDescription(request.getDescription());
        existing.setParent(request.getParent());
        existing.setAncestors(
                request.getAncestors() == null ? new ArrayList<>() : new ArrayList<>(request.getAncestors()));
        existing.setOrderIndex(request.getOrderIndex());

        return categoryRepository.save(existing);
    }

    @Transactional
    public Category save(Category category) {
        category.setAncestors(
                category.getAncestors() == null ? new ArrayList<>() : new ArrayList<>(category.getAncestors()));
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay danh muc");
        }
        categoryRepository.deleteById(id);
    }
}

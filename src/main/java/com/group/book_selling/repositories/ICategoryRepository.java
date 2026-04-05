package com.group.book_selling.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group.book_selling.models.Category;

public interface ICategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    Optional<Category> findBySlug(String slug);
}

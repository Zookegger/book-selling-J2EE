package com.group.book_selling.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group.book_selling.models.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findBySlug(String slug);

    boolean existsByEmail(String email);
}

package com.group.book_selling.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group.book_selling.models.Author;

public interface IAuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findByName(String name);

    Optional<Author> findBySlug(String slug);

    boolean existsByEmail(String email);
}

package com.group.book_selling.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group.book_selling.models.Publisher;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {

    Optional<Publisher> findBySlug(String slug);

    boolean existsByName(String name);
}

package com.group.book_selling.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group.book_selling.models.Publisher;

public interface IPublisherRepository extends JpaRepository<Publisher, Long> {

    Optional<Publisher> findByName(String name);

    Optional<Publisher> findBySlug(String slug);

    boolean existsByName(String name);
}

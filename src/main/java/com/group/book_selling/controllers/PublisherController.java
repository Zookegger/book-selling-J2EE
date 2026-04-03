package com.group.book_selling.controllers;

import java.util.List;

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

import com.group.book_selling.models.Publisher;
import com.group.book_selling.services.PublisherService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * API CRUD co ban cho nha xuat ban.
 */
@RestController
@RequestMapping("/api/publishers")
@RequiredArgsConstructor
public class PublisherController {

    private final PublisherService publisherService;

    /** Lay danh sach nha xuat ban. */
    @GetMapping
    public List<Publisher> findAll() {
        return publisherService.findAll();
    }

    /** Lay chi tiet nha xuat ban. */
    @GetMapping("/{id}")
    public Publisher findById(@PathVariable Long id) {
        return publisherService.findById(id);
    }

    /** Tao nha xuat ban moi. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Publisher create(@Valid @RequestBody Publisher request) {
        return publisherService.create(request);
    }

    /** Cap nhat nha xuat ban. */
    @PutMapping("/{id}")
    public Publisher update(@PathVariable Long id, @Valid @RequestBody Publisher request) {
        return publisherService.update(id, request);
    }

    /** Xoa nha xuat ban theo id. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        publisherService.delete(id);
    }
}

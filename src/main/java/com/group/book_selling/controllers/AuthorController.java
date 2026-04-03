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

import com.group.book_selling.models.Author;
import com.group.book_selling.services.AuthorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * API co ban de quan ly tac gia.
 *
 * <p>Cac endpoint nay dung cho khoi tao du an va thu nghiem CRUD nhanh.</p>
 */
@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;
    
    /** Lay danh sach tac gia. */
    @GetMapping
    public List<Author> findAll() {
        return authorService.findAll();
    }

    /** Lay chi tiet tac gia theo id. */
    @GetMapping("/{id}")
    public Author findById(@PathVariable Long id) {
        return authorService.findById(id);
    }

    /** Tao moi tac gia. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Author create(@Valid @RequestBody Author request) {
        return authorService.create(request);
    }

    /** Cap nhat thong tin tac gia. */
    @PutMapping("/{id}")
    public Author update(@PathVariable Long id, @Valid @RequestBody Author request) {
        return authorService.update(id, request);
    }

    /** Xoa tac gia theo id. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        authorService.delete(id);
    }
}

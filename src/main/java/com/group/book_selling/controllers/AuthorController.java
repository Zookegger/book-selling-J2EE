package com.group.book_selling.controllers;

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

import com.group.book_selling.models.Author;
import com.group.book_selling.repository.IAuthorRepository;
import com.group.book_selling.utils.SlugUtils;

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

    private final IAuthorRepository authorRepository;
    
    /** Lay danh sach tac gia. */
    @GetMapping
    public List<Author> findAll() {
        return authorRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    /** Lay chi tiet tac gia theo id. */
    @GetMapping("/{id}")
    public Author findById(@PathVariable Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay tac gia"));
    }

    /** Tao moi tac gia. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Author create(@Valid @RequestBody Author request) {
        request.setId(null);
        request.setSlug(SlugUtils.slugify(request.getName()));
        return authorRepository.save(request);
    }

    /** Cap nhat thong tin tac gia. */
    @PutMapping("/{id}")
    public Author update(@PathVariable Long id, @Valid @RequestBody Author request) {
        Author existing = authorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay tac gia"));

        existing.setName(request.getName());
        existing.setSlug(SlugUtils.slugify(request.getName()));
        existing.setEmail(request.getEmail());
        existing.setBio(request.getBio());
        existing.setBirthDate(request.getBirthDate());
        existing.setWebsite(request.getWebsite());

        return authorRepository.save(existing);
    }

    /** Xoa tac gia theo id. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!authorRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay tac gia");
        }
        authorRepository.deleteById(id);
    }
}

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

import com.group.book_selling.models.Publisher;
import com.group.book_selling.repository.IPublisherRepository;
import com.group.book_selling.utils.SlugUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * API CRUD co ban cho nha xuat ban.
 */
@RestController
@RequestMapping("/api/publishers")
@RequiredArgsConstructor
public class PublisherController {

    private final IPublisherRepository publisherRepository;

    /** Lay danh sach nha xuat ban. */
    @GetMapping
    public List<Publisher> findAll() {
        return publisherRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    /** Lay chi tiet nha xuat ban. */
    @GetMapping("/{id}")
    public Publisher findById(@PathVariable Long id) {
        return publisherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay nha xuat ban"));
    }

    /** Tao nha xuat ban moi. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Publisher create(@Valid @RequestBody Publisher request) {
        request.setId(null);
        request.setSlug(SlugUtils.slugify(request.getName()));
        return publisherRepository.save(request);
    }

    /** Cap nhat nha xuat ban. */
    @PutMapping("/{id}")
    public Publisher update(@PathVariable Long id, @Valid @RequestBody Publisher request) {
        Publisher existing = publisherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay nha xuat ban"));

        existing.setName(request.getName());
        existing.setSlug(SlugUtils.slugify(request.getName()));
        existing.setDescription(request.getDescription());
        existing.setLocation(request.getLocation());
        existing.setContactEmail(request.getContactEmail());
        existing.setWebsite(request.getWebsite());
        existing.setLogo(request.getLogo());
        existing.setActive(request.isActive());

        return publisherRepository.save(existing);
    }

    /** Xoa nha xuat ban theo id. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!publisherRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay nha xuat ban");
        }
        publisherRepository.deleteById(id);
    }
}

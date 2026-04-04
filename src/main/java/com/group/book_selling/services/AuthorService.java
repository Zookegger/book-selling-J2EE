package com.group.book_selling.services;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.group.book_selling.models.Author;
import com.group.book_selling.repositories.IAuthorRepository;
import com.group.book_selling.utils.SlugUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final IAuthorRepository authorRepository;

    @Transactional(readOnly = true)
    public List<Author> findAll() {
        return authorRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Transactional(readOnly = true)
    public Author findById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay tac gia"));
    }

    @Transactional
    public Author create(Author request) {
        request.setId(null);
        request.setSlug(SlugUtils.slugify(request.getName()));
        return authorRepository.save(request);
    }

    @Transactional
    public Author update(Long id, Author request) {
        Author existing = findById(id);

        existing.setName(request.getName());
        existing.setSlug(SlugUtils.slugify(request.getName()));
        existing.setEmail(request.getEmail());
        existing.setBio(request.getBio());
        existing.setBirthDate(request.getBirthDate());
        existing.setWebsite(request.getWebsite());

        return authorRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!authorRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay tac gia");
        }
        authorRepository.deleteById(id);
    }
}

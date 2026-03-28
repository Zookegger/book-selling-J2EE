package com.group.book_selling.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import com.group.book_selling.models.Book;
import com.group.book_selling.models.Category;
import com.group.book_selling.models.Publisher;
import com.group.book_selling.repository.IAuthorRepository;
import com.group.book_selling.repository.IBookRepository;
import com.group.book_selling.repository.ICategoryRepository;
import com.group.book_selling.repository.IPublisherRepository;
import com.group.book_selling.utils.SlugUtils;

import jakarta.validation.Valid;

/**
 * API CRUD co ban cho sach.
 *
 * <p>Controller nay xu ly map id tac gia/danh muc/nha xuat ban thanh entity truoc khi luu.</p>
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final IBookRepository bookRepository;
    private final IAuthorRepository authorRepository;
    private final ICategoryRepository categoryRepository;
    private final IPublisherRepository publisherRepository;

    public BookController(IBookRepository bookRepository,
            IAuthorRepository authorRepository,
            ICategoryRepository categoryRepository,
            IPublisherRepository publisherRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.publisherRepository = publisherRepository;
    }

    /** Lay danh sach sach. */
    @GetMapping
    public List<Book> findAll() {
        return bookRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    /** Lay chi tiet sach theo id. */
    @GetMapping("/{id}")
    public Book findById(@PathVariable Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay sach"));
    }

    /** Tao moi sach. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book create(@Valid @RequestBody BookRequest request) {
        Book book = new Book();
        applyRequestToBook(book, request);
        return bookRepository.save(book);
    }

    /** Cap nhat thong tin sach. */
    @PutMapping("/{id}")
    public Book update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay sach"));

        applyRequestToBook(existing, request);
        return bookRepository.save(existing);
    }

    /** Xoa sach theo id. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay sach");
        }
        bookRepository.deleteById(id);
    }

    private void applyRequestToBook(Book target, BookRequest request) {
        target.setTitle(request.title());
        target.setSlug(SlugUtils.slugify(request.title()));
        target.setSubtitle(request.subtitle());
        target.setDescription(request.description());
        target.setIsbn(request.isbn());
        target.setPublicationDate(request.publicationDate());
        target.setLanguage(request.language());
        target.setPageCount(request.pageCount());
        target.setCoverImage(request.coverImage());

        target.setPublisher(resolvePublisher(request.publisherId()));
        target.setAuthors(resolveAuthors(request.authorIds()));
        target.setCategories(resolveCategories(request.categoryIds()));
        target.setFormats(request.formats() == null ? new ArrayList<>() : new ArrayList<>(request.formats()));
    }

    private Publisher resolvePublisher(Long publisherId) {
        if (publisherId == null) {
            return null;
        }

        return publisherRepository.findById(publisherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "publisherId khong hop le: " + publisherId));
    }

    private List<Author> resolveAuthors(List<Long> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Author> authors = authorRepository.findAllById(authorIds);
        validateResolvedCount("authorIds", authorIds, authors.size());
        return authors;
    }

    private List<Category> resolveCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Category> categories = categoryRepository.findAllById(categoryIds);
        validateResolvedCount("categoryIds", categoryIds, categories.size());
        return categories;
    }

    private void validateResolvedCount(String fieldName, List<Long> requestedIds, int resolvedCount) {
        int uniqueCount = Set.copyOf(requestedIds).size();
        if (uniqueCount != resolvedCount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    fieldName + " co gia tri khong ton tai trong he thong");
        }
    }
}

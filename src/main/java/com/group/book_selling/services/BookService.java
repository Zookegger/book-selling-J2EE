package com.group.book_selling.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.group.book_selling.controllers.BookRequest;
import com.group.book_selling.dto.Suggestion;
import com.group.book_selling.models.Author;
import com.group.book_selling.models.Book;
import com.group.book_selling.models.Category;
import com.group.book_selling.models.Publisher;
import com.group.book_selling.repository.IAuthorRepository;
import com.group.book_selling.repository.IBookRepository;
import com.group.book_selling.repository.ICategoryRepository;
import com.group.book_selling.repository.IPublisherRepository;
import com.group.book_selling.utils.SlugUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {

    private final IBookRepository bookRepository;
    private final IPublisherRepository publisherRepository;
    private final IAuthorRepository authorRepository;
    private final ICategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Book> findBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return bookRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        }
        return bookRepository.searchBooks(keyword.trim());
    }

    @Transactional(readOnly = true)
    public List<Suggestion> autocomplete(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        List<Book> books = bookRepository.searchBooks(keyword.trim());
        Map<String, Suggestion> seen = new LinkedHashMap<>();

        for (Book book : books) {
            String title = book.getTitle();
            String slug = book.getSlug();
            String isbn = book.getIsbn();

            String key = (slug != null && !slug.isBlank()) ? slug
                    : (title != null ? title : (isbn != null ? isbn : ""));
            if (key.isBlank() || seen.containsKey(key)) {
                continue;
            }

            String author = null;
            if (book.getAuthors() != null && !book.getAuthors().isEmpty() && book.getAuthors().get(0) != null) {
                author = book.getAuthors().get(0).getName();
            }

            seen.put(key, new Suggestion(title, slug, author, isbn));
            if (seen.size() >= 12) {
                break;
            }
        }

        return new ArrayList<>(seen.values());
    }

    @Transactional(readOnly = true)
    public Book findBySlug(String slug) {
        return bookRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay sach"));
    }

    @Transactional
    public Book createBook(BookRequest request) {
        return createBook(request, request.coverImage());
    }

    @Transactional
    public Book createBook(BookRequest request, String coverImagePath) {
        Book book = new Book();

        applyRequestToBook(book, request);
        if (coverImagePath != null && !coverImagePath.isBlank()) {
            book.setCoverImage(coverImagePath);
        }

        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(String slug, BookRequest request, String coverImagePath) {
        Book existing = findBySlug(slug);

        applyRequestToBook(existing, request);
        if (coverImagePath != null && !coverImagePath.isBlank()) {
            existing.setCoverImage(coverImagePath);
        }

        return bookRepository.save(existing);
    }

    @Transactional
    public void deleteBySlug(String slug) {
        Book book = findBySlug(slug);
        bookRepository.delete(book);
    }

    @Transactional(readOnly = true)
    public List<Publisher> findAllPublishers() {
        return publisherRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Transactional(readOnly = true)
    public List<Author> findAllAuthors() {
        return authorRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Transactional(readOnly = true)
    public List<Category> findAllCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Transactional(readOnly = true)
    public List<CategoryWithCount> findCategoryWithCount() {
        return findAllCategories().stream()
                .map(category -> new CategoryWithCount(
                        category.getId(),
                        category.getName(),
                        bookRepository.countByCategories_Id(category.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<Category, List<Book>> findBestsellersByCategory() {
        List<Category> categories = categoryRepository
                .findAll(Sort.by(Sort.Direction.ASC, "orderIndex").and(Sort.by("id")));

        Map<Category, List<Book>> popularByCategory = new LinkedHashMap<>();
        for (Category category : categories) {
            List<Book> books = bookRepository.findTop5ByCategories_IdOrderByCreatedAtDesc(category.getId());
            if (!books.isEmpty()) {
                popularByCategory.put(category, books);
            }
        }

        return popularByCategory;
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

        if (request.coverImage() != null && !request.coverImage().isBlank()) {
            target.setCoverImage(request.coverImage());
        }

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

    public record CategoryWithCount(Long id, String name, Long count) {
    }
}
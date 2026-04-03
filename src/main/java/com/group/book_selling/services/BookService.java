package com.group.book_selling.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group.book_selling.controllers.BookRequest;
import com.group.book_selling.models.Author;
import com.group.book_selling.models.Book;
import com.group.book_selling.models.Category;
import com.group.book_selling.models.Publisher;
import com.group.book_selling.repository.IAuthorRepository;
import com.group.book_selling.repository.IBookRepository;
import com.group.book_selling.repository.ICategoryRepository;
import com.group.book_selling.repository.IPublisherRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {

    private final IBookRepository bookRepository;
    private final IPublisherRepository publisherRepository;
    private final IAuthorRepository authorRepository;
    private final ICategoryRepository categoryRepository;

    @Transactional
    public Book createBook(BookRequest request) {
        Book book = new Book();
        
        // 1. Map các trường cơ bản
        book.setTitle(request.title());
        book.setSubtitle(request.subtitle());
        book.setDescription(request.description());
        book.setIsbn(request.isbn());
        book.setPublicationDate(request.publicationDate());
        book.setLanguage(request.language());
        book.setPageCount(request.pageCount());
        book.setCoverImage(request.coverImage());

        // 2. Map Publisher (Nhà xuất bản)
        if (request.publisherId() != null) {
            Publisher publisher = publisherRepository.findById(request.publisherId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Nhà xuất bản với ID: " + request.publisherId()));
            book.setPublisher(publisher);
        }

        // 3. Map Authors (Tác giả)
        if (request.authorIds() != null && !request.authorIds().isEmpty()) {
            List<Author> authors = authorRepository.findAllById(request.authorIds());
            book.setAuthors(authors);
        }

        // 4. Map Categories (Danh mục)
        if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.categoryIds());
            book.setCategories(categories);
        }

        // 5. Map Formats (Định dạng sách)
        if (request.formats() != null) {
            book.setFormats(new ArrayList<>(request.formats()));
        } else {
            book.setFormats(new ArrayList<>());
        }

        // Lưu vào Database
        return bookRepository.save(book);
    }
}
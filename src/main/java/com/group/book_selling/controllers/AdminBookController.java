package com.group.book_selling.controllers;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.group.book_selling.models.Book;
import com.group.book_selling.repository.IBookRepository;

@Controller
@RequestMapping("/admin/books")
public class AdminBookController {

    private final IBookRepository bookRepository;

    public AdminBookController(IBookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        List<Book> books;
        if (keyword == null || keyword.trim().isEmpty()) {
            books = bookRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        } else {
            books = bookRepository.searchBooks(keyword.trim());
        }
        model.addAttribute("books", books);
        model.addAttribute("keyword", keyword);
        return "admin/books/list";
    }
}

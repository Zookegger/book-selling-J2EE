package com.group.book_selling.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.group.book_selling.models.Book;
import com.group.book_selling.models.Category;
import com.group.book_selling.repository.IBookRepository;
import com.group.book_selling.repository.ICategoryRepository;

import lombok.RequiredArgsConstructor;

/**
 *
 * @author Nguyen Duc Trung
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final IBookRepository bookRepository;
    private final ICategoryRepository categoryRepository;

    @GetMapping("/")
    public String homePage(Model model) {
        return "index";
    }

    @GetMapping("/genres")
    public String categories(Model model) {
        List<Category> categories = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "orderIndex").and(Sort.by("id")));
        List<Map<String, Object>> categoryWithCount = categories.stream().map(c -> {
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("id", c.getId());
            data.put("name", c.getName());
            data.put("count", bookRepository.countByCategories_Id(c.getId()));
            return data;
        }).toList();

        model.addAttribute("categoryWithCount", categoryWithCount);
        model.addAttribute("selectedCategoryIds", List.of());
        return "books/categories";
    }

    @GetMapping("/bestsellers")
    public String bestSellers(Model model) {
        List<Category> categories = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "orderIndex").and(Sort.by("id")));

        Map<Category, List<Book>> popularByCategory = new java.util.LinkedHashMap<>();
        for (Category category : categories) {
            List<Book> books = bookRepository.findTop5ByCategories_IdOrderByCreatedAtDesc(category.getId());
            if (!books.isEmpty()) {
                popularByCategory.put(category, books);
            }
        }

        model.addAttribute("popularByCategory", popularByCategory);
        return "books/bestsellers";
    }
}

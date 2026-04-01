/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.group.book_selling.controllers;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.group.book_selling.models.Category;
import com.group.book_selling.repository.IBookRepository;
import com.group.book_selling.repository.ICategoryRepository;

/**
 *
 * @author Nguyen Duc Trung
 */
@Controller
public class HomeController {

    private final ICategoryRepository categoryRepository;
    private final IBookRepository bookRepository;

    public HomeController(ICategoryRepository categoryRepository, IBookRepository bookRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
    }

    @GetMapping("/")
    public String homePage(Model model) {
        addCategoryData(model);
        return "index";
    }

    @GetMapping("/genres")
    public String categoriesPage(Model model) {
        addCategoryData(model);
        return "categories";
    }

    private void addCategoryData(Model model) {
        List<Category> categories = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        List<CategoryWithCount> categoryWithCount = categories.stream()
                .map(c -> new CategoryWithCount(c.getId(), c.getName(), bookRepository.countByCategories_Id(c.getId())))
                .toList();

        model.addAttribute("categoryWithCount", categoryWithCount);
        model.addAttribute("selectedCategoryIds", List.of());
    }

    public record CategoryWithCount(Long id, String name, Long count) {}
}

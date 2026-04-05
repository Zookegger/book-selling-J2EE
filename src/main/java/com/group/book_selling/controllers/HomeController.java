package com.group.book_selling.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.group.book_selling.models.Book;
import com.group.book_selling.models.Category;
import com.group.book_selling.services.BookService;

import lombok.RequiredArgsConstructor;

/**
 *
 * @author Nguyen Duc Trung
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final BookService bookService;

    @GetMapping("/")
    public String homePage(Model model) {
        Map<Category, List<Book>> popularByCategory = bookService.findBestsellersByCategory();
        model.addAttribute("popularByCategory", popularByCategory);

        return "index";
    }

    @GetMapping("/about")
    public String aboutPage() {
        return "home/about";
    }

    @GetMapping("/contact")
    public String contactPage() {
        return "home/contact";
    }

    @GetMapping("/faq")
    public String faqPage() {
        return "home/faq";
    }
}

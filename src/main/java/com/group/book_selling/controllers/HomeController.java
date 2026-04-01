package com.group.book_selling.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

/**
 *
 * @author Nguyen Duc Trung
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    @GetMapping("/")
    public String homePage(Model model) {
        return "index";
    }
}

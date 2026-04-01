package com.group.book_selling.controllers;

import org.springframework.web.bind.annotation.GetMapping;

public class CartController {

    @GetMapping("/cart")
    public String showCart() {
        return "cart/cart";
    }

}

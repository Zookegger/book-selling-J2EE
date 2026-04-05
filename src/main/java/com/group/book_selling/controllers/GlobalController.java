package com.group.book_selling.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.group.book_selling.models.Cart;
import com.group.book_selling.utils.CartSessionUtils;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalController {

    @ModelAttribute("cartItemCount")
    public int getCartItemCount(HttpSession session) {
        Cart cart = CartSessionUtils.getOrCreate(session);
        return cart.getTotalQuantity();
    }

    @ModelAttribute("currentUser")
    public Object getCurrentUser(Authentication authentication) {
        return (authentication != null && authentication.isAuthenticated()) ? authentication.getPrincipal() : null;
    }
}

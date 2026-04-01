package com.group.book_selling.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group.book_selling.models.Cart;
import com.group.book_selling.services.CartService;
import com.group.book_selling.utils.CartSessionUtils;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/cart")
    public String showCart(Model model, HttpSession session) {
        Cart cart = CartSessionUtils.getOrCreate(session);
        model.addAttribute("cart", cart);
        model.addAttribute("physicalItems", cart.getPhysicalItems()); 
        model.addAttribute("digitalItems", cart.getDigitalItems()); 
        return "cart/cart";
    }

    @PostMapping("/add")
    public String addToCart(
        @RequestParam Long bookId,
        @RequestParam String sku,
        @RequestParam(defaultValue = "1") int qty,
        HttpSession session,
        RedirectAttributes redirectAttrs
    ) {
        Cart cart = CartSessionUtils.getOrCreate(session);
        try {
            cartService.addToCart(cart, bookId, sku, qty);
            CartSessionUtils.save(session, cart);
            redirectAttrs.addFlashAttribute("success", "Added to cart successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/books/" + bookId;
    }

    
}

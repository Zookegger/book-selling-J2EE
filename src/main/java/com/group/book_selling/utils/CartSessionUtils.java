package com.group.book_selling.utils;

import com.group.book_selling.models.Cart;

import jakarta.servlet.http.HttpSession;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CartSessionUtils {
    private static final String CART_KEY = "cart";

    public static Cart getOrCreate(HttpSession session) {
        Cart cart = (Cart) session.getAttribute(CART_KEY);
        if (cart == null) {
            cart = new Cart();
            session.setAttribute(CART_KEY, cart);
        }
        return cart;
    }

    public static void save(HttpSession session, Cart cart) {
        session.setAttribute(CART_KEY, cart);
    }

    public static void destroy(HttpSession session) {
        session.removeAttribute(CART_KEY);
    }
}

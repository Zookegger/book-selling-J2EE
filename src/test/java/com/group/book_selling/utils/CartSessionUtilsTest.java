package com.group.book_selling.utils;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import com.group.book_selling.models.Cart;

class CartSessionUtilsTest {

    @Test
    void getOrCreate_whenMissing_createsAndStoresCart() {
        MockHttpSession session = new MockHttpSession();

        Cart cart = CartSessionUtils.getOrCreate(session);

        assertSame(cart, session.getAttribute("cart"));
    }

    @Test
    void getOrCreate_whenExisting_returnsSameCart() {
        MockHttpSession session = new MockHttpSession();
        Cart existing = new Cart();
        session.setAttribute("cart", existing);

        Cart result = CartSessionUtils.getOrCreate(session);

        assertSame(existing, result);
    }

    @Test
    void save_storesProvidedCart() {
        MockHttpSession session = new MockHttpSession();
        Cart cart = new Cart();

        CartSessionUtils.save(session, cart);

        assertSame(cart, session.getAttribute("cart"));
    }

    @Test
    void destroy_removesCartFromSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("cart", new Cart());

        CartSessionUtils.destroy(session);

        assertNull(session.getAttribute("cart"));
    }
}

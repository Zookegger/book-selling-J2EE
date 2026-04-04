package com.group.book_selling.controllers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.group.book_selling.models.Cart;
import com.group.book_selling.services.CartService;

@ExtendWith(MockitoExtension.class)
public class CartControllerTest {

	private MockMvc mockMvc;

	@Mock
	private CartService cartService;

	@BeforeEach
	void setUp() {
		CartController controller = new CartController(cartService);
		this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	void showCart_returnsCartViewWithExpectedModel() throws Exception {
		MockHttpSession session = new MockHttpSession();

		mockMvc.perform(get("/cart/view").session(session))
				.andExpect(status().isOk())
				.andExpect(view().name("cart/cart"))
				.andExpect(model().attributeExists("cart"))
				.andExpect(model().attributeExists("physicalItems"))
				.andExpect(model().attributeExists("digitalItems"))
				.andExpect(model().attributeExists("subtotal"))
				.andExpect(model().attributeExists("tax"))
				.andExpect(model().attributeExists("total"));
	}

	@Test
	void addToCart_success_savesToSessionAndRedirects() throws Exception {
		MockHttpSession session = new MockHttpSession();

		mockMvc.perform(post("/cart/add")
				.session(session)
				.param("bookId", "1")
				.param("sku", "SKU-001")
				.param("qty", "2"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/cart/view"))
				.andExpect(flash().attribute("success", "Added to cart successfully!"));

		verify(cartService).addToCart(any(Cart.class), eq(1L), eq("SKU-001"), eq(2));
		assertNotNull(session.getAttribute("cart"));
	}

	@Test
	void addToCart_whenServiceThrows_setsErrorFlashAndRedirects() throws Exception {
		MockHttpSession session = new MockHttpSession();
		doThrow(new IllegalStateException("Out of stock"))
				.when(cartService)
				.addToCart(any(Cart.class), eq(1L), eq("SKU-001"), eq(1));

		mockMvc.perform(post("/cart/add")
				.session(session)
				.param("bookId", "1")
				.param("sku", "SKU-001")
				.param("qty", "1"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/cart/view"))
				.andExpect(flash().attribute("error", "Out of stock"));
	}

	@Test
	void updateCart_success_callsServiceAndRedirects() throws Exception {
		MockHttpSession session = new MockHttpSession();

		mockMvc.perform(post("/cart/update")
				.session(session)
				.param("bookId", "10")
				.param("sku", "SKU-PHYSICAL")
				.param("qty", "4"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/cart/view"));

		verify(cartService).updateQty(any(Cart.class), eq(10L), eq("SKU-PHYSICAL"), eq(4));
		assertNotNull(session.getAttribute("cart"));
	}

	@Test
	void removeFromCart_success_callsServiceAndRedirects() throws Exception {
		MockHttpSession session = new MockHttpSession();

		mockMvc.perform(post("/cart/remove")
				.session(session)
				.param("sku", "SKU-DIGITAL"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/cart/view"));

		verify(cartService).removeFromCart(any(Cart.class), eq("SKU-DIGITAL"));
		assertNotNull(session.getAttribute("cart"));
	}

	@Test
	void removeFromCart_whenServiceThrows_setsErrorFlashAndRedirects() throws Exception {
		MockHttpSession session = new MockHttpSession();
		doThrow(new IllegalArgumentException("Invalid sku"))
				.when(cartService)
				.removeFromCart(any(Cart.class), eq("BAD-SKU"));

		mockMvc.perform(post("/cart/remove")
				.session(session)
				.param("sku", "BAD-SKU"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/cart/view"))
				.andExpect(flash().attribute("error", "Invalid sku"));
	}
}

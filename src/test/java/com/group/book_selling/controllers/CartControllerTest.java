package com.group.book_selling.controllers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.group.book_selling.models.Book;
import com.group.book_selling.models.BookFormat;
import com.group.book_selling.models.BookFormatType;
import com.group.book_selling.models.Cart;
import com.group.book_selling.services.CartService;
import com.group.book_selling.services.CouponService;

@ExtendWith(MockitoExtension.class)
public class CartControllerTest {

	private MockMvc mockMvc;

	@Mock
	private CartService cartService;
	@Mock
	private CouponService couponService;

	@BeforeEach
	void setUp() {
		CartController controller = new CartController(cartService, couponService);
		this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		lenient().when(couponService.resolveAppliedCoupon(any())).thenReturn(null);
		lenient().when(couponService.calculateDiscount(any(), any())).thenReturn(BigDecimal.ZERO);
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
				.andExpect(flash().attribute("success", "Thêm vào giỏ hàng thành công."));

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
	void updateCart_success_returnsJsonAndCallsService() throws Exception {
		MockHttpSession session = new MockHttpSession();
		Cart cart = new Cart();
		Book book = new Book();
		book.setId(10L);
		book.setTitle("Sample Book");
		book.setSlug("sample-book");

		BookFormat format = new BookFormat();
		format.setSku("SKU-PHYSICAL");
		format.setFormatType(BookFormatType.PHYSICAL);
		format.setActive(true);
		format.setPrice(BigDecimal.valueOf(100000));
		format.setCurrency("VND");
		format.setStockQuantity(20);
		cart.add(book, format, 1);

		doAnswer(invocation -> {
			Cart target = invocation.getArgument(0);
			target.getItems().stream()
					.filter(item -> "SKU-PHYSICAL".equals(item.getSku()))
					.findFirst()
					.ifPresent(item -> item.setQty(4));
			return null;
		}).when(cartService).updateQty(any(Cart.class), eq(10L), eq("SKU-PHYSICAL"), eq(4));
		session.setAttribute("cart", cart);

		mockMvc.perform(post("/cart/update")
				.session(session)
				.param("bookId", "10")
				.param("sku", "SKU-PHYSICAL")
				.param("qty", "4"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.qty").value(4))
				.andExpect(jsonPath("$.subtotal").exists())
				.andExpect(jsonPath("$.tax").exists())
				.andExpect(jsonPath("$.total").exists());

		verify(cartService).updateQty(any(Cart.class), eq(10L), eq("SKU-PHYSICAL"), eq(4));
		assertNotNull(session.getAttribute("cart"));
	}

	@Test
	void updateCart_whenServiceThrows_returnsBadRequestJson() throws Exception {
		MockHttpSession session = new MockHttpSession();
		doThrow(new IllegalArgumentException("Invalid quantity"))
				.when(cartService)
				.updateQty(any(Cart.class), eq(10L), eq("SKU-PHYSICAL"), eq(0));

		mockMvc.perform(post("/cart/update")
				.session(session)
				.param("bookId", "10")
				.param("sku", "SKU-PHYSICAL")
				.param("qty", "0"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Invalid quantity"));
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

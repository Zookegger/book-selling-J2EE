package com.group.book_selling.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.group.book_selling.models.Book;
import com.group.book_selling.models.BookFormat;
import com.group.book_selling.models.BookFormatType;
import com.group.book_selling.models.Cart;
import com.group.book_selling.repository.IBookRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private Cart cart;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(bookRepository);
    }

    @Test
    void addToCart_withValidSku_callsCartAdd() {
        BookFormat physical = physicalFormat("SKU-PHYSICAL", new BigDecimal("120.00"), new BigDecimal("99.00"), 10);
        BookFormat digital = digitalFormat("SKU-DIGITAL", new BigDecimal("40.00"));
        Book book = bookWithFormats(1L, physical, digital);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        cartService.addToCart(cart, 1L, "SKU-DIGITAL", 2);

        verify(cart).add(eq(book), eq(digital), eq(2));
    }

    @Test
    void addToCart_whenBookMissing_throwsEntityNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cartService.addToCart(cart, 99L, "SKU", 1));
    }

    @Test
    void addToCart_withSkuNotInBook_throwsIllegalArgumentException() {
        BookFormat physical = physicalFormat("SKU-PHYSICAL", new BigDecimal("120.00"), null, 10);
        Book book = bookWithFormats(1L, physical);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThrows(IllegalArgumentException.class, () -> cartService.addToCart(cart, 1L, "WRONG-SKU", 1));
    }

    @Test
    void updateQty_withValidSku_callsCartUpdateQty() {
        BookFormat physical = physicalFormat("SKU-PHYSICAL", new BigDecimal("120.00"), null, 10);
        Book book = bookWithFormats(1L, physical);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        cartService.updateQty(cart, 1L, "SKU-PHYSICAL", 7);

        verify(cart).updateQty(eq("SKU-PHYSICAL"), eq(7), eq(physical));
    }

    @Test
    void updateQty_whenBookMissing_throwsEntityNotFoundException() {
        when(bookRepository.findById(55L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cartService.updateQty(cart, 55L, "SKU", 2));
    }

    @Test
    void updateQty_withSkuNotInBook_throwsIllegalArgumentException() {
        BookFormat physical = physicalFormat("SKU-PHYSICAL", new BigDecimal("120.00"), null, 10);
        Book book = bookWithFormats(1L, physical);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThrows(IllegalArgumentException.class, () -> cartService.updateQty(cart, 1L, "OTHER-SKU", 2));
    }

    @Test
    void removeFromCart_callsCartRemove() {
        cartService.removeFromCart(cart, "SKU-REMOVE");

        verify(cart).remove("SKU-REMOVE");
    }

    @Test
    void clearCart_callsCartClear() {
        cartService.clearCart(cart);

        verify(cart).clear();
    }

    private Book bookWithFormats(Long id, BookFormat... formats) {
        return Book.builder()
                .id(id)
                .title("Test Book")
                .slug("test-book")
                .description("Book for tests")
                .publicationDate(LocalDate.of(2024, 1, 1))
                .language("vi")
                .formats(List.of(formats))
                .build();
    }

    private BookFormat physicalFormat(String sku, BigDecimal price, BigDecimal discounted, int stock) {
        return BookFormat.builder()
                .formatType(BookFormatType.PHYSICAL)
                .sku(sku)
                .price(price)
                .discountedPrice(discounted)
                .currency("VND")
                .stockQuantity(stock)
                .active(true)
                .build();
    }

    private BookFormat digitalFormat(String sku, BigDecimal price) {
        return BookFormat.builder()
                .formatType(BookFormatType.DIGITAL)
                .sku(sku)
                .price(price)
                .currency("VND")
                .active(true)
                .build();
    }
}

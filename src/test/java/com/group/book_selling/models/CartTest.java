package com.group.book_selling.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class CartTest {

    @Test
    void add_withInactiveFormat_throwsIllegalArgumentException() {
        Cart cart = new Cart();
        Book book = book(1L, "Clean Code");
        BookFormat format = physicalFormat("SKU-P", new BigDecimal("100.00"), null, 10, false, "VND");

        assertThrows(IllegalArgumentException.class, () -> cart.add(book, format, 1));
    }

    @Test
    void add_physicalWhenOutOfStock_throwsIllegalStateException() {
        Cart cart = new Cart();
        Book book = book(1L, "Clean Code");
        BookFormat format = physicalFormat("SKU-P", new BigDecimal("100.00"), null, 0, true, "VND");

        assertThrows(IllegalStateException.class, () -> cart.add(book, format, 1));
    }

    @Test
    void add_physicalUsesDiscountedPrice_whenPresent() {
        Cart cart = new Cart();
        Book book = book(1L, "Effective Java");
        BookFormat format = physicalFormat("SKU-P", new BigDecimal("100.00"), new BigDecimal("80.00"), 10, true, "VND");

        cart.add(book, format, 2);

        CartItem item = findBySku(cart, "SKU-P");
        assertEquals(0, item.getUnitPrice().compareTo(new BigDecimal("80.00")));
        assertEquals(2, item.getQty());
    }

    @Test
    void add_digitalIsIdempotent_keepsSingleQuantity() {
        Cart cart = new Cart();
        Book book = book(2L, "Refactoring");
        BookFormat format = digitalFormat("SKU-D", new BigDecimal("40.00"), "VND");

        cart.add(book, format, 1);
        cart.add(book, format, 5);

        CartItem item = findBySku(cart, "SKU-D");
        assertEquals(1, cart.size());
        assertEquals(1, item.getQty());
    }

    @Test
    void add_physicalAccumulatesAndCapsByStock() {
        Cart cart = new Cart();
        Book book = book(3L, "Domain-Driven Design");
        BookFormat format = physicalFormat("SKU-P", new BigDecimal("150.00"), null, 3, true, "VND");

        cart.add(book, format, 2);
        cart.add(book, format, 5);

        CartItem item = findBySku(cart, "SKU-P");
        assertEquals(3, item.getQty());
    }

    @Test
    void updateQty_physicalCapsByStock() {
        Cart cart = new Cart();
        Book book = book(4L, "Patterns of Enterprise Application Architecture");
        BookFormat format = physicalFormat("SKU-P", new BigDecimal("200.00"), null, 4, true, "VND");

        cart.add(book, format, 1);
        cart.updateQty("SKU-P", 99, format);

        CartItem item = findBySku(cart, "SKU-P");
        assertEquals(4, item.getQty());
    }

    @Test
    void updateQty_digitalDoesNotChangeQuantity() {
        Cart cart = new Cart();
        Book book = book(5L, "The Pragmatic Programmer");
        BookFormat format = digitalFormat("SKU-D", new BigDecimal("50.00"), "VND");

        cart.add(book, format, 1);
        cart.updateQty("SKU-D", 9, format);

        CartItem item = findBySku(cart, "SKU-D");
        assertEquals(1, item.getQty());
    }

    @Test
    void totals_andFiltersByCurrency_areCorrect() {
        Cart cart = new Cart();
        Book vndBook = book(6L, "VND Book");
        Book usdBook = book(7L, "USD Book");

        cart.add(vndBook, physicalFormat("SKU-VND", new BigDecimal("100.00"), null, 10, true, "VND"), 2);
        cart.add(usdBook, digitalFormat("SKU-USD", new BigDecimal("40.00"), "USD"), 1);

        assertEquals(3, cart.getTotalQuantity());
        assertEquals(0, cart.getTotalPrice("VND").compareTo(new BigDecimal("200.00")));
        assertEquals(0, cart.getTotalPrice("USD").compareTo(new BigDecimal("40.00")));
        assertEquals(0, cart.getTotalTax("VND").compareTo(new BigDecimal("10.00")));
        assertEquals(0, cart.getGrandTotalPrice("VND").compareTo(new BigDecimal("210.00")));
        assertEquals(0, cart.getTotalTax("USD").compareTo(new BigDecimal("2.00")));
        assertEquals(0, cart.getGrandTotalPrice("USD").compareTo(new BigDecimal("42.00")));
    }

    @Test
    void vatExemptBook_hasZeroTax() {
        Cart cart = new Cart();
        Book exemptBook = book(10L, "Giáo trình Toán Cao Cấp", category("Giáo trình"));

        cart.add(exemptBook, physicalFormat("SKU-EXEMPT", new BigDecimal("100.00"), null, 10, true, "VND"), 2);

        assertEquals(0, cart.getTotalTax("VND").compareTo(BigDecimal.ZERO));
        assertEquals(0, cart.getGrandTotalPrice("VND").compareTo(new BigDecimal("200.00")));
    }

    @Test
    void getPhysicalAndDigitalItems_splitCorrectly() {
        Cart cart = new Cart();
        Book book = book(8L, "Mixed Formats");

        cart.add(book, physicalFormat("SKU-P", new BigDecimal("90.00"), null, 5, true, "VND"), 1);
        cart.add(book, digitalFormat("SKU-D", new BigDecimal("35.00"), "VND"), 1);
        cart.add(book, audiobookFormat("SKU-A", new BigDecimal("55.00"), "VND"), 1);

        assertEquals(1, cart.getPhysicalItems().size());
        assertEquals(2, cart.getDigitalItems().size());
    }

    @Test
    void containsRemoveAndClear_workAsExpected() {
        Cart cart = new Cart();
        Book book = book(9L, "Testing Book");
        BookFormat format = physicalFormat("SKU-P", new BigDecimal("60.00"), null, 3, true, "VND");

        cart.add(book, format, 1);
        assertTrue(cart.contains("SKU-P"));
        assertFalse(cart.isEmpty());

        cart.remove("SKU-P");
        assertFalse(cart.contains("SKU-P"));
        assertTrue(cart.isEmpty());

        cart.add(book, format, 1);
        cart.clear();
        assertTrue(cart.isEmpty());
    }

    private CartItem findBySku(Cart cart, String sku) {
        return cart.getItems().stream()
                .filter(i -> sku.equals(i.getSku()))
                .findFirst()
                .orElseThrow();
    }

    private Book book(Long id, String title) {
        return Book.builder()
                .id(id)
                .title(title)
                .slug(title.toLowerCase().replace(" ", "-"))
                .description("Test description")
                .publicationDate(LocalDate.of(2024, 1, 1))
                .language("vi")
                .formats(List.of())
                .build();
    }

    private Book book(Long id, String title, Category category) {
        return Book.builder()
                .id(id)
                .title(title)
                .slug(title.toLowerCase().replace(" ", "-"))
                .description("Test description")
                .publicationDate(LocalDate.of(2024, 1, 1))
                .language("vi")
                .categories(List.of(category))
                .formats(List.of())
                .build();
    }

    private Category category(String name) {
        return Category.builder()
                .id(1L)
                .name(name)
                .slug(name.toLowerCase().replace(" ", "-"))
                .build();
    }

    private BookFormat physicalFormat(String sku, BigDecimal price, BigDecimal discounted, int stock, boolean active, String currency) {
        return BookFormat.builder()
                .formatType(BookFormatType.PHYSICAL)
                .sku(sku)
                .price(price)
                .discountedPrice(discounted)
                .currency(currency)
                .stockQuantity(stock)
                .active(active)
                .build();
    }

    private BookFormat digitalFormat(String sku, BigDecimal price, String currency) {
        return BookFormat.builder()
                .formatType(BookFormatType.DIGITAL)
                .sku(sku)
                .price(price)
                .currency(currency)
                .active(true)
                .build();
    }

    private BookFormat audiobookFormat(String sku, BigDecimal price, String currency) {
        return BookFormat.builder()
                .formatType(BookFormatType.AUDIOBOOK)
                .sku(sku)
                .price(price)
                .currency(currency)
                .active(true)
                .build();
    }
}

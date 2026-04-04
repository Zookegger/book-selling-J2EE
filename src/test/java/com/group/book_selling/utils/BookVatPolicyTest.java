package com.group.book_selling.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.group.book_selling.models.Book;
import com.group.book_selling.models.Category;

class BookVatPolicyTest {

    @Test
    void resolveVatRate_withExemptCategory_returnsZero() {
        Book book = book("Sách bất kỳ", List.of(category("Giáo trình")));

        assertEquals(0, BookVatPolicy.resolveVatRate(book).compareTo(BookVatPolicy.VAT_EXEMPT_RATE));
    }

    @Test
    void resolveVatRate_withExemptTitleKeyword_returnsZero() {
        Book book = book("Sách chính trị và quản trị công", List.of());

        assertEquals(0, BookVatPolicy.resolveVatRate(book).compareTo(BookVatPolicy.VAT_EXEMPT_RATE));
    }

    @Test
    void resolveVatRate_withRegularNovel_returnsFivePercent() {
        Book book = book("Tiểu thuyết trinh thám", List.of(category("Văn học")));

        assertEquals(0, BookVatPolicy.resolveVatRate(book).compareTo(BookVatPolicy.VAT_STANDARD_BOOK_RATE));
    }

    @Test
    void resolveVatRate_withNullBook_returnsFivePercent() {
        assertEquals(0, BookVatPolicy.resolveVatRate(null).compareTo(BookVatPolicy.VAT_STANDARD_BOOK_RATE));
    }

    private Book book(String title, List<Category> categories) {
        return Book.builder()
                .id(1L)
                .title(title)
                .slug("test-book")
                .description("Test")
                .publicationDate(LocalDate.of(2024, 1, 1))
                .language("vi")
                .categories(categories)
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
}

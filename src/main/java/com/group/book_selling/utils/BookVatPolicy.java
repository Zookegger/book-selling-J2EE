package com.group.book_selling.utils;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

import com.group.book_selling.models.Book;
import com.group.book_selling.models.Category;

public final class BookVatPolicy {

    public static final BigDecimal VAT_EXEMPT_RATE = BigDecimal.ZERO;
    public static final BigDecimal VAT_STANDARD_BOOK_RATE = new BigDecimal("0.05");

    private static final List<String> EXEMPT_KEYWORDS = List.of(
            "chinh tri",
            "giao khoa",
            "giao trinh",
            "giao duc",
            "van ban phap luat",
            "phap luat",
            "khoa hoc",
            "ky thuat",
            "khoa hoc ky thuat",
            "dan toc thieu so",
            "nha nuoc");

    private BookVatPolicy() {
    }

    public static BigDecimal resolveVatRate(Book book) {
        if (book == null) {
            return VAT_STANDARD_BOOK_RATE;
        }

        if (isExemptByCategory(book.getCategories()) || containsExemptKeyword(book.getTitle())) {
            return VAT_EXEMPT_RATE;
        }

        return VAT_STANDARD_BOOK_RATE;
    }

    private static boolean isExemptByCategory(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return false;
        }

        return categories.stream()
                .map(Category::getName)
                .anyMatch(BookVatPolicy::containsExemptKeyword);
    }

    private static boolean containsExemptKeyword(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String normalized = normalize(text);
        return EXEMPT_KEYWORDS.stream().anyMatch(normalized::contains);
    }

    private static String normalize(String input) {
        String nfd = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return nfd.replace('đ', 'd').trim();
    }
}

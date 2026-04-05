package com.group.book_selling.services;

import org.springframework.stereotype.Service;

import com.group.book_selling.models.Book;
import com.group.book_selling.models.BookFormat;
import com.group.book_selling.models.Cart;
import com.group.book_selling.repositories.IBookRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    private final IBookRepository bookRepository;

    public void addToCart(Cart cart, Long bookId, String sku, int qty) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sách."));

        BookFormat format = resolveFormat(book, sku);
        cart.add(book, format, qty);
    }

    public void removeFromCart(Cart cart, String sku) {
        cart.remove(sku);
    }

    public void updateQty(Cart cart, Long bookId, String sku, int newQty) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sách."));

        BookFormat format = resolveFormat(book, sku);
        cart.updateQty(sku, newQty, format);
    }

    public void clearCart(Cart cart) {
        cart.clear();
    }

    private BookFormat resolveFormat(Book book, String sku) {
        return book.getFormats().stream()
                .filter(f -> f.getSku().equals(sku))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy định dạng sách với SKU: " + sku));
    }
}
package com.group.book_selling.validators;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import com.group.book_selling.models.Book;
import com.group.book_selling.repository.IBookRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class UniqueIsbnValidator implements ConstraintValidator<UniqueIsbn, String> {

    @Autowired
    private IBookRepository bookRepository;

    @Override
    public boolean isValid(String isbn, ConstraintValidatorContext context) {
        if (isbn == null || isbn.isBlank()) {
            return true;
        }

        if (bookRepository == null) {
            return true;
        }

        Optional<Book> existingBook = bookRepository.findByIsbn(isbn);
        if (existingBook.isEmpty()) {
            return true;
        }

        Long currentBookId = extractPathBookId();
        if (currentBookId == null) {
            return false;
        }

        Long foundId = existingBook.get().getId();
        return foundId != null && foundId.equals(currentBookId);
    }

    private Long extractPathBookId() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            return null;
        }

        Object value = attrs.getRequest().getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(value instanceof Map<?, ?> pathVariables)) {
            return null;
        }

        Object idRaw = pathVariables.get("id");
        if (idRaw == null) {
            return null;
        }

        try {
            return Long.valueOf(idRaw.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

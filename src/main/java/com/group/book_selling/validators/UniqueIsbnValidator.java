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

        // Tìm xem ISBN này đã có ai dùng chưa
        Optional<Book> existingBook = bookRepository.findByIsbn(isbn);
        
        // Nếu chưa ai dùng -> Hợp lệ luôn
        if (existingBook.isEmpty()) {
            return true;
        }

        // Nếu ĐÃ CÓ người dùng, ta phải kiểm tra xem đó có phải là CHÍNH NÓ không
        String currentSlug = extractPathSlug();

        // Nếu không có slug trên URL (nghĩa là đang là trang Create mới) 
        // mà ISBN lại tồn tại -> Chắc chắn là trùng của người khác -> false
        if (currentSlug == null) {
            return false;
        }

        // Nếu đang Edit: ISBN trùng nhưng thuộc về cuốn sách có Slug này -> Hợp lệ
        return existingBook.get().getSlug().equals(currentSlug);
    }

    private String extractPathSlug() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;

        Map<String, String> pathVariables = (Map<String, String>) attrs.getRequest()
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        
        if (pathVariables == null) return null;

        // Lấy "slug" vì URL của bạn là /books/{slug}/edit
        return pathVariables.get("slug");
    }
}

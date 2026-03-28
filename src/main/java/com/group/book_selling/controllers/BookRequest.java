package com.group.book_selling.controllers;

import java.time.LocalDate;
import java.util.List;

import com.group.book_selling.models.BookFormat;
import com.group.book_selling.validators.UniqueIsbn;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO cho API tao/cap nhat sach.
 *
 * <p>Danh sach tac gia va danh muc duoc truyen bang id de tranh tao du lieu long nhau.</p>
 */
public record BookRequest(
        @NotBlank String title,
        String subtitle,
        @NotBlank String description,
        @UniqueIsbn String isbn,
        @NotNull LocalDate publicationDate,
        String language,
        Integer pageCount,
        Long publisherId,
        List<Long> authorIds,
        List<Long> categoryIds,
        String coverImage,
        List<@Valid BookFormat> formats) {
}

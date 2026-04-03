package com.group.book_selling.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.group.book_selling.models.BookFormat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO cho API tao/cap nhat sach.
 *
 * <p>Danh sach tac gia va danh muc duoc truyen bang id de tranh tao du lieu long nhau.</p>
 */
public record BookRequest(
        @NotBlank(message = "Tiêu đề không được để trống") 
        String title,
        
        String subtitle,
        
        @NotBlank(message = "Mô tả không được để trống") 
        String description,
        
        String isbn,
        
        @NotNull(message = "Ngày xuất bản không được để trống")
        @DateTimeFormat(pattern = "yyyy-MM-dd") 
        LocalDate publicationDate,
        
        String language,
        
        Integer pageCount,
        
        Long publisherId,
        
        List<Long> authorIds,
        
        List<Long> categoryIds,
        
        String coverImage,
        
        List<@Valid BookFormat> formats) {
}

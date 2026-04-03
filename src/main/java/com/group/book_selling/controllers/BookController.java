package com.group.book_selling.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group.book_selling.dto.Suggestion;
import com.group.book_selling.models.Author;
import com.group.book_selling.models.Book;
import com.group.book_selling.models.Category;
import com.group.book_selling.services.BookService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller MVC cho quản lý sách.
 *
 * <p>
 * Controller này hiển thị trang sách và xử lý form tạo/cập nhật/xóa.
 * </p>
 */
@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        List<Book> books = bookService.findBooks(keyword);
        model.addAttribute("books", books);
        model.addAttribute("keyword", keyword);
        return "books/search";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword, Model model) {
        return list(keyword, model);
    }

    @GetMapping("/autocomplete")
    @ResponseBody
    public List<Suggestion> autocomplete(@RequestParam String keyword) {
        return bookService.autocomplete(keyword);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("bookRequest")) {
            model.addAttribute("bookRequest", emptyBookRequest());
        }
        addReferenceData(model);

        model.addAttribute("book", null);
        model.addAttribute("pageTitle", "Thêm sách mới");

        return "books/form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("bookRequest") BookRequest request,
            BindingResult result,
            @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            addReferenceData(model);
            model.addAttribute("book", null);
            model.addAttribute("pageTitle", "Thêm sách mới");
            return "books/form";
        }

        try {
            String coverImagePath = null;

            // XỬ LÝ LƯU FILE
            if (coverFile != null && !coverFile.isEmpty()) {
                String originalFileName = StringUtils.cleanPath(coverFile.getOriginalFilename());
                String fileName = System.currentTimeMillis() + "_" + originalFileName;
                String uploadDir = "uploads/";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                try (InputStream inputStream = coverFile.getInputStream()) {
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                coverImagePath = "/uploads/" + fileName;
            }

            Book book = bookService.createBook(request, coverImagePath);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo sách thành công.");
            return "redirect:/books/" + book.getSlug();

        } catch (IOException e) {
            model.addAttribute("errorMessage", "Lỗi lưu file: " + e.getMessage());
            addReferenceData(model);
            model.addAttribute("book", null);
            model.addAttribute("pageTitle", "Thêm sách mới");
            return "books/form";
        }
    }

    @GetMapping("/{slug}")
    public String detail(@PathVariable String slug, Model model) {
        Book book = bookService.findBySlug(slug);
        model.addAttribute("book", book);
        return "books/detail";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{slug}/edit")
    public String showEditForm(@PathVariable String slug, Model model) {
        Book book = bookService.findBySlug(slug);

        // Nếu chưa có bookRequest trong model (lần đầu vào trang edit)
        if (!model.containsAttribute("bookRequest")) {
            model.addAttribute("bookRequest", createRequest(book));
        }

        addReferenceData(model);
        model.addAttribute("book", book);
        model.addAttribute("pageTitle", "Chỉnh sửa sách: " + book.getTitle());

        return "books/form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{slug}/edit")
    public String update(@PathVariable String slug,
            @Valid @ModelAttribute("bookRequest") BookRequest request,
            BindingResult result,
            @RequestParam(value = "coverFile", required = false) MultipartFile coverFile, // PHẢI thêm tham số này
            Model model,
            RedirectAttributes redirectAttributes) {

        Book existing = bookService.findBySlug(slug);

        if (result.hasErrors()) {
            addReferenceData(model);
            model.addAttribute("book", existing);
            model.addAttribute("pageTitle", "Chỉnh sửa sách");
            return "books/form";
        }

        try {
            String coverImagePath = null;

            // 2. Xử lý nếu người dùng có chọn ảnh mới
            if (coverFile != null && !coverFile.isEmpty()) {
                String originalFileName = StringUtils.cleanPath(coverFile.getOriginalFilename());
                String fileName = System.currentTimeMillis() + "_" + originalFileName;
                String uploadDir = "uploads/";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                try (InputStream inputStream = coverFile.getInputStream()) {
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }

                coverImagePath = "/uploads/" + fileName;
            }

            // 3. Lưu vào Database
            Book updated = bookService.updateBook(slug, request, coverImagePath);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sách thành công.");
            return "redirect:/books/" + updated.getSlug();

        } catch (IOException e) {
            model.addAttribute("errorMessage", "Lỗi khi cập nhật ảnh: " + e.getMessage());
            addReferenceData(model);
            model.addAttribute("book", existing);
            return "books/form";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{slug}/delete")
    public String delete(@PathVariable String slug, RedirectAttributes redirectAttributes) {
        bookService.deleteBySlug(slug);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa sách thành công.");
        return "redirect:/books";
    }

    private BookRequest emptyBookRequest() {
        return new BookRequest(
                "", // title
                "", // subtitle
                "", // description
                "", // isbn
                null, // publicationDate
                "en", // language
                null, // pageCount
                null, // publisherId
                new ArrayList<>(), // authorIds (Sửa null thành ArrayList rỗng)
                new ArrayList<>(), // categoryIds (Sửa null thành ArrayList rỗng)
                null, // coverImage
                new ArrayList<>() // formats (Sửa null thành ArrayList rỗng)
        );
    }

    private BookRequest createRequest(Book book) {
        return new BookRequest(
                book.getTitle(),
                book.getSubtitle(),
                book.getDescription(),
                book.getIsbn(),
                book.getPublicationDate(),
                book.getLanguage(),
                book.getPageCount(),
                book.getPublisher() == null ? null : book.getPublisher().getId(),
                book.getAuthors() == null ? new ArrayList<>() : book.getAuthors().stream().map(Author::getId).toList(),
                book.getCategories() == null ? new ArrayList<>()
                        : book.getCategories().stream().map(Category::getId).toList(),
                book.getCoverImage(),
                book.getFormats());
    }

    private void addReferenceData(Model model) {
        model.addAttribute("publishers", bookService.findAllPublishers());
        model.addAttribute("authors", bookService.findAllAuthors());
        model.addAttribute("categories", bookService.findAllCategories());
    }
}

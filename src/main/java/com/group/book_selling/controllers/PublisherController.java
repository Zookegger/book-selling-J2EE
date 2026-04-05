package com.group.book_selling.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group.book_selling.models.Book;
import com.group.book_selling.models.Publisher;
import com.group.book_selling.services.BookService;
import com.group.book_selling.services.PublisherService;

import lombok.RequiredArgsConstructor;

/**
 * Controller cho nhà xuất bản.
 */
@Controller
@RequestMapping("/publishers")
@RequiredArgsConstructor
public class PublisherController {

    private final PublisherService publisherService;
    private final BookService bookService;

    /** Lấy danh sách nhà xuất bản. */
    @GetMapping
    public String list(Model model) {
        List<Publisher> allPublishers = publisherService.findAll();
        model.addAttribute("publishers", allPublishers); 
        model.addAttribute("publisher", null); // Báo cho HTML biết đây là trang danh sách

        return "publisher/list";
    }

    /** Lấy chi tiết nhà xuất bản và danh sách sách của nhà xuất bản đó theo slug */
    @GetMapping("/{slug}")
    public String getPublisherBySlug(
            @PathVariable String slug,
            @RequestParam(required = false) String keyword,
            Model model) {

        Publisher publisher = publisherService.findBySlug(slug);
        List<Book> books = bookService.findBooksByPublisherSlug(slug);
        List<Publisher> allPublishers = publisherService.findAll();

        model.addAttribute("publisher", publisher);
        model.addAttribute("books", books);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedPublisher", slug); 
        model.addAttribute("publishers", allPublishers); 

        return "publisher/list"; 
    }

    /** Trang tạo mới. */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("publisher", new Publisher());
        model.addAttribute("pageTitle", "Thêm Nhà xuất bản mới");
        return "publisher/form";
    }

    /** Trang chỉnh sửa. */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Publisher publisher = publisherService.findById(id);

        model.addAttribute("publisher", publisher);
        model.addAttribute("pageTitle", "Chỉnh sửa: " + publisher.getName());
        return "publisher/form";
    }

    /** Lưu dữ liệu (Dùng cho cả Create và Update). */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/save")
    public String save(@ModelAttribute Publisher publisher, RedirectAttributes ra) {
        if (publisher.getId() == null) {
            publisherService.create(publisher);
            ra.addFlashAttribute("successMessage", "Đã thêm nhà xuất bản thành công!");
        } else {
            publisherService.update(publisher.getId(), publisher);
            ra.addFlashAttribute("successMessage", "Đã cập nhật nhà xuất bản thành công!");
        }
        
        return "redirect:/publishers"; // Đã thêm chữ "s"
    }

    /** Xóa nhà xuất bản. */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        publisherService.delete(id);
        ra.addFlashAttribute("successMessage", "Đã xóa nhà xuất bản.");
        return "redirect:/publishers"; // Đã thêm chữ "s"
    }
}
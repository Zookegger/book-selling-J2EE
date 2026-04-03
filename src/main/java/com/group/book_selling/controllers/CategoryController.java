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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group.book_selling.models.Category;
import com.group.book_selling.services.CategoryService;

import lombok.RequiredArgsConstructor;

/**
 * Controller SSR cho quan tri danh muc sach.
 */
@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /** Trang danh sach. */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAllForAdminList());
        return "categories/list";
    }

    /** Trang tao moi. */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("parents", categoryService.findAll());
        model.addAttribute("pageTitle", "Thêm Thể loại mới");
        return "categories/form";
    }

    /** Trang chinh sua. */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id);
        List<Category> parents = categoryService.findAllExceptId(id);

        model.addAttribute("category", category);
        model.addAttribute("parents", parents);
        model.addAttribute("pageTitle", "Chỉnh sửa: " + category.getName());
        return "categories/form";
    }

    /** Luu du lieu. */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/save")
    public String save(@ModelAttribute Category category, RedirectAttributes ra) {
        categoryService.save(category);
        ra.addFlashAttribute("successMessage", "Đã lưu danh mục thành công!");
        return "redirect:/categories";
    }

    /** Xoa danh muc. */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteAdmin(@PathVariable Long id, RedirectAttributes ra) {
        categoryService.delete(id);
        ra.addFlashAttribute("successMessage", "Đã xóa danh mục.");
        return "redirect:/categories";
    }
}

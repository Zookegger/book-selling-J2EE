package com.group.book_selling.controllers;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group.book_selling.models.Category;
import com.group.book_selling.repository.ICategoryRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final ICategoryRepository categoryRepository;

    // 1. Trang danh sách
    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "orderIndex")));
        return "categories/list";
    }

    // 2. Trang tạo mới
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("parents", categoryRepository.findAll()); // Để chọn danh mục cha
        model.addAttribute("pageTitle", "Thêm Thể loại mới");
        return "categories/form";
    }

    // 3. Trang chỉnh sửa (Sửa lỗi bấm nút Edit không được)
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        
        model.addAttribute("category", category);
        // Lấy tất cả danh mục trừ chính nó để làm danh mục cha (tránh vòng lặp vô tận)
        List<Category> parents = categoryRepository.findAll().stream()
                .filter(c -> !c.getId().equals(id))
                .toList();
        
        model.addAttribute("parents", parents);
        model.addAttribute("pageTitle", "Chỉnh sửa: " + category.getName());
        return "categories/form";
    }

    // 4. Lưu dữ liệu
    @PostMapping("/save")
    public String save(@ModelAttribute Category category, RedirectAttributes ra) {
        // Slug sẽ tự tạo nhờ @PrePersist/@PreUpdate trong Model của bạn rồi
        categoryRepository.save(category);
        ra.addFlashAttribute("successMessage", "Đã lưu danh mục thành công!");
        return "redirect:/admin/categories";
    }

    // 5. Xóa
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        categoryRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "Đã xóa danh mục.");
        return "redirect:/admin/categories";
    }
}
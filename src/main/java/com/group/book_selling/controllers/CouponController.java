package com.group.book_selling.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.group.book_selling.models.Coupon;
import com.group.book_selling.models.CouponUsage;
import com.group.book_selling.services.CouponService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String list(Model model) {
        model.addAttribute("coupons", couponService.list());
        model.addAttribute("coupon", new Coupon());
        return "admin/coupon/list";
    }

    @ResponseBody
    @GetMapping("/valid")
    public ResponseEntity<List<Coupon>> listValidCoupons(Model model) {
        return ResponseEntity.ok(couponService.findValidCoupons());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("coupons", couponService.list());
        model.addAttribute("coupon", new Coupon());
        model.addAttribute("openCreateModal", true);
        return "admin/coupon/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Coupon request) {
        couponService.create(request);
        return "redirect:/coupons";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("coupons", couponService.list());
        model.addAttribute("coupon", couponService.findById(id));
        model.addAttribute("openEditModal", true);
        return "admin/coupon/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute Coupon request) {
        couponService.update(id, request);
        return "redirect:/coupons";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        couponService.delete(id);
        return "redirect:/coupons";
    }

    @GetMapping("/usages")
    public String showUsages(@RequestParam Long userId, Model model) {
        List<CouponUsage> usages = couponService.showUsages(userId);
        model.addAttribute("usages", usages);
        return "coupons/usages";
    }

    @GetMapping("/{couponId}/usages")
    public String showUsage(@PathVariable Long couponId, @RequestParam Long userId, Model model) {
        CouponUsage usage = couponService.showUsage(userId, couponId);
        model.addAttribute("usage", usage);
        return "coupons/usage";
    }

    @PostMapping("/apply")
    @ResponseBody
    public ResponseEntity<Coupon> apply(@RequestParam String code, @RequestParam Long userId) {
        return ResponseEntity.ok(couponService.applyCoupon(code, userId));
    }
}
package com.group.book_selling.controllers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.group.book_selling.models.Cart;
import com.group.book_selling.models.Coupon;
import com.group.book_selling.models.CouponType;
import com.group.book_selling.models.CouponUsage;
import com.group.book_selling.models.CustomUserDetail;
import com.group.book_selling.models.User;
import com.group.book_selling.repositories.IUserRepository;
import com.group.book_selling.services.CouponService;
import com.group.book_selling.utils.CartSessionUtils;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final IUserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/coupons")
    public String list(Model model) {
        model.addAttribute("coupons", couponService.list());
        model.addAttribute("coupon", new Coupon());
        return "admin/coupon/list";
    }

    @ResponseBody
    @GetMapping("/coupons/valid")
    public ResponseEntity<List<Coupon>> listValidCoupons(Model model) {
        return ResponseEntity.ok(couponService.findValidCoupons());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/coupons/create")
    public String createForm(Model model) {
        model.addAttribute("coupons", couponService.list());
        model.addAttribute("coupon", new Coupon());
        model.addAttribute("openCreateModal", true);
        return "admin/coupon/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/coupons/create")
    public String create(@Valid @ModelAttribute Coupon request) {
        couponService.create(request);
        return "redirect:/coupons";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/coupons/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("coupons", couponService.list());
        model.addAttribute("coupon", couponService.findById(id));
        model.addAttribute("openEditModal", true);
        return "admin/coupon/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/coupons/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute Coupon request) {
        couponService.update(id, request);
        return "redirect:/coupons";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/coupons/{id}/delete")
    public String delete(@PathVariable Long id) {
        couponService.delete(id);
        return "redirect:/coupons";
    }

    @GetMapping("/coupons/usages")
    public String showUsages(@RequestParam Long userId, Model model) {
        List<CouponUsage> usages = couponService.showUsages(userId);
        model.addAttribute("usages", usages);
        return "coupons/usages";
    }

    @GetMapping("/coupons/{couponId}/usages")
    public String showUsage(@PathVariable Long couponId, @RequestParam Long userId, Model model) {
        CouponUsage usage = couponService.showUsage(userId, couponId);
        model.addAttribute("usage", usage);
        return "coupons/usage";
    }

    @PostMapping("/coupons/apply")
    @ResponseBody
    public ResponseEntity<Coupon> apply(@RequestParam String code, @RequestParam Long userId) {
        return ResponseEntity.ok(couponService.applyCoupon(code, userId));
    }

    @PostMapping("/coupons/apply-coupon")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> applyForCart(
            @RequestParam String code,
            @AuthenticationPrincipal CustomUserDetail userDetail,
            HttpSession session) {

        if (userDetail == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Vui lòng đăng nhập để áp dụng mã giảm giá."));
        }

        User user = userRepository.findByEmail(userDetail.getEmail());
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy thông tin người dùng."));
        }

        try {
            Coupon applied = couponService.applyCoupon(code, user.getId());

            Cart cart = CartSessionUtils.getOrCreate(session);
            BigDecimal subtotal = cart.getTotalPrice("VND");
            BigDecimal tax = cart.getTotalTax("VND");
            BigDecimal discount = calculateDiscount(subtotal, applied);
            BigDecimal finalTotal = subtotal.add(tax).subtract(discount).max(BigDecimal.ZERO);

            session.setAttribute("appliedCouponCode", applied.getCode());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Áp dụng mã giảm giá thành công.",
                    "couponCode", applied.getCode(),
                    "subtotal", subtotal,
                    "tax", tax,
                    "discount", discount,
                    "total", finalTotal));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                    "success", false,
                    "message", ex.getReason() == null ? "Áp dụng mã giảm giá thất bại." : ex.getReason()));
        }
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal, Coupon coupon) {
        if (subtotal == null || coupon == null || coupon.getDiscountAmount() == null || coupon.getDiscountAmount() <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountValue = BigDecimal.valueOf(coupon.getDiscountAmount());
        if (coupon.getDiscountType() == CouponType.PERCENTAGE) {
            return subtotal.multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                    .min(subtotal);
        }

        return discountValue.max(BigDecimal.ZERO).min(subtotal);
    }
}
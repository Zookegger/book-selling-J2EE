package com.group.book_selling.controllers;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group.book_selling.models.Cart;
import com.group.book_selling.models.Coupon;
import com.group.book_selling.services.CartService;
import com.group.book_selling.services.CouponService;
import com.group.book_selling.utils.CartSessionUtils;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CouponService couponService;

    @GetMapping("/view")
    public String showCart(Model model, HttpSession session) {
        Cart cart = CartSessionUtils.getOrCreate(session);
        var subtotal = cart.getTotalPrice("VND");
        var tax = cart.getTotalTax("VND");
        Coupon appliedCoupon = couponService.resolveAppliedCoupon(session);
        BigDecimal discount = couponService.calculateDiscount(subtotal, appliedCoupon);
        var total = subtotal.add(tax).subtract(discount).max(BigDecimal.ZERO);

        model.addAttribute("cart", cart);
        model.addAttribute("physicalItems", cart.getPhysicalItems());
        model.addAttribute("digitalItems", cart.getDigitalItems());
        model.addAttribute("couponCode", appliedCoupon != null ? appliedCoupon.getCode() : "");
        model.addAttribute("discount", discount);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("tax", tax);
        model.addAttribute("total", total);
        return "cart/cart";
    }

    @PostMapping("/add")
    public String addToCart(
            @RequestParam Long bookId,
            @RequestParam String sku,
            @RequestParam(defaultValue = "1") int qty,
            HttpSession session,
            RedirectAttributes redirectAttrs) {
        Cart cart = CartSessionUtils.getOrCreate(session);
        try {
            cartService.addToCart(cart, bookId, sku, qty);
            CartSessionUtils.save(session, cart);
            redirectAttrs.addFlashAttribute("success", "Added to cart successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart/view";
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCart(
            @RequestParam Long bookId,
            @RequestParam String sku,
            @RequestParam int qty,
            HttpSession session) {
        Cart cart = CartSessionUtils.getOrCreate(session);
        try {
            cartService.updateQty(cart, bookId, sku, qty);
            CartSessionUtils.save(session, cart);

            var subtotal = cart.getTotalPrice("VND");
            var tax = cart.getTotalTax("VND");
            Coupon appliedCoupon = couponService.resolveAppliedCoupon(session);
            BigDecimal discount = couponService.calculateDiscount(subtotal, appliedCoupon);
            var total = subtotal.add(tax).subtract(discount).max(BigDecimal.ZERO);
            var updatedItem = cart.getItems().stream()
                    .filter(item -> item.getSku().equals(sku))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy sản phẩm trong giỏ hàng."));
            var lineTotal = updatedItem.getSubtotal();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "qty", updatedItem.getQty(),
                    "lineTotal", lineTotal,
                    "subtotal", subtotal,
                    "tax", tax,
                    "discount", discount,
                    "total", total));

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()));
        }
    }

    @PostMapping("/remove")
    public String removeFromCart(
            @RequestParam String sku,
            HttpSession session,
            RedirectAttributes redirectAttrs) {
        Cart cart = CartSessionUtils.getOrCreate(session);
        try {
            cartService.removeFromCart(cart, sku);
            CartSessionUtils.save(session, cart);
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart/view";
    }
}

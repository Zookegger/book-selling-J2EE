package com.group.book_selling.controllers;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group.book_selling.models.Order;
import com.group.book_selling.models.OrderStatus;
import com.group.book_selling.repositories.IOrderRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminOrderController {

    private final IOrderRepository orderRepository;

    @GetMapping("/admin/orders")
    public String orders(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "q", required = false) String keyword,
            Model model) {
        List<Order> allOrders = orderRepository.findAll().stream()
                .sorted(Comparator.comparing(this::resolveSortTime).reversed())
                .toList();

        OrderStatus selectedStatus = parseStatus(status);
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        String keywordLower = normalizedKeyword.toLowerCase(Locale.ROOT);

        List<Order> orders = allOrders.stream()
                .filter(order -> selectedStatus == null || order.getOrderStatus() == selectedStatus)
                .filter(order -> keywordLower.isBlank() || matchesKeyword(order, keywordLower))
                .collect(Collectors.toList());

        model.addAttribute("orders", orders);
        model.addAttribute("totalOrders", allOrders.size());
        model.addAttribute("pendingCount", countByStatus(allOrders, OrderStatus.PENDING_PAYMENT));
        model.addAttribute("processingCount", countByStatus(allOrders, OrderStatus.PROCESSING));
        model.addAttribute("completedCount", countByStatus(allOrders, OrderStatus.COMPLETED));
        model.addAttribute("cancelledCount", countByStatus(allOrders, OrderStatus.CANCELLED));
        model.addAttribute("selectedStatus", selectedStatus == null ? "ALL" : selectedStatus.name());
        model.addAttribute("keyword", normalizedKeyword);

        return "admin/orders/list";
    }

    @GetMapping("/admin/orders/{id}")
    public String orderDetail(
            @PathVariable("id") Long id,
            @RequestParam(name = "status", required = false) String filterStatus,
            @RequestParam(name = "q", required = false) String keyword,
            Model model,
            RedirectAttributes redirectAttributes) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
            appendFilterParams(redirectAttributes, filterStatus, keyword);
            return "redirect:/admin/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("availableStatuses", OrderStatus.values());
        model.addAttribute("filterStatus", filterStatus);
        model.addAttribute("keyword", keyword);
        return "admin/orders/detail";
    }

    @PostMapping("/admin/orders/{id}/status")
    public String updateOrderStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status,
            @RequestParam(name = "filterStatus", required = false) String filterStatus,
            @RequestParam(name = "q", required = false) String keyword,
            RedirectAttributes redirectAttributes) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
            appendFilterParams(redirectAttributes, filterStatus, keyword);
            return "redirect:/admin/orders";
        }

        OrderStatus newStatus = parseStatus(status);
        if (newStatus == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái đơn hàng không hợp lệ.");
            appendFilterParams(redirectAttributes, filterStatus, keyword);
            return "redirect:/admin/orders/" + id;
        }

        if (order.getOrderStatus() == newStatus) {
            redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã ở trạng thái " + readableStatus(newStatus) + ".");
        } else {
            order.setOrderStatus(newStatus);
            orderRepository.save(order);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái đơn hàng thành " + readableStatus(newStatus) + ".");
        }

        appendFilterParams(redirectAttributes, filterStatus, keyword);
        return "redirect:/admin/orders/" + id;
    }

    private long countByStatus(List<Order> orders, OrderStatus status) {
        return orders.stream().filter(order -> order.getOrderStatus() == status).count();
    }

    private OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            return null;
        }

        try {
            return OrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean matchesKeyword(Order order, String keywordLower) {
        String orderNumber = order.getOrderNumber() == null ? "" : order.getOrderNumber().toLowerCase(Locale.ROOT);
        String recipientName = order.getRecipientName() == null ? "" : order.getRecipientName().toLowerCase(Locale.ROOT);
        String email = order.getCustomerEmail() == null ? "" : order.getCustomerEmail().toLowerCase(Locale.ROOT);
        String phone = order.getCustomerPhone() == null ? "" : order.getCustomerPhone().toLowerCase(Locale.ROOT);
        return orderNumber.contains(keywordLower)
                || recipientName.contains(keywordLower)
                || email.contains(keywordLower)
                || phone.contains(keywordLower);
    }

    private LocalDateTime resolveSortTime(Order order) {
        if (order.getPlacedAt() != null) {
            return order.getPlacedAt();
        }
        if (order.getCreatedAt() != null) {
            return order.getCreatedAt();
        }
        return LocalDateTime.MIN;
    }

    private String readableStatus(OrderStatus status) {
        return switch (status) {
            case PENDING_PAYMENT -> "Chờ thanh toán";
            case PROCESSING -> "Đang xử lý";
            case COMPLETED -> "Hoàn tất";
            case CANCELLED -> "Đã hủy";
        };
    }

    private void appendFilterParams(RedirectAttributes redirectAttributes, String status, String keyword) {
        if (status != null && !status.isBlank()) {
            redirectAttributes.addAttribute("status", status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            redirectAttributes.addAttribute("q", keyword.trim());
        }
    }
}
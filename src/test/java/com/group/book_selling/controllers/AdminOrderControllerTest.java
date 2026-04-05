package com.group.book_selling.controllers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.group.book_selling.models.Order;
import com.group.book_selling.models.OrderStatus;
import com.group.book_selling.repositories.IOrderRepository;

class AdminOrderControllerTest {

    @Test
    void orders_withStatusFilter_returnsFilteredList() {
        IOrderRepository orderRepository = Mockito.mock(IOrderRepository.class);
        AdminOrderController controller = new AdminOrderController(orderRepository);

        Order completedOrder = sampleOrder(1L, "ORD-001", "alice@example.com", OrderStatus.COMPLETED);
        Order pendingOrder = sampleOrder(2L, "ORD-002", "bob@example.com", OrderStatus.PENDING_PAYMENT);

        when(orderRepository.findAll()).thenReturn(List.of(completedOrder, pendingOrder));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.orders("COMPLETED", "", model);

        @SuppressWarnings("unchecked")
        List<Order> filteredOrders = (List<Order>) model.getAttribute("orders");

        assertEquals("admin/orders/list", view);
        assertEquals(1, filteredOrders.size());
        assertEquals("ORD-001", filteredOrders.get(0).getOrderNumber());
        assertEquals("COMPLETED", model.getAttribute("selectedStatus"));
    }

    @Test
    void updateOrderStatus_withValidStatus_updatesAndRedirectsToDetail() {
        IOrderRepository orderRepository = Mockito.mock(IOrderRepository.class);
        AdminOrderController controller = new AdminOrderController(orderRepository);

        Order order = sampleOrder(10L, "ORD-010", "customer@example.com", OrderStatus.PENDING_PAYMENT);
        when(orderRepository.findById(10L)).thenReturn(java.util.Optional.of(order));

        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        String view = controller.updateOrderStatus(10L, "PROCESSING", "PENDING_PAYMENT", "ORD-010", redirect);

        assertEquals("redirect:/admin/orders/10", view);
        assertEquals(OrderStatus.PROCESSING, order.getOrderStatus());
        verify(orderRepository).save(order);
        assertTrue(redirect.getFlashAttributes().containsKey("successMessage"));
    }

    @Test
    void updateOrderStatus_withInvalidStatus_doesNotSaveAndReturnsError() {
        IOrderRepository orderRepository = Mockito.mock(IOrderRepository.class);
        AdminOrderController controller = new AdminOrderController(orderRepository);

        Order order = sampleOrder(99L, "ORD-099", "customer@example.com", OrderStatus.PENDING_PAYMENT);
        when(orderRepository.findById(99L)).thenReturn(java.util.Optional.of(order));

        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        String view = controller.updateOrderStatus(99L, "UNKNOWN_STATUS", "ALL", "", redirect);

        assertEquals("redirect:/admin/orders/99", view);
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getOrderStatus());
        verify(orderRepository, never()).save(order);
        assertTrue(redirect.getFlashAttributes().containsKey("errorMessage"));
    }

    private Order sampleOrder(Long id, String orderNumber, String email, OrderStatus status) {
        return Order.builder()
                .id(id)
                .orderNumber(orderNumber)
                .recipientName("Nguyen Van A")
                .customerEmail(email)
                .customerPhone("0900000000")
                .shippingStreetDetails("123 Street")
                .shippingWard("Ward")
                .shippingDistrict("District")
                .shippingProvinceOrCity("City")
                .subtotal(BigDecimal.valueOf(100_000))
                .tax(BigDecimal.valueOf(10_000))
                .discount(BigDecimal.ZERO)
                .grandTotal(BigDecimal.valueOf(110_000))
                .currency("VND")
                .orderStatus(status)
                .build();
    }
}
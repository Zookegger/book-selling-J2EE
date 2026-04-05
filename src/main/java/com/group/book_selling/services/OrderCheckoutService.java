package com.group.book_selling.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.group.book_selling.dto.CheckoutForm;
import com.group.book_selling.models.Cart;
import com.group.book_selling.models.Coupon;
import com.group.book_selling.models.Order;
import com.group.book_selling.models.OrderItem;
import com.group.book_selling.models.OrderStatus;
import com.group.book_selling.models.Payment;
import com.group.book_selling.models.PaymentStatus;
import com.group.book_selling.models.User;
import com.group.book_selling.repositories.IOrderRepository;
import com.group.book_selling.repositories.IUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderCheckoutService {

    private static final DateTimeFormatter ORDER_TS_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final IUserRepository userRepository;
    private final IOrderRepository orderRepository;
    private final CouponService couponService;
    private final LocationLookupService locationLookupService;

    @Transactional
    public Order placeOrderWithFakePayment(String userEmail, Cart cart, Coupon appliedCoupon,
            CheckoutForm checkoutForm) {
        if (cart == null || cart.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giỏ hàng đang trống.");
        }

        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng.");
        }

        if (checkoutForm.getSelectedAddressIndex() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng chọn địa chỉ giao hàng.");
        }

        List<User.Address> addresses = user.getAddresses();
        int addressIndex = checkoutForm.getSelectedAddressIndex();
        if (addresses == null || addressIndex < 0 || addressIndex >= addresses.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Địa chỉ giao hàng không hợp lệ.");
        }

        User.Address selectedAddress = addresses.get(addressIndex);
            String shippingStreetDetails = nullSafe(selectedAddress.getStreetDetails());
            String shippingWard = locationLookupService.resolveWardName(selectedAddress.getWard());
            String shippingDistrict = locationLookupService.resolveDistrictName(selectedAddress.getDistrict());
            String shippingProvinceOrCity = locationLookupService.resolveProvinceName(selectedAddress.getProvinceOrCity());

            if (isBlank(shippingStreetDetails)
                    || isBlank(shippingWard)
                    || isBlank(shippingDistrict)
                    || isBlank(shippingProvinceOrCity)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng chọn địa chỉ giao hàng.");
        }

        Coupon consumedCoupon = null;
        if (appliedCoupon != null && appliedCoupon.getCode() != null && !appliedCoupon.getCode().isBlank()) {
            consumedCoupon = couponService.consumeCoupon(appliedCoupon.getCode(), user.getId());
        }

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(item -> OrderItem.builder()
                        .bookId(item.getBookId())
                        .sku(item.getSku())
                        .title(item.getTitle())
                        .coverImage(item.getCoverImage())
                        .formatType(item.getFormatType())
                        .unitPrice(item.getUnitPrice())
                        .vatRate(item.getVatRate())
                        .quantity(item.getQty())
                        .currency(item.getCurrency())
                        .build())
                .toList();

        BigDecimal subtotal = orderItems.stream()
                .map(OrderItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = orderItems.stream()
                .map(OrderItem::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = couponService.calculateDiscount(subtotal, consumedCoupon);
        BigDecimal grandTotal = subtotal.add(tax).subtract(discount).max(BigDecimal.ZERO);

        String orderNumber = nextOrderNumber();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .customerEmail(nullSafe(user.getEmail()))
                .customerPhone(nullSafe(selectedAddress.getPhoneNumber()))
                .recipientName(nullSafe(selectedAddress.getRecipientName()))
                    .shippingStreetDetails(shippingStreetDetails)
                    .shippingWard(shippingWard)
                    .shippingDistrict(shippingDistrict)
                    .shippingProvinceOrCity(shippingProvinceOrCity)
                .items(orderItems)
                .couponCode(consumedCoupon != null ? consumedCoupon.getCode() : null)
                .subtotal(subtotal)
                .tax(tax)
                .discount(discount)
                .grandTotal(grandTotal)
                .currency(resolveCurrency(orderItems))
                .orderStatus(OrderStatus.COMPLETED)
                .placedAt(LocalDateTime.now())
                .build();

        Payment payment = Payment.builder()
                .paymentMethod(checkoutForm.getPaymentMethod())
                .paymentStatus(PaymentStatus.SUCCESS)
                .amount(grandTotal)
                .currency(order.getCurrency())
                .providerReference("SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT))
                .message("Simulated payment auto-approved")
                .paidAt(LocalDateTime.now())
                .build();

        order.attachPayment(payment);
        return orderRepository.save(order);
    }

    private String nextOrderNumber() {
        for (int i = 0; i < 5; i++) {
            String candidate = "ORD-" + LocalDateTime.now().format(ORDER_TS_FMT)
                    + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
            if (!orderRepository.existsByOrderNumber(candidate)) {
                return candidate;
            }
        }

        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Không thể tạo mã đơn hàng. Vui lòng thử lại.");
    }

    private String resolveCurrency(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(OrderItem::getCurrency)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse("VND");
    }

    private String nullSafe(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }
}

package com.group.book_selling.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group.book_selling.models.Order;
import com.group.book_selling.models.OrderStatus;

public interface IOrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    List<Order> findByOrderStatusAndPlacedAtBetween(OrderStatus orderStatus, LocalDateTime from, LocalDateTime to);

    boolean existsByOrderNumber(String orderNumber);
}

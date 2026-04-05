package com.group.book_selling.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group.book_selling.models.Payment;

public interface IPaymentRepository extends JpaRepository<Payment, Long> {
}

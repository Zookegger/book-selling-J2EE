package com.group.book_selling.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.group.book_selling.models.Coupon;

public interface ICouponRepository extends JpaRepository<Coupon, Long> {
    // Find by the unique string code (e.g., 'SUMMER2026')
    Optional<Coupon> findByCode(String code);

    // Check if a coupon exists and is currently within its valid date range
    @Query("SELECT c FROM Coupon c WHERE c.code = :code " +
            "AND CURRENT_TIMESTAMP BETWEEN c.validFrom AND c.expiresAt")
    List<Coupon> findValidCoupon(String code);

    @Query("SELECT c FROM Coupon c WHERE CURRENT_TIMESTAMP BETWEEN c.validFrom AND c.expiresAt")
    List<Coupon> listValidCoupon();
}

package com.group.book_selling.repositories;

import com.group.book_selling.models.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    // 1. Find all usage history for a specific user
    List<CouponUsage> findByUserIdOrderByUsedAtDesc(Long userId);

    // 2. Count how many times a specific user has used a specific coupon
    // Essential for "Limit 1 per customer" logic
    long countByCouponIdAndUserId(Long couponId, Long userId);

    // 3. Find usage by a specific coupon code and user ID
    @Query("SELECT cu FROM CouponUsage cu JOIN cu.coupon c WHERE c.code = :code AND cu.user.id = :userId")
    Optional<CouponUsage> findByCodeAndUserId(@Param("code") String code, @Param("userId") Long userId);

    // 4. Get a summary of how many times a coupon has been used total
    long countByCouponId(Long couponId);
}
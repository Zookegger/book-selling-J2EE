package com.group.book_selling.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.group.book_selling.models.CouponUsage;

@Repository
public interface ICouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    List<CouponUsage> findByUserId(Long userId);

    long countByCouponIdAndUserId(Long couponId, Long userId);

    CouponUsage findByCouponIdAndUserId(Long couponId, Long userId);

    @Query("SELECT cu FROM CouponUsage cu JOIN cu.coupon c WHERE c.code = :code AND cu.user.id = :userId")
    Optional<CouponUsage> findByCodeAndUserId(@Param("code") String code, @Param("userId") Long userId);

    long countByCouponId(Long couponId);
    
    long countByUserId(Long userId);
}
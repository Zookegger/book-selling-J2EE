package com.group.book_selling.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.group.book_selling.models.Coupon;
import com.group.book_selling.models.CouponUsage;
import com.group.book_selling.models.User;
import com.group.book_selling.repositories.ICouponRepository;
import com.group.book_selling.repositories.ICouponUsageRepository;
import com.group.book_selling.repositories.IUserRepository;

import lombok.RequiredArgsConstructor;

/**
 *
 * @author Nguyễn Đức Trung
 */
@Service
@RequiredArgsConstructor
public class CouponService {
    private final ICouponRepository couponRepository;
    private final ICouponUsageRepository couponUsageRepository;
    private final IUserRepository userRepository;

    public List<Coupon> list() {
        return couponRepository.findAll();
    }

    public Coupon findById(Long id) {
        return couponRepository.findById(id).orElse(null);
    }
    public Coupon findByCode(String code) {
        return couponRepository.findByCode(code).orElse(null);
    }

    public List<Coupon> findValidCoupons() {
        return couponRepository.listValidCoupon();
    }

    public Coupon applyCoupon(String code, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Không tìm thay người dùng với ID: " + userId));

        Coupon validCoupon = couponRepository.findValidCoupon(code).stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Mã giảm giá không hợp lệ hoặc đã hết hạn: " + code));

        boolean alreadyUsed = couponUsageRepository.countByCouponIdAndUserId(validCoupon.getId(), userId) > 0;
        if (alreadyUsed) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã giảm giá đã được sử dụng bởi người dùng này");
        }

        validCoupon.addUsage(user);

        return couponRepository.save(validCoupon);
    }

    public List<CouponUsage> showUsages(Long userId) {
        return couponUsageRepository.findByUserId(userId);
    }

    public CouponUsage showUsage(Long userId, Long couponId) {
        return couponUsageRepository.findByCouponIdAndUserId(couponId, userId);
    }

    public Coupon create(Coupon request) {
        return couponRepository.save(request);
    }

    public Coupon update(Long id, Coupon request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        coupon.setCode(request.getCode());
        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountAmount(request.getDiscountAmount());
        coupon.setValidFrom(request.getValidFrom());
        coupon.setExpiresAt(request.getExpiresAt());

        return couponRepository.save(coupon);
    }

    public void delete(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        couponRepository.deleteById(id);
    }
}

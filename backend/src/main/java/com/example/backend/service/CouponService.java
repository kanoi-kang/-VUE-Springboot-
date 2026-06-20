package com.example.backend.service;

import com.example.backend.common.BusinessException;
import com.example.backend.entity.Coupon;
import com.example.backend.entity.CouponUser;
import com.example.backend.repository.CouponRepository;
import com.example.backend.repository.CouponUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUserRepository couponUserRepository;

    public List<Coupon> getAvailableCoupons() {
        return couponRepository.findAvailableCoupons(LocalDateTime.now());
    }

    public List<Map<String, Object>> getUserCoupons(Long userId) {
        List<CouponUser> userCoupons = couponUserRepository.findByUserId(userId);
        return userCoupons.stream().map(cu -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", cu.getId());
            item.put("status", cu.getStatus());
            item.put("createTime", cu.getCreateTime());
            item.put("usedTime", cu.getUsedTime());

            couponRepository.findById(cu.getCouponId()).ifPresent(coupon -> {
                item.put("couponId", coupon.getId());
                item.put("name", coupon.getName());
                item.put("description", coupon.getDescription());
                item.put("couponType", coupon.getCouponType());
                item.put("discount", coupon.getDiscount());
                item.put("minAmount", coupon.getMinAmount());
                item.put("startTime", coupon.getStartTime());
                item.put("endTime", coupon.getEndTime());
            });
            return item;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> receiveCoupon(Long userId, Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException("优惠券不存在"));

        if (coupon.getRemainCount() <= 0) {
            throw new BusinessException("优惠券已领完");
        }

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartTime() != null && now.isBefore(coupon.getStartTime())) {
            throw new BusinessException("优惠券还未开始");
        }
        if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
            throw new BusinessException("优惠券已过期");
        }

        long userReceivedCount = couponUserRepository.countByUserIdAndCouponId(userId, couponId);
        if (userReceivedCount >= coupon.getPerLimit()) {
            throw new BusinessException("您已领取过该优惠券");
        }

        CouponUser couponUser = new CouponUser();
        couponUser.setUserId(userId);
        couponUser.setCouponId(couponId);
        couponUser.setStatus(0);
        couponUserRepository.save(couponUser);

        coupon.setRemainCount(coupon.getRemainCount() - 1);
        couponRepository.save(coupon);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "领取成功");
        return result;
    }

    @Transactional
    public boolean useCoupon(Long userId, Long couponId, Long orderId) {
        CouponUser couponUser = couponUserRepository.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new BusinessException("用户未持有该优惠券"));

        if (couponUser.getStatus() == 1) {
            throw new BusinessException("优惠券已使用");
        }

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException("优惠券不存在"));

        if (coupon.getEndTime() != null && LocalDateTime.now().isAfter(coupon.getEndTime())) {
            throw new BusinessException("优惠券已过期");
        }

        couponUser.setStatus(1);
        couponUser.setUsedTime(LocalDateTime.now());
        couponUser.setOrderId(orderId);
        couponUserRepository.save(couponUser);

        return true;
    }

    public Map<String, Object> calculateDiscount(Long couponId, BigDecimal orderAmount) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException("优惠券不存在"));

        if (coupon.getMinAmount() != null && orderAmount.compareTo(coupon.getMinAmount()) < 0) {
            throw new BusinessException("订单金额未达到优惠券使用门槛");
        }

        BigDecimal discount = coupon.getDiscount();

        if ("PERCENT".equals(coupon.getCouponType())) {
            discount = orderAmount.multiply(coupon.getDiscount()).divide(BigDecimal.valueOf(100));
            if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
                discount = coupon.getMaxDiscount();
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("discountAmount", discount);
        result.put("couponName", coupon.getName());
        return result;
    }
}
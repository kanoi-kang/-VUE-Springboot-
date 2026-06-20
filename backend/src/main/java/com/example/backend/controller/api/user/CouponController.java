package com.example.backend.controller.api.user;

import com.example.backend.common.Result;
import com.example.backend.entity.Coupon;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/coupons")
@Tag(name = "用户优惠券接口")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping("/available")
    @Operation(summary = "获取可领取优惠券")
    public Result<List<Coupon>> getAvailableCoupons() {
        return Result.success(couponService.getAvailableCoupons());
    }

    @GetMapping("/my")
    @Operation(summary = "获取我的优惠券")
    public Result<List<Map<String, Object>>> getUserCoupons(
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(couponService.getUserCoupons(principal.getUserId()));
    }

    @PostMapping("/receive/{couponId}")
    @Operation(summary = "领取优惠券")
    public Result<Map<String, Object>> receiveCoupon(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "优惠券ID") @PathVariable Long couponId) {
        Map<String, Object> result = couponService.receiveCoupon(principal.getUserId(), couponId);
        return Result.success(result);
    }

    @GetMapping("/calculate")
    @Operation(summary = "计算优惠券折扣")
    public Result<Map<String, Object>> calculateDiscount(
            @Parameter(description = "优惠券ID") @RequestParam Long couponId,
            @Parameter(description = "订单金额") @RequestParam BigDecimal orderAmount) {
        Map<String, Object> result = couponService.calculateDiscount(couponId, orderAmount);
        return Result.success(result);
    }
}
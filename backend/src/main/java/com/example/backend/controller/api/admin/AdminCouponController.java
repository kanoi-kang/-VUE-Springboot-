package com.example.backend.controller.api.admin;

import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.dto.CouponRequest;
import com.example.backend.entity.Coupon;
import com.example.backend.repository.CouponRepository;
import com.example.backend.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/coupons")
@Tag(name = "管理员优惠券接口")
public class AdminCouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @PostMapping
    @Operation(summary = "创建优惠券")
    public Result<Coupon> createCoupon(@Valid @RequestBody CouponRequest request) {
        Coupon coupon = new Coupon();
        coupon.setName(request.getName());
        coupon.setDescription(request.getDescription());
        coupon.setCouponType(request.getCouponType());
        coupon.setDiscount(request.getDiscount());
        coupon.setMinAmount(request.getMinAmount());
        coupon.setMaxDiscount(request.getMaxDiscount());
        coupon.setTotalCount(request.getTotalCount());
        coupon.setRemainCount(request.getTotalCount());
        coupon.setPerLimit(request.getPerLimit());
        coupon.setStartTime(request.getStartTime());
        coupon.setEndTime(request.getEndTime());
        coupon.setCategoryIds(request.getCategoryIds());
        coupon.setProductIds(request.getProductIds());
        coupon.setStatus(1);

        Coupon saved = couponRepository.save(coupon);
        return Result.success(saved);
    }

    @GetMapping
    @Operation(summary = "获取优惠券列表")
    public Result<PageResult<Coupon>> getCoupons(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));
        
        Specification<Coupon> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("name"), "%" + keyword + "%"));
        }
        
        Page<Coupon> couponPage = couponRepository.findAll(spec, pageable);
        return Result.success(PageResult.of(couponPage.getTotalElements(), page, size, couponPage.getContent()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新优惠券")
    public Result<Coupon> updateCoupon(@PathVariable Long id, @Valid @RequestBody CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("优惠券不存在"));
        coupon.setName(request.getName());
        coupon.setDescription(request.getDescription());
        coupon.setCouponType(request.getCouponType());
        coupon.setDiscount(request.getDiscount());
        coupon.setMinAmount(request.getMinAmount());
        coupon.setMaxDiscount(request.getMaxDiscount());
        coupon.setPerLimit(request.getPerLimit());
        coupon.setStartTime(request.getStartTime());
        coupon.setEndTime(request.getEndTime());
        coupon.setCategoryIds(request.getCategoryIds());
        coupon.setProductIds(request.getProductIds());
        return Result.success(couponRepository.save(coupon));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取优惠券详情")
    public Result<Coupon> getCouponById(@PathVariable Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("优惠券不存在"));
        return Result.success(coupon);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除优惠券")
    public Result<String> deleteCoupon(@PathVariable Long id) {
        couponRepository.deleteById(id);
        return Result.success("删除成功");
    }
}
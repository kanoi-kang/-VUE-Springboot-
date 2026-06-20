package com.example.backend.controller.api.user;

import com.example.backend.common.Result;
import com.example.backend.dto.ReviewRequest;
import com.example.backend.entity.Review;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/reviews")
@Tag(name = "用户评价接口")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    @Operation(summary = "创建评价")
    public Result<Review> createReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReviewRequest request) {
        Review review = reviewService.createReview(
                principal.getUserId(),
                request.getProductId(),
                request.getOrderId(),
                request.getRating(),
                request.getContent(),
                request.getPicUrls()
        );
        return Result.success(review);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "获取商品评价列表")
    public Result<Page<Map<String, Object>>> getProductReviews(
            @Parameter(description = "商品ID") @PathVariable Long productId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        Page<Map<String, Object>> reviews = reviewService.getProductReviews(productId, page, size);
        return Result.success(reviews);
    }

    @GetMapping("/product/{productId}/stats")
    @Operation(summary = "获取商品评价统计")
    public Result<Map<String, Object>> getProductReviewStats(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        Map<String, Object> stats = reviewService.getProductReviewStats(productId);
        return Result.success(stats);
    }

    @GetMapping("/my")
    @Operation(summary = "获取我的评价列表")
    public Result<List<Map<String, Object>>> getUserReviews(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<Map<String, Object>> reviews = reviewService.getUserReviews(principal.getUserId());
        return Result.success(reviews);
    }
}
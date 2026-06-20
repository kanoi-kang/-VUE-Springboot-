package com.example.backend.controller.api.user;

import com.example.backend.common.Result;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/collections")
@Tag(name = "用户收藏接口")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    @PostMapping("/{productId}")
    @Operation(summary = "添加收藏")
    public Result<Map<String, Object>> addCollection(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        Map<String, Object> result = collectionService.addCollection(principal.getUserId(), productId);
        return Result.success(result);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "取消收藏")
    public Result<Map<String, Object>> removeCollection(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        Map<String, Object> result = collectionService.removeCollection(principal.getUserId(), productId);
        return Result.success(result);
    }

    @GetMapping
    @Operation(summary = "获取用户收藏列表")
    public Result<List<Map<String, Object>>> getUserCollections(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<Map<String, Object>> collections = collectionService.getUserCollections(principal.getUserId());
        return Result.success(collections);
    }

    @GetMapping("/{productId}/status")
    @Operation(summary = "检查是否已收藏")
    public Result<Map<String, Object>> checkCollection(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        boolean collected = collectionService.isCollected(principal.getUserId(), productId);
        Map<String, Object> result = Map.of("collected", collected);
        return Result.success(result);
    }
}
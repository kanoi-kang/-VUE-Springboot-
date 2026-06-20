package com.example.backend.controller.api.user;

import com.example.backend.common.Result;
import com.example.backend.common.PageResult;
import com.example.backend.dto.OrderRequest;
import com.example.backend.dto.OrderVO;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Result<OrderVO> createOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody OrderRequest request) {
        return Result.success(orderService.createOrder(principal.getUserId(), request));
    }

    @GetMapping
    public Result<PageResult<OrderVO>> getOrderList(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(orderService.getOrderList(principal.getUserId(), page, pageSize, status));
    }

    @GetMapping("/{orderId}")
    public Result<OrderVO> getOrderById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId) {
        return Result.success(orderService.getOrderById(principal.getUserId(), orderId));
    }

    @PutMapping("/{orderId}/pay")
    public Result<Void> payOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId,
            @RequestParam String payType) {
        orderService.payOrder(principal.getUserId(), orderId, payType);
        return Result.success(null);
    }

    @PutMapping("/{orderId}/confirm")
    public Result<Void> confirmOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId) {
        orderService.confirmOrder(principal.getUserId(), orderId);
        return Result.success(null);
    }

    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId) {
        orderService.cancelOrder(principal.getUserId(), orderId);
        return Result.success(null);
    }

    @PutMapping("/{orderId}/items/{itemId}/quantity")
    public Result<Void> updateOrderItemQuantity(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        orderService.updateOrderItemQuantity(principal.getUserId(), orderId, itemId, quantity);
        return Result.success(null);
    }
}
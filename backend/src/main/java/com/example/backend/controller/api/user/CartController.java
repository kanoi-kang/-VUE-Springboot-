package com.example.backend.controller.api.user;

import com.example.backend.common.Result;
import com.example.backend.dto.CartAddRequest;
import com.example.backend.dto.CartVO;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public Result<List<CartVO>> getCartList(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(cartService.getCartList(principal.getUserId()));
    }

    @PostMapping
    public Result<CartVO> addToCart(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CartAddRequest request) {
        return Result.success(cartService.addToCart(principal.getUserId(), request));
    }

    @PutMapping("/{cartId}")
    public Result<Void> updateCartItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long cartId,
            @RequestParam Integer quantity) {
        cartService.updateCartItem(principal.getUserId(), cartId, quantity);
        return Result.success(null);
    }

    @DeleteMapping("/{cartId}")
    public Result<Void> removeCartItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long cartId) {
        cartService.removeCartItem(principal.getUserId(), cartId);
        return Result.success(null);
    }

    @DeleteMapping
    public Result<Void> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        cartService.clearCart(principal.getUserId());
        return Result.success(null);
    }
}
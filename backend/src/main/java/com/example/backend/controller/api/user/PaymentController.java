package com.example.backend.controller.api.user;

import com.example.backend.common.Result;
import com.example.backend.dto.PaymentRequest;
import com.example.backend.dto.PaymentVO;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public Result<PaymentVO> createPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PaymentRequest request) {
        return Result.success(paymentService.createPayment(principal.getUserId(), request.getOrderNo(), request.getType()));
    }

    @GetMapping("/order/{orderNo}")
    public Result<PaymentVO> getPaymentByOrderNo(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String orderNo) {
        return Result.success(paymentService.getPaymentByOrderNo(principal.getUserId(), orderNo));
    }

    @PostMapping("/mock/{paymentNo}")
    public Result<Void> mockPayment(@PathVariable String paymentNo) {
        paymentService.mockPayment(paymentNo);
        return Result.success(null);
    }
}
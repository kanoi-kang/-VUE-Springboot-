package com.example.backend.controller.api.user;

import com.example.backend.common.Result;
import com.example.backend.dto.RefundRequest;
import com.example.backend.dto.RefundVO;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/refunds")
@Tag(name = "用户退款接口")
public class RefundController {

    @Autowired
    private RefundService refundService;

    @PostMapping
    @Operation(summary = "申请退款")
    public Result<RefundVO> createRefund(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody RefundRequest request) {
        RefundVO refund = refundService.createRefund(principal.getUserId(), request);
        return Result.success(refund);
    }

    @GetMapping
    @Operation(summary = "获取我的退款列表")
    public Result<List<RefundVO>> getMyRefunds(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<RefundVO> refunds = refundService.getUserRefunds(principal.getUserId());
        return Result.success(refunds);
    }

    @GetMapping("/{refundId}")
    @Operation(summary = "获取退款详情")
    public Result<RefundVO> getRefundDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "退款ID") @PathVariable Long refundId) {
        RefundVO refund = refundService.getUserRefund(principal.getUserId(), refundId);
        return Result.success(refund);
    }
}
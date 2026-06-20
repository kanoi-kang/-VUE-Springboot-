package com.example.backend.controller.api.admin;

import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.dto.RefundVO;
import com.example.backend.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/refunds")
@Tag(name = "管理端退款接口")
public class AdminRefundController {

    @Autowired
    private RefundService refundService;

    @GetMapping
    @Operation(summary = "获取退款列表")
    public Result<PageResult<RefundVO>> getRefunds(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        Page<RefundVO> refundPage = refundService.getRefunds(page - 1, size, status, keyword);
        return Result.success(PageResult.of(refundPage.getTotalElements(), page, size, refundPage.getContent()));
    }

    @GetMapping("/{refundId}")
    @Operation(summary = "获取退款详情")
    public Result<RefundVO> getRefundDetail(
            @Parameter(description = "退款ID") @PathVariable Long refundId) {
        RefundVO refund = refundService.getRefund(refundId);
        return Result.success(refund);
    }

    @PutMapping("/{refundId}/approve")
    @Operation(summary = "通过退款申请")
    public Result<RefundVO> approveRefund(
            @Parameter(description = "退款ID") @PathVariable Long refundId,
            @RequestParam(required = false) String remark) {
        RefundVO refund = refundService.approveRefund(refundId, remark);
        return Result.success(refund);
    }

    @PutMapping("/{refundId}/reject")
    @Operation(summary = "拒绝退款申请")
    public Result<RefundVO> rejectRefund(
            @Parameter(description = "退款ID") @PathVariable Long refundId,
            @RequestParam(required = false) String remark) {
        RefundVO refund = refundService.rejectRefund(refundId, remark);
        return Result.success(refund);
    }
}
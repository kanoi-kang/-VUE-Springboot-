package com.example.backend.service;

import com.example.backend.dto.RefundRequest;
import com.example.backend.dto.RefundVO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RefundService {

    RefundVO createRefund(Long userId, RefundRequest request);

    RefundVO getUserRefund(Long userId, Long refundId);

    List<RefundVO> getUserRefunds(Long userId);

    RefundVO getRefund(Long refundId);

    List<RefundVO> getAllRefunds();

    List<RefundVO> getRefundsByStatus(Integer status);
    
    Page<RefundVO> getRefunds(int page, int size, Integer status, String keyword);

    RefundVO approveRefund(Long refundId, String remark);

    RefundVO rejectRefund(Long refundId, String remark);

    boolean hasPendingRefund(Long orderId);
}
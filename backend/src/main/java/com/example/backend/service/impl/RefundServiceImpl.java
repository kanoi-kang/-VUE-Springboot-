package com.example.backend.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.dto.RefundRequest;
import com.example.backend.dto.RefundVO;
import com.example.backend.entity.Order;
import com.example.backend.entity.Refund;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.RefundRepository;
import com.example.backend.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@Service
public class RefundServiceImpl implements RefundService {

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    @Transactional
    public RefundVO createRefund(Long userId, RefundRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此订单");
        }

        if (order.getStatus() == -1) {
            throw new BusinessException(400, "订单已取消，无法申请退款");
        }

        if (refundRepository.existsByOrderId(request.getOrderId())) {
            throw new BusinessException(400, "该订单已有退款申请");
        }

        Refund refund = new Refund();
        refund.setOrderId(order.getId());
        refund.setUserId(userId);
        refund.setOrderNo(order.getOrderNo());
        refund.setRefundNo(generateRefundNo());
        refund.setReason(request.getReason());
        refund.setDescription(request.getDescription());
        refund.setAmount(order.getPayAmount());
        refund.setStatus(0);

        refund = refundRepository.save(refund);
        return toVO(refund);
    }

    @Override
    public RefundVO getUserRefund(Long userId, Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException(404, "退款申请不存在"));

        if (!refund.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权查看此退款申请");
        }

        return toVO(refund);
    }

    @Override
    public List<RefundVO> getUserRefunds(Long userId) {
        return refundRepository.findByUserIdOrderByCreateTimeDesc(userId,
                org.springframework.data.domain.Pageable.unpaged()).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public RefundVO getRefund(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException(404, "退款申请不存在"));
        return toVO(refund);
    }

    @Override
    public List<RefundVO> getAllRefunds() {
        return refundRepository.findAll(org.springframework.data.domain.Pageable.unpaged()).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RefundVO> getRefundsByStatus(Integer status) {
        return refundRepository.findByStatusOrderByCreateTimeDesc(status,
                org.springframework.data.domain.Pageable.unpaged()).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RefundVO> getRefunds(int page, int size, Integer status, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        
        Specification<Refund> spec = Specification.where(null);
        
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(root.get("orderNo"), "%" + keyword + "%"),
                cb.like(root.get("refundNo"), "%" + keyword + "%")
            ));
        }
        
        return refundRepository.findAll(spec, pageable).map(this::toVO);
    }

    @Override
    @Transactional
    public RefundVO approveRefund(Long refundId, String remark) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException(404, "退款申请不存在"));

        if (refund.getStatus() != 0) {
            throw new BusinessException(400, "退款申请状态不是待处理");
        }

        Order order = orderRepository.findById(refund.getOrderId())
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        refund.setStatus(1);
        refund.setAdminRemark(remark);
        refund.setProcessTime(LocalDateTime.now());

        order.setStatus(-1);
        order.setCloseTime(LocalDateTime.now());
        orderRepository.save(order);

        refund = refundRepository.save(refund);
        return toVO(refund);
    }

    @Override
    @Transactional
    public RefundVO rejectRefund(Long refundId, String remark) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException(404, "退款申请不存在"));

        if (refund.getStatus() != 0) {
            throw new BusinessException(400, "退款申请状态不是待处理");
        }

        refund.setStatus(2);
        refund.setAdminRemark(remark);
        refund.setProcessTime(LocalDateTime.now());

        refund = refundRepository.save(refund);
        return toVO(refund);
    }

    @Override
    public boolean hasPendingRefund(Long orderId) {
        return refundRepository.existsByOrderId(orderId);
    }

    private RefundVO toVO(Refund refund) {
        RefundVO vo = new RefundVO();
        vo.setId(refund.getId());
        vo.setOrderId(refund.getOrderId());
        vo.setOrderNo(refund.getOrderNo());
        vo.setRefundNo(refund.getRefundNo());
        vo.setReason(refund.getReason());
        vo.setDescription(refund.getDescription());
        vo.setAmount(refund.getAmount());
        vo.setStatus(refund.getStatus());
        vo.setStatusText(getStatusText(refund.getStatus()));
        vo.setAdminRemark(refund.getAdminRemark());
        vo.setCreateTime(refund.getCreateTime());
        vo.setProcessTime(refund.getProcessTime());
        return vo;
    }

    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待处理";
            case 1: return "已通过";
            case 2: return "已拒绝";
            default: return "未知";
        }
    }

    private String generateRefundNo() {
        return "REF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int)(Math.random() * 10000));
    }
}
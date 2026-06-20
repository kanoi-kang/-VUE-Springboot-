package com.example.backend.service;

import com.example.backend.common.BusinessException;
import com.example.backend.dto.PaymentVO;
import com.example.backend.entity.Order;
import com.example.backend.entity.Payment;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private static final DateTimeFormatter PAYMENT_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    public PaymentVO createPayment(Long userId, String orderNo, String type) {
        Optional<Order> orderOpt = orderRepository.findByOrderNo(orderNo);
        if (orderOpt.isEmpty()) {
            throw new BusinessException(404, "订单不存在");
        }

        Order order = orderOpt.get();
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此订单");
        }

        if (order.getStatus() != 0) {
            throw new BusinessException(400, "订单状态错误");
        }

        Payment existingPayment = paymentRepository.findByOrderNo(orderNo).orElse(null);
        if (existingPayment != null && "SUCCESS".equals(existingPayment.getStatus())) {
            throw new BusinessException(400, "订单已支付");
        }

        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setOrderNo(orderNo);
        payment.setUserId(userId);
        payment.setType(type);
        payment.setAmount(order.getPayAmount());
        payment.setStatus("PENDING");
        Payment savedPayment = paymentRepository.save(payment);

        return convertToVO(savedPayment);
    }

    @Transactional
    public void mockPayment(String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new BusinessException(404, "支付记录不存在"));

        if (!"PENDING".equals(payment.getStatus())) {
            throw new BusinessException(400, "支付状态错误");
        }

        payment.setStatus("SUCCESS");
        payment.setTradeNo(UUID.randomUUID().toString().substring(0, 20).toUpperCase());
        payment.setNotifyTime(LocalDateTime.now());
        paymentRepository.save(payment);

        Order order = orderRepository.findByOrderNo(payment.getOrderNo())
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        order.setPayType(payment.getType());
        order.setPayStatus(1);
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderRepository.save(order);
    }

    public PaymentVO getPaymentByOrderNo(Long userId, String orderNo) {
        Optional<Payment> paymentOpt = paymentRepository.findByOrderNo(orderNo);
        if (paymentOpt.isEmpty()) {
            throw new BusinessException(404, "支付记录不存在");
        }

        Payment payment = paymentOpt.get();
        if (!payment.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权查看此支付记录");
        }

        return convertToVO(payment);
    }

    private String generatePaymentNo() {
        String timestamp = LocalDateTime.now().format(PAYMENT_NO_FORMAT);
        String uuid = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "PAY" + timestamp + uuid;
    }

    private PaymentVO convertToVO(Payment payment) {
        PaymentVO vo = new PaymentVO();
        vo.setId(payment.getId());
        vo.setPaymentNo(payment.getPaymentNo());
        vo.setOrderNo(payment.getOrderNo());
        vo.setType(payment.getType());
        vo.setAmount(payment.getAmount());
        vo.setStatus(payment.getStatus());
        vo.setTradeNo(payment.getTradeNo());
        vo.setCreateTime(payment.getCreateTime());
        return vo;
    }
}
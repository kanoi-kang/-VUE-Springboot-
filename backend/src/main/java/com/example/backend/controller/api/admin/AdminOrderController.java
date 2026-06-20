package com.example.backend.controller.api.admin;

import com.example.backend.common.Result;
import com.example.backend.common.PageResult;
import com.example.backend.dto.OrderStats;
import com.example.backend.dto.OrderVO;
import com.example.backend.entity.Order;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.OrderItemRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public Result<PageResult<OrderVO>> getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

        Page<Order> orderPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            if (status != null) {
                orderPage = orderRepository.searchByKeywordAndStatus(keyword.trim(), status, pageable);
            } else {
                orderPage = orderRepository.searchByKeyword(keyword.trim(), pageable);
            }
        } else if (status != null) {
            orderPage = orderRepository.findByStatus(status, pageable);
        } else {
            orderPage = orderRepository.findAllByOrderByCreateTimeDesc(pageable);
        }

        List<OrderVO> orderVOs = orderPage.getContent().stream()
                .map(order -> {
                    OrderVO vo = new OrderVO();
                    vo.setId(order.getId());
                    vo.setOrderNo(order.getOrderNo());
                    vo.setUserId(order.getUserId());
                    userRepository.findById(order.getUserId()).ifPresent(user -> {
                        vo.setUserName(user.getUsername());
                    });
                    vo.setStatus(order.getStatus());
                    vo.setStatusText(getStatusText(order.getStatus()));
                    vo.setPayType(order.getPayType());
                    vo.setPayStatus(order.getPayStatus());
                    vo.setTotalAmount(order.getTotalAmount());
                    vo.setFreightAmount(order.getFreightAmount());
                    vo.setDiscountAmount(order.getDiscountAmount());
                    vo.setPayAmount(order.getPayAmount());
                    vo.setConsignee(order.getConsignee());
                    vo.setPhone(order.getPhone());
                    vo.setProvince(order.getProvince());
                    vo.setCity(order.getCity());
                    vo.setDistrict(order.getDistrict());
                    vo.setDetailAddress(order.getDetailAddress());
                    vo.setTrackingNo(order.getTrackingNo());
                    vo.setCreateTime(order.getCreateTime());
                    return vo;
                })
                .collect(Collectors.toList());

        return Result.success(PageResult.of(orderPage.getTotalElements(), page, pageSize, orderVOs));
    }

    @GetMapping("/{orderId}")
    public Result<OrderVO> getOrderById(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new com.example.backend.common.BusinessException(404, "Order not found"));

        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setStatus(order.getStatus());
        vo.setStatusText(getStatusText(order.getStatus()));
        vo.setPayType(order.getPayType());
        vo.setPayStatus(order.getPayStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setFreightAmount(order.getFreightAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setConsignee(order.getConsignee());
        vo.setPhone(order.getPhone());
        vo.setProvince(order.getProvince());
        vo.setCity(order.getCity());
        vo.setDistrict(order.getDistrict());
        vo.setDetailAddress(order.getDetailAddress());
        vo.setRemark(order.getRemark());
        vo.setTrackingNo(order.getTrackingNo());
        vo.setCreateTime(order.getCreateTime());
        vo.setPayTime(order.getPayTime());
        vo.setShipTime(order.getShipTime());
        vo.setConfirmTime(order.getConfirmTime());

        List<com.example.backend.entity.OrderItem> items = orderItemRepository.findByOrderId(orderId);
        vo.setItems(items.stream()
                .map(item -> {
                    com.example.backend.dto.OrderItemVO itemVO = new com.example.backend.dto.OrderItemVO();
                    itemVO.setId(item.getId());
                    itemVO.setProductId(item.getProductId());
                    itemVO.setProductName(item.getProductName());
                    itemVO.setPic(item.getPic());
                    itemVO.setSpecificationId(item.getSpecificationId());
                    itemVO.setSpecName(item.getSpecName());
                    itemVO.setQuantity(item.getQuantity());
                    itemVO.setPrice(item.getPrice());
                    itemVO.setTotalPrice(item.getTotalPrice());
                    return itemVO;
                })
                .collect(Collectors.toList()));

        return Result.success(vo);
    }

    @PutMapping("/{orderId}")
    public Result<Order> updateOrder(@PathVariable Long orderId, @RequestBody Order orderUpdate) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new com.example.backend.common.BusinessException(404, "Order not found"));

        if (orderUpdate.getConsignee() != null) {
            order.setConsignee(orderUpdate.getConsignee());
        }
        if (orderUpdate.getPhone() != null) {
            order.setPhone(orderUpdate.getPhone());
        }
        if (orderUpdate.getProvince() != null) {
            order.setProvince(orderUpdate.getProvince());
        }
        if (orderUpdate.getCity() != null) {
            order.setCity(orderUpdate.getCity());
        }
        if (orderUpdate.getDistrict() != null) {
            order.setDistrict(orderUpdate.getDistrict());
        }
        if (orderUpdate.getDetailAddress() != null) {
            order.setDetailAddress(orderUpdate.getDetailAddress());
        }
        if (orderUpdate.getRemark() != null) {
            order.setRemark(orderUpdate.getRemark());
        }
        if (orderUpdate.getTrackingNo() != null) {
            order.setTrackingNo(orderUpdate.getTrackingNo());
        }

        return Result.success(orderRepository.save(order));
    }

    @PutMapping("/{orderId}/ship")
    public Result<Void> shipOrder(
            @PathVariable Long orderId,
            @RequestParam String trackingNo) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new com.example.backend.common.BusinessException(404, "Order not found"));

        if (order.getStatus() != 1) {
            throw new com.example.backend.common.BusinessException(400, "Order status error");
        }

        order.setStatus(2);
        order.setShipTime(LocalDateTime.now());
        order.setTrackingNo(trackingNo);
        orderRepository.save(order);

        return Result.success(null);
    }

    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new com.example.backend.common.BusinessException(404, "Order not found"));

        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new com.example.backend.common.BusinessException(400, "Order cannot be cancelled");
        }

        order.setStatus(-1);
        order.setCloseTime(LocalDateTime.now());
        orderRepository.save(order);

        return Result.success(null);
    }

    @PutMapping("/{orderId}/confirm")
    public Result<Void> confirmOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new com.example.backend.common.BusinessException(404, "Order not found"));

        if (order.getStatus() != 2) {
            throw new com.example.backend.common.BusinessException(400, "Order cannot be confirmed");
        }

        order.setStatus(3);
        order.setConfirmTime(LocalDateTime.now());
        orderRepository.save(order);

        return Result.success(null);
    }

    @DeleteMapping("/{orderId}")
    public Result<String> deleteOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new com.example.backend.common.BusinessException(404, "Order not found"));

        orderRepository.delete(order);
        return Result.success("删除成功");
    }

    @GetMapping("/stats")
    public Result<Object> getOrderStats() {
        long totalOrders = orderRepository.count();
        long pendingPayment = orderRepository.countByStatus(0);
        long pendingShipment = orderRepository.countByStatus(1);
        long pendingReceipt = orderRepository.countByStatus(2);
        long completed = orderRepository.countByStatus(3);
        long cancelled = orderRepository.countByStatus(-1);

        Double todayAmount = orderRepository.sumTotalAmountBetween(
                LocalDateTime.now().withHour(0).withMinute(0).withSecond(0),
                LocalDateTime.now().withHour(23).withMinute(59).withSecond(59));

        OrderStats stats = new OrderStats(
                totalOrders,
                pendingPayment,
                pendingShipment,
                pendingReceipt,
                completed,
                cancelled,
                todayAmount != null ? todayAmount : 0.0
        );

        return Result.success(stats);
    }

    private String getStatusText(Integer status) {
        switch (status) {
            case -1: return "已取消";
            case 0: return "待付款";
            case 1: return "待发货";
            case 2: return "待收货";
            case 3: return "已完成";
            default: return "未知";
        }
    }
}
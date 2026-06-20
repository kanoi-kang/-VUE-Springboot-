package com.example.backend.service;

import com.example.backend.common.BusinessException;
import com.example.backend.common.PageResult;
import com.example.backend.dto.OrderItemRequest;
import com.example.backend.dto.OrderRequest;
import com.example.backend.dto.OrderVO;
import com.example.backend.dto.OrderItemVO;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CouponService couponService;

    @Transactional
    public OrderVO createOrder(Long userId, OrderRequest request) {
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new BusinessException(404, "Address not found"));

        List<Cart> cartItems = new ArrayList<>();
        List<OrderItemRequest> orderItems = new ArrayList<>();

        if (request.getCartItemIds() != null && !request.getCartItemIds().isEmpty()) {
            for (Long cartId : request.getCartItemIds()) {
                Cart cart = cartRepository.findById(cartId)
                        .orElseThrow(() -> new BusinessException(404, "Cart item not found"));
                if (!cart.getUserId().equals(userId)) {
                    throw new BusinessException(403, "Access denied");
                }
                cartItems.add(cart);

                OrderItemRequest itemRequest = new OrderItemRequest();
                itemRequest.setProductId(cart.getProductId());
                itemRequest.setQuantity(cart.getQuantity());
                itemRequest.setSpecificationId(cart.getSpecificationId());
                itemRequest.setSpecName(cart.getSpecName());
                orderItems.add(itemRequest);
            }
        } else if (request.getItems() != null && !request.getItems().isEmpty()) {
            orderItems.addAll(request.getItems());
        } else {
            throw new BusinessException(400, "No items in order");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setStatus(0);
        order.setPayStatus(0);
        order.setConsignee(address.getConsignee());
        order.setPhone(address.getPhone());
        order.setProvince(address.getProvince());
        order.setCity(address.getCity());
        order.setDistrict(address.getDistrict());
        order.setDetailAddress(address.getDetailAddress());
        order.setAddressId(request.getAddressId());
        order.setRemark(request.getRemark());
        order.setFreightAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest itemRequest : orderItems) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new BusinessException(404, "Product not found"));

            if (product.getIsPublish() == null || product.getIsPublish() != 1) {
                throw new BusinessException(400, "该商品已下架: " + product.getName());
            }
            if (product.getStatus() == null || product.getStatus() != 1) {
                throw new BusinessException(400, "该商品已下架: " + product.getName());
            }
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new BusinessException(400, "Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setPic(product.getPic());
            orderItem.setSpecificationId(itemRequest.getSpecificationId());
            orderItem.setSpecName(itemRequest.getSpecName());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            items.add(orderItem);

            totalAmount = totalAmount.add(orderItem.getTotalPrice());

            product.setStock(product.getStock() - itemRequest.getQuantity());
            product.setSales(product.getSales() + itemRequest.getQuantity());
            productRepository.save(product);
        }

        // 处理优惠券
        if (request.getCouponId() != null) {
            Map<String, Object> discountResult = couponService.calculateDiscount(request.getCouponId(), totalAmount);
            order.setCouponId(request.getCouponId());
            order.setDiscountAmount((BigDecimal) discountResult.get("discountAmount"));
        }

        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount.subtract(order.getDiscountAmount()).add(order.getFreightAmount()));

        Order savedOrder = orderRepository.save(order);

        for (OrderItem item : items) {
            item.setOrderId(savedOrder.getId());
            orderItemRepository.save(item);
        }

        if (!cartItems.isEmpty()) {
            cartRepository.deleteAll(cartItems);
        }

        return convertToVO(savedOrder, items);
    }

    public OrderVO getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "Access denied");
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return convertToVO(order, items);
    }

    public PageResult<OrderVO> getOrderList(Long userId, Integer page, Integer pageSize, Integer status) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        Page<Order> orderPage;
        if (status != null) {
            orderPage = orderRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            orderPage = orderRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
        }

        List<OrderVO> orderVOs = orderPage.getContent().stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    return convertToVO(order, items);
                })
                .collect(Collectors.toList());

        return PageResult.of(orderPage.getTotalElements(), page, pageSize, orderVOs);
    }

    @Transactional
    public void payOrder(Long userId, Long orderId, String payType) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "Access denied");
        }

        if (order.getStatus() != 0) {
            throw new BusinessException(400, "Order status error");
        }

        // 使用优惠券
        if (order.getCouponId() != null) {
            couponService.useCoupon(userId, order.getCouponId(), orderId);
        }

        order.setPayType(payType);
        order.setPayStatus(1);
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Transactional
    public void shipOrder(Long orderId, String trackingNo) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "Order not found"));

        if (order.getStatus() != 1) {
            throw new BusinessException(400, "Order status error");
        }

        order.setStatus(2);
        order.setShipTime(LocalDateTime.now());
        order.setTrackingNo(trackingNo);
        orderRepository.save(order);
    }

    @Transactional
    public void confirmOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "Access denied");
        }

        if (order.getStatus() != 2) {
            throw new BusinessException(400, "Order status error");
        }

        order.setStatus(3);
        order.setConfirmTime(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "Access denied");
        }

        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BusinessException(400, "Order cannot be cancelled");
        }

        order.setStatus(-1);
        order.setCloseTime(LocalDateTime.now());
        orderRepository.save(order);

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            });
        }
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(ORDER_NO_FORMAT);
        String uuid = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD" + timestamp + uuid;
    }

    private OrderVO convertToVO(Order order, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(order.getStatus());
        vo.setStatusText(getStatusText(order.getStatus()));
        vo.setPayType(order.getPayType());
        vo.setPayStatus(order.getPayStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setFreightAmount(order.getFreightAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setUserId(order.getUserId());
        vo.setConsignee(order.getConsignee());
        vo.setPhone(order.getPhone());
        vo.setProvince(order.getProvince());
        vo.setCity(order.getCity());
        vo.setDistrict(order.getDistrict());
        vo.setDetailAddress(order.getDetailAddress());
        vo.setCouponId(order.getCouponId());
        vo.setRemark(order.getRemark());
        vo.setTrackingNo(order.getTrackingNo());
        vo.setCreateTime(order.getCreateTime());
        vo.setPayTime(order.getPayTime());
        vo.setShipTime(order.getShipTime());
        vo.setConfirmTime(order.getConfirmTime());

        vo.setItems(items.stream()
                .map(this::convertToItemVO)
                .collect(Collectors.toList()));

        return vo;
    }

    private OrderItemVO convertToItemVO(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        vo.setId(item.getId());
        vo.setProductId(item.getProductId());
        vo.setProductName(item.getProductName());
        vo.setPic(item.getPic());
        vo.setSpecificationId(item.getSpecificationId());
        vo.setSpecName(item.getSpecName());
        vo.setQuantity(item.getQuantity());
        
        Product product = productRepository.findById(item.getProductId()).orElse(null);
        vo.setStock(product != null ? product.getStock() : 0);
        
        vo.setPrice(item.getPrice());
        vo.setTotalPrice(item.getTotalPrice());
        return vo;
    }

    @Transactional
    public void updateOrderItemQuantity(Long userId, Long orderId, Long itemId, Integer quantity) {
        logger.info("updateOrderItemQuantity called - userId: {}, orderId: {}, itemId: {}, quantity: {}", 
                    userId, orderId, itemId, quantity);
        
        if (quantity == null || quantity < 1) {
            logger.warn("Invalid quantity: {}", quantity);
            throw new BusinessException(400, "Invalid quantity");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "Order not found"));
        logger.info("Found order: {}, status: {}", order.getOrderNo(), order.getStatus());

        if (!order.getUserId().equals(userId)) {
            logger.warn("Access denied - order userId: {}, request userId: {}", order.getUserId(), userId);
            throw new BusinessException(403, "Access denied");
        }

        if (order.getStatus() != 0) {
            logger.warn("Order status is not pending: {}", order.getStatus());
            throw new BusinessException(400, "Only pending orders can modify item quantity");
        }

        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(404, "Order item not found"));
        logger.info("Found order item: productId: {}, currentQuantity: {}", item.getProductId(), item.getQuantity());

        if (!item.getOrderId().equals(orderId)) {
            logger.warn("Order item does not belong to this order - item orderId: {}, request orderId: {}", 
                        item.getOrderId(), orderId);
            throw new BusinessException(400, "Order item does not belong to this order");
        }

        Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new BusinessException(404, "Product not found"));

        Integer currentStock = product.getStock() != null ? product.getStock() : 0;
        int quantityDiff = quantity - item.getQuantity();
        logger.info("Current stock: {}, quantity diff: {}", currentStock, quantityDiff);

        if (quantityDiff > 0) {
            if (currentStock < quantityDiff) {
                logger.warn("Insufficient stock - need: {}, available: {}", quantityDiff, currentStock);
                throw new BusinessException(400, "Insufficient stock");
            }
            product.setStock(currentStock - quantityDiff);
            logger.info("Stock updated to: {}", product.getStock());
        } else if (quantityDiff < 0) {
            product.setStock(currentStock - quantityDiff);
            logger.info("Stock updated to: {}", product.getStock());
        }
        productRepository.save(product);

        item.setQuantity(quantity);
        item.setTotalPrice(item.getPrice().multiply(BigDecimal.valueOf(quantity)));
        orderItemRepository.save(item);
        logger.info("Order item quantity updated to: {}", quantity);

        BigDecimal totalAmount = orderItemRepository.findByOrderId(orderId).stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount.subtract(order.getDiscountAmount()).add(order.getFreightAmount()));
        orderRepository.save(order);
        logger.info("Order total amount updated to: {}", totalAmount);
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
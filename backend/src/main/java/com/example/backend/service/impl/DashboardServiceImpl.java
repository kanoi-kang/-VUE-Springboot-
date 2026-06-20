package com.example.backend.service.impl;

import com.example.backend.dto.DashboardStats;
import com.example.backend.repository.*;
import com.example.backend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();

        stats.setTotalOrders(orderRepository.count());
        stats.setPendingOrders(orderRepository.countByStatus(0));
        stats.setShippedOrders(orderRepository.countByStatus(2));
        stats.setCompletedOrders(orderRepository.countByStatus(3));

        Double totalRevenue = orderRepository.sumTotalAmount();
        stats.setTotalRevenue(totalRevenue != null ? totalRevenue : 0.0);

        stats.setTotalProducts(productRepository.count());
        stats.setTotalCategories(categoryRepository.count());
        stats.setTotalUsers(userRepository.count());
        stats.setTotalCoupons(couponRepository.count());

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        Double todayRevenue = orderRepository.sumTotalAmountBetween(todayStart, todayEnd);
        stats.setTodayRevenue(todayRevenue != null ? todayRevenue : 0.0);

        stats.setTodayOrders(orderRepository.countByCreateTimeBetween(todayStart, todayEnd));

        return stats;
    }

    @Override
    public List<Map<String, Object>> getRevenueByDays(Integer days) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);

            Double revenue = orderRepository.sumTotalAmountBetween(start, end);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("revenue", revenue != null ? revenue : 0.0);
            result.add(dayData);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getOrderTrendByDays(Integer days) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);

            Long count = orderRepository.countByCreateTimeBetween(start, end);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("orders", count);
            result.add(dayData);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getTopProducts(Integer limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Object[]> topProducts = productRepository.findTopProducts(limit);

        for (Object[] row : topProducts) {
            Map<String, Object> productData = new HashMap<>();
            productData.put("productId", row[0]);
            productData.put("productName", row[1]);
            productData.put("salesCount", row[2]);
            result.add(productData);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getCategoryStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Object[]> categoryStats = categoryRepository.countProductsByCategory();

        for (Object[] row : categoryStats) {
            Map<String, Object> categoryData = new HashMap<>();
            categoryData.put("categoryId", row[0]);
            categoryData.put("categoryName", row[1]);
            categoryData.put("productCount", row[2]);
            result.add(categoryData);
        }

        return result;
    }
}
package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    
    private Long totalOrders;
    
    private Long pendingOrders;
    
    private Long shippedOrders;
    
    private Long completedOrders;
    
    private Double totalRevenue;
    
    private Long totalProducts;
    
    private Long totalCategories;
    
    private Long totalUsers;
    
    private Long totalCoupons;
    
    private Double todayRevenue;
    
    private Long todayOrders;
}
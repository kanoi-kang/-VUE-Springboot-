package com.example.backend.service;

import com.example.backend.dto.DashboardStats;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    DashboardStats getStats();

    List<Map<String, Object>> getRevenueByDays(Integer days);

    List<Map<String, Object>> getOrderTrendByDays(Integer days);

    List<Map<String, Object>> getTopProducts(Integer limit);

    List<Map<String, Object>> getCategoryStats();
}
package com.example.backend.controller.api.admin;

import com.example.backend.common.Result;
import com.example.backend.dto.DashboardStats;
import com.example.backend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    public Result<DashboardStats> getDashboard() {
        return Result.success(dashboardService.getStats());
    }

    @GetMapping("/dashboard/revenue")
    public Result<Map<String, Object>> getRevenueStats(
            @RequestParam(defaultValue = "7") Integer days) {
        Map<String, Object> result = new HashMap<>();
        result.put("days", days);
        result.put("revenue", dashboardService.getRevenueByDays(days));
        return Result.success(result);
    }

    @GetMapping("/dashboard/orders-trend")
    public Result<Map<String, Object>> getOrderTrend(
            @RequestParam(defaultValue = "7") Integer days) {
        Map<String, Object> result = new HashMap<>();
        result.put("days", days);
        result.put("orders", dashboardService.getOrderTrendByDays(days));
        return Result.success(result);
    }

    @GetMapping("/dashboard/topProducts")
    public Result<Map<String, Object>> getTopProducts(
            @RequestParam(defaultValue = "10") Integer limit) {
        Map<String, Object> result = new HashMap<>();
        result.put("products", dashboardService.getTopProducts(limit));
        return Result.success(result);
    }

    @GetMapping("/dashboard/categoryStats")
    public Result<Map<String, Object>> getCategoryStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("categories", dashboardService.getCategoryStats());
        return Result.success(result);
    }
}
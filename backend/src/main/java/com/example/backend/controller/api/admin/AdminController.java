package com.example.backend.controller.api.admin;

import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.dto.AdminLoginRequest;
import com.example.backend.dto.AdminLoginResponse;
import com.example.backend.dto.AdminVO;
import com.example.backend.entity.Admin;
import com.example.backend.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    public Result<AdminLoginResponse> login(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletRequest httpRequest,
            HttpSession session) {
        String ip = getClientIp(httpRequest);
        AdminLoginResponse response = adminService.login(request, ip, session);
        return Result.success(response);
    }

    @GetMapping("/profile")
    public Result<AdminVO> getProfile(@RequestHeader("Authorization") String token) {
        return Result.success(adminService.getProfile(token));
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(
            @RequestHeader("Authorization") String token,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        adminService.updatePassword(token, oldPassword, newPassword);
        return Result.success(null);
    }

    @GetMapping("/admins")
    public Result<PageResult<AdminVO>> getAdminList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<AdminVO> adminPage = adminService.getAdminList(pageable, keyword);
        return Result.success(PageResult.of(adminPage.getTotalElements(), page, pageSize, adminPage.getContent()));
    }

    @GetMapping("/admins/{id}")
    public Result<AdminVO> getAdminById(@PathVariable Long id) {
        return Result.success(adminService.getAdminById(id));
    }

    @PostMapping("/admins")
    public Result<AdminVO> createAdmin(@RequestBody Admin admin) {
        return Result.success(adminService.createAdmin(admin));
    }

    @PutMapping("/admins/{id}")
    public Result<AdminVO> updateAdmin(@PathVariable Long id, @RequestBody Admin admin) {
        return Result.success(adminService.updateAdmin(id, admin));
    }

    @DeleteMapping("/admins/{id}")
    public Result<String> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return Result.success("删除成功");
    }

    @PutMapping("/admins/{id}/status")
    public Result<Void> updateAdminStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminService.updateAdminStatus(id, status);
        return Result.success(null);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
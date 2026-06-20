package com.example.backend.service;

import com.example.backend.common.BusinessException;
import com.example.backend.dto.AdminLoginRequest;
import com.example.backend.dto.AdminLoginResponse;
import com.example.backend.dto.AdminVO;
import com.example.backend.entity.Admin;
import com.example.backend.repository.AdminRepository;
import com.example.backend.security.JwtUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public AdminLoginResponse login(AdminLoginRequest request, String ip, HttpSession session) {
        String captcha = (String) session.getAttribute("admin_captcha");
        if (captcha == null || !captcha.equalsIgnoreCase(request.getCaptcha())) {
            throw new BusinessException(400, "Invalid captcha");
        }
        session.removeAttribute("admin_captcha");

        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException(401, "Invalid username or password");
        }

        if (admin.getStatus() != 1) {
            throw new BusinessException(403, "Account is disabled");
        }

        admin.setLastLoginTime(LocalDateTime.now());
        admin.setLastLoginIp(ip);
        adminRepository.save(admin);

        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername(), "ADMIN");

        return new AdminLoginResponse(token, convertToVO(admin));
    }

    public AdminVO getProfile(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long adminId = jwtUtil.getUserIdFromToken(token);
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(404, "Admin not found"));
        return convertToVO(admin);
    }

    public void updatePassword(String token, String oldPassword, String newPassword) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long adminId = jwtUtil.getUserIdFromToken(token);
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(404, "Admin not found"));

        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            throw new BusinessException(400, "Old password is incorrect");
        }

        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
    }

    public Page<AdminVO> getAdminList(Pageable pageable, String keyword) {
        Page<Admin> adminPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            adminPage = adminRepository.findByUsernameContainingOrNicknameContaining(keyword, keyword, pageable);
        } else {
            adminPage = adminRepository.findAll(pageable);
        }
        return adminPage.map(this::convertToVO);
    }

    public AdminVO getAdminById(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Admin not found"));
        return convertToVO(admin);
    }

    @Transactional
    public AdminVO createAdmin(Admin admin) {
        if (adminRepository.existsByUsername(admin.getUsername())) {
            throw new BusinessException(400, "Username already exists");
        }

        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setCreateTime(LocalDateTime.now());
        Admin savedAdmin = adminRepository.save(admin);
        return convertToVO(savedAdmin);
    }

    @Transactional
    public AdminVO updateAdmin(Long id, Admin admin) {
        Admin existing = adminRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Admin not found"));

        if (admin.getNickname() != null) {
            existing.setNickname(admin.getNickname());
        }
        if (admin.getEmail() != null) {
            existing.setEmail(admin.getEmail());
        }
        if (admin.getPhone() != null) {
            existing.setPhone(admin.getPhone());
        }
        if (admin.getAvatar() != null) {
            existing.setAvatar(admin.getAvatar());
        }
        if (admin.getPassword() != null && !admin.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(admin.getPassword()));
        }

        return convertToVO(adminRepository.save(existing));
    }

    @Transactional
    public void deleteAdmin(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Admin not found"));
        adminRepository.delete(admin);
    }

    @Transactional
    public void updateAdminStatus(Long id, Integer status) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Admin not found"));
        admin.setStatus(status);
        adminRepository.save(admin);
    }

    private AdminVO convertToVO(Admin admin) {
        AdminVO vo = new AdminVO();
        vo.setId(admin.getId());
        vo.setUsername(admin.getUsername());
        vo.setEmail(admin.getEmail());
        vo.setPhone(admin.getPhone());
        vo.setNickname(admin.getNickname());
        vo.setAvatar(admin.getAvatar());
        vo.setStatus(admin.getStatus());
        vo.setLastLoginTime(admin.getLastLoginTime());
        vo.setLastLoginIp(admin.getLastLoginIp());
        vo.setCreateTime(admin.getCreateTime());
        return vo;
    }
}
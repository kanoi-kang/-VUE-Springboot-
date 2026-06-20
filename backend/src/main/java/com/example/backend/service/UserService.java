package com.example.backend.service;

import com.example.backend.common.BusinessException;
import com.example.backend.dto.*;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public UserVO register(RegisterRequest request, HttpSession session) {
        String captcha = (String) session.getAttribute("captcha");
        if (captcha == null || !captcha.equalsIgnoreCase(request.getCaptcha())) {
            throw new BusinessException(400, "Invalid captcha");
        }
        session.removeAttribute("captcha");

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(400, "Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(400, "Email already exists");
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(400, "Phone number already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());

        User savedUser = userRepository.save(user);
        return convertToVO(savedUser);
    }

    public LoginResponse login(LoginRequest request, HttpSession session) {
        String captcha = (String) session.getAttribute("captcha");
        if (captcha == null || !captcha.equalsIgnoreCase(request.getCaptcha())) {
            throw new BusinessException(400, "Invalid captcha");
        }
        session.removeAttribute("captcha");

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "Invalid username or password");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(403, "Account is disabled");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), "USER");

        return new LoginResponse(token, convertToVO(user));
    }

    public UserVO getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "User not found"));
        return convertToVO(user);
    }

    public UserVO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "User not found"));
        return convertToVO(user);
    }

    @Transactional
    public UserVO updateUser(Long userId, UserVO userVO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "User not found"));

        if (userVO.getNickname() != null) {
            user.setNickname(userVO.getNickname());
        }
        if (userVO.getAvatar() != null) {
            user.setAvatar(userVO.getAvatar());
        }
        if (userVO.getGender() != null) {
            user.setGender(userVO.getGender());
        }
        if (userVO.getBirthDate() != null) {
            user.setBirthDate(userVO.getBirthDate());
        }
        if (userVO.getAddress() != null) {
            user.setAddress(userVO.getAddress());
        }

        User updatedUser = userRepository.save(user);
        return convertToVO(updatedUser);
    }

    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(400, "Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setGender(user.getGender());
        vo.setBirthDate(user.getBirthDate());
        vo.setAddress(user.getAddress());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
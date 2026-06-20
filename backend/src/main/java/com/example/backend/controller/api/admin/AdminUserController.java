package com.example.backend.controller.api.admin;

import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public Result<PageResult<User>> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<User> userPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            Page<User> usernamePage = userRepository.findByUsernameContaining(keyword, pageable);
            Page<User> emailPage = userRepository.findByEmailContaining(keyword, pageable);
            Page<User> nicknamePage = userRepository.findByNicknameContaining(keyword, pageable);
            userPage = usernamePage;
            if (status != null) {
                userPage = userRepository.findByUsernameContainingAndStatus(keyword, status, pageable);
            }
        } else if (status != null) {
            userPage = userRepository.findByStatus(status, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        return Result.success(PageResult.of(userPage.getTotalElements(), page, pageSize, userPage.getContent()));
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setPassword("******");
        return Result.success(user);
    }

    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User userUpdate) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (userUpdate.getNickname() != null) {
            user.setNickname(userUpdate.getNickname());
        }
        if (userUpdate.getEmail() != null) {
            user.setEmail(userUpdate.getEmail());
        }
        if (userUpdate.getPhone() != null) {
            user.setPhone(userUpdate.getPhone());
        }
        if (userUpdate.getGender() != null) {
            user.setGender(userUpdate.getGender());
        }
        if (userUpdate.getBirthDate() != null) {
            user.setBirthDate(userUpdate.getBirthDate());
        }
        if (userUpdate.getAddress() != null) {
            user.setAddress(userUpdate.getAddress());
        }

        return Result.success(userRepository.save(user));
    }

    @PutMapping("/{id}/status")
    public Result<User> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setStatus(status);
        return Result.success(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setIsDeleted(1);
        userRepository.save(user);
        return Result.success("删除成功");
    }

    @GetMapping("/stats")
    public Result<Object> getUserStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(1);
        long frozenUsers = userRepository.countByStatus(0);
        return Result.success(new Object() {
            public final long total = totalUsers;
            public final long active = activeUsers;
            public final long frozen = frozenUsers;
        });
    }
}
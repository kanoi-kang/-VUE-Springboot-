package com.example.backend.controller.api.user;

import com.example.backend.common.Result;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.LoginResponse;
import com.example.backend.dto.PasswordUpdateRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.UserVO;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Value("${server.port:8080}")
    private String serverPort;

    private static final String AVATAR_DIR = "uploads/avatars";
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif"};
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    @PostMapping("/auth/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request, HttpSession session) {
        UserVO user = userService.register(request, session);
        return Result.success(user);
    }

    @PostMapping("/auth/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        LoginResponse response = userService.login(request, session);
        return Result.success(response);
    }

    @GetMapping("/me")
    public Result<UserVO> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserVO user = userService.getUserInfo(userPrincipal.getUserId());
        return Result.success(user);
    }

    @PutMapping("/me")
    public Result<UserVO> updateCurrentUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UserVO userVO) {
        UserVO user = userService.updateUser(userPrincipal.getUserId(), userVO);
        return Result.success(user);
    }

    @PostMapping("/me/avatar")
    public Result<UserVO> uploadAvatar(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("avatar") MultipartFile file) {
        
        if (file.isEmpty()) {
            return Result.error("请选择要上传的图片");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("图片大小不能超过2MB");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename).toLowerCase();
        
        boolean isValidExtension = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (ext.equals(extension)) {
                isValidExtension = true;
                break;
            }
        }
        
        if (!isValidExtension) {
            return Result.error("只支持 JPG、PNG、GIF 格式的图片");
        }

        try {
            Path uploadPath = Paths.get(AVATAR_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String newFilename = UUID.randomUUID().toString() + "." + extension;
            Path filePath = uploadPath.resolve(newFilename);
            
            Files.copy(file.getInputStream(), filePath);

            String avatarUrl = "http://localhost:" + serverPort + "/" + AVATAR_DIR + "/" + newFilename;

            UserVO userVO = new UserVO();
            userVO.setAvatar(avatarUrl);
            UserVO user = userService.updateUser(userPrincipal.getUserId(), userVO);

            return Result.success(user);

        } catch (IOException e) {
            return Result.error("图片上传失败");
        }
    }

    @PutMapping("/password")
    public Result<?> updatePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(userPrincipal.getUserId(), request.getOldPassword(), request.getNewPassword());
        return Result.success("Password updated successfully");
    }

    @GetMapping("/users/{username}")
    public Result<UserVO> getUserByUsername(@PathVariable String username) {
        UserVO user = userService.getUserByUsername(username);
        return Result.success(user);
    }
}

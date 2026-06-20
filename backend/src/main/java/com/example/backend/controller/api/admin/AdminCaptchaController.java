package com.example.backend.controller.api.admin;

import com.example.backend.common.CaptchaGenerator;
import com.example.backend.common.CaptchaGenerator.CaptchaResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.io.IOException;

@RestController
@RequestMapping("/api/admin")
public class AdminCaptchaController {

    @GetMapping("/captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CaptchaResult result = CaptchaGenerator.generate();
        
        request.getSession().setAttribute("admin_captcha", result.getCode().toLowerCase());
        
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        ImageIO.write(result.getImage(), "png", response.getOutputStream());
    }
}
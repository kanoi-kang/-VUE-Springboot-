package com.example.backend.controller.api.user;

import com.example.backend.common.CaptchaGenerator;
import com.example.backend.common.CaptchaGenerator.CaptchaResult;
import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/api/user/auth")
public class CaptchaController {

    @GetMapping("/captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CaptchaResult result = CaptchaGenerator.generate();
        
        request.getSession().setAttribute("captcha", result.getCode().toLowerCase());
        
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        ImageIO.write(result.getImage(), "png", response.getOutputStream());
    }
}
package com.example.backend.config;

import com.example.backend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // 允许所有 OPTIONS 请求通过（CORS 预检请求）
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 公开接口 - 无需认证
                        .requestMatchers("/api/user/auth/**").permitAll()
                        .requestMatchers("/api/user/products/**").permitAll()
                        .requestMatchers("/api/user/categories/**").permitAll()
                        .requestMatchers("/api/user/brands/**").permitAll()
                        .requestMatchers("/api/user/reviews/product/**").permitAll()
                        .requestMatchers("/api/user/coupons/available").permitAll()
                        .requestMatchers("/api/goods/**").permitAll()
                        
                        // 管理端公开接口
                        .requestMatchers("/api/admin/login").permitAll()
                        .requestMatchers("/api/admin/captcha").permitAll()
                        
                        // Swagger文档
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        
                        // 需要管理员权限
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        
                        // 用户端其他接口需要认证
                        .requestMatchers("/api/user/**").authenticated()
                        
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许的来源
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",    // 用户端
                "http://localhost:5174",    // 管理端
                "http://localhost:5175",    // 用户端备用端口
                "http://localhost:8080"     // 本地测试
        ));
        // 允许的HTTP方法
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // 允许的请求头
        config.setAllowedHeaders(List.of("*"));
        // 允许携带凭证（如Cookie、Authorization）
        config.setAllowCredentials(true);
        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
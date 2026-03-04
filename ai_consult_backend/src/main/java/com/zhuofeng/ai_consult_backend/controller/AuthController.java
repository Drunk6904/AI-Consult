package com.zhuofeng.ai_consult_backend.controller;

import com.zhuofeng.ai_consult_backend.dto.ApiResponse;
import com.zhuofeng.ai_consult_backend.dto.auth.AuthResponse;
import com.zhuofeng.ai_consult_backend.dto.auth.LoginRequest;
import com.zhuofeng.ai_consult_backend.dto.auth.RegisterRequest;
import com.zhuofeng.ai_consult_backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 注册接口。
     */
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    /**
     * 登录接口。
     */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }
}

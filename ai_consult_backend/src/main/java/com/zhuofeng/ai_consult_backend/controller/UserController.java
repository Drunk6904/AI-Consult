package com.zhuofeng.ai_consult_backend.controller;

import com.zhuofeng.ai_consult_backend.dto.ApiResponse;
import com.zhuofeng.ai_consult_backend.dto.auth.UserInfo;
import com.zhuofeng.ai_consult_backend.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器。
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 获取当前用户信息接口。
     */
    @GetMapping("/me")
    public ApiResponse<UserInfo> me() {
        return ApiResponse.ok(authService.currentUser());
    }
}

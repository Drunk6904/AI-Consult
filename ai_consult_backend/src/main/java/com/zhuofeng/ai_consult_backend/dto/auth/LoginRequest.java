package com.zhuofeng.ai_consult_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求对象。
 */
@Data
public class LoginRequest {

    /**
     * 手机号。
     */
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /**
     * 明文密码。
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}

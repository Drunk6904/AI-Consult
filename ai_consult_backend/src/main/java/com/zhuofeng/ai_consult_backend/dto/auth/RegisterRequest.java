package com.zhuofeng.ai_consult_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求对象。
 */
@Data
public class RegisterRequest {

    /**
     * 手机号，示例校验为中国大陆手机号。
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 明文密码，后端会转为哈希保存。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度需在6到64之间")
    private String password;
}

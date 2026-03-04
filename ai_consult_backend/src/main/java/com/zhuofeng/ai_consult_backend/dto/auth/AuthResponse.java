package com.zhuofeng.ai_consult_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证成功返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * JWT令牌。
     */
    private String token;

    /**
     * 用户信息。
     */
    private UserInfo user;
}

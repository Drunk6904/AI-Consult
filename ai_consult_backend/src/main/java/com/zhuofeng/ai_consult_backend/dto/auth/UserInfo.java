package com.zhuofeng.ai_consult_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    /**
     * 用户ID。
     */
    private String userId;

    /**
     * 手机号。
     */
    private String phone;

    /**
     * 是否已注册用户。
     */
    private Boolean isRegistered;
}

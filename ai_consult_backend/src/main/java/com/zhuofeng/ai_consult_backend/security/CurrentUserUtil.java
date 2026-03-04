package com.zhuofeng.ai_consult_backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前用户工具类。
 */
public final class CurrentUserUtil {

    private CurrentUserUtil() {
    }

    /**
     * 获取当前登录用户对象。
     */
    public static AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("当前用户未登录或登录态无效");
        }
        return user;
    }

    /**
     * 获取当前用户ID。
     */
    public static String getCurrentUserId() {
        return getCurrentUser().getUserId();
    }
}

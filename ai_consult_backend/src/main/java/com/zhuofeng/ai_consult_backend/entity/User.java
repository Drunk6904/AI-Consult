package com.zhuofeng.ai_consult_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * 用户实体。
 * 说明：
 * 1. phone 为唯一登录标识；
 * 2. passwordHash 保存加密后的密码；
 * 3. isRegistered 用于权限分级。
 */
@Data
@Entity
@Table(name = "app_user")
public class User {

    /**
     * 用户ID（UUID字符串）。
     */
    @Id
    @Column(length = 36)
    private String id;

    /**
     * 手机号（唯一）。
     */
    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    /**
     * 密码哈希值（BCrypt）。
     */
    @Column(nullable = false, length = 255)
    private String passwordHash;

    /**
     * 是否注册用户。
     */
    @Column(nullable = false)
    private Boolean isRegistered;

    /**
     * 创建时间。
     */
    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (isRegistered == null) {
            isRegistered = Boolean.TRUE;
        }
    }
}

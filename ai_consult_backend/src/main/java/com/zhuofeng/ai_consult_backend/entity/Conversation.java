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
 * 会话实体。
 */
@Data
@Entity
@Table(name = "conversation")
public class Conversation {

    /**
     * 会话ID（UUID字符串）。
     */
    @Id
    @Column(length = 36)
    private String id;

    /**
     * 所属用户ID。
     */
    @Column(nullable = false, length = 36)
    private String userId;

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
    }
}

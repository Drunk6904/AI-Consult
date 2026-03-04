package com.zhuofeng.ai_consult_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;

/**
 * 消息实体。
 */
@Data
@Entity
@Table(name = "message")
public class Message {

    /**
     * 主键ID（自增）。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属会话ID。
     */
    @Column(nullable = false, length = 36)
    private String conversationId;

    /**
     * 发送方类型：user / ai。
     */
    @Column(nullable = false, length = 20)
    private String senderType;

    /**
     * 消息正文（TEXT）。
     */
    @Lob
    @Column(nullable = false)
    private String content;

    /**
     * AI消息来源JSON（sources[]），用户消息可为空。
     */
    @Lob
    @Column
    private String sourcesJson;

    /**
     * 创建时间。
     */
    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

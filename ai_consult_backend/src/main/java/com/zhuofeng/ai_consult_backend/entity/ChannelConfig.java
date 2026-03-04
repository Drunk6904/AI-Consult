package com.zhuofeng.ai_consult_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;

/**
 * 渠道配置实体（占位扩展）。
 */
@Data
@Entity
@Table(name = "channel_config")
public class ChannelConfig {

    /**
     * 渠道ID。
     */
    @Id
    @Column(length = 50)
    private String channelId;

    /**
     * 渠道类型：wechat/web/dingtalk。
     */
    @Column(nullable = false, length = 20)
    private String channelType;

    /**
     * 渠道名称。
     */
    @Column(nullable = false, length = 100)
    private String channelName;

    /**
     * webhook地址（可空）。
     */
    @Column(length = 500)
    private String webhookUrl;

    /**
     * 渠道状态：enabled/disabled。
     */
    @Column(nullable = false, length = 20)
    private String status;

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

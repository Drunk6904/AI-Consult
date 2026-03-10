package com.zhuofeng.ai_consult_backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 流式输出配置类
 * 
 * 用于管理 Dify Chatflow 流式输出的相关配置
 * 包括流式开关、超时时间、重试次数等
 */
@Component
public class StreamingConfig {

    @Value("${dify.streaming.enabled:enabled}")
    private String streamingEnabled;

    @Value("${dify.streaming.timeout:30}")
    private int streamingTimeout;

    @Value("${dify.streaming.retry.max:3}")
    private int streamingRetryMax;

    private static final String ENABLED = "enabled";
    private static final String DISABLED = "disabled";

    @PostConstruct
    public void validateConfig() {
        // 验证流式开关值
        if (streamingEnabled == null || streamingEnabled.trim().isEmpty()) {
            System.out.println("⚠ DIFY_STREAMING_ENABLED 未设置，使用默认值: enabled");
            streamingEnabled = ENABLED;
        } else {
            streamingEnabled = streamingEnabled.trim().toLowerCase();
            if (!ENABLED.equals(streamingEnabled) && !DISABLED.equals(streamingEnabled)) {
                System.err.println("⚠ DIFY_STREAMING_ENABLED 值无效: '" + streamingEnabled + "'，使用默认值: enabled");
                System.err.println("   有效值为: enabled, disabled");
                streamingEnabled = ENABLED;
            }
        }

        // 验证超时时间
        if (streamingTimeout <= 0) {
            System.err.println("⚠ DIFY_STREAMING_TIMEOUT 值无效: " + streamingTimeout + "，使用默认值: 30");
            streamingTimeout = 30;
        }

        // 验证重试次数
        if (streamingRetryMax < 0) {
            System.err.println("⚠ DIFY_STREAMING_RETRY_MAX 值无效: " + streamingRetryMax + "，使用默认值: 3");
            streamingRetryMax = 3;
        }

        System.out.println("========================================");
        System.out.println("流式输出配置加载成功:");
        System.out.println("  - 流式开关: " + streamingEnabled);
        System.out.println("  - 超时时间: " + streamingTimeout + " 秒");
        System.out.println("  - 最大重试: " + streamingRetryMax + " 次");
        System.out.println("========================================");
    }

    /**
     * 检查流式输出是否启用
     * @return true 如果流式输出启用
     */
    public boolean isStreamingEnabled() {
        return ENABLED.equals(streamingEnabled);
    }

    /**
     * 获取流式输出超时时间（秒）
     * @return 超时时间
     */
    public int getStreamingTimeout() {
        return streamingTimeout;
    }

    /**
     * 获取最大重试次数
     * @return 最大重试次数
     */
    public int getStreamingRetryMax() {
        return streamingRetryMax;
    }

    /**
     * 获取流式开关原始值
     * @return enabled 或 disabled
     */
    public String getStreamingEnabledRaw() {
        return streamingEnabled;
    }
}

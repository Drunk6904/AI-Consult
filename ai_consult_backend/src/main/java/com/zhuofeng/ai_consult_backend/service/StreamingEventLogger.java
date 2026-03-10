package com.zhuofeng.ai_consult_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class StreamingEventLogger {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    /**
     * 记录连接建立事件
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     */
    public void logConnectionEstablished(String sessionId, String userId) {
        String timestamp = TIMESTAMP_FORMATTER.format(Instant.now());
        log.info("[STREAMING] Connection established | sessionId={} | userId={} | timestamp={}",
                sessionId, userId, timestamp);
    }

    /**
     * 记录数据传输事件
     *
     * @param sessionId     会话ID
     * @param userId        用户ID
     * @param sequenceNumber 序列号
     * @param contentLength 内容长度
     */
    public void logDataTransmitted(String sessionId, String userId, long sequenceNumber, int contentLength) {
        String timestamp = TIMESTAMP_FORMATTER.format(Instant.now());
        log.info("[STREAMING] Data transmitted | sessionId={} | userId={} | sequence={} | contentLength={} | timestamp={}",
                sessionId, userId, sequenceNumber, contentLength, timestamp);
    }

    /**
     * 记录错误事件
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @param errorType 错误类型
     * @param message   错误消息
     */
    public void logError(String sessionId, String userId, String errorType, String message) {
        String timestamp = TIMESTAMP_FORMATTER.format(Instant.now());
        log.error("[STREAMING] Error occurred | sessionId={} | userId={} | errorType={} | message={} | timestamp={}",
                sessionId, userId, errorType, message, timestamp);
    }

    /**
     * 记录超时事件
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @param duration  持续时间（毫秒）
     */
    public void logTimeout(String sessionId, String userId, long duration) {
        String timestamp = TIMESTAMP_FORMATTER.format(Instant.now());
        log.warn("[STREAMING] Timeout occurred | sessionId={} | userId={} | duration={}ms | timestamp={}",
                sessionId, userId, duration, timestamp);
    }

    /**
     * 记录断开连接事件
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @param reason    断开原因
     */
    public void logDisconnection(String sessionId, String userId, String reason) {
        String timestamp = TIMESTAMP_FORMATTER.format(Instant.now());
        log.info("[STREAMING] Connection disconnected | sessionId={} | userId={} | reason={} | timestamp={}",
                sessionId, userId, reason, timestamp);
    }

    /**
     * 记录性能指标
     *
     * @param sessionId       会话ID
     * @param userId          用户ID
     * @param timeToFirstByte 首字节时间（毫秒）
     * @param totalDuration   总持续时间（毫秒）
     */
    public void logPerformanceMetrics(String sessionId, String userId, long timeToFirstByte, long totalDuration) {
        String timestamp = TIMESTAMP_FORMATTER.format(Instant.now());
        log.info("[STREAMING] Performance metrics | sessionId={} | userId={} | timeToFirstByte={}ms | totalDuration={}ms | timestamp={}",
                sessionId, userId, timeToFirstByte, totalDuration, timestamp);
    }
}

package com.zhuofeng.ai_consult_backend.service;

import com.zhuofeng.ai_consult_backend.config.StreamingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流式连接错误处理服务
 *
 * 用于处理流式输出连接中的错误，实现指数退避重试机制
 */
@Service
public class StreamingErrorHandler {

    @Autowired
    private StreamingConfig streamingConfig;

    private static final long BASE_DELAY_MS = 1000L;

    private final Map<String, RetryContext> retryContexts = new ConcurrentHashMap<>();

    /**
     * 重试上下文内部类
     * 用于跟踪重试状态
     */
    public static class RetryContext {
        private int attemptCount;
        private Throwable lastError;
        private Instant nextRetryTime;

        public RetryContext() {
            this.attemptCount = 0;
            this.lastError = null;
            this.nextRetryTime = Instant.now();
        }

        public int getAttemptCount() {
            return attemptCount;
        }

        public void setAttemptCount(int attemptCount) {
            this.attemptCount = attemptCount;
        }

        public Throwable getLastError() {
            return lastError;
        }

        public void setLastError(Throwable lastError) {
            this.lastError = lastError;
        }

        public Instant getNextRetryTime() {
            return nextRetryTime;
        }

        public void setNextRetryTime(Instant nextRetryTime) {
            this.nextRetryTime = nextRetryTime;
        }

        public void incrementAttempt() {
            this.attemptCount++;
        }
    }

    /**
     * 处理连接错误
     * 使用指数退避策略计算下次重试时间
     *
     * @param sessionId 会话ID
     * @param error     发生的错误
     * @return 更新后的重试上下文
     */
    public RetryContext handleConnectionError(String sessionId, Throwable error) {
        RetryContext context = getRetryContext(sessionId);
        context.setLastError(error);
        context.incrementAttempt();

        long backoffDelay = calculateBackoffDelay(context.getAttemptCount());
        context.setNextRetryTime(Instant.now().plusMillis(backoffDelay));

        System.out.println("Streaming connection error for session " + sessionId +
                ": " + error.getMessage() +
                ". Attempt " + context.getAttemptCount() +
                ". Next retry at: " + context.getNextRetryTime() +
                " (backoff: " + backoffDelay + "ms)");

        return context;
    }

    /**
     * 判断是否应进行重试
     * 基于最大重试次数限制
     *
     * @param sessionId 会话ID
     * @return true 如果应该重试，false 如果已达到最大重试次数
     */
    public boolean shouldRetry(String sessionId) {
        RetryContext context = retryContexts.get(sessionId);
        if (context == null) {
            return true;
        }
        return context.getAttemptCount() < streamingConfig.getStreamingRetryMax();
    }

    /**
     * 计算指数退避延迟时间
     * 基础延迟为1000ms，每次重试翻倍 (1000ms, 2000ms, 4000ms, ...)
     *
     * @param attemptCount 当前尝试次数
     * @return 延迟时间（毫秒）
     */
    public long calculateBackoffDelay(int attemptCount) {
        if (attemptCount <= 0) {
            return 0;
        }
        return BASE_DELAY_MS * (1L << (attemptCount - 1));
    }

    /**
     * 重置重试上下文
     * 用于新连接开始时清除之前的重试状态
     *
     * @param sessionId 会话ID
     */
    public void resetRetryContext(String sessionId) {
        retryContexts.remove(sessionId);
        System.out.println("Retry context reset for session: " + sessionId);
    }

    /**
     * 获取或创建重试上下文
     *
     * @param sessionId 会话ID
     * @return 该会话的重试上下文
     */
    public RetryContext getRetryContext(String sessionId) {
        return retryContexts.computeIfAbsent(sessionId, k -> {
            System.out.println("Creating new retry context for session: " + sessionId);
            return new RetryContext();
        });
    }

    /**
     * 移除重试上下文
     * 用于清理不再需要的会话状态
     *
     * @param sessionId 会话ID
     */
    public void removeRetryContext(String sessionId) {
        retryContexts.remove(sessionId);
    }

    /**
     * 获取当前所有重试上下文数量
     * 用于监控和调试
     *
     * @return 重试上下文数量
     */
    public int getRetryContextCount() {
        return retryContexts.size();
    }

    /**
     * 清除所有重试上下文
     * 用于系统重置或测试
     */
    public void clearAllRetryContexts() {
        retryContexts.clear();
        System.out.println("All retry contexts cleared");
    }

    /**
     * 处理流式错误
     * 根据错误类型和重试策略决定是否重试
     *
     * @param error     发生的错误
     * @param sessionId 会话ID
     * @return Flux 包含错误事件或空（如果可以重试）
     */
    public reactor.core.publisher.Flux<Map<String, Object>> handleError(Throwable error, String sessionId) {
        // 处理连接错误
        RetryContext context = handleConnectionError(sessionId, error);

        // 判断是否应重试
        if (shouldRetry(sessionId)) {
            System.out.println("Will retry streaming for session: " + sessionId +
                    ", attempt: " + context.getAttemptCount());
            // 返回空Flux，表示可以重试
            return reactor.core.publisher.Flux.empty();
        } else {
            System.out.println("Max retry attempts reached for session: " + sessionId);
            // 返回错误事件
            Map<String, Object> errorEvent = new java.util.HashMap<>();
            errorEvent.put("event", "error");
            errorEvent.put("error_type", error.getClass().getSimpleName());
            errorEvent.put("error_message", error.getMessage());
            errorEvent.put("session_id", sessionId);
            errorEvent.put("is_end", true);
            return reactor.core.publisher.Flux.just(errorEvent);
        }
    }
}

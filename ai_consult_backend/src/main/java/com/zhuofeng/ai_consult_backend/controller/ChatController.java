package com.zhuofeng.ai_consult_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.zhuofeng.ai_consult_backend.config.StreamingConfig;
import com.zhuofeng.ai_consult_backend.service.DifyService;
import com.zhuofeng.ai_consult_backend.service.StreamingEventLogger;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    private final DifyService difyService;
    private final StreamingConfig streamingConfig;
    private final StreamingEventLogger streamingEventLogger;

    public ChatController(DifyService difyService, StreamingConfig streamingConfig,
            StreamingEventLogger streamingEventLogger) {
        this.difyService = difyService;
        this.streamingConfig = streamingConfig;
        this.streamingEventLogger = streamingEventLogger;
    }

    @PostMapping("/completions")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String query = (String) request.get("query");
            String userId = (String) request.get("user");
            String sessionId = (String) request.get("session_id");
            String conversationId = (String) request.get("conversation_id");

            if (query == null || query.isEmpty()) {
                response.put("success", false);
                response.put("message", "Query cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }

            if (userId == null) {
                userId = "anonymous";
            }

            // 检查用户是否已注册（认证）
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isRegistered = authentication != null && authentication.isAuthenticated() &&
                    !(authentication.getPrincipal() instanceof String
                            && "anonymousUser".equals(authentication.getPrincipal()));
            log.info("User registration status: {}, Workflow type: {}", isRegistered, difyService.getWorkflowType());

            // 只传递有效的 UUID 格式 conversation_id 给 Dify
            // 前端传递的 session_id 是自定义格式，不能直接传给 Dify
            String difyConversationId = null;
            if (conversationId != null && !conversationId.isEmpty() && isValidUUID(conversationId)) {
                difyConversationId = conversationId;
            }

            // 使用统一的 executeChat 方法，自动根据 FEATURE_FLAG_WORKFLOW_TYPE 路由
            Map<String, Object> chatResponse = difyService.executeChat(
                    query, userId, difyConversationId, isRegistered, sessionId).block();

            log.info("Chat response received: {}", chatResponse);

            // 构建标准响应格式
            Map<String, Object> data = new HashMap<>();
            if (chatResponse != null) {
                // 从响应中提取信息
                // 返回的数据结构：{answer: "...", conversation_id: "...", ...}
                data.put("answer", chatResponse.get("answer"));
                data.put("conversationId", chatResponse.get("conversation_id"));
                data.put("messageId", chatResponse.get("message_id"));
                data.put("session_id", sessionId);
                data.put("is_registered", isRegistered); // 返回用户注册状态
                data.put("workflow_type", difyService.getWorkflowType()); // 返回当前使用的模式
            }

            response.put("success", true);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to process chat request", e);
            response.put("success", false);
            response.put("message", "Failed to process chat request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 流式聊天接口 - SSE 输出
     * 用于实时返回 AI 生成的回答
     */
    @PostMapping(value = "/completions/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Object>> chatStreaming(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        String userId = (String) request.get("user");
        String sessionId = (String) request.get("session_id");
        String conversationId = (String) request.get("conversation_id");

        if (query == null || query.isEmpty()) {
            Map<String, Object> errorEvent = new HashMap<>();
            errorEvent.put("event", "error");
            errorEvent.put("error_message", "Query cannot be empty");
            errorEvent.put("is_end", true);
            return Flux.just(errorEvent);
        }

        final String finalUserId = userId == null ? "anonymous" : userId;

        // 检查用户是否已注册（认证）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final boolean isRegistered = authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String
                        && "anonymousUser".equals(authentication.getPrincipal()));

        log.info("Streaming chat request - User: {}, Registered: {}, Session: {}", finalUserId, isRegistered,
                sessionId);

        // 只传递有效的 UUID 格式 conversation_id 给 Dify
        String difyConversationId = null;
        if (conversationId != null && !conversationId.isEmpty() && isValidUUID(conversationId)) {
            difyConversationId = conversationId;
        }
        final String finalDifyConversationId = difyConversationId;

        // 使用流式执行方法
        return difyService.executeChatStreaming(query, finalUserId, finalDifyConversationId, isRegistered, sessionId)
                .map(event -> {
                    // 添加用户信息到每个事件
                    event.put("user_id", finalUserId);
                    event.put("is_registered", isRegistered);
                    return event;
                })
                .onErrorResume(error -> {
                    log.error("Streaming error occurred", error);
                    streamingEventLogger.logError(sessionId, finalUserId, "CONTROLLER_ERROR", error.getMessage());

                    Map<String, Object> errorEvent = new HashMap<>();
                    errorEvent.put("event", "error");
                    errorEvent.put("error_message", error.getMessage());
                    errorEvent.put("user_id", finalUserId);
                    errorEvent.put("is_registered", isRegistered);
                    errorEvent.put("is_end", true);
                    return Flux.just(errorEvent);
                });
    }

    /**
     * 流式聊天接口 - SseEmitter 版本（兼容性更好）
     * 用于实时返回 AI 生成的回答
     */
    @PostMapping(value = "/completions/stream-emitter")
    public SseEmitter chatStreamingEmitter(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        String userId = (String) request.get("user");
        String sessionId = (String) request.get("session_id");
        String conversationId = (String) request.get("conversation_id");

        if (userId == null) {
            userId = "anonymous";
        }

        // 检查用户是否已注册（认证）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isRegistered = authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String
                        && "anonymousUser".equals(authentication.getPrincipal()));

        // 只传递有效的 UUID 格式 conversation_id 给 Dify
        String difyConversationId = null;
        if (conversationId != null && !conversationId.isEmpty() && isValidUUID(conversationId)) {
            difyConversationId = conversationId;
        }

        // 创建 SseEmitter，设置超时时间
        long timeout = streamingConfig.getStreamingTimeout() * 1000L + 10000L; // 额外10秒缓冲
        SseEmitter emitter = new SseEmitter(timeout);

        String finalUserId = userId;
        String finalSessionId = sessionId;
        String finalDifyConversationId = difyConversationId;

        // 异步处理流式响应
        Flux<Map<String, Object>> stream = difyService.executeChatStreaming(
                query, finalUserId, finalDifyConversationId, isRegistered, finalSessionId);

        AtomicBoolean isCompleted = new AtomicBoolean(false);

        stream.subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        event -> {
                            if (isCompleted.get())
                                return;
                            try {
                                // 添加用户信息
                                event.put("user_id", finalUserId);
                                event.put("is_registered", isRegistered);
                                emitter.send(event);

                                // 检查是否是结束事件
                                if (Boolean.TRUE.equals(event.get("is_end"))) {
                                    isCompleted.set(true);
                                    emitter.complete();
                                }
                            } catch (IOException e) {
                                log.error("Error sending SSE event", e);
                                isCompleted.set(true);
                                emitter.completeWithError(e);
                            }
                        },
                        error -> {
                            log.error("Streaming error", error);
                            if (!isCompleted.get()) {
                                try {
                                    Map<String, Object> errorEvent = new HashMap<>();
                                    errorEvent.put("event", "error");
                                    errorEvent.put("error_message", error.getMessage());
                                    errorEvent.put("user_id", finalUserId);
                                    errorEvent.put("is_registered", isRegistered);
                                    errorEvent.put("is_end", true);
                                    emitter.send(errorEvent);
                                } catch (IOException e) {
                                    log.error("Error sending error event", e);
                                }
                                isCompleted.set(true);
                                emitter.completeWithError(error);
                            }
                        },
                        () -> {
                            if (!isCompleted.get()) {
                                isCompleted.set(true);
                                emitter.complete();
                            }
                        });

        // 处理客户端断开连接
        emitter.onCompletion(() -> {
            log.info("SSE connection completed for session: {}", finalSessionId);
            streamingEventLogger.logDisconnection(finalSessionId, finalUserId, "completed");
        });

        emitter.onTimeout(() -> {
            log.warn("SSE connection timeout for session: {}", finalSessionId);
            streamingEventLogger.logTimeout(finalSessionId, finalUserId,
                    streamingConfig.getStreamingTimeout() * 1000L);
        });

        emitter.onError((error) -> {
            log.error("SSE connection error for session: {}", finalSessionId, error);
            streamingEventLogger.logError(finalSessionId, finalUserId, "SSE_ERROR", error.getMessage());
        });

        return emitter;
    }

    /**
     * 获取当前工作流类型配置
     */
    @PostMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("workflow_type", difyService.getWorkflowType());
            data.put("supported_types", new String[]{"workflow", "chatflow"});
            data.put("streaming_enabled", streamingConfig.isStreamingEnabled());
            data.put("streaming_timeout", streamingConfig.getStreamingTimeout());
            data.put("streaming_retry_max", streamingConfig.getStreamingRetryMax());
            
            response.put("success", true);
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get config", e);
            response.put("success", false);
            response.put("message", "Failed to get config: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 验证字符串是否为有效的 UUID 格式
     */
    private boolean isValidUUID(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return false;
        }
        try {
            java.util.UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

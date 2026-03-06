package com.zhuofeng.ai_consult_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhuofeng.ai_consult_backend.service.DifyService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    private final DifyService difyService;

    public ChatController(DifyService difyService) {
        this.difyService = difyService;
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
                                  !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()));
            log.info("User registration status: {}", isRegistered);

            // 只传递有效的 UUID 格式 conversation_id 给 Dify
            // 前端传递的 session_id 是自定义格式，不能直接传给 Dify
            String difyConversationId = null;
            if (conversationId != null && !conversationId.isEmpty() && isValidUUID(conversationId)) {
                difyConversationId = conversationId;
            }
            
            // 如果提供了有效的会话 ID，则使用带会话 ID 的 workflow 方法
            Map<String, Object> workflowResponse;
            if (difyConversationId != null && !difyConversationId.isEmpty()) {
                workflowResponse = difyService.workflow(query, userId, difyConversationId, isRegistered).block();
            } else {
                workflowResponse = difyService.workflow(query, userId, isRegistered).block();
            }
            
            log.info("Workflow response received: {}", workflowResponse);

            // 构建标准响应格式
            Map<String, Object> data = new HashMap<>();
            if (workflowResponse != null) {
                // 从 Workflow 响应中提取信息
                // Workflow 返回的数据结构：{answer: "...", conversation_id: "...", ...}
                data.put("answer", workflowResponse.get("answer"));
                data.put("conversationId", workflowResponse.get("conversation_id"));
                data.put("messageId", workflowResponse.get("message_id"));
                data.put("session_id", sessionId);
                data.put("is_registered", isRegistered); // 返回用户注册状态
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

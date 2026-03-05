package com.zhuofeng.ai_consult_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

            if (query == null || query.isEmpty()) {
                response.put("success", false);
                response.put("message", "Query cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }

            if (userId == null) {
                userId = "anonymous";
            }

            Map<String, Object> chatflowResponse = difyService.chatflow(query, userId).block();
            log.info("Chatflow response received: {}", chatflowResponse);

            // 构建标准响应格式
            Map<String, Object> data = new HashMap<>();
            if (chatflowResponse != null) {
                // 从 Chatflow 响应中提取信息
                // Chatflow 返回的数据结构：{answer: "...", conversation_id: "...", ...}
                data.put("answer", chatflowResponse.get("answer"));
                data.put("conversationId", chatflowResponse.get("conversation_id"));
                data.put("messageId", chatflowResponse.get("message_id"));
                data.put("session_id", request.get("session_id"));
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
}

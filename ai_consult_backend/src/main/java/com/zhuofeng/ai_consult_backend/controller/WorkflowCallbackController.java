package com.zhuofeng.ai_consult_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/api/v1/workflow")
public class WorkflowCallbackController {

    // 用于存储工作流结果的临时缓存
    private static final Map<String, Map<String, Object>> workflowResults = new ConcurrentHashMap<>();

    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> workflowCallback(@RequestBody Map<String, Object> payload) {
        log.info("Received workflow callback: {}", payload);
        
        // 处理工作流返回的结果
        String sessionId = (String) payload.get("session_id");
        if (sessionId == null) {
            log.error("Missing session_id in callback payload");
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Missing session_id"));
        }
        
        // 存储结果到缓存
        workflowResults.put(sessionId, payload);
        log.info("Stored workflow result for session: {}", sessionId);
        
        // 返回成功响应
        return ResponseEntity.ok(Map.of("status", "success", "message", "Callback received"));
    }

    @PostMapping("/result")
    public ResponseEntity<Map<String, Object>> getWorkflowResult(@RequestBody Map<String, Object> request) {
        String sessionId = (String) request.get("session_id");
        if (sessionId == null) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Missing session_id"));
        }
        
        // 从缓存中获取结果
        Map<String, Object> result = workflowResults.get(sessionId);
        if (result == null) {
            return ResponseEntity.ok(Map.of("status", "pending", "message", "Result not ready yet"));
        }
        
        // 获取结果后从缓存中移除
        workflowResults.remove(sessionId);
        
        return ResponseEntity.ok(Map.of("status", "success", "data", result));
    }
}

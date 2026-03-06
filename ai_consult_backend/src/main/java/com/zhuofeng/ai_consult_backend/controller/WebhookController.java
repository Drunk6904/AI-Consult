package com.zhuofeng.ai_consult_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.zhuofeng.ai_consult_backend.service.DifyService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhook")
public class WebhookController {
    private final DifyService difyService;
    
    @Value("${dify.webhook.url}")
    private String webhookUrl;
    
    @Value("${dify.webhook.debug.url}")
    private String webhookDebugUrl;

    public WebhookController(DifyService difyService) {
        this.difyService = difyService;
    }

    /**
     * 获取 Webhook 配置
     * 
     * @return Webhook 配置信息
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getWebhookConfig() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("webhookUrl", webhookUrl);
            config.put("webhookDebugUrl", webhookDebugUrl);
            
            response.put("success", true);
            response.put("data", config);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get webhook config", e);
            response.put("success", false);
            response.put("message", "Failed to get webhook config: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 测试 Webhook 连接
     * 
     * @param isDebug 是否使用调试 URL
     * @return 测试结果
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testWebhook(@RequestParam(value = "isDebug", defaultValue = "false") boolean isDebug) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 构建测试 payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("test", true);
            payload.put("message", "This is a test webhook request");
            payload.put("timestamp", System.currentTimeMillis());
            
            // 发送测试 Webhook
            Map<String, Object> webhookResponse = difyService.sendWebhook(payload, isDebug).block();
            
            response.put("success", true);
            response.put("message", "Webhook test completed");
            response.put("response", webhookResponse);
            response.put("url", isDebug ? webhookDebugUrl : webhookUrl);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to test webhook", e);
            response.put("success", false);
            response.put("message", "Failed to test webhook: " + e.getMessage());
            response.put("url", isDebug ? webhookDebugUrl : webhookUrl);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 发送自定义 Webhook 请求
     * 
     * @param payload Webhook  payload
     * @param isDebug 是否使用调试 URL
     * @return Webhook 响应
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendWebhook(@RequestBody Map<String, Object> payload, 
                                                         @RequestParam(value = "isDebug", defaultValue = "false") boolean isDebug) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (payload == null || payload.isEmpty()) {
                response.put("success", false);
                response.put("message", "Payload cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 发送 Webhook
            Map<String, Object> webhookResponse = difyService.sendWebhook(payload, isDebug).block();
            
            response.put("success", true);
            response.put("message", "Webhook sent successfully");
            response.put("response", webhookResponse);
            response.put("url", isDebug ? webhookDebugUrl : webhookUrl);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to send webhook", e);
            response.put("success", false);
            response.put("message", "Failed to send webhook: " + e.getMessage());
            response.put("url", isDebug ? webhookDebugUrl : webhookUrl);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

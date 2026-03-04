package com.zhuofeng.ai_consult_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Map<String, Boolean> healthCheck() {
        Map<String, Boolean> response = new HashMap<>();
        response.put("ok", true);
        return response;
    }
}

package com.zhuofeng.ai_consult_backend.service;

import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    private final String SECRET_KEY = "your-secret-key-for-jwt-token-generation";
    private final long EXPIRATION_TIME = 86400000; // 24 hours

    public String generateToken(String username) {
        // 简化的JWT生成实现
        // 实际项目中应该使用标准的JWT库
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("iat", System.currentTimeMillis());
        claims.put("exp", System.currentTimeMillis() + EXPIRATION_TIME);

        // 简单的Base64编码，实际项目中应该使用标准的JWT签名
        String header = Base64.getEncoder().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
        String payload = Base64.getEncoder().encodeToString(claims.toString().getBytes());
        String signature = Base64.getEncoder().encodeToString((header + "." + payload + "." + SECRET_KEY).getBytes());

        return header + "." + payload + "." + signature;
    }

    public String extractUsername(String token) {
        // 简化的JWT解析实现
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(Base64.getDecoder().decode(parts[1]));
                // 简单解析，实际项目中应该使用标准的JWT库
                if (payload.contains("sub")) {
                    int start = payload.indexOf("sub") + 5;
                    int end = payload.indexOf(",", start);
                    if (end == -1) end = payload.length() - 1;
                    return payload.substring(start, end).replace('"', ' ').trim();
                }
            }
        } catch (Exception e) {
            // 解析失败
        }
        return null;
    }

    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername != null && extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        // 简化的过期检查
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(Base64.getDecoder().decode(parts[1]));
                if (payload.contains("exp")) {
                    int start = payload.indexOf("exp") + 5;
                    int end = payload.indexOf(",", start);
                    if (end == -1) end = payload.length() - 1;
                    long exp = Long.parseLong(payload.substring(start, end).trim());
                    return exp < System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            // 解析失败
        }
        return true;
    }
}
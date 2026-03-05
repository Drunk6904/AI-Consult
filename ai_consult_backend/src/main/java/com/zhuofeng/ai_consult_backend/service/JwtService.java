package com.zhuofeng.ai_consult_backend.service;

import com.zhuofeng.ai_consult_backend.model.User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtService {
    private final String SECRET_KEY = "your-secret-key-change-in-production";
    private final long EXPIRATION_TIME = 7200000; // 2 hours

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getUsername());
        claims.put("userId", user.getId());
        claims.put("iat", System.currentTimeMillis());
        claims.put("exp", System.currentTimeMillis() + EXPIRATION_TIME);
        
        // 添加角色信息
        List<String> roles = user.getRoles().stream()
            .map(role -> role.getRoleCode())
            .collect(Collectors.toList());
        claims.put("roles", roles);
        
        // 添加权限信息
        Set<String> permissions = new HashSet<>();
        user.getRoles().forEach(role -> {
            role.getPermissions().forEach(permission -> {
                permissions.add(permission.getPermissionCode());
            });
        });
        claims.put("permissions", permissions);

        // Base64 编码 JWT
        String header = Base64.getEncoder().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
        String payload = Base64.getEncoder().encodeToString(claims.toString().getBytes());
        String signature = Base64.getEncoder().encodeToString((header + "." + payload + "." + SECRET_KEY).getBytes());

        return header + "." + payload + "." + signature;
    }

    public String extractUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(Base64.getDecoder().decode(parts[1]));
                // 解析 payload 字符串，格式类似：{sub=admin, userId=1, iat=1234567890, exp=1234567890, roles=[ADMIN], permissions=[...]}
                int subStart = payload.indexOf("sub=");
                if (subStart != -1) {
                    subStart += 4; // "sub=" 的长度
                    int subEnd = payload.indexOf(",", subStart);
                    if (subEnd == -1) subEnd = payload.indexOf("}", subStart);
                    if (subEnd != -1) {
                        return payload.substring(subStart, subEnd).trim();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("JWT 解析失败：" + e.getMessage());
        }
        return null;
    }

    public List<String> extractRoles(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(Base64.getDecoder().decode(parts[1]));
                if (payload.contains("roles")) {
                    int start = payload.indexOf("roles");
                    int end = payload.indexOf("]", start);
                    if (end != -1) {
                        String rolesStr = payload.substring(start, end + 1);
                        // 简单解析角色列表
                        List<String> roles = new ArrayList<>();
                        int quoteStart = rolesStr.indexOf("[");
                        int quoteEnd = rolesStr.lastIndexOf("]");
                        if (quoteStart != -1 && quoteEnd != -1) {
                            String content = rolesStr.substring(quoteStart + 1, quoteEnd);
                            String[] roleArray = content.split(",");
                            for (String role : roleArray) {
                                roles.add(role.trim().replace("\"", ""));
                            }
                        }
                        return roles;
                    }
                }
            }
        } catch (Exception e) {
            // 解析失败
        }
        return new ArrayList<>();
    }

    public Set<String> extractPermissions(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(Base64.getDecoder().decode(parts[1]));
                if (payload.contains("permissions")) {
                    int start = payload.indexOf("permissions");
                    int end = payload.indexOf("]", start);
                    if (end != -1) {
                        String permsStr = payload.substring(start, end + 1);
                        // 简单解析权限集合
                        Set<String> permissions = new HashSet<>();
                        int braceStart = permsStr.indexOf("[");
                        int braceEnd = permsStr.lastIndexOf("]");
                        if (braceStart != -1 && braceEnd != -1) {
                            String content = permsStr.substring(braceStart + 1, braceEnd);
                            String[] permArray = content.split(",");
                            for (String perm : permArray) {
                                permissions.add(perm.trim().replace("\"", ""));
                            }
                        }
                        return permissions;
                    }
                }
            }
        } catch (Exception e) {
            // 解析失败
        }
        return new HashSet<>();
    }

    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        System.out.println("JWT 验证 - 提取的用户名：" + extractedUsername + ", 期望用户名：" + username);
        boolean expired = isTokenExpired(token);
        System.out.println("JWT 验证 - Token 是否过期：" + expired);
        boolean valid = (extractedUsername != null && extractedUsername.equals(username) && !expired);
        System.out.println("JWT 验证 - 最终结果：" + valid);
        return valid;
    }

    private boolean isTokenExpired(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(Base64.getDecoder().decode(parts[1]));
                System.out.println("JWT Payload: " + payload);
                if (payload.contains("exp")) {
                    int start = payload.indexOf("exp=") + 4; // "exp=" 的长度是 4
                    int end = payload.indexOf(",", start);
                    if (end == -1) end = payload.indexOf("}", start);
                    if (end == -1) end = payload.length() - 1;
                    String expStr = payload.substring(start, end).trim();
                    System.out.println("exp 字符串：" + expStr);
                    long exp = Long.parseLong(expStr);
                    System.out.println("过期时间：" + exp + ", 当前时间：" + System.currentTimeMillis());
                    boolean isExpired = exp < System.currentTimeMillis();
                    System.out.println("Token 是否过期：" + isExpired);
                    return isExpired;
                }
            }
        } catch (Exception e) {
            System.err.println("检查 Token 过期失败：" + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }
}
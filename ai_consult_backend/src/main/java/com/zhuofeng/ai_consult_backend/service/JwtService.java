package com.zhuofeng.ai_consult_backend.service;

import com.zhuofeng.ai_consult_backend.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.expiration.time:7200000}")
    private long expirationTime;

    private static final long DEFAULT_EXPIRATION_TIME = 7200000; // 2 hours
    private static final int MIN_SECRET_KEY_LENGTH = 32;

    @PostConstruct
    public void validateConfig() {
        // 验证 JWT 密钥是否设置
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalStateException(
                    "JWT 配置错误：jwt.secret.key 未设置。" +
                            "请在 .env 文件中设置 JWT_SECRET_KEY 环境变量，" +
                            "或使用命令：export JWT_SECRET_KEY=your-secret-key");
        }

        // 验证 JWT 密钥长度
        if (secretKey.length() < MIN_SECRET_KEY_LENGTH) {
            throw new IllegalStateException(
                    String.format(
                            "JWT 配置错误：jwt.secret.key 长度不足。当前长度：%d，要求至少 %d 个字符。" +
                                    "请使用更长的密钥，建议使用随机生成的复杂字符串（如：openssl rand -base64 32）",
                            secretKey.length(),
                            MIN_SECRET_KEY_LENGTH));
        }

        // 验证过期时间
        if (expirationTime <= 0) {
            System.err.println("JWT 配置警告：jwt.expiration.time 设置无效，使用默认值 7200000ms（2小时）");
            expirationTime = DEFAULT_EXPIRATION_TIME;
        }

        System.out.println("JWT 配置验证通过：密钥长度=" + secretKey.length() + "，过期时间=" + expirationTime + "ms");
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getUsername());
        claims.put("userId", user.getId());
        claims.put("iat", System.currentTimeMillis());
        claims.put("exp", System.currentTimeMillis() + expirationTime);

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
        String signature = Base64.getEncoder().encodeToString((header + "." + payload + "." + secretKey).getBytes());

        return header + "." + payload + "." + signature;
    }

    public String extractUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(Base64.getDecoder().decode(parts[1]));
                // 解析 payload 字符串，格式类似：{sub=admin, userId=1, iat=1234567890, exp=1234567890,
                // roles=[ADMIN], permissions=[...]}
                int subStart = payload.indexOf("sub=");
                if (subStart != -1) {
                    subStart += 4; // "sub=" 的长度
                    int subEnd = payload.indexOf(",", subStart);
                    if (subEnd == -1)
                        subEnd = payload.indexOf("}", subStart);
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

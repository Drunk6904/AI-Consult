package com.zhuofeng.ai_consult_backend.security;

import com.zhuofeng.ai_consult_backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT工具类。
 * 负责生成、解析与校验令牌。
 */
@Component
public class JwtUtil {

    /**
     * JWT签名密钥。
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * JWT有效期（分钟）。
     */
    @Value("${jwt.expireMinutes:1440}")
    private long expireMinutes;

    /**
     * 生成JWT（包含userId、phone、isRegistered）。
     */
    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(expireMinutes, ChronoUnit.MINUTES);
        return Jwts.builder()
                .subject(user.getId())
                .claim("phone", user.getPhone())
                .claim("isRegistered", Boolean.TRUE.equals(user.getIsRegistered()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析用户ID。
     */
    public String getUserId(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 校验token是否有效。
     */
    public boolean isValid(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            return expiration != null && expiration.after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}

package com.zhuofeng.ai_consult_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局CORS配置。
 * 说明：
 * 1. 该配置用于普通MVC链路跨域；
 * 2. 安全链路中的跨域由 SecurityConfig 中的 CorsConfigurationSource 提供；
 * 3. 两者使用同一配置项，避免冲突。
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 允许跨域来源，支持逗号分隔多个域名。
     */
    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        registry.addMapping("/**")
                .allowedOrigins(origins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

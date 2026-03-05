package com.zhuofeng.ai_consult_backend.config;

import com.zhuofeng.ai_consult_backend.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors
                        .configurationSource(request -> {
                            org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
                            config.setAllowedOriginPatterns(
                                    java.util.Arrays.asList("http://localhost:*", "http://127.0.0.1:*"));
                            config.setAllowedMethods(
                                    java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                            config.setAllowedHeaders(java.util.Arrays.asList("*"));
                            config.setAllowCredentials(true);
                            config.setMaxAge(3600L);
                            return config;
                        }))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 公开接口
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/health/**").permitAll()
                        // 需要认证的接口（临时放宽用于调试）
                        .requestMatchers("/api/v1/roles/**").authenticated()
                        .requestMatchers("/api/v1/permissions/**").authenticated()
                        .requestMatchers("/api/v1/users/**").authenticated()
                        // 知识库和聊天功能需要认证即可
                        .requestMatchers("/api/v1/knowledge/**").authenticated()
                        .requestMatchers("/api/v1/chat/**").authenticated()
                        // 其他接口需要认证
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

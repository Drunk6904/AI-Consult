package com.zhuofeng.ai_consult_backend.filter;

import com.zhuofeng.ai_consult_backend.service.JwtService;
import com.zhuofeng.ai_consult_backend.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        System.out.println("========== JWT 过滤器开始执行 ==========");
        System.out.println("请求 URL: " + request.getRequestURI());
        
        // 从请求头中获取 JWT token
        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authHeader);
        
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("Token: " + token);
            
            String username = jwtService.extractUsername(token);
            System.out.println("从 JWT 中提取的用户名：" + username);
            
            if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println("开始加载用户详情：" + username);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                System.out.println("用户详情加载成功：" + username);
                System.out.println("用户权限：" + userDetails.getAuthorities());
                
                if (jwtService.validateToken(token, username)) {
                    System.out.println("JWT Token 验证成功");
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("安全上下文已设置，认证对象：" + authToken);
                } else {
                    System.out.println("JWT Token 验证失败");
                }
            } else {
                System.out.println("用户名为空或安全上下文已存在");
            }
        } else {
            System.out.println("Authorization Header 不存在或格式不正确");
        }
        
        System.out.println("========== JWT 过滤器执行完成 ==========");
        filterChain.doFilter(request, response);
    }
}

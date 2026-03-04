package com.zhuofeng.ai_consult_backend.service;

import com.zhuofeng.ai_consult_backend.dto.auth.AuthResponse;
import com.zhuofeng.ai_consult_backend.dto.auth.LoginRequest;
import com.zhuofeng.ai_consult_backend.dto.auth.RegisterRequest;
import com.zhuofeng.ai_consult_backend.dto.auth.UserInfo;
import com.zhuofeng.ai_consult_backend.entity.User;
import com.zhuofeng.ai_consult_backend.repository.UserRepository;
import com.zhuofeng.ai_consult_backend.security.CurrentUserUtil;
import com.zhuofeng.ai_consult_backend.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务。
 * 负责注册、登录、获取当前用户信息。
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 注册：
     * 1. 校验手机号唯一；
     * 2. 使用BCrypt哈希密码；
     * 3. 保存用户并签发JWT。
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        userRepository.findByPhone(request.getPhone()).ifPresent(u -> {
            throw new IllegalArgumentException("手机号已注册");
        });

        User user = new User();
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsRegistered(Boolean.TRUE);
        User saved = userRepository.save(user);

        String token = jwtUtil.generateToken(saved);
        return new AuthResponse(token, toUserInfo(saved));
    }

    /**
     * 登录：
     * 1. 查询用户；
     * 2. 校验密码；
     * 3. 返回JWT与用户信息。
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new IllegalArgumentException("账号或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("账号或密码错误");
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, toUserInfo(user));
    }

    /**
     * 获取当前登录用户信息。
     */
    @Transactional(readOnly = true)
    public UserInfo currentUser() {
        String userId = CurrentUserUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return toUserInfo(user);
    }

    private UserInfo toUserInfo(User user) {
        return new UserInfo(user.getId(), user.getPhone(), user.getIsRegistered());
    }
}

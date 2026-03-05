package com.zhuofeng.ai_consult_backend.service;

import com.zhuofeng.ai_consult_backend.model.Role;
import com.zhuofeng.ai_consult_backend.model.User;
import com.zhuofeng.ai_consult_backend.repository.RoleRepository;
import com.zhuofeng.ai_consult_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setStatus(1);
        
        // 默认分配普通用户角色
        Optional<Role> userRole = roleRepository.findByRoleCode("USER");
        if (userRole.isPresent()) {
            user.setRoles(new HashSet<>(java.util.Set.of(userRole.get())));
        }

        return userRepository.save(user);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        
        if (user.getStatus() == 0) {
            throw new RuntimeException("User account is disabled");
        }

        return user;
    }
}

package com.zhuofeng.ai_consult_backend.service;

import com.zhuofeng.ai_consult_backend.model.Role;
import com.zhuofeng.ai_consult_backend.model.User;
import com.zhuofeng.ai_consult_backend.repository.RoleRepository;
import com.zhuofeng.ai_consult_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User save(User user) {
        // 加密密码
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public User assignRoles(Long userId, Set<Long> roleIds) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Set<Role> roles = new java.util.HashSet<>(roleRepository.findAllById(roleIds));
        user.setRoles(roles);
        
        return userRepository.save(user);
    }

    public User addRoles(Long userId, Set<Long> roleIds) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Set<Role> existingRoles = user.getRoles();
        Set<Role> newRoles = new java.util.HashSet<>(roleRepository.findAllById(roleIds));
        existingRoles.addAll(newRoles);
        user.setRoles(existingRoles);
        
        return userRepository.save(user);
    }

    public User removeRoles(Long userId, Set<Long> roleIds) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Set<Role> existingRoles = user.getRoles();
        existingRoles.removeIf(role -> roleIds.contains(role.getId()));
        user.setRoles(existingRoles);
        
        return userRepository.save(user);
    }

    public User updateStatus(Long userId, Integer status) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setStatus(status);
        return userRepository.save(user);
    }
}

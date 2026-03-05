package com.zhuofeng.ai_consult_backend.controller;

import com.zhuofeng.ai_consult_backend.model.User;
import com.zhuofeng.ai_consult_backend.service.JwtService;
import com.zhuofeng.ai_consult_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUsers() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<User> users = userService.findAll();
            response.put("success", true);
            response.put("data", users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            response.put("success", true);
            response.put("data", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userService.findByUsername(user.getUsername()).isPresent()) {
                response.put("success", false);
                response.put("message", "用户名已存在");
                return ResponseEntity.badRequest().body(response);
            }
            User savedUser = userService.save(user);
            response.put("success", true);
            response.put("data", savedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            User existingUser = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            existingUser.setPhone(user.getPhone());
            existingUser.setAvatar(user.getAvatar());
            existingUser.setStatus(user.getStatus());
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                existingUser.setPassword(user.getPassword());
            }
            User updatedUser = userService.save(existingUser);
            response.put("success", true);
            response.put("data", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.deleteById(id);
            response.put("success", true);
            response.put("message", "用户删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<Map<String, Object>> assignRoles(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        try {
            @SuppressWarnings("unchecked")
            List<Integer> roleIds = (List<Integer>) requestBody.get("roleIds");
            Set<Long> roleIdsLong = roleIds.stream().map(Long::valueOf).collect(Collectors.toSet());

            User user = userService.assignRoles(id, roleIdsLong);
            response.put("success", true);
            response.put("data", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer status = (Integer) requestBody.get("status");
            User user = userService.updateStatus(id, status);
            response.put("success", true);
            response.put("data", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

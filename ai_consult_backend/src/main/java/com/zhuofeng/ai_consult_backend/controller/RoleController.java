package com.zhuofeng.ai_consult_backend.controller;

import com.zhuofeng.ai_consult_backend.model.Role;
import com.zhuofeng.ai_consult_backend.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getRoles() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Role> roles = roleService.findAll();
            response.put("success", true);
            response.put("data", roles);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRole(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Role role = roleService.findById(id).orElseThrow(() -> new RuntimeException("角色不存在"));
            response.put("success", true);
            response.put("data", role);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody Role role) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (roleService.existsByRoleCode(role.getRoleCode())) {
                response.put("success", false);
                response.put("message", "角色代码已存在");
                return ResponseEntity.badRequest().body(response);
            }
            Role savedRole = roleService.save(role);
            response.put("success", true);
            response.put("data", savedRole);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRole(@PathVariable Long id, @RequestBody Role role) {
        Map<String, Object> response = new HashMap<>();
        try {
            Role existingRole = roleService.findById(id).orElseThrow(() -> new RuntimeException("角色不存在"));
            existingRole.setRoleName(role.getRoleName());
            existingRole.setRoleCode(role.getRoleCode());
            existingRole.setDescription(role.getDescription());
            existingRole.setStatus(role.getStatus());
            Role updatedRole = roleService.save(existingRole);
            response.put("success", true);
            response.put("data", updatedRole);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            roleService.deleteById(id);
            response.put("success", true);
            response.put("message", "角色删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

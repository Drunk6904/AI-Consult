package com.zhuofeng.ai_consult_backend.controller;

import com.zhuofeng.ai_consult_backend.model.Permission;
import com.zhuofeng.ai_consult_backend.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/permissions")
@CrossOrigin(origins = "*")
public class PermissionController {
    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getPermissions() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Permission> permissions = permissionService.findAll();
            response.put("success", true);
            response.put("data", permissions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPermission(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Permission permission = permissionService.findById(id)
                .orElseThrow(() -> new RuntimeException("权限不存在"));
            response.put("success", true);
            response.put("data", permission);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPermission(@RequestBody Permission permission) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (permissionService.existsByPermissionCode(permission.getPermissionCode())) {
                response.put("success", false);
                response.put("message", "权限代码已存在");
                return ResponseEntity.badRequest().body(response);
            }
            Permission savedPermission = permissionService.save(permission);
            response.put("success", true);
            response.put("data", savedPermission);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePermission(@PathVariable Long id, @RequestBody Permission permission) {
        Map<String, Object> response = new HashMap<>();
        try {
            Permission existingPermission = permissionService.findById(id)
                .orElseThrow(() -> new RuntimeException("权限不存在"));
            existingPermission.setPermissionName(permission.getPermissionName());
            existingPermission.setPermissionCode(permission.getPermissionCode());
            existingPermission.setResourceType(permission.getResourceType());
            existingPermission.setParentId(permission.getParentId());
            existingPermission.setPath(permission.getPath());
            existingPermission.setIcon(permission.getIcon());
            existingPermission.setSortOrder(permission.getSortOrder());
            existingPermission.setStatus(permission.getStatus());
            Permission updatedPermission = permissionService.save(existingPermission);
            response.put("success", true);
            response.put("data", updatedPermission);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePermission(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            permissionService.deleteById(id);
            response.put("success", true);
            response.put("message", "权限删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

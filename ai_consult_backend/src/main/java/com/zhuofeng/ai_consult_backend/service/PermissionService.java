package com.zhuofeng.ai_consult_backend.service;

import com.zhuofeng.ai_consult_backend.model.Permission;
import com.zhuofeng.ai_consult_backend.repository.PermissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    public Optional<Permission> findById(Long id) {
        return permissionRepository.findById(id);
    }

    public Optional<Permission> findByPermissionCode(String permissionCode) {
        return permissionRepository.findByPermissionCode(permissionCode);
    }

    public Permission save(Permission permission) {
        return permissionRepository.save(permission);
    }

    public void deleteById(Long id) {
        permissionRepository.deleteById(id);
    }

    public boolean existsByPermissionCode(String permissionCode) {
        return permissionRepository.existsByPermissionCode(permissionCode);
    }

    public List<Permission> findByStatus(Integer status) {
        return permissionRepository.findByStatus(status);
    }

    public List<Permission> findByParentId(Long parentId) {
        return permissionRepository.findByParentId(parentId);
    }
}

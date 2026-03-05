package com.zhuofeng.ai_consult_backend.repository;

import com.zhuofeng.ai_consult_backend.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByPermissionCode(String permissionCode);
    
    boolean existsByPermissionCode(String permissionCode);
    
    List<Permission> findByStatus(Integer status);
    
    List<Permission> findByParentId(Long parentId);
}

package com.zhuofeng.ai_consult_backend.repository;

import com.zhuofeng.ai_consult_backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleCode(String roleCode);
    
    boolean existsByRoleCode(String roleCode);
}

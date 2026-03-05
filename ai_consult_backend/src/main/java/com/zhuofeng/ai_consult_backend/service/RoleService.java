package com.zhuofeng.ai_consult_backend.service;

import com.zhuofeng.ai_consult_backend.model.Role;
import com.zhuofeng.ai_consult_backend.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    public Optional<Role> findByRoleCode(String roleCode) {
        return roleRepository.findByRoleCode(roleCode);
    }

    public Role save(Role role) {
        return roleRepository.save(role);
    }

    public void deleteById(Long id) {
        roleRepository.deleteById(id);
    }

    public boolean existsByRoleCode(String roleCode) {
        return roleRepository.existsByRoleCode(roleCode);
    }
}

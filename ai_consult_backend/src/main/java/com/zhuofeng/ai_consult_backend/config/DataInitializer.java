package com.zhuofeng.ai_consult_backend.config;

import com.zhuofeng.ai_consult_backend.model.Permission;
import com.zhuofeng.ai_consult_backend.model.Role;
import com.zhuofeng.ai_consult_backend.model.User;
import com.zhuofeng.ai_consult_backend.repository.PermissionRepository;
import com.zhuofeng.ai_consult_backend.repository.RoleRepository;
import com.zhuofeng.ai_consult_backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(RoleRepository roleRepository, PermissionRepository permissionRepository, 
                                   UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("========== 开始初始化角色和权限数据 ==========");
            
            // 初始化权限
            List<Permission> permissions = initPermissions(permissionRepository);
            
            // 初始化角色
            initRoles(roleRepository, permissionRepository, permissions);
            
            // 创建默认管理员账号
            initAdminUser(userRepository, roleRepository, passwordEncoder);
            
            System.out.println("========== 角色和权限数据初始化完成 ==========");
        };
    }

    private List<Permission> initPermissions(PermissionRepository permissionRepository) {
        // 知识库管理权限
        Permission knowledgeManage = createPermission("知识库管理", "knowledge:manage", "MENU", 0L, "/knowledge", "folder", 1);
        Permission knowledgeRead = createPermission("查看知识库", "knowledge:read", "BUTTON", knowledgeManage.getId(), null, null, 1);
        Permission knowledgeUpload = createPermission("上传文档", "knowledge:upload", "BUTTON", knowledgeManage.getId(), null, null, 2);
        Permission knowledgeDelete = createPermission("删除文档", "knowledge:delete", "BUTTON", knowledgeManage.getId(), null, null, 3);
        
        // 聊天功能权限
        Permission chatSend = createPermission("发送消息", "chat:send", "BUTTON", 0L, null, "chat", 2);
        Permission chatHistory = createPermission("查看历史", "chat:history", "BUTTON", 0L, null, "history", 3);
        Permission chatManage = createPermission("管理会话", "chat:manage", "BUTTON", 0L, null, "settings", 4);
        
        // 用户管理权限
        Permission userManage = createPermission("用户管理", "user:manage", "MENU", 0L, "/users", "users", 5);
        Permission userRead = createPermission("查看用户", "user:read", "BUTTON", userManage.getId(), null, null, 1);
        Permission userCreate = createPermission("创建用户", "user:create", "BUTTON", userManage.getId(), null, null, 2);
        Permission userUpdate = createPermission("更新用户", "user:update", "BUTTON", userManage.getId(), null, null, 3);
        Permission userDelete = createPermission("删除用户", "user:delete", "BUTTON", userManage.getId(), null, null, 4);
        Permission userRole = createPermission("分配角色", "user:role", "BUTTON", userManage.getId(), null, null, 5);
        
        // 角色管理权限
        Permission roleManage = createPermission("角色管理", "role:manage", "MENU", 0L, "/roles", "shield", 6);
        Permission roleRead = createPermission("查看角色", "role:read", "BUTTON", roleManage.getId(), null, null, 1);
        Permission roleCreate = createPermission("创建角色", "role:create", "BUTTON", roleManage.getId(), null, null, 2);
        Permission roleUpdate = createPermission("更新角色", "role:update", "BUTTON", roleManage.getId(), null, null, 3);
        Permission roleDelete = createPermission("删除角色", "role:delete", "BUTTON", roleManage.getId(), null, null, 4);
        Permission rolePermission = createPermission("分配权限", "role:permission", "BUTTON", roleManage.getId(), null, null, 5);
        
        List<Permission> allPermissions = Arrays.asList(
            knowledgeManage, knowledgeRead, knowledgeUpload, knowledgeDelete,
            chatSend, chatHistory, chatManage,
            userManage, userRead, userCreate, userUpdate, userDelete, userRole,
            roleManage, roleRead, roleCreate, roleUpdate, roleDelete, rolePermission
        );
        
        for (Permission permission : allPermissions) {
            if (!permissionRepository.existsByPermissionCode(permission.getPermissionCode())) {
                permissionRepository.save(permission);
                System.out.println("创建权限：" + permission.getPermissionCode());
            }
        }
        
        return permissionRepository.findAll();
    }

    private Permission createPermission(String name, String code, String type, Long parentId, String path, String icon, int sort) {
        Permission permission = new Permission();
        permission.setPermissionName(name);
        permission.setPermissionCode(code);
        permission.setResourceType(type);
        permission.setParentId(parentId);
        permission.setPath(path);
        permission.setIcon(icon);
        permission.setSortOrder(sort);
        permission.setStatus(1);
        return permission;
    }

    private void initRoles(RoleRepository roleRepository, PermissionRepository permissionRepository, List<Permission> allPermissions) {
        // 超级管理员角色
        if (!roleRepository.existsByRoleCode("SUPER_ADMIN")) {
            Role superAdmin = new Role();
            superAdmin.setRoleName("超级管理员");
            superAdmin.setRoleCode("SUPER_ADMIN");
            superAdmin.setDescription("系统最高权限，可管理所有功能和用户");
            superAdmin.setStatus(1);
            superAdmin.setPermissions(new java.util.HashSet<>(allPermissions));
            roleRepository.save(superAdmin);
            System.out.println("创建角色：SUPER_ADMIN");
        }
        
        // 管理员角色
        if (!roleRepository.existsByRoleCode("ADMIN")) {
            Role admin = new Role();
            admin.setRoleName("管理员");
            admin.setRoleCode("ADMIN");
            admin.setDescription("管理知识库、用户和聊天记录");
            admin.setStatus(1);
            // 管理员拥有除角色管理外的所有权限
            java.util.Set<Permission> adminPermissions = new java.util.HashSet<>();
            for (Permission p : allPermissions) {
                if (!p.getPermissionCode().startsWith("role:")) {
                    adminPermissions.add(p);
                }
            }
            admin.setPermissions(adminPermissions);
            roleRepository.save(admin);
            System.out.println("创建角色：ADMIN");
        }
        
        // 普通用户角色
        if (!roleRepository.existsByRoleCode("USER")) {
            Role user = new Role();
            user.setRoleName("普通用户");
            user.setRoleCode("USER");
            user.setDescription("使用聊天功能，管理个人知识库");
            user.setStatus(1);
            java.util.Set<Permission> userPermissions = new java.util.HashSet<>();
            for (Permission p : allPermissions) {
                if (p.getPermissionCode().startsWith("chat:") || 
                    p.getPermissionCode().startsWith("knowledge:")) {
                    userPermissions.add(p);
                }
            }
            user.setPermissions(userPermissions);
            roleRepository.save(user);
            System.out.println("创建角色：USER");
        }
        
        // 访客角色
        if (!roleRepository.existsByRoleCode("GUEST")) {
            Role guest = new Role();
            guest.setRoleName("访客");
            guest.setRoleCode("GUEST");
            guest.setDescription("仅可使用聊天功能");
            guest.setStatus(1);
            java.util.Set<Permission> guestPermissions = new java.util.HashSet<>();
            for (Permission p : allPermissions) {
                if (p.getPermissionCode().equals("chat:send") || 
                    p.getPermissionCode().equals("chat:history")) {
                    guestPermissions.add(p);
                }
            }
            guest.setPermissions(guestPermissions);
            roleRepository.save(guest);
            System.out.println("创建角色：GUEST");
        }
    }

    private void initAdminUser(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        // 检查是否已存在管理员账号
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@qq.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setStatus(1);
            admin.setAvatar("");
            
            // 分配超级管理员角色
            Role superAdminRole = roleRepository.findByRoleCode("SUPER_ADMIN").orElseThrow();
            admin.setRoles(new java.util.HashSet<>(java.util.Set.of(superAdminRole)));
            
            userRepository.save(admin);
            System.out.println("创建默认管理员账号：admin / admin");
        } else {
            System.out.println("管理员账号已存在，跳过创建");
        }
    }
}

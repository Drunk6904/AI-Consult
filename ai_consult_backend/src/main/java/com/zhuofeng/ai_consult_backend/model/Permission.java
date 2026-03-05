package com.zhuofeng.ai_consult_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "permission_name", nullable = false, length = 100)
    private String permissionName;
    
    @Column(name = "permission_code", nullable = false, unique = true, length = 100)
    private String permissionCode;
    
    @Column(name = "resource_type", length = 20)
    private String resourceType;  // MENU, BUTTON, API
    
    @Column(name = "parent_id")
    private Long parentId = 0L;
    
    @Column(length = 255)
    private String path;
    
    @Column(length = 50)
    private String icon;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(nullable = false)
    private Integer status = 1;  // 1:正常，0:禁用
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

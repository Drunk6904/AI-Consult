package com.zhuofeng.ai_consult_backend.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 当前认证用户对象。
 * 用于在SecurityContext中携带业务所需用户信息。
 */
@Data
@AllArgsConstructor
public class AuthenticatedUser implements UserDetails {

    /**
     * 用户ID。
     */
    private String userId;

    /**
     * 用户手机号。
     */
    private String phone;

    /**
     * 是否已注册。
     */
    private Boolean isRegistered;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return phone;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

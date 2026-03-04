package com.zhuofeng.ai_consult_backend.repository;

import com.zhuofeng.ai_consult_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 用户仓储。
 */
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * 按手机号查询用户。
     */
    Optional<User> findByPhone(String phone);
}

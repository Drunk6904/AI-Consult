package com.zhuofeng.ai_consult_backend.repository;

import com.zhuofeng.ai_consult_backend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 会话仓储。
 */
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    /**
     * 通过会话ID和用户ID联合查询，防止越权访问。
     */
    Optional<Conversation> findByIdAndUserId(String id, String userId);
}

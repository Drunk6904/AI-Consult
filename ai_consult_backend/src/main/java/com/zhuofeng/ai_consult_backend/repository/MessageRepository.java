package com.zhuofeng.ai_consult_backend.repository;

import com.zhuofeng.ai_consult_backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 消息仓储。
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 查询指定会话的所有消息，按时间正序返回。
     */
    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);
}

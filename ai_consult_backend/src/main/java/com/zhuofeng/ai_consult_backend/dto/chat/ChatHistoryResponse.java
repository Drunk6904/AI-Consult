package com.zhuofeng.ai_consult_backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天历史返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryResponse {

    /**
     * 会话ID。
     */
    private String conversationId;

    /**
     * 消息列表（按时间升序）。
     */
    private List<MessageView> messages = new ArrayList<>();
}

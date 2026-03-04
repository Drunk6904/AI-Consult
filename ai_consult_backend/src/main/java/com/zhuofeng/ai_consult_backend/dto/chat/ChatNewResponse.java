package com.zhuofeng.ai_consult_backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 新建会话返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatNewResponse {

    /**
     * 会话ID。
     */
    private String conversationId;
}

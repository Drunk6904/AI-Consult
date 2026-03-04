package com.zhuofeng.ai_consult_backend.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 聊天发送请求对象。
 */
@Data
public class ChatSendRequest {

    /**
     * 会话ID。
     */
    @NotBlank(message = "conversationId不能为空")
    private String conversationId;

    /**
     * 消息内容。
     */
    @NotBlank(message = "content不能为空")
    private String content;
}

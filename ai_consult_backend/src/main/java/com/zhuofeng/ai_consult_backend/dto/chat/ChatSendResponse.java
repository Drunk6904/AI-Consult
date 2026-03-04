package com.zhuofeng.ai_consult_backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天发送返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSendResponse {

    /**
     * 会话ID。
     */
    private String conversationId;

    /**
     * AI回复文本。
     */
    private String aiReply;

    /**
     * 引用来源列表。
     */
    private List<SourceItem> sources = new ArrayList<>();
}

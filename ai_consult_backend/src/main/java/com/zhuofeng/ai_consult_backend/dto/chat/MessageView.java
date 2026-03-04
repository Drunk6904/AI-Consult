package com.zhuofeng.ai_consult_backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 历史消息展示对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageView {

    /**
     * 发送方类型：user / ai。
     */
    private String senderType;

    /**
     * 消息文本。
     */
    private String content;

    /**
     * 创建时间。
     */
    private Instant createdAt;

    /**
     * AI消息引用来源，用户消息通常为空。
     */
    private List<SourceItem> sources = new ArrayList<>();
}

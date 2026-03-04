package com.zhuofeng.ai_consult_backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * AI回复对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiReply {

    /**
     * AI回复文本。
     */
    private String answer;

    /**
     * 引用来源列表。
     */
    private List<SourceItem> sources = new ArrayList<>();
}

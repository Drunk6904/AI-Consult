package com.zhuofeng.ai_consult_backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI引用来源结构。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceItem {

    /**
     * 文档名称。
     */
    private String docName;

    /**
     * 文档片段内容。
     */
    private String snippet;

    /**
     * 定位信息（页码、章节、URL锚点等，可为空）。
     */
    private String locator;

    /**
     * 相关性分值（可为空）。
     */
    private Double score;
}

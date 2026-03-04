package com.zhuofeng.ai_consult_backend.service;

import tools.jackson.databind.JsonNode;
import com.zhuofeng.ai_consult_backend.dto.chat.AiReply;
import com.zhuofeng.ai_consult_backend.dto.chat.SourceItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI调用客户端。
 * 规则：
 * 1. ai.mock=true 直接返回Mock；
 * 2. ai.mock=false 调用真实接口；
 * 3. 调用失败自动降级为Mock，避免接口500。
 */
@Component
public class AiClient {

    private final RestTemplate restTemplate;

    @Value("${ai.base-url:http://localhost:8081}")
    private String aiBaseUrl;

    @Value("${ai.mock:true}")
    private boolean aiMock;

    public AiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 统一AI调用入口。
     */
    public AiReply askAi(String conversationId, String userId, boolean isRegistered, String query) {
        if (aiMock) {
            return mockReply(query);
        }

        try {
            String url = aiBaseUrl + "/api/ask";
            Map<String, Object> body = new HashMap<>();
            body.put("conversationId", conversationId);
            body.put("userId", userId);
            body.put("isRegistered", isRegistered);
            body.put("query", query);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            JsonNode root = restTemplate.postForObject(url, new HttpEntity<>(body, headers), JsonNode.class);
            if (root == null) {
                return mockFallbackReply(query);
            }

            String answer = extractAnswer(root);
            List<SourceItem> sources = extractSources(root);

            if (!StringUtils.hasText(answer)) {
                return mockFallbackReply(query);
            }

            return new AiReply(answer, sources);
        } catch (Exception ex) {
            return mockFallbackReply(query);
        }
    }

    private AiReply mockReply(String query) {
        return new AiReply("（MOCK）收到：" + query, new ArrayList<>());
    }

    /**
     * 真实AI失败时的降级策略。
     */
    private AiReply mockFallbackReply(String query) {
        return new AiReply("（MOCK降级）收到：" + query, new ArrayList<>());
    }

    private String extractAnswer(JsonNode root) {
        if (root.has("data") && root.get("data").has("answer")) {
            return root.get("data").get("answer").asText();
        }
        if (root.has("answer")) {
            return root.get("answer").asText();
        }
        return null;
    }

    private List<SourceItem> extractSources(JsonNode root) {
        JsonNode sourceNode = null;
        if (root.has("data") && root.get("data").has("sources")) {
            sourceNode = root.get("data").get("sources");
        } else if (root.has("sources")) {
            sourceNode = root.get("sources");
        }

        List<SourceItem> result = new ArrayList<>();
        if (sourceNode == null || !sourceNode.isArray()) {
            return result;
        }

        for (JsonNode item : sourceNode) {
            String docName = readText(item, "docName");
            String snippet = readText(item, "snippet");
            String locator = readText(item, "locator");
            Double score = item.has("score") && item.get("score").isNumber() ? item.get("score").asDouble() : null;
            result.add(new SourceItem(docName, snippet, locator, score));
        }
        return result;
    }

    private String readText(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText();
        }
        return null;
    }
}

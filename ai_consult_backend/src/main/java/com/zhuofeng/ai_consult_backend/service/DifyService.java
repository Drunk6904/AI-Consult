package com.zhuofeng.ai_consult_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class DifyService {
        private final WebClient webClient;
        private final String knowledgeBaseId;

        public DifyService(@Value("${dify.api.key}") String apiKey,
                        @Value("${dify.api.url}") String apiUrl,
                        @Value("${dify.knowledge.base.id}") String knowledgeBaseId) {
                this.knowledgeBaseId = knowledgeBaseId;
                this.webClient = WebClient.builder()
                                .baseUrl(apiUrl + "/v1")
                                .defaultHeader("Authorization", "Bearer " + apiKey)
                                .build();
        }

        /**
         * 上传文件到Dify知识库
         * 
         * @param file     本地文件
         * @param fileName 文件名
         * @return 文档ID
         */
        public Mono<String> uploadDocument(File file, String fileName) {
                return webClient.post()
                                .uri("/knowledge_bases/" + knowledgeBaseId + "/documents")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .body(BodyInserters.fromMultipartData("file",
                                                new FileSystemResource(file)))
                                .retrieve()
                                .bodyToMono(Map.class)
                                .map(response -> response != null ? (String) response.get("id") : null);
        }

        /**
         * 删除知识库中的文档
         * 
         * @param documentId 文档ID
         * @return 是否成功
         */
        public Mono<Boolean> deleteDocument(String documentId) {
                return webClient.delete()
                                .uri("/knowledge_bases/" + knowledgeBaseId + "/documents/" + documentId)
                                .retrieve()
                                .bodyToMono(Map.class)
                                .map(response -> response != null ? (Boolean) response.get("success") : false);
        }

        /**
         * 列出知识库中的所有文档
         * 
         * @return 文档列表
         */
        public Mono<Map<String, Object>> listDocuments() {
                return webClient.get()
                                .uri("/knowledge_bases/" + knowledgeBaseId + "/documents")
                                .retrieve()
                                .bodyToMono(Map.class)
                                .map(response -> response != null ? response : new HashMap<>());
        }

        /**
         * 调用Dify聊天API
         * 
         * @param query  用户查询
         * @param userId 用户ID
         * @return 聊天结果
         */
        public Mono<Map<String, Object>> chat(String query, String userId) {
                Map<String, Object> request = new HashMap<>();
                request.put("query", query);
                request.put("user", userId);
                request.put("stream", false);

                return webClient.post()
                                .uri("/chat/completions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(request)
                                .retrieve()
                                .bodyToMono(Map.class)
                                .map(response -> response != null ? response : new HashMap<>());
        }
}

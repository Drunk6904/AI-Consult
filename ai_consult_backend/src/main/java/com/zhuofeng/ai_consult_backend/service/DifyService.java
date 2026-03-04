package com.zhuofeng.ai_consult_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class DifyService {
   private final WebClient webClient;
   private final String knowledgeBaseId;
   private final String baseUrl;

   public DifyService(
         @Value("${dify.api.url}") String apiUrl,
         @Value("${dify.api.key}") String apiKey,
         @Value("${dify.knowledge.base.id}") String knowledgeBaseId) {
      this.knowledgeBaseId = knowledgeBaseId;
      this.baseUrl = apiUrl + "/v1";
      this.webClient = WebClient.builder()
            .baseUrl(this.baseUrl)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build();
   }

   /**
    * 上传文档到Dify知识库
    * 
    * @param file     文件
    * @param fileName 文件名
    * @return 文档ID
    */
   public Mono<String> uploadDocument(File file, String fileName) {
      System.out.println("=======================================");
      System.out.println("Uploading file to Dify: " + fileName);
      System.out.println("Dify API URL: " + baseUrl);
      System.out.println("Knowledge base ID: " + knowledgeBaseId);
      System.out.println("File path: " + file.getAbsolutePath());
      System.out.println("File exists: " + file.exists());
      System.out.println("File size: " + file.length());
      System.out.println("File can read: " + file.canRead());

      return webClient.post()
            .uri("/datasets/" + knowledgeBaseId + "/documents")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData("file",
                  new FileSystemResource(file)))
            .retrieve()
            .onStatus(status -> !status.is2xxSuccessful(), response -> {
               System.out.println("Dify upload failed with status: " + response.statusCode());
               return response.bodyToMono(String.class)
                     .flatMap(body -> {
                        System.out.println("Dify error response: " + body);
                        return Mono.error(new RuntimeException("Dify upload failed: " + body));
                     });
            })
            .bodyToMono(Map.class)
            .doOnSuccess(response -> {
               System.out.println("Dify upload success response: " + response);
               System.out.println("=======================================");
            })
            .map(response -> response != null ? (String) response.get("id") : null);
   }

   /**
    * 删除Dify知识库中的文档
    * 
    * @param documentId 文档ID
    * @return 是否成功
    */
   public Mono<Boolean> deleteDocument(String documentId) {
        return webClient.delete()
                .uri("/datasets/" + knowledgeBaseId + "/documents/" + documentId)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> response != null);
    }

    /**
     * 列出Dify知识库中的文档
     * 
     * @return 文档列表
     */
    public Mono<Map<String, Object>> listDocuments() {
        return webClient.get()
                .uri("/datasets/" + knowledgeBaseId + "/documents")
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> response);
    }

    /**
     * 与Dify进行聊天
     * 
     * @param query  问题
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
                .map(response -> response);
    }
}

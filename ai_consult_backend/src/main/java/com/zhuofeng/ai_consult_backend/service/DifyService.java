package com.zhuofeng.ai_consult_backend.service;

import com.zhuofeng.ai_consult_backend.config.StreamingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class DifyService {
    private final WebClient webClient;
    private final WebClient datasetWebClient;
    private final WebClient webhookWebClient;
    private final String publicKnowledgeBaseId; // 公开知识库（30%）
    private final String privateKnowledgeBaseId; // 非公开知识库（70%）
    private final String baseUrl;
    private final String chatflowApiKey;
    private final String datasetApiKey;
    private final String webhookUrl;
    private final String webhookDebugUrl;

    @Value("${dify.feature.workflow.type:workflow}")
    private String workflowType;

    private static final String WORKFLOW_TYPE = "workflow";
    private static final String CHATFLOW_TYPE = "chatflow";

    @Autowired
    private StreamingConfig streamingConfig;
    @Autowired
    private StreamingEventLogger streamingEventLogger;
    @Autowired
    private StreamingErrorHandler streamingErrorHandler;

    public DifyService(
            @Value("${dify.api.url}") String apiUrl,
            @Value("${dify.chatflow.api.key}") String chatflowApiKey,
            @Value("${dify.dataset.api.key}") String datasetApiKey,
            @Value("${dify.knowledge.base.id}") String knowledgeBaseId,
            @Value("${dify.knowledge.private.base.id}") String privateKnowledgeBaseId,
            @Value("${dify.webhook.url}") String webhookUrl,
            @Value("${dify.webhook.debug.url}") String webhookDebugUrl) {
        this.publicKnowledgeBaseId = knowledgeBaseId;
        this.privateKnowledgeBaseId = privateKnowledgeBaseId;
        this.chatflowApiKey = chatflowApiKey;
        this.datasetApiKey = datasetApiKey;
        this.webhookUrl = webhookUrl;
        this.webhookDebugUrl = webhookDebugUrl;
        // 直接使用配置中提供的 URL
        this.baseUrl = apiUrl;
        this.webClient = WebClient.builder()
                .baseUrl(this.baseUrl)
                .defaultHeader("Authorization", "Bearer " + chatflowApiKey)
                .build();
        this.datasetWebClient = WebClient.builder()
                .baseUrl(this.baseUrl)
                .defaultHeader("Authorization", "Bearer " + datasetApiKey)
                .build();
        this.webhookWebClient = WebClient.builder()
                .build();
        System.out.println("DifyService initialized with base URL: " + this.baseUrl);
        System.out.println("Public knowledge base ID: " + this.publicKnowledgeBaseId);
        System.out.println("Private knowledge base ID: " + this.privateKnowledgeBaseId);
        System.out.println("Webhook URL: " + this.webhookUrl);
        System.out.println("Webhook Debug URL: " + this.webhookDebugUrl);
    }

    @PostConstruct
    public void validateWorkflowType() {
        // 验证并规范化 workflowType
        if (workflowType == null || workflowType.trim().isEmpty()) {
            System.out.println("⚠ FEATURE_FLAG_WORKFLOW_TYPE 未设置，使用默认值: workflow");
            workflowType = WORKFLOW_TYPE;
        } else {
            workflowType = workflowType.trim().toLowerCase();
            if (!WORKFLOW_TYPE.equals(workflowType) && !CHATFLOW_TYPE.equals(workflowType)) {
                throw new IllegalStateException(
                        String.format(
                                "FEATURE_FLAG_WORKFLOW_TYPE 配置无效: '%s'。有效值为: '%s' 或 '%s'",
                                workflowType, WORKFLOW_TYPE, CHATFLOW_TYPE));
            }
            System.out.println("✓ FEATURE_FLAG_WORKFLOW_TYPE: " + workflowType);
        }
    }

    /**
     * 统一的聊天执行入口
     * 根据 FEATURE_FLAG_WORKFLOW_TYPE 配置自动路由到 workflow 或 chatflow
     * 
     * @param query          用户输入的问题
     * @param userId         用户 ID
     * @param conversationId 会话 ID（用于维持对话上下文）
     * @param isRegistered   用户是否已注册
     * @param sessionId      会话 ID（用于工作流回调）
     * @return 问答结果
     */
    public Mono<Map<String, Object>> executeChat(String query, String userId, String conversationId,
            boolean isRegistered, String sessionId) {

        // 根据配置选择调用方式
        if (CHATFLOW_TYPE.equals(workflowType)) {
            System.out.println("使用 Chatflow 模式处理请求");
            return executeChatflow(query, userId, conversationId, isRegistered, sessionId);
        } else {
            System.out.println("使用 Workflow 模式处理请求");
            return executeWorkflow(query, userId, conversationId, isRegistered, sessionId);
        }
    }

    /**
     * 执行 Workflow 模式聊天
     */
    private Mono<Map<String, Object>> executeWorkflow(String query, String userId, String conversationId,
            boolean isRegistered, String sessionId) {
        Map<String, Object> request = new HashMap<>();
        request.put("query", query);
        request.put("inputs", new HashMap<>()); // 必须包含 inputs 字段
        request.put("user", userId);
        request.put("response_mode", "blocking");

        // 如果提供了会话 ID，则传递给 Dify 以维持对话上下文
        if (conversationId != null && !conversationId.isEmpty()) {
            request.put("conversation_id", conversationId);
        }

        // 传递 session_id 用于工作流回调
        if (sessionId != null && !sessionId.isEmpty()) {
            request.put("session_id", sessionId);
        }

        // 根据用户注册状态设置不同的参数，以便 Dify 工作流能够识别并访问相应的知识库
        Map<String, Object> inputs = (Map<String, Object>) request.get("inputs");
        inputs.put("is_registered", isRegistered);
        // 传递知识库 ID 信息
        inputs.put("public_knowledge_base_id", publicKnowledgeBaseId);
        inputs.put("private_knowledge_base_id", privateKnowledgeBaseId);
        // 传递 session_id 到 inputs 中，确保工作流可以访问
        if (sessionId != null && !sessionId.isEmpty()) {
            inputs.put("session_id", sessionId);
        }

        // 使用工作流webhook URL（正式环境）
        String workflowUrl = this.webhookUrl;
        // 使用工作流webhook URL（调试环境）
        // String workflowUrl = this.webhookDebugUrl;

        // 打印请求信息
        System.out.println("=======================================");
        System.out.println("Sending workflow request to: " + workflowUrl);
        System.out.println("Request data: " + request);

        return webhookWebClient.post()
                .uri(workflowUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response -> {
                    return response.bodyToMono(String.class)
                            .flatMap(body -> {
                                System.out.println("Workflow error response: " + body);
                                return Mono.error(new RuntimeException("Dify workflow failed: " + body));
                            });
                })
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .doOnSuccess(response -> {
                    System.out.println("Workflow success response: " + response);
                    System.out.println("=======================================");
                });
    }

    /**
     * 执行 Chatflow 模式聊天
     */
    private Mono<Map<String, Object>> executeChatflow(String query, String userId, String conversationId,
            boolean isRegistered, String sessionId) {
        Map<String, Object> request = new HashMap<>();
        request.put("query", query);
        request.put("inputs", new HashMap<>());
        request.put("user", userId);
        request.put("response_mode", "blocking");

        // 如果提供了会话 ID，则传递给 Dify
        if (conversationId != null && !conversationId.isEmpty()) {
            request.put("conversation_id", conversationId);
        }

        // 设置 inputs
        Map<String, Object> inputs = (Map<String, Object>) request.get("inputs");
        inputs.put("is_registered", isRegistered);
        inputs.put("public_knowledge_base_id", publicKnowledgeBaseId);
        inputs.put("private_knowledge_base_id", privateKnowledgeBaseId);
        if (sessionId != null && !sessionId.isEmpty()) {
            inputs.put("session_id", sessionId);
        }

        System.out.println("=======================================");
        System.out.println("Sending chatflow request to: " + baseUrl + "/chat-messages");
        System.out.println("Request data: " + request);

        return webClient.post()
                .uri("/chat-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response -> {
                    return response.bodyToMono(String.class)
                            .flatMap(body -> {
                                System.out.println("Chatflow error response: " + body);
                                return Mono.error(new RuntimeException("Dify chatflow failed: " + body));
                            });
                })
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .doOnSuccess(response -> {
                    System.out.println("Chatflow success response: " + response);
                    System.out.println("=======================================");
                });
    }

    /**
     * 上传文档到Dify知识库
     * 
     * @param file     文件
     * @param fileName 文件名
     * @param isPublic 是否上传到公开知识库
     * @return 文档ID
     */
    public Mono<String> uploadDocument(File file, String fileName, boolean isPublic) {
        System.out.println("=======================================");
        System.out.println("Uploading file to Dify: " + fileName);
        System.out.println("Dify API URL: " + baseUrl);
        String targetKnowledgeBaseId = isPublic ? publicKnowledgeBaseId : privateKnowledgeBaseId;
        System.out.println("Knowledge base ID: " + targetKnowledgeBaseId);
        System.out.println("File path: " + file.getAbsolutePath());
        System.out.println("File exists: " + file.exists());
        System.out.println("File size: " + file.length());
        System.out.println("File can read: " + file.canRead());

        // 构建与界面配置一致的 data 参数
        String data = buildDataConfig();
        System.out.println("Data config: " + data);

        // 创建多部分表单数据
        LinkedMultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
        multipartData.add("data", data);
        multipartData.add("file", new FileSystemResource(file));

        return datasetWebClient.post()
                .uri("/datasets/" + targetKnowledgeBaseId + "/document/create-by-file")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartData))
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
                .map(response -> {
                    if (response == null || response.get("document") == null) {
                        return null;
                    }
                    // 注意：实际返回的是 document 对象，id 在 document 内部
                    Map<String, Object> document = (Map<String, Object>) response.get("document");
                    return (String) document.get("id");
                });
    }

    /**
     * 上传文档到Dify公开知识库（默认）
     * 
     * @param file     文件
     * @param fileName 文件名
     * @return 文档ID
     */
    public Mono<String> uploadDocument(File file, String fileName) {
        return uploadDocument(file, fileName, true);
    }

    /**
     * 构建与 Dify 界面配置匹配的 data JSON
     */
    private String buildDataConfig() {
        try {
            // 使用 Jackson 或手动构建 JSON
            Map<String, Object> config = new HashMap<>();

            // 1. 索引技术：高质量
            config.put("indexing_technique", "high_quality");

            // 2. 处理规则（匹配界面配置）
            Map<String, Object> processRule = new HashMap<>();
            processRule.put("mode", "custom"); // 使用 custom 才能指定详细规则

            Map<String, Object> rules = new HashMap<>();

            // 分段设置
            Map<String, Object> segmentation = new HashMap<>();
            segmentation.put("separator", "\\n\\n"); // 分段标识符：\n\n
            segmentation.put("max_tokens", 1024); // 分段最大长度：1024
            segmentation.put("overlap", 50); // 分段重叠长度：50
            rules.put("segmentation", segmentation);

            // 文本预处理规则（替换连续空格、换行、制表符）
            java.util.List<Map<String, Object>> preProcessingRules = new java.util.ArrayList<>();

            Map<String, Object> removeExtraSpaces = new HashMap<>();
            removeExtraSpaces.put("id", "remove_extra_spaces");
            removeExtraSpaces.put("enabled", true);
            preProcessingRules.add(removeExtraSpaces);

            Map<String, Object> removeUrlsEmails = new HashMap<>();
            removeUrlsEmails.put("id", "remove_urls_emails");
            removeUrlsEmails.put("enabled", false); // 界面未勾选
            preProcessingRules.add(removeUrlsEmails);

            rules.put("pre_processing_rules", preProcessingRules);

            processRule.put("rules", rules);
            config.put("process_rule", processRule);

            // 3. 摘要设置（可选，如不需要可移除）
            Map<String, Object> summarySetting = new HashMap<>();
            summarySetting.put("enable", false); // 如果界面没开，设为 false
            config.put("summary_index_setting", summarySetting);

            // 转换为 JSON 字符串
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(config);

        } catch (Exception e) {
            // 降级为简单配置
            return "{\"indexing_technique\":\"high_quality\",\"process_rule\":{\"mode\":\"automatic\"}}";
        }
    }

    /**
     * 删除 Dify 知识库中的文档
     * 
     * @param documentId 文档 ID
     * @param isPublic   是否从公开知识库删除
     * @return 是否成功
     */
    public Mono<Boolean> deleteDocument(String documentId, boolean isPublic) {
        String targetKnowledgeBaseId = isPublic ? publicKnowledgeBaseId : privateKnowledgeBaseId;
        return datasetWebClient.delete()
                .uri("/datasets/" + targetKnowledgeBaseId + "/documents/" + documentId)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response -> {
                    return response.bodyToMono(String.class)
                            .flatMap(body -> {
                                return Mono.error(new RuntimeException("Dify delete failed: " + body));
                            });
                })
                .bodyToMono(Void.class)
                .map(response -> true);
    }

    /**
     * 删除 Dify 公开知识库中的文档（默认）
     * 
     * @param documentId 文档 ID
     * @return 是否成功
     */
    public Mono<Boolean> deleteDocument(String documentId) {
        return deleteDocument(documentId, true);
    }

    /**
     * 列出 Dify 知识库中的文档
     * 
     * @param isPublic 是否列出公开知识库
     * @return 文档列表
     */
    public Mono<Map<String, Object>> listDocuments(boolean isPublic) {
        String targetKnowledgeBaseId = isPublic ? publicKnowledgeBaseId : privateKnowledgeBaseId;
        return datasetWebClient.get()
                .uri("/datasets/" + targetKnowledgeBaseId + "/documents")
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response -> {
                    return response.bodyToMono(String.class)
                            .flatMap(body -> {
                                return Mono.error(new RuntimeException("Dify list failed: " + body));
                            });
                })
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });
    }

    /**
     * 列出 Dify 公开知识库中的文档（默认）
     * 
     * @return 文档列表
     */
    public Mono<Map<String, Object>> listDocuments() {
        return listDocuments(true);
    }

    /**
     * 调用 Dify Workflow 进行问答
     * 
     * @param query        用户输入的问题
     * @param userId       用户 ID
     * @param isRegistered 用户是否已注册
     * @return 问答结果
     */
    public Mono<Map<String, Object>> workflow(String query, String userId, boolean isRegistered) {
        return workflow(query, userId, null, isRegistered);
    }

    /**
     * 调用 Dify Workflow 进行问答（带会话 ID）
     * 
     * @param query          用户输入的问题
     * @param userId         用户 ID
     * @param conversationId 会话 ID（用于维持对话上下文）
     * @param isRegistered   用户是否已注册
     * @return 问答结果
     */
    public Mono<Map<String, Object>> workflow(String query, String userId, String conversationId,
            boolean isRegistered) {
        return executeWorkflow(query, userId, conversationId, isRegistered, null);
    }

    /**
     * 调用 Dify Workflow 进行问答（带会话 ID 和 sessionId）
     * 
     * @param query          用户输入的问题
     * @param userId         用户 ID
     * @param conversationId 会话 ID（用于维持对话上下文）
     * @param isRegistered   用户是否已注册
     * @param sessionId      会话 ID（用于工作流回调）
     * @return 问答结果
     */
    public Mono<Map<String, Object>> workflow(String query, String userId, String conversationId,
            boolean isRegistered, String sessionId) {
        return executeWorkflow(query, userId, conversationId, isRegistered, sessionId);
    }

    /**
     * 调用 Dify Workflow 进行问答（默认，未注册用户）
     * 
     * @param query  用户输入的问题
     * @param userId 用户 ID
     * @return 问答结果
     */
    public Mono<Map<String, Object>> workflow(String query, String userId) {
        return workflow(query, userId, false);
    }

    /**
     * 调用 Dify Chatflow 进行问答（默认，未注册用户）
     * 
     * @param query  用户输入的问题
     * @param userId 用户 ID
     * @return 问答结果
     */
    public Mono<Map<String, Object>> chatflow(String query, String userId) {
        return executeChatflow(query, userId, null, false, null);
    }

    /**
     * 调用 Dify Chatflow 进行问答（带会话 ID，默认未注册用户）
     * 
     * @param query          用户输入的问题
     * @param userId         用户 ID
     * @param conversationId 会话 ID（用于维持对话上下文）
     * @return 问答结果
     */
    public Mono<Map<String, Object>> chatflow(String query, String userId, String conversationId) {
        return executeChatflow(query, userId, conversationId, false, null);
    }

    /**
     * 调用 Dify Chatflow 进行问答
     * 
     * @param query        用户输入的问题
     * @param userId       用户 ID
     * @param isRegistered 用户是否已注册
     * @return 问答结果
     */
    public Mono<Map<String, Object>> chatflow(String query, String userId, boolean isRegistered) {
        return executeChatflow(query, userId, null, isRegistered, null);
    }

    /**
     * 调用 Dify Chatflow 进行问答（带会话 ID）
     * 
     * @param query          用户输入的问题
     * @param userId         用户 ID
     * @param conversationId 会话 ID（用于维持对话上下文）
     * @param isRegistered   用户是否已注册
     * @return 问答结果
     */
    public Mono<Map<String, Object>> chatflow(String query, String userId, String conversationId,
            boolean isRegistered) {
        return executeChatflow(query, userId, conversationId, isRegistered, null);
    }

    /**
     * 与Dify进行聊天（传统方式，不区分 workflow/chatflow）
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
                .onStatus(status -> !status.is2xxSuccessful(), response -> {
                    return response.bodyToMono(String.class)
                            .flatMap(body -> {
                                return Mono.error(new RuntimeException("Dify chat failed: " + body));
                            });
                })
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });
    }

    /**
     * 发送 Webhook 请求
     * 
     * @param payload Webhook payload
     * @param isDebug 是否使用调试 URL
     * @return Webhook 响应
     */
    public Mono<Map<String, Object>> sendWebhook(Map<String, Object> payload, boolean isDebug) {
        String targetUrl = isDebug ? webhookDebugUrl : webhookUrl;
        System.out.println("Sending webhook to: " + targetUrl);
        System.out.println("Webhook payload: " + payload);

        return webhookWebClient.post()
                .uri(targetUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response -> {
                    return response.bodyToMono(String.class)
                            .flatMap(body -> {
                                return Mono.error(new RuntimeException("Webhook failed: " + body));
                            });
                })
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .doOnSuccess(response -> {
                    System.out.println("Webhook sent successfully: " + response);
                })
                .doOnError(error -> {
                    System.out.println("Webhook failed: " + error.getMessage());
                });
    }

    /**
     * 发送 Webhook 请求（默认使用生产 URL）
     * 
     * @param payload Webhook payload
     * @return Webhook 响应
     */
    public Mono<Map<String, Object>> sendWebhook(Map<String, Object> payload) {
        return sendWebhook(payload, false);
    }

    /**
     * 构建 Webhook payload
     * 
     * @param query         用户查询
     * @param answer        AI 回答
     * @param userId        用户 ID
     * @param isRegistered  是否注册用户
     * @param knowledgeBase 使用的知识库
     * @return Webhook payload
     */
    public Map<String, Object> buildWebhookPayload(String query, String answer, String userId, boolean isRegistered,
            String knowledgeBase) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("query", query);
        payload.put("answer", answer);
        payload.put("user_id", userId);
        payload.put("is_registered", isRegistered);
        payload.put("knowledge_base", knowledgeBase);
        payload.put("timestamp", System.currentTimeMillis());
        return payload;
    }

    /**
     * 获取当前工作流类型
     * 
     * @return workflow 或 chatflow
     */
    public String getWorkflowType() {
        return workflowType;
    }

    /**
     * 统一的流式聊天执行入口
     * 根据 FEATURE_FLAG_WORKFLOW_TYPE 配置自动路由到 workflow 或 chatflow
     * 
     * @param query          用户输入的问题
     * @param userId         用户 ID
     * @param conversationId 会话 ID（用于维持对话上下文）
     * @param isRegistered   用户是否已注册
     * @param sessionId      会话 ID（用于工作流回调）
     * @return 流式问答结果 (Flux SSE 事件)
     */
    public Flux<Map<String, Object>> executeChatStreaming(String query, String userId, String conversationId,
            boolean isRegistered, String sessionId) {

        // 检查流式输出是否启用
        if (!streamingConfig.isStreamingEnabled()) {
            System.out.println("流式输出已禁用，回退到阻塞模式");
            return executeChat(query, userId, conversationId, isRegistered, sessionId)
                    .flatMapMany(response -> {
                        Map<String, Object> event = new HashMap<>();
                        event.put("event", "message");
                        event.put("message_id", response.get("message_id"));
                        event.put("conversation_id", response.get("conversation_id"));
                        event.put("answer", response.get("answer"));
                        event.put("sequence", 0);
                        event.put("is_end", true);
                        return Flux.just(event);
                    });
        }

        // 根据配置选择调用方式
        if (CHATFLOW_TYPE.equals(workflowType)) {
            System.out.println("使用 Chatflow 流式模式处理请求");
            return executeChatflowStreaming(query, userId, conversationId, isRegistered, sessionId);
        } else {
            System.out.println("Workflow 模式不支持流式输出，回退到阻塞模式");
            return executeChat(query, userId, conversationId, isRegistered, sessionId)
                    .flatMapMany(response -> {
                        Map<String, Object> event = new HashMap<>();
                        event.put("event", "message");
                        event.put("message_id", response.get("message_id"));
                        event.put("conversation_id", response.get("conversation_id"));
                        event.put("answer", response.get("answer"));
                        event.put("sequence", 0);
                        event.put("is_end", true);
                        return Flux.just(event);
                    });
        }
    }

    /**
     * 执行 Chatflow 流式模式聊天
     */
    private Flux<Map<String, Object>> executeChatflowStreaming(String query, String userId, String conversationId,
            boolean isRegistered, String sessionId) {

        Map<String, Object> request = createStreamingRequest(query, userId, conversationId, isRegistered, sessionId);
        Instant startTime = Instant.now();
        String requestId = userId + "_" + startTime.toEpochMilli();

        System.out.println("=======================================");
        System.out.println("Sending streaming chatflow request to: " + baseUrl + "/chat-messages");
        System.out.println("Request data: " + request);

        AtomicLong sequenceCounter = new AtomicLong(0);
        AtomicReference<String> messageIdRef = new AtomicReference<>();
        AtomicReference<String> conversationIdRef = new AtomicReference<>();

        return webClient.post()
                .uri("/chat-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response -> {
                    return response.bodyToMono(String.class)
                            .flatMap(body -> {
                                System.out.println("Streaming chatflow error response: " + body);
                                streamingEventLogger.logError(requestId, userId, "HTTP_ERROR", body);
                                return Mono.error(new RuntimeException("Dify streaming chatflow failed: " + body));
                            });
                })
                .bodyToFlux(String.class)
                .doOnSubscribe(subscription -> {
                    streamingEventLogger.logConnectionEstablished(requestId, userId);
                })
                .flatMap(sseEvent -> {
                    try {
                        // 解析 SSE 事件
                        Map<String, Object> eventData = parseSseEvent(sseEvent);
                        if (eventData == null) {
                            return Flux.empty();
                        }

                        // 提取并保存 message_id 和 conversation_id
                        if (eventData.containsKey("message_id") && messageIdRef.get() == null) {
                            messageIdRef.set((String) eventData.get("message_id"));
                        }
                        if (eventData.containsKey("conversation_id") && conversationIdRef.get() == null) {
                            conversationIdRef.set((String) eventData.get("conversation_id"));
                        }

                        // 添加序列号
                        long sequence = sequenceCounter.getAndIncrement();
                        eventData.put("sequence", sequence);

                        // 检查是否是结束事件
                        String event = (String) eventData.get("event");
                        boolean isEnd = "message_end".equals(event) || "error".equals(event);
                        eventData.put("is_end", isEnd);

                        // 记录数据传输
                        String answer = (String) eventData.getOrDefault("answer", "");
                        streamingEventLogger.logDataTransmitted(requestId, userId, sequence, answer.length());

                        if (isEnd) {
                            long timeToFirstByte = Duration.between(startTime, Instant.now()).toMillis();
                            streamingEventLogger.logPerformanceMetrics(requestId, userId, timeToFirstByte,
                                    timeToFirstByte);
                        }

                        return Flux.just(eventData);
                    } catch (Exception e) {
                        System.out.println("Error parsing SSE event: " + e.getMessage());
                        streamingEventLogger.logError(requestId, userId, "PARSE_ERROR", e.getMessage());
                        return Flux.error(e);
                    }
                })
                .timeout(Duration.ofSeconds(streamingConfig.getStreamingTimeout()))
                .onErrorResume(error -> {
                    System.out.println("Streaming error occurred: " + error.getMessage());
                    streamingEventLogger.logError(requestId, userId, "STREAM_ERROR", error.getMessage());
                    return streamingErrorHandler.handleError(error, requestId);
                })
                .doOnComplete(() -> {
                    long totalDuration = Duration.between(startTime, Instant.now()).toMillis();
                    long totalEvents = sequenceCounter.get();
                    streamingEventLogger.logPerformanceMetrics(requestId, userId, totalDuration, totalDuration);
                    System.out.println("Streaming completed. Total events: " + totalEvents + ", Duration: "
                            + totalDuration + "ms");
                    System.out.println("=======================================");
                });
    }

    /**
     * 创建流式请求体
     */
    private Map<String, Object> createStreamingRequest(String query, String userId, String conversationId,
            boolean isRegistered, String sessionId) {
        Map<String, Object> request = new HashMap<>();
        request.put("query", query);
        request.put("inputs", new HashMap<>());
        request.put("user", userId);
        request.put("response_mode", "streaming");

        // 如果提供了会话 ID，则传递给 Dify
        if (conversationId != null && !conversationId.isEmpty()) {
            request.put("conversation_id", conversationId);
        }

        // 设置 inputs
        Map<String, Object> inputs = (Map<String, Object>) request.get("inputs");
        inputs.put("is_registered", isRegistered);
        inputs.put("public_knowledge_base_id", publicKnowledgeBaseId);
        inputs.put("private_knowledge_base_id", privateKnowledgeBaseId);
        if (sessionId != null && !sessionId.isEmpty()) {
            inputs.put("session_id", sessionId);
        }

        return request;
    }

    /**
     * 解析 SSE 事件
     */
    private Map<String, Object> parseSseEvent(String sseEvent) {
        if (sseEvent == null || sseEvent.trim().isEmpty()) {
            return null;
        }

        // SSE 事件格式: data: {...}
        String dataPrefix = "data: ";
        if (sseEvent.startsWith(dataPrefix)) {
            String jsonData = sseEvent.substring(dataPrefix.length());
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(jsonData, Map.class);
            } catch (Exception e) {
                System.out.println("Failed to parse SSE data: " + jsonData);
                return null;
            }
        }

        return null;
    }
}

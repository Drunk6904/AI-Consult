package com.zhuofeng.ai_consult_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 应用配置属性类
 * 
 * 用于集中管理所有从环境变量或配置文件加载的配置项
 * 通过 @ConfigurationProperties 自动绑定以 "app" 为前缀的配置
 */
@Configuration
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {

    /**
     * Dify 平台配置
     */
    private final Dify dify = new Dify();
    
    /**
     * JWT 认证配置
     */
    private final Jwt jwt = new Jwt();
    
    /**
     * 数据库配置
     */
    private final Database database = new Database();

    public Dify getDify() {
        return dify;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public Database getDatabase() {
        return database;
    }

    /**
     * Dify 平台配置类
     */
    public static class Dify {
        
        /**
         * Dify API 基础 URL
         */
        @NotBlank(message = "DIFY_API_URL 不能为空")
        private String apiUrl;
        
        /**
         * Workflow API Key
         */
        @NotBlank(message = "DIFY_WORKFLOW_API_KEY 不能为空")
        private String workflowApiKey;
        
        /**
         * Chatflow API Key
         */
        @NotBlank(message = "DIFY_CHATFLOW_API_KEY 不能为空")
        private String chatflowApiKey;
        
        /**
         * Dataset API Key
         */
        @NotBlank(message = "DIFY_DATASET_API_KEY 不能为空")
        private String datasetApiKey;
        
        /**
         * 公开知识库 ID
         */
        @NotBlank(message = "DIFY_KNOWLEDGE_BASE_ID 不能为空")
        private String knowledgeBaseId;
        
        /**
         * 私有知识库 ID
         */
        @NotBlank(message = "DIFY_KNOWLEDGE_PRIVATE_BASE_ID 不能为空")
        private String knowledgePrivateBaseId;
        
        /**
         * 工作流 ID（可选）
         */
        private String workflowId;
        
        /**
         * Webhook 正式 URL（可选）
         */
        private String webhookUrl;
        
        /**
         * Webhook 调试 URL（可选）
         */
        private String webhookDebugUrl;
        
        /**
         * 功能切换标志：workflow | chatflow
         */
        private String workflowType = "workflow";

        // Getters and Setters
        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        public String getWorkflowApiKey() {
            return workflowApiKey;
        }

        public void setWorkflowApiKey(String workflowApiKey) {
            this.workflowApiKey = workflowApiKey;
        }

        public String getChatflowApiKey() {
            return chatflowApiKey;
        }

        public void setChatflowApiKey(String chatflowApiKey) {
            this.chatflowApiKey = chatflowApiKey;
        }

        public String getDatasetApiKey() {
            return datasetApiKey;
        }

        public void setDatasetApiKey(String datasetApiKey) {
            this.datasetApiKey = datasetApiKey;
        }

        public String getKnowledgeBaseId() {
            return knowledgeBaseId;
        }

        public void setKnowledgeBaseId(String knowledgeBaseId) {
            this.knowledgeBaseId = knowledgeBaseId;
        }

        public String getKnowledgePrivateBaseId() {
            return knowledgePrivateBaseId;
        }

        public void setKnowledgePrivateBaseId(String knowledgePrivateBaseId) {
            this.knowledgePrivateBaseId = knowledgePrivateBaseId;
        }

        public String getWorkflowId() {
            return workflowId;
        }

        public void setWorkflowId(String workflowId) {
            this.workflowId = workflowId;
        }

        public String getWebhookUrl() {
            return webhookUrl;
        }

        public void setWebhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
        }

        public String getWebhookDebugUrl() {
            return webhookDebugUrl;
        }

        public void setWebhookDebugUrl(String webhookDebugUrl) {
            this.webhookDebugUrl = webhookDebugUrl;
        }

        public String getWorkflowType() {
            return workflowType;
        }

        public void setWorkflowType(String workflowType) {
            this.workflowType = workflowType;
        }
    }

    /**
     * JWT 认证配置类
     */
    public static class Jwt {
        
        /**
         * JWT 签名密钥（至少32个字符）
         */
        @NotBlank(message = "JWT_SECRET_KEY 不能为空")
        private String secretKey;
        
        /**
         * Token 过期时间（毫秒）
         */
        @NotNull(message = "JWT_EXPIRATION_TIME 不能为空")
        @Positive(message = "JWT_EXPIRATION_TIME 必须为正数")
        private Long expirationTime = 7200000L;

        // Getters and Setters
        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public Long getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(Long expirationTime) {
            this.expirationTime = expirationTime;
        }
    }

    /**
     * 数据库配置类
     */
    public static class Database {
        
        /**
         * 数据库连接 URL
         */
        private String url = "jdbc:h2:mem:testdb";
        
        /**
         * 数据库驱动类名
         */
        private String driver = "org.h2.Driver";
        
        /**
         * 数据库用户名
         */
        private String username = "sa";
        
        /**
         * 数据库密码
         */
        private String password = "";

        // Getters and Setters
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDriver() {
            return driver;
        }

        public void setDriver(String driver) {
            this.driver = driver;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}

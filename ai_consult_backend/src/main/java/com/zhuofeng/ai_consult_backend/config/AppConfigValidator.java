package com.zhuofeng.ai_consult_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用配置验证器
 * 
 * 在应用启动时验证所有必需的环境变量和配置项
 * 确保配置正确后再启动应用，避免运行时错误
 */
@Component
public class AppConfigValidator implements CommandLineRunner {

    @Value("${dify.api.url:}")
    private String difyApiUrl;

    @Value("${dify.workflow.api.key:}")
    private String difyWorkflowApiKey;

    @Value("${dify.chatflow.api.key:}")
    private String difyChatflowApiKey;

    @Value("${dify.dataset.api.key:}")
    private String difyDatasetApiKey;

    @Value("${dify.knowledge.base.id:}")
    private String difyKnowledgeBaseId;

    @Value("${dify.knowledge.private.base.id:}")
    private String difyKnowledgePrivateBaseId;

    @Value("${dify.feature.workflow.type:workflow}")
    private String workflowType;

    @Value("${jwt.secret.key:}")
    private String jwtSecretKey;

    @Value("${jwt.expiration.time:7200000}")
    private long jwtExpirationTime;

    private static final int MIN_JWT_KEY_LENGTH = 32;
    private static final String[] VALID_WORKFLOW_TYPES = {"workflow", "chatflow"};

    @Override
    public void run(String... args) {
        System.out.println("========================================");
        System.out.println("开始验证应用配置...");
        System.out.println("========================================");

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 验证 Dify API URL
        if (isBlank(difyApiUrl)) {
            errors.add("DIFY_API_URL 未设置。请在 .env 文件中设置 Dify API 基础 URL");
        } else {
            System.out.println("✓ DIFY_API_URL: " + maskString(difyApiUrl));
        }

        // 验证 Workflow API Key
        if (isBlank(difyWorkflowApiKey)) {
            errors.add("DIFY_WORKFLOW_API_KEY 未设置。请在 .env 文件中设置 Workflow API Key");
        } else {
            System.out.println("✓ DIFY_WORKFLOW_API_KEY: " + maskApiKey(difyWorkflowApiKey));
        }

        // 验证 Chatflow API Key
        if (isBlank(difyChatflowApiKey)) {
            errors.add("DIFY_CHATFLOW_API_KEY 未设置。请在 .env 文件中设置 Chatflow API Key");
        } else {
            System.out.println("✓ DIFY_CHATFLOW_API_KEY: " + maskApiKey(difyChatflowApiKey));
        }

        // 验证 Dataset API Key
        if (isBlank(difyDatasetApiKey)) {
            errors.add("DIFY_DATASET_API_KEY 未设置。请在 .env 文件中设置 Dataset API Key");
        } else {
            System.out.println("✓ DIFY_DATASET_API_KEY: " + maskApiKey(difyDatasetApiKey));
        }

        // 验证知识库 ID
        if (isBlank(difyKnowledgeBaseId)) {
            errors.add("DIFY_KNOWLEDGE_BASE_ID 未设置。请在 .env 文件中设置公开知识库 ID");
        } else {
            System.out.println("✓ DIFY_KNOWLEDGE_BASE_ID: " + maskUuid(difyKnowledgeBaseId));
        }

        if (isBlank(difyKnowledgePrivateBaseId)) {
            errors.add("DIFY_KNOWLEDGE_PRIVATE_BASE_ID 未设置。请在 .env 文件中设置私有知识库 ID");
        } else {
            System.out.println("✓ DIFY_KNOWLEDGE_PRIVATE_BASE_ID: " + maskUuid(difyKnowledgePrivateBaseId));
        }

        // 验证功能切换标志
        if (isBlank(workflowType)) {
            warnings.add("FEATURE_FLAG_WORKFLOW_TYPE 未设置，使用默认值: workflow");
            workflowType = "workflow";
        } else if (!isValidWorkflowType(workflowType)) {
            errors.add(String.format(
                "FEATURE_FLAG_WORKFLOW_TYPE 设置无效: '%s'。有效值为: workflow, chatflow",
                workflowType
            ));
        } else {
            System.out.println("✓ FEATURE_FLAG_WORKFLOW_TYPE: " + workflowType);
        }

        // 验证 JWT 密钥
        if (isBlank(jwtSecretKey)) {
            errors.add("JWT_SECRET_KEY 未设置。请在 .env 文件中设置 JWT 签名密钥");
        } else if (jwtSecretKey.length() < MIN_JWT_KEY_LENGTH) {
            errors.add(String.format(
                "JWT_SECRET_KEY 长度不足。当前长度: %d，要求至少 %d 个字符。" +
                "请使用更长的密钥，建议使用随机生成的复杂字符串（如: openssl rand -base64 32）",
                jwtSecretKey.length(),
                MIN_JWT_KEY_LENGTH
            ));
        } else {
            System.out.println("✓ JWT_SECRET_KEY: 长度=" + jwtSecretKey.length() + "（符合要求）");
        }

        // 验证 JWT 过期时间
        if (jwtExpirationTime <= 0) {
            warnings.add("JWT_EXPIRATION_TIME 设置无效，使用默认值: 7200000ms（2小时）");
        } else {
            System.out.println("✓ JWT_EXPIRATION_TIME: " + jwtExpirationTime + "ms");
        }

        // 输出警告信息
        if (!warnings.isEmpty()) {
            System.out.println("\n⚠ 配置警告:");
            for (String warning : warnings) {
                System.out.println("  - " + warning);
            }
        }

        // 输出错误信息并终止应用
        if (!errors.isEmpty()) {
            System.err.println("\n========================================");
            System.err.println("配置验证失败！请修复以下错误:");
            System.err.println("========================================");
            for (int i = 0; i < errors.size(); i++) {
                System.err.println((i + 1) + ". " + errors.get(i));
            }
            System.err.println("\n请检查 .env 文件或环境变量设置，然后重新启动应用。");
            System.err.println("========================================");
            throw new IllegalStateException("配置验证失败: " + errors.size() + " 个错误");
        }

        System.out.println("\n========================================");
        System.out.println("✓ 配置验证通过！应用启动中...");
        System.out.println("========================================");
    }

    /**
     * 检查字符串是否为空或空白
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 验证 workflowType 是否有效
     */
    private boolean isValidWorkflowType(String type) {
        for (String validType : VALID_WORKFLOW_TYPES) {
            if (validType.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 掩码 API Key，只显示前8位
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "***";
        }
        return apiKey.substring(0, 8) + "***";
    }

    /**
     * 掩码 UUID，只显示前8位
     */
    private String maskUuid(String uuid) {
        if (uuid == null || uuid.length() <= 8) {
            return "***";
        }
        return uuid.substring(0, 8) + "***";
    }

    /**
     * 掩码一般字符串
     */
    private String maskString(String str) {
        if (str == null || str.length() <= 10) {
            return str;
        }
        return str.substring(0, Math.min(str.length(), 30)) + "...";
    }
}

package com.zhuofeng.ai_consult_backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Dotenv 环境后置处理器
 * 
 * 在 Spring Boot 启动早期加载 .env 文件中的环境变量
 * 确保在其他 Bean 初始化之前，环境变量已经可用
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    // 需要加载到系统属性的环境变量列表
    private static final Set<String> ENV_VARS = Set.of(
        "DIFY_API_URL",
        "DIFY_WORKFLOW_API_KEY",
        "DIFY_CHATFLOW_API_KEY",
        "DIFY_DATASET_API_KEY",
        "DIFY_KNOWLEDGE_BASE_ID",
        "DIFY_KNOWLEDGE_PRIVATE_BASE_ID",
        "DIFY_WORKFLOW_ID",
        "DIFY_WEBHOOK_URL",
        "DIFY_WEBHOOK_DEBUG_URL",
        "FEATURE_FLAG_WORKFLOW_TYPE",
        "JWT_SECRET_KEY",
        "JWT_EXPIRATION_TIME",
        "DB_URL",
        "DB_DRIVER",
        "DB_USERNAME",
        "DB_PASSWORD",
        "MAX_FILE_SIZE",
        "MAX_REQUEST_SIZE"
    );

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            // 加载 .env 文件
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")  // 从当前目录加载
                    .ignoreIfMissing() // 如果文件不存在则忽略
                    .load();

            System.out.println("========================================");
            System.out.println("DotenvEnvironmentPostProcessor: 正在加载 .env 文件...");
            System.out.println("========================================");

            Map<String, Object> envProperties = new HashMap<>();
            int loadedCount = 0;
            
            for (String varName : ENV_VARS) {
                String value = dotenv.get(varName);
                if (value != null && !value.isEmpty()) {
                    // 添加到环境属性中
                    envProperties.put(varName, value);
                    
                    // 同时设置系统属性
                    System.setProperty(varName, value);
                    
                    // 打印加载信息（敏感信息部分掩码）
                    if (varName.contains("KEY") || varName.contains("SECRET") || varName.contains("PASSWORD")) {
                        System.out.println("✓ " + varName + ": " + maskString(value));
                    } else {
                        System.out.println("✓ " + varName + ": " + value);
                    }
                    loadedCount++;
                }
            }

            // 将属性添加到 Spring 环境中
            if (!envProperties.isEmpty()) {
                MapPropertySource propertySource = new MapPropertySource("dotenv", envProperties);
                environment.getPropertySources().addFirst(propertySource);
            }

            System.out.println("========================================");
            System.out.println("成功加载 " + loadedCount + " 个环境变量");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("警告：加载 .env 文件失败: " + e.getMessage());
            System.err.println("将使用系统环境变量或 application.properties 中的默认值");
            e.printStackTrace();
        }
    }

    /**
     * 掩码敏感字符串
     */
    private String maskString(String str) {
        if (str == null || str.length() <= 8) {
            return "***";
        }
        return str.substring(0, 8) + "***";
    }
}

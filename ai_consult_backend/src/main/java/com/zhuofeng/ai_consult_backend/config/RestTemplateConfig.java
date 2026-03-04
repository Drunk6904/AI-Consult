package com.zhuofeng.ai_consult_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类。
 * 用于调用外部AI服务接口。
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 创建RestTemplate实例。
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

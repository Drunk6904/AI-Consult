package com.zhuofeng.ai_consult_backend.repository;

import com.zhuofeng.ai_consult_backend.entity.ChannelConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 渠道配置仓储。
 */
public interface ChannelConfigRepository extends JpaRepository<ChannelConfig, String> {
}

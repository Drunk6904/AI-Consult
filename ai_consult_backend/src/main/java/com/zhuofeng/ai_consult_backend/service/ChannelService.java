package com.zhuofeng.ai_consult_backend.service;

import com.zhuofeng.ai_consult_backend.entity.ChannelConfig;
import com.zhuofeng.ai_consult_backend.repository.ChannelConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 渠道服务（占位实现）。
 */
@Service
public class ChannelService {

    private final ChannelConfigRepository channelConfigRepository;

    public ChannelService(ChannelConfigRepository channelConfigRepository) {
        this.channelConfigRepository = channelConfigRepository;
    }

    /**
     * 获取渠道列表。
     * 若数据库为空，则自动初始化一组占位配置。
     */
    @Transactional
    public List<ChannelConfig> listChannels() {
        List<ChannelConfig> list = channelConfigRepository.findAll();
        if (!list.isEmpty()) {
            return list;
        }

        ChannelConfig web = new ChannelConfig();
        web.setChannelId("web-main");
        web.setChannelType("web");
        web.setChannelName("网页渠道");
        web.setWebhookUrl(null);
        web.setStatus("enabled");

        ChannelConfig wechat = new ChannelConfig();
        wechat.setChannelId("wechat-official");
        wechat.setChannelType("wechat");
        wechat.setChannelName("微信公众号");
        wechat.setWebhookUrl(null);
        wechat.setStatus("disabled");

        ChannelConfig dingtalk = new ChannelConfig();
        dingtalk.setChannelId("dingtalk-robot");
        dingtalk.setChannelType("dingtalk");
        dingtalk.setChannelName("钉钉机器人");
        dingtalk.setWebhookUrl(null);
        dingtalk.setStatus("disabled");

        return channelConfigRepository.saveAll(List.of(web, wechat, dingtalk));
    }
}

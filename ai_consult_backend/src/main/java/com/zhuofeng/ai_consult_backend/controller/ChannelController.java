package com.zhuofeng.ai_consult_backend.controller;

import com.zhuofeng.ai_consult_backend.dto.ApiResponse;
import com.zhuofeng.ai_consult_backend.entity.ChannelConfig;
import com.zhuofeng.ai_consult_backend.service.ChannelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 渠道控制器（占位接口）。
 */
@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    /**
     * 获取渠道列表。
     */
    @GetMapping
    public ApiResponse<List<ChannelConfig>> listChannels() {
        return ApiResponse.ok(channelService.listChannels());
    }
}

package com.zhuofeng.ai_consult_backend.controller;

import com.zhuofeng.ai_consult_backend.dto.ApiResponse;
import com.zhuofeng.ai_consult_backend.dto.chat.ChatHistoryResponse;
import com.zhuofeng.ai_consult_backend.dto.chat.ChatNewResponse;
import com.zhuofeng.ai_consult_backend.dto.chat.ChatSendRequest;
import com.zhuofeng.ai_consult_backend.dto.chat.ChatSendResponse;
import com.zhuofeng.ai_consult_backend.security.CurrentUserUtil;
import com.zhuofeng.ai_consult_backend.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 聊天控制器。
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 新建会话接口。
     */
    @PostMapping("/new")
    public ApiResponse<ChatNewResponse> newConversation() {
        String userId = CurrentUserUtil.getCurrentUserId();
        return ApiResponse.ok(chatService.newConversation(userId));
    }

    /**
     * 发送消息接口。
     */
    @PostMapping("/send")
    public ApiResponse<ChatSendResponse> send(@Valid @RequestBody ChatSendRequest request) {
        String userId = CurrentUserUtil.getCurrentUserId();
        return ApiResponse.ok(chatService.send(userId, request));
    }

    /**
     * 查询会话历史接口。
     */
    @GetMapping("/history")
    public ApiResponse<ChatHistoryResponse> history(@RequestParam String conversationId) {
        String userId = CurrentUserUtil.getCurrentUserId();
        return ApiResponse.ok(chatService.history(userId, conversationId));
    }
}

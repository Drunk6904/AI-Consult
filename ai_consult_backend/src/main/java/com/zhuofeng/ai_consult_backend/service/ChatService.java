package com.zhuofeng.ai_consult_backend.service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.zhuofeng.ai_consult_backend.dto.chat.AiReply;
import com.zhuofeng.ai_consult_backend.dto.chat.ChatHistoryResponse;
import com.zhuofeng.ai_consult_backend.dto.chat.ChatNewResponse;
import com.zhuofeng.ai_consult_backend.dto.chat.ChatSendRequest;
import com.zhuofeng.ai_consult_backend.dto.chat.ChatSendResponse;
import com.zhuofeng.ai_consult_backend.dto.chat.MessageView;
import com.zhuofeng.ai_consult_backend.dto.chat.SourceItem;
import com.zhuofeng.ai_consult_backend.entity.Conversation;
import com.zhuofeng.ai_consult_backend.entity.Message;
import com.zhuofeng.ai_consult_backend.entity.User;
import com.zhuofeng.ai_consult_backend.repository.ConversationRepository;
import com.zhuofeng.ai_consult_backend.repository.MessageRepository;
import com.zhuofeng.ai_consult_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天业务服务。
 */
@Service
public class ChatService {

    /**
     * 深度咨询关键词。
     * 按任务要求，对未注册用户命中这些词时进行拦截。
     */
    private static final List<String> DEEP_KEYWORDS = List.of(
            "合同", "条款", "发票", "定制", "部署", "接口", "折扣", "法务", "私有化", "对接", "二开"
    );

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AiClient aiClient;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private ObjectMapper objectMapper;

    public ChatService(ConversationRepository conversationRepository,
                       MessageRepository messageRepository,
                       UserRepository userRepository,
                       AiClient aiClient) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.aiClient = aiClient;
    }

    /**
     * 创建新会话。
     */
    @Transactional
    public ChatNewResponse newConversation(String userId) {
        userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        Conversation saved = conversationRepository.save(conversation);
        return new ChatNewResponse(saved.getId());
    }

    /**
     * 发送消息（严格按任务要求的6步执行）：
     * 1) 校验conversation归属；
     * 2) 保存用户消息；
     * 3) 未注册关键词拦截；
     * 4) 否则调用AI；
     * 5) 保存AI回复；
     * 6) 返回响应。
     */
    @Transactional
    public ChatSendResponse send(String userId, ChatSendRequest request) {
        // 1) 校验会话归属，防止用户越权访问他人会话。
        Conversation conversation = conversationRepository.findByIdAndUserId(request.getConversationId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("会话不存在或无权限访问"));

        // 2) 保存用户消息。
        Message userMessage = new Message();
        userMessage.setConversationId(conversation.getId());
        userMessage.setSenderType("user");
        userMessage.setContent(request.getContent());
        userMessage.setSourcesJson(null);
        messageRepository.save(userMessage);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        String aiReplyText;
        List<SourceItem> sources;

        // 3) 未注册用户命中深度咨询关键词时直接拦截。
        // 这是任务B要求的权限分级策略：未注册用户不能访问深度咨询能力。
        if (!Boolean.TRUE.equals(user.getIsRegistered()) && hitDeepKeyword(request.getContent())) {
            aiReplyText = "请注册后进行深度咨询";
            sources = new ArrayList<>();
        } else {
            // 4) 调用AI客户端（内部已实现真实调用+失败降级）。
            AiReply aiReply = aiClient.askAi(
                    conversation.getId(),
                    userId,
                    Boolean.TRUE.equals(user.getIsRegistered()),
                    request.getContent()
            );
            aiReplyText = aiReply.getAnswer();
            sources = aiReply.getSources();
        }

        // 5) 保存AI回复。
        Message aiMessage = new Message();
        aiMessage.setConversationId(conversation.getId());
        aiMessage.setSenderType("ai");
        aiMessage.setContent(aiReplyText);
        aiMessage.setSourcesJson(writeSourcesJson(sources));
        messageRepository.save(aiMessage);

        // 6) 返回前端。
        return new ChatSendResponse(conversation.getId(), aiReplyText, sources);
    }

    /**
     * 查询会话历史。
     */
    @Transactional(readOnly = true)
    public ChatHistoryResponse history(String userId, String conversationId) {
        conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("会话不存在或无权限访问"));

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        List<MessageView> views = messages.stream()
                .map(msg -> new MessageView(
                        msg.getSenderType(),
                        msg.getContent(),
                        msg.getCreatedAt(),
                        readSourcesJson(msg.getSourcesJson())
                ))
                .toList();

        return new ChatHistoryResponse(conversationId, views);
    }

    private boolean hitDeepKeyword(String content) {
        if (!StringUtils.hasText(content)) {
            return false;
        }
        return DEEP_KEYWORDS.stream().anyMatch(content::contains);
    }

    private String writeSourcesJson(List<SourceItem> sources) {
        try {
            return objectMapper.writeValueAsString(sources == null ? List.of() : sources);
        } catch (Exception ex) {
            return "[]";
        }
    }

    private List<SourceItem> readSourcesJson(String sourcesJson) {
        if (!StringUtils.hasText(sourcesJson)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(sourcesJson, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }
}

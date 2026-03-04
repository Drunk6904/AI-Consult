<template>
  <div class="chat-window" :class="{ 'chat-window--open': isOpen }">
    <!-- 聊天窗口头部 -->
    <div class="chat-window__header" @click="toggleChat">
      <div class="chat-window__header__title">
        <span>智能客服</span>
        <span class="chat-window__header__badge" v-if="unreadCount > 0">{{ unreadCount }}</span>
      </div>
      <div class="chat-window__header__controls">
        <button class="chat-window__header__close" @click.stop="isOpen = false">×</button>
      </div>
    </div>

    <!-- 聊天窗口主体 -->
    <div class="chat-window__body" v-if="isOpen">
      <!-- 消息列表 -->
      <div class="chat-window__messages" ref="messagesContainer">
        <div 
          v-for="(message, index) in messages" 
          :key="index"
          :class="['chat-window__message', `chat-window__message--${message.role}`]"
        >
          <div class="chat-window__message__content">
            <div v-if="message.role === 'assistant'" class="chat-window__message__avatar">AI</div>
            <div v-else class="chat-window__message__avatar">我</div>
            <div class="chat-window__message__text" v-html="formatMessage(message.content)"></div>
            <div v-if="message.sources && message.sources.length > 0" class="chat-window__message__sources">
              <div class="chat-window__message__sources__title">参考来源：</div>
              <div 
                v-for="(source, sourceIndex) in message.sources" 
                :key="sourceIndex"
                class="chat-window__message__sources__item"
              >
                {{ source.doc_name }} (第{{ source.page }}页)
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-window__input-area">
        <input 
          type="text" 
          v-model="inputMessage"
          placeholder="请输入您的问题..."
          class="chat-window__input"
          @keyup.enter="sendMessage"
        />
        <button class="chat-window__send-button" @click="sendMessage">发送</button>
      </div>
    </div>

    <!-- 聊天窗口触发器 -->
    <div class="chat-window__trigger" v-if="!isOpen" @click="isOpen = true">
      <div class="chat-window__trigger__icon">💬</div>
      <div class="chat-window__trigger__text">在线客服</div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatWindow',
  data() {
    return {
      isOpen: false,
      inputMessage: '',
      messages: [
        {
          role: 'assistant',
          content: '您好！我是智能客服助手，有什么可以帮助您的吗？',
          sources: []
        }
      ],
      unreadCount: 0,
      sessionId: null
    }
  },
  methods: {
    toggleChat() {
      this.isOpen = !this.isOpen
      if (this.isOpen) {
        this.unreadCount = 0
        this.scrollToBottom()
      }
    },
    sendMessage() {
      if (!this.inputMessage.trim()) return

      // 添加用户消息
      const userMessage = {
        role: 'user',
        content: this.inputMessage
      }
      this.messages.push(userMessage)
      this.inputMessage = ''
      this.scrollToBottom()

      // 模拟AI回复
      setTimeout(() => {
        const aiMessage = {
          role: 'assistant',
          content: '这是一个模拟的AI回复，实际项目中会调用后端API获取真实回答。',
          sources: [
            {
              doc_name: '产品手册.pdf',
              page: 10
            }
          ]
        }
        this.messages.push(aiMessage)
        this.scrollToBottom()
      }, 1000)
    },
    scrollToBottom() {
      setTimeout(() => {
        if (this.$refs.messagesContainer) {
          this.$refs.messagesContainer.scrollTop = this.$refs.messagesContainer.scrollHeight
        }
      }, 100)
    },
    formatMessage(content) {
      // 简单的Markdown支持
      return content
        .replace(/\*(.*?)\*/g, '<strong>$1</strong>')
        .replace(/\n/g, '<br>')
    }
  }
}
</script>

<style scoped>
.chat-window {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 350px;
  max-height: 500px;
  background: #ffffff;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.chat-window__header {
  background: #4a90e2;
  color: white;
  padding: 12px 16px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-window__header__title {
  font-weight: 600;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.chat-window__header__badge {
  background: #ff4757;
  color: white;
  border-radius: 10px;
  padding: 2px 8px;
  font-size: 12px;
  margin-left: 8px;
}

.chat-window__header__close {
  background: none;
  border: none;
  color: white;
  font-size: 18px;
  cursor: pointer;
  padding: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-window__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 400px;
}

.chat-window__messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-window__message {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 18px;
  line-height: 1.4;
}

.chat-window__message--user {
  align-self: flex-end;
  background: #e3f2fd;
  border-bottom-right-radius: 4px;
}

.chat-window__message--assistant {
  align-self: flex-start;
  background: #f5f5f5;
  border-bottom-left-radius: 4px;
}

.chat-window__message__content {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.chat-window__message__avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #4a90e2;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.chat-window__message__text {
  flex: 1;
  font-size: 14px;
  word-break: break-word;
}

.chat-window__message__sources {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
  padding-top: 8px;
  border-top: 1px solid #eee;
}

.chat-window__message__sources__title {
  font-weight: 500;
  margin-bottom: 4px;
}

.chat-window__message__sources__item {
  margin-left: 12px;
  list-style: disc;
}

.chat-window__input-area {
  padding: 12px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 8px;
}

.chat-window__input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.chat-window__input:focus {
  border-color: #4a90e2;
}

.chat-window__send-button {
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: background-color 0.2s;
}

.chat-window__send-button:hover {
  background: #357abd;
}

.chat-window__trigger {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background: #4a90e2;
  color: white;
  border-radius: 50px;
  padding: 12px 20px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  transition: transform 0.2s, background-color 0.2s;
}

.chat-window__trigger:hover {
  transform: scale(1.05);
  background: #357abd;
}

.chat-window__trigger__icon {
  font-size: 18px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .chat-window {
    width: 90vw;
    right: 5vw;
    left: 5vw;
  }
}
</style>
<template>
  <div class="chat-content">
    <div class="chat-content__messages" ref="messagesContainer">
      <div 
        v-for="(message, index) in messages" 
        :key="message.id || index"
        :class="['chat-content__message', `chat-content__message--${message.role}`]"
      >
        <div class="chat-content__message__avatar">
          {{ message.role === 'assistant' ? 'AI' : '我' }}
        </div>
        <div class="chat-content__message__body">
          <div class="chat-content__message__text" v-html="formatMessage(message.content)"></div>
          <div v-if="message.sources && message.sources.length > 0" class="chat-content__message__sources">
            <div class="chat-content__message__sources__title">参考来源：</div>
            <div 
              v-for="(source, sourceIndex) in message.sources" 
              :key="sourceIndex"
              class="chat-content__message__sources__item"
            >
              {{ source.doc_name }} (第{{ source.page }}页)
            </div>
          </div>
          <div v-if="message.confidence" class="chat-content__message__confidence">
            置信度：{{ Math.round(message.confidence * 100) }}%
          </div>
          <div v-if="message.timestamp" class="chat-content__message__time">
            {{ formatTime(message.timestamp) }}
          </div>
        </div>
      </div>
      
      <div v-if="isLoading" class="chat-content__loading">
        <div class="chat-content__loading__spinner"></div>
        <span>AI 正在思考...</span>
      </div>
      
      <div v-if="error" class="chat-content__error">
        {{ error }}
      </div>
    </div>

    <div class="chat-content__input-area">
      <input 
        type="text" 
        v-model="inputMessage"
        placeholder="请输入您的问题..."
        class="chat-content__input"
        @keyup.enter="sendMessage"
        :disabled="isLoading"
      />
      <button 
        class="chat-content__send-button" 
        @click="sendMessage"
        :disabled="isLoading || !inputMessage.trim()"
      >
        发送
      </button>
    </div>
  </div>
</template>

<script>
import conversationService from '../services/conversationService'

export default {
  name: 'ChatContent',
  props: {
    sessionId: {
      type: String,
      required: true
    },
    initialMessages: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      messages: [],
      inputMessage: '',
      isLoading: false,
      error: null
    }
  },
  watch: {
    sessionId: {
      immediate: true,
      handler(newSessionId) {
        console.log('sessionId 变化:', newSessionId)
        if (newSessionId) {
          this.messages = []
          this.$nextTick(() => {
            this.loadSession(newSessionId)
          })
        }
      }
    },
    initialMessages: {
      immediate: true,
      handler(newMessages) {
        console.log('initialMessages 变化:', newMessages)
        if (newMessages && newMessages.length > 0) {
          this.messages = [...newMessages]
        } else {
          this.messages = []
        }
      }
    }
  },
  mounted() {
    this.scrollToBottom()
  },
  updated() {
    this.scrollToBottom()
  },
  methods: {
    loadSession(sessionId) {
      this.$emit('load-session', sessionId)
    },
    async sendMessage() {
      if (!this.inputMessage.trim()) return

      const userMessage = {
        role: 'user',
        content: this.inputMessage,
        timestamp: Date.now()
      }
      this.messages.push(userMessage)
      this.inputMessage = ''
      this.$emit('message-sent', userMessage)
      this.scrollToBottom()

      this.isLoading = true
      this.error = null

      try {
        // 获取 Dify 的 conversation_id
        const difyConversationId = conversationService.getDifyConversationId(this.sessionId)
        
        // 获取当前用户信息
        const storedUser = localStorage.getItem('user')
        const user = storedUser ? JSON.parse(storedUser) : null
        
        const requestBody = {
          query: userMessage.content,
          user: user ? user.username : 'anonymous',
          session_id: this.sessionId
        }
        
        // 如果有 Dify conversation_id，则传递给后端
        if (difyConversationId) {
          requestBody.conversation_id = difyConversationId
        }
        
        // 创建AI消息占位符（流式输出）
        const aiMessage = {
          role: 'assistant',
          content: '',
          sources: [],
          timestamp: Date.now()
        }
        this.messages.push(aiMessage)
        this.$emit('message-received', aiMessage)

        // 使用流式API
        const response = await fetch('/api/v1/chat/completions/stream', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            // 如果用户已登录，添加 token
            ...(localStorage.getItem('token') ? { 'Authorization': `Bearer ${localStorage.getItem('token')}` } : {})
          },
          body: JSON.stringify(requestBody)
        })

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }

        const reader = response.body.getReader()
        const decoder = new TextDecoder()
        let buffer = ''
        let fullAnswer = ''

        while (true) {
          const { done, value } = await reader.read()
          if (done) break

          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop() || ''

          for (const line of lines) {
            const trimmedLine = line.trim()
            if (trimmedLine.startsWith('data: ')) {
              const dataStr = trimmedLine.substring(6)
              try {
                const data = JSON.parse(dataStr)
                if (data.event === 'message' && data.answer) {
                  fullAnswer += data.answer
                  aiMessage.content = fullAnswer
                  this.scrollToBottom()
                }
                // 保存 Dify 返回的 conversation_id
                if (data.conversation_id && data.conversation_id !== difyConversationId) {
                  conversationService.setDifyConversationId(this.sessionId, data.conversation_id)
                  console.log('保存 Dify conversation_id:', data.conversation_id)
                }
              } catch (e) {
                console.error('解析 SSE 数据失败:', e)
              }
            }
          }
        }
      } catch (error) {
        console.error('发送消息失败:', error)
        this.error = '发送失败，请稍后再试'
        const errorMessage = {
          role: 'assistant',
          content: '抱歉，暂时无法处理您的请求，请稍后再试。',
          sources: [],
          timestamp: Date.now()
        }
        this.messages.push(errorMessage)
        this.$emit('message-received', errorMessage)
      } finally {
        this.isLoading = false
        this.scrollToBottom()
      }
    },
    scrollToBottom() {
      setTimeout(() => {
        if (this.$refs.messagesContainer) {
          this.$refs.messagesContainer.scrollTop = this.$refs.messagesContainer.scrollHeight
        }
      }, 100)
    },
    formatMessage(content) {
      return content
        .replace(/\*(.*?)\*/g, '<strong>$1</strong>')
        .replace(/\n/g, '<br>')
    },
    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      return `${hours}:${minutes}`
    }
  }
}
</script>

<style scoped>
.chat-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #ffffff;
}

.chat-content__messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #f9f9f9;
}

.chat-content__message {
  display: flex;
  gap: 8px;
  max-width: 85%;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chat-content__message--user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.chat-content__message--assistant {
  align-self: flex-start;
}

.chat-content__message__avatar {
  width: 32px;
  height: 32px;
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

.chat-content__message__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.chat-content__message--user .chat-content__message__body {
  background: #4a90e2;
  color: white;
  padding: 10px 14px;
  border-radius: 18px;
  border-bottom-right-radius: 4px;
}

.chat-content__message--assistant .chat-content__message__body {
  background: white;
  padding: 10px 14px;
  border-radius: 18px;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chat-content__message__text {
  font-size: 14px;
  word-break: break-word;
  line-height: 1.5;
}

.chat-content__message__sources {
  font-size: 11px;
  color: #666;
  padding-top: 6px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  margin-top: 4px;
}

.chat-content__message__sources__title {
  font-weight: 500;
  margin-bottom: 3px;
  color: #333;
}

.chat-content__message__sources__item {
  margin-left: 10px;
  list-style: disc;
}

.chat-content__message__confidence {
  font-size: 10px;
  color: #999;
  font-style: italic;
}

.chat-content__message__time {
  font-size: 10px;
  color: #999;
  text-align: right;
  margin-top: 2px;
}

.chat-content__message--user .chat-content__message__sources,
.chat-content__message--user .chat-content__message__sources__title,
.chat-content__message--user .chat-content__message__confidence,
.chat-content__message--user .chat-content__message__time {
  color: rgba(255, 255, 255, 0.9);
}

.chat-content__message--user .chat-content__message__sources {
  border-top-color: rgba(255, 255, 255, 0.3);
}

.chat-content__message--user .chat-content__message__sources__title {
  color: rgba(255, 255, 255, 1);
}

.chat-content__loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  color: #666;
  font-size: 14px;
  gap: 12px;
  align-self: flex-start;
}

.chat-content__loading__spinner {
  width: 20px;
  height: 20px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #4a90e2;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.chat-content__error {
  padding: 12px;
  background: #fee;
  color: #c33;
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
}

.chat-content__input-area {
  padding: 20px 24px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 12px;
  background: white;
}

.chat-content__input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 24px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.chat-content__input:focus {
  border-color: #4a90e2;
}

.chat-content__input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.chat-content__send-button {
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 24px;
  padding: 0 24px;
  min-width: 80px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.2s;
}

.chat-content__send-button:hover:not(:disabled) {
  background: #357abd;
}

.chat-content__send-button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>

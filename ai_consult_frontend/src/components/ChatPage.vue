<template>
  <div>
    <div class="chat-window" v-if="isOpen">
      <div class="chat-window__header" @click="toggleChat">
        <div class="chat-window__header__title">
          <span>智能客服</span>
          <span class="chat-window__header__badge" v-if="unreadCount > 0">{{ unreadCount }}</span>
        </div>
        <div class="chat-window__header__controls">
          <button class="chat-window__header__close" @click.stop="isOpen = false">×</button>
        </div>
      </div>
      
      <div class="chat-window__body">
        <div class="chat-window__content">
          <div class="chat-window__sidebar">
            <ConversationList 
              :currentSessionId="currentSessionId"
              @conversation-select="handleConversationSelect"
              @conversation-deleted="handleConversationDeleted"
            />
          </div>
          
          <div class="chat-window__main">
            <div v-if="!currentSessionId" class="chat-window__empty">
              <div class="chat-window__empty__icon">💬</div>
              <div class="chat-window__empty__title">欢迎使用智能客服</div>
              <div class="chat-window__empty__subtitle">选择一个会话或创建新会话开始聊天</div>
              <button class="chat-window__empty__button" @click="createNewConversation">
                新建会话
              </button>
            </div>
            
            <div v-else class="chat-window__chat">
              <ChatContent
                ref="chatContent"
                :sessionId="currentSessionId"
                :initialMessages="currentConversation?.messages || []"
                @message-sent="handleMessageSent"
                @message-received="handleMessageReceived"
                @load-session="handleLoadSession"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="chat-window__trigger" v-else @click="isOpen = true">
      <div class="chat-window__trigger__icon">💬</div>
      <div class="chat-window__trigger__text">在线客服</div>
    </div>
  </div>
</template>

<script>
import ConversationList from './ConversationList.vue'
import ChatContent from './ChatContent.vue'
import conversationService from '../services/conversationService'

export default {
  name: 'ChatPage',
  components: {
    ConversationList,
    ChatContent
  },
  data() {
    return {
      isOpen: false,
      currentSessionId: null,
      currentConversation: null,
      unreadCount: 0
    }
  },
  mounted() {
    this.loadCurrentSession()
  },
  methods: {
    toggleChat() {
      this.isOpen = !this.isOpen
      if (this.isOpen) {
        this.unreadCount = 0
      }
    },
    loadCurrentSession() {
      const sessionId = conversationService.getCurrentSessionId()
      if (sessionId) {
        this.currentSessionId = sessionId
        this.loadConversation(sessionId)
      } else {
        const data = conversationService.getConversations()
        if (data.conversations.length > 0) {
          const latest = data.conversations.sort((a, b) => b.updatedAt - a.updatedAt)[0]
          this.currentSessionId = latest.id
          this.loadConversation(latest.id)
        }
      }
    },
    loadConversation(sessionId) {
      const conversation = conversationService.getConversation(sessionId)
      this.currentConversation = conversation
      console.log('加载会话:', sessionId, conversation)
    },
    handleConversationSelect(sessionId) {
      console.log('切换会话:', sessionId)
      this.currentSessionId = sessionId
      this.$nextTick(() => {
        this.loadConversation(sessionId)
      })
    },
    handleConversationDeleted(deletedId) {
      if (this.currentSessionId === deletedId) {
        this.currentSessionId = null
        this.currentConversation = null
      }
    },
    handleMessageSent(message) {
      if (this.currentSessionId) {
        conversationService.addMessageToConversation(this.currentSessionId, message)
        this.loadConversation(this.currentSessionId)
      }
    },
    handleMessageReceived(message) {
      if (this.currentSessionId) {
        conversationService.addMessageToConversation(this.currentSessionId, message)
        this.loadConversation(this.currentSessionId)
      }
    },
    handleLoadSession(sessionId) {
      this.loadConversation(sessionId)
    },
    createNewConversation() {
      const conversation = conversationService.createNewConversation()
      this.currentSessionId = conversation.id
      this.currentConversation = conversation
    }
  }
}
</script>

<style scoped>
.chat-window {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 800px;
  height: 500px;
  background: white;
  border-radius: 12px;
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
  overflow: hidden;
}

.chat-window__content {
  display: flex;
  height: 100%;
}

.chat-window__sidebar {
  width: 240px;
  min-width: 240px;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.chat-window__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-window__empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f9f9f9;
  padding: 20px;
  text-align: center;
}

.chat-window__empty__icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.chat-window__empty__title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.chat-window__empty__subtitle {
  font-size: 12px;
  color: #666;
  margin-bottom: 16px;
}

.chat-window__empty__button {
  padding: 8px 24px;
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.chat-window__empty__button:hover {
  background: #357abd;
  transform: translateY(-2px);
}

.chat-window__chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
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

@media (max-width: 768px) {
  .chat-window {
    width: 90vw;
    right: 5vw;
    left: 5vw;
    height: 70vh;
  }
  
  .chat-window__sidebar {
    width: 200px;
    min-width: 200px;
  }
}
</style>

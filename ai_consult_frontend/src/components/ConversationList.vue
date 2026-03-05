<template>
  <div class="conversation-list">
    <div class="conversation-list__header">
      <button class="conversation-list__new-button" @click="createNewConversation">
        <span class="conversation-list__new-button__icon">+</span>
        <span>新建会话</span>
      </button>
    </div>
    
    <div class="conversation-list__body">
      <div v-if="conversations.length === 0" class="conversation-list__empty">
        <div class="conversation-list__empty__icon">💬</div>
        <div class="conversation-list__empty__text">暂无历史会话</div>
        <div class="conversation-list__empty__hint">点击"新建会话"开始聊天</div>
      </div>
      
      <div 
        v-for="conversation in conversations" 
        :key="conversation.id"
        :class="['conversation-list__item', { 'conversation-list__item--active': conversation.id === currentSessionId }]"
        @click="selectConversation(conversation.id)"
      >
        <div class="conversation-list__item__content">
          <div class="conversation-list__item__icon">💬</div>
          <div class="conversation-list__item__info">
            <div class="conversation-list__item__title">{{ conversation.title }}</div>
            <div class="conversation-list__item__preview">{{ getPreview(conversation) }}</div>
            <div class="conversation-list__item__time">{{ formatTime(conversation.updatedAt) }}</div>
          </div>
        </div>
        <button 
          class="conversation-list__item__delete"
          @click.stop="deleteConversation(conversation.id)"
          title="删除会话"
        >
          ×
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import conversationService from '../services/conversationService'

export default {
  name: 'ConversationList',
  props: {
    currentSessionId: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      conversations: []
    }
  },
  mounted() {
    this.loadConversations()
  },
  methods: {
    loadConversations() {
      const data = conversationService.getConversations()
      this.conversations = data.conversations.sort((a, b) => b.updatedAt - a.updatedAt)
    },
    createNewConversation() {
      const conversation = conversationService.createNewConversation()
      this.loadConversations()
      this.$emit('conversation-select', conversation.id)
    },
    selectConversation(id) {
      if (id !== this.currentSessionId) {
        conversationService.setCurrentSessionId(id)
        this.$emit('conversation-select', id)
      }
    },
    deleteConversation(id) {
      if (confirm('确定要删除这个会话吗？')) {
        conversationService.deleteConversation(id)
        this.loadConversations()
        
        if (id === this.currentSessionId) {
          const remaining = this.conversations.filter(c => c.id !== id)
          if (remaining.length > 0) {
            this.$emit('conversation-select', remaining[0].id)
          } else {
            this.$emit('conversation-select', null)
          }
        }
        
        this.$emit('conversation-deleted', id)
      }
    },
    getPreview(conversation) {
      if (!conversation.messages || conversation.messages.length === 0) {
        return '暂无消息'
      }
      
      const lastMessage = conversation.messages[conversation.messages.length - 1]
      const preview = lastMessage.content
      const prefix = lastMessage.role === 'user' ? '我：' : 'AI: '
      return prefix + (preview.length > 30 ? preview.substring(0, 30) + '...' : preview)
    },
    formatTime(timestamp) {
      if (!timestamp) return ''
      
      const now = Date.now()
      const diff = now - timestamp
      
      const minute = 60 * 1000
      const hour = 60 * minute
      const day = 24 * hour
      
      if (diff < minute) {
        return '刚刚'
      } else if (diff < hour) {
        return Math.floor(diff / minute) + '分钟前'
      } else if (diff < day) {
        return Math.floor(diff / hour) + '小时前'
      } else if (diff < 7 * day) {
        return Math.floor(diff / day) + '天前'
      } else {
        const date = new Date(timestamp)
        const year = date.getFullYear()
        const month = (date.getMonth() + 1).toString().padStart(2, '0')
        const day = date.getDate().toString().padStart(2, '0')
        return `${year}-${month}-${day}`
      }
    }
  }
}
</script>

<style scoped>
.conversation-list {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f7fa;
}

.conversation-list__header {
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  background: white;
}

.conversation-list__new-button {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.2s, transform 0.2s;
}

.conversation-list__new-button:hover {
  background: #357abd;
  transform: translateY(-2px);
}

.conversation-list__new-button__icon {
  font-size: 18px;
  font-weight: bold;
}

.conversation-list__body {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.conversation-list__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #999;
  text-align: center;
}

.conversation-list__empty__icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.conversation-list__empty__text {
  font-size: 14px;
  margin-bottom: 8px;
  font-weight: 500;
}

.conversation-list__empty__hint {
  font-size: 12px;
  color: #bbb;
}

.conversation-list__item {
  display: flex;
  align-items: center;
  padding: 12px;
  margin-bottom: 4px;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.conversation-list__item:hover {
  background: #f0f7ff;
  transform: translateX(4px);
}

.conversation-list__item--active {
  background: #e3f2fd;
  border-color: #4a90e2;
}

.conversation-list__item__content {
  flex: 1;
  display: flex;
  gap: 12px;
  align-items: flex-start;
  overflow: hidden;
}

.conversation-list__item__icon {
  font-size: 20px;
  flex-shrink: 0;
}

.conversation-list__item__info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow: hidden;
}

.conversation-list__item__title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conversation-list__item__preview {
  font-size: 12px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conversation-list__item__time {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

.conversation-list__item__delete {
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  color: #999;
  font-size: 18px;
  cursor: pointer;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.2s;
  flex-shrink: 0;
}

.conversation-list__item:hover .conversation-list__item__delete {
  opacity: 1;
}

.conversation-list__item__delete:hover {
  background: #fee;
  color: #c33;
}

.conversation-list__body::-webkit-scrollbar {
  width: 6px;
}

.conversation-list__body::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.conversation-list__body::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.conversation-list__body::-webkit-scrollbar-thumb:hover {
  background: #aaa;
}
</style>

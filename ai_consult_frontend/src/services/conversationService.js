const STORAGE_KEY = 'chat_conversations'
const CURRENT_SESSION_KEY = 'chat_current_session_id'

export default {
  getConversations() {
    try {
      const data = localStorage.getItem(STORAGE_KEY)
      return data ? JSON.parse(data) : { conversations: [] }
    } catch (error) {
      console.error('Failed to get conversations:', error)
      return { conversations: [] }
    }
  },
  
  saveConversation(conversation) {
    try {
      const data = this.getConversations()
      const index = data.conversations.findIndex(c => c.id === conversation.id)
      if (index >= 0) {
        data.conversations[index] = conversation
      } else {
        data.conversations.unshift(conversation)
      }
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
      return true
    } catch (error) {
      console.error('Failed to save conversation:', error)
      return false
    }
  },
  
  getConversation(id) {
    try {
      const data = this.getConversations()
      return data.conversations.find(c => c.id === id) || null
    } catch (error) {
      console.error('Failed to get conversation:', error)
      return null
    }
  },
  
  deleteConversation(id) {
    try {
      const data = this.getConversations()
      data.conversations = data.conversations.filter(c => c.id !== id)
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
      
      if (this.getCurrentSessionId() === id) {
        localStorage.removeItem(CURRENT_SESSION_KEY)
      }
      
      return true
    } catch (error) {
      console.error('Failed to delete conversation:', error)
      return false
    }
  },
  
  getCurrentSessionId() {
    return localStorage.getItem(CURRENT_SESSION_KEY)
  },
  
  setCurrentSessionId(id) {
    localStorage.setItem(CURRENT_SESSION_KEY, id)
  },
  
  clearCurrentSessionId() {
    localStorage.removeItem(CURRENT_SESSION_KEY)
  },
  
  createNewConversation(title = '新会话') {
    const id = 'sess_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    const now = Date.now()
    const conversation = {
      id,
      title,
      messages: [
        {
          role: 'assistant',
          content: '您好！我是智能客服助手，有什么可以帮助您的吗？',
          sources: [],
          timestamp: now
        }
      ],
      createdAt: now,
      updatedAt: now
    }
    this.saveConversation(conversation)
    this.setCurrentSessionId(id)
    return conversation
  },
  
  updateConversationMessages(id, messages) {
    const conversation = this.getConversation(id)
    if (conversation) {
      conversation.messages = messages
      conversation.updatedAt = Date.now()
      
      if (messages.length > 0) {
        const firstUserMessage = messages.find(m => m.role === 'user')
        if (firstUserMessage) {
          conversation.title = firstUserMessage.content.substring(0, 20) + (firstUserMessage.content.length > 20 ? '...' : '')
        }
      }
      
      this.saveConversation(conversation)
      return conversation
    }
    return null
  },
  
  addMessageToConversation(id, message) {
    const conversation = this.getConversation(id)
    if (conversation) {
      conversation.messages.push(message)
      conversation.updatedAt = Date.now()
      
      if (conversation.messages.length === 2 && message.role === 'user') {
        conversation.title = message.content.substring(0, 20) + (message.content.length > 20 ? '...' : '')
      }
      
      this.saveConversation(conversation)
      return conversation
    }
    return null
  }
}

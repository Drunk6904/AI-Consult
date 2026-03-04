<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createConversation, getHistory, sendMessage, type ChatMessage, type SourceItem } from '../api/chat'
import { me, type UserInfo } from '../api/auth'

const router = useRouter()
const loading = ref(false)
const text = ref('')
const conversationId = ref('')
const userInfo = ref<UserInfo | null>(null)
const messages = ref<ChatMessage[]>([])

/**
 * 页面初始化：加载用户信息与会话数据。
 */
const init = async () => {
  try {
    userInfo.value = await me()
  } catch (_e) {
    ElMessage.error('登录态失效，请重新登录')
    logout()
    return
  }

  const localConversation = localStorage.getItem('ai_conversation_id')
  if (localConversation) {
    conversationId.value = localConversation
    await loadHistory()
    return
  }

  const created = await createConversation()
  conversationId.value = created.conversationId
  localStorage.setItem('ai_conversation_id', conversationId.value)
}

/**
 * 拉取历史消息。
 */
const loadHistory = async () => {
  if (!conversationId.value) {
    return
  }
  try {
    const history = await getHistory(conversationId.value)
    messages.value = history.messages
  } catch (_e) {
    const created = await createConversation()
    conversationId.value = created.conversationId
    localStorage.setItem('ai_conversation_id', conversationId.value)
    messages.value = []
  }
}

/**
 * 发送消息：
 * 1. 本地追加用户消息；
 * 2. 调用后端send接口；
 * 3. 追加AI回复（含sources展示数据）。
 */
const onSend = async () => {
  if (!text.value.trim() || loading.value || !conversationId.value) {
    return
  }
  const content = text.value.trim()
  text.value = ''

  messages.value.push({
    senderType: 'user',
    content,
    createdAt: new Date().toISOString(),
    sources: []
  })

  loading.value = true
  try {
    const resp = await sendMessage(conversationId.value, content)
    messages.value.push({
      senderType: 'ai',
      content: resp.aiReply,
      createdAt: new Date().toISOString(),
      sources: resp.sources || []
    })
  } catch (error: any) {
    ElMessage.error(error?.message || '发送失败')
  } finally {
    loading.value = false
  }
}

/**
 * 格式化引用来源文本。
 */
const formatSource = (source: SourceItem): string => {
  const doc = source.docName || '未知文档'
  const snippet = source.snippet || '无摘要'
  const locator = source.locator ? `（${source.locator}）` : ''
  return `${doc}：${snippet}${locator}`
}

const logout = () => {
  localStorage.removeItem('ai_token')
  localStorage.removeItem('ai_user')
  localStorage.removeItem('ai_conversation_id')
  router.push('/login')
}

onMounted(() => {
  init()
})
</script>

<template>
  <div class="chat-page">
    <header class="chat-header">
      <div>
        <h2>AI智能客服聊天</h2>
        <div v-if="userInfo" class="meta">
          用户：{{ userInfo.phone }}
          <span class="tag" :class="userInfo.isRegistered ? 'yes' : 'no'">
            {{ userInfo.isRegistered ? '已注册' : '未注册' }}
          </span>
        </div>
      </div>
      <div class="actions">
        <router-link to="/health">健康检查</router-link>
        <el-button plain @click="logout">退出登录</el-button>
      </div>
    </header>

    <main class="chat-body">
      <div v-if="messages.length === 0" class="empty">暂无消息，开始提问吧</div>

      <div v-for="(msg, idx) in messages" :key="idx" class="msg-row" :class="msg.senderType">
        <div class="bubble">
          <div class="content">{{ msg.content }}</div>

          <!-- DEV-013：AI回复下方展示sources -->
          <div v-if="msg.senderType === 'ai' && msg.sources?.length" class="sources">
            <div class="sources-title">引用来源：</div>
            <ul>
              <li v-for="(source, sourceIdx) in msg.sources" :key="sourceIdx">
                {{ formatSource(source) }}
              </li>
            </ul>
          </div>
        </div>
      </div>
    </main>

    <footer class="chat-input">
      <el-input
        v-model="text"
        type="textarea"
        :rows="3"
        placeholder="请输入你的问题..."
        @keydown.ctrl.enter.prevent="onSend"
      />
      <el-button type="primary" :loading="loading" @click="onSend">发送</el-button>
    </footer>
  </div>
</template>

<style scoped>
.chat-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f4f6fb;
}

.chat-header {
  height: 72px;
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-header h2 {
  margin: 0;
  font-size: 20px;
}

.meta {
  margin-top: 4px;
  color: #6b7280;
  font-size: 13px;
}

.tag {
  margin-left: 8px;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.tag.yes {
  background: #dcfce7;
  color: #166534;
}

.tag.no {
  background: #fee2e2;
  color: #991b1b;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 18px;
}

.empty {
  text-align: center;
  color: #6b7280;
  margin-top: 50px;
}

.msg-row {
  display: flex;
  margin-bottom: 12px;
}

.msg-row.user {
  justify-content: flex-end;
}

.msg-row.ai {
  justify-content: flex-start;
}

.bubble {
  max-width: min(700px, 86vw);
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 12px;
  line-height: 1.6;
}

.msg-row.user .bubble {
  background: #dbeafe;
}

.sources {
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px dashed #cbd5e1;
  font-size: 13px;
  color: #334155;
}

.sources-title {
  margin-bottom: 4px;
  font-weight: 600;
}

.sources ul {
  margin: 0;
  padding-left: 18px;
}

.chat-input {
  padding: 12px;
  background: #fff;
  border-top: 1px solid #e5e7eb;
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.chat-input .el-button {
  height: 40px;
}

@media (max-width: 768px) {
  .chat-header {
    height: auto;
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>

<script setup>
import { ref, onMounted, computed } from 'vue'
import FileUpload from './components/FileUpload.vue'
import ChatWindow from './components/ChatWindow.vue'
import ChatPage from './components/ChatPage.vue'
import AuthComponent from './components/AuthComponent.vue'
import KnowledgeBase from './components/KnowledgeBase.vue'
import RoleManagement from './views/RoleManagement.vue'
import UserManagement from './views/UserManagement.vue'

const healthStatus = ref(null)
const errorMessage = ref('')
const isLoading = ref(false)
const uploadHistory = ref([])
const user = ref(null)
const userRoles = ref([])
const documents = ref({ data: [] })
const documentsLoading = ref(false)
const documentsError = ref('')
const currentView = ref('home')

const checkHealth = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const response = await fetch('/api/health')
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    healthStatus.value = await response.json()
  } catch (error) {
    errorMessage.value = `Error: ${error.message}`
    healthStatus.value = null
  } finally {
    isLoading.value = false
  }
}

const handleUploadSuccess = (result) => {
  uploadHistory.value.unshift({
    id: Date.now(),
    fileName: result.fileName,
    fileSize: result.fileSize,
    fileType: result.fileType,
    timestamp: new Date().toLocaleString()
  })
}

const handleUploadError = (error) => {
  console.error('上传失败:', error)
}

const handleLoginSuccess = (userData) => {
  user.value = userData
  // 获取用户角色
  const storedRoles = localStorage.getItem('user_roles')
  if (storedRoles) {
    userRoles.value = JSON.parse(storedRoles)
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('user_roles')
  localStorage.removeItem('user_permissions')
  user.value = null
  userRoles.value = []
  currentView.value = 'home'
}

const isAdmin = computed(() => {
  return userRoles.value.includes('ADMIN') || userRoles.value.includes('SUPER_ADMIN')
})

const userPermissions = computed(() => {
  const permissions = localStorage.getItem('user_permissions')
  return permissions ? JSON.parse(permissions) : []
})

const switchView = (view) => {
  currentView.value = view
}

const fetchDocuments = async () => {
  documentsLoading.value = true
  documentsError.value = ''
  try {
    const response = await fetch('/api/v1/knowledge/documents')
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    const data = await response.json()
    if (data.success) {
      documents.value = data.data
    } else {
      throw new Error(data.message || 'Failed to fetch documents')
    }
  } catch (error) {
    documentsError.value = `Error: ${error.message}`
    documents.value = { data: [] }
  } finally {
    documentsLoading.value = false
  }
}

onMounted(() => {
  checkHealth()
  // 检查本地存储中的用户信息
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    user.value = JSON.parse(storedUser)
    // 当用户登录后，获取文档列表
    fetchDocuments()
  }
})
</script>

<template>
  <div class="container">
    <h1>AI 智能客服系统</h1>
    
    <!-- 用户认证 -->
    <section class="auth-section">
      <div v-if="user" class="user-info">
        <span>欢迎，{{ user.username }} ({{ user.roles ? user.roles.join(', ') : '' }})</span>
        <button @click="handleLogout" class="logout-btn">退出登录</button>
      </div>
      <div v-else>
        <AuthComponent @login-success="handleLoginSuccess" />
      </div>
    </section>
    
    <!-- 导航菜单（仅管理员可见） -->
    <section v-if="user && isAdmin" class="admin-nav">
      <div class="nav-tabs">
        <button 
          :class="['nav-tab', currentView === 'home' ? 'active' : '']"
          @click="switchView('home')"
        >首页</button>
        <button 
          :class="['nav-tab', currentView === 'roles' ? 'active' : '']"
          @click="switchView('roles')"
          v-if="userPermissions?.includes('role:manage')"
        >角色管理</button>
        <button 
          :class="['nav-tab', currentView === 'users' ? 'active' : '']"
          @click="switchView('users')"
          v-if="userPermissions?.includes('user:manage')"
        >用户管理</button>
      </div>
    </section>
    
    <!-- 管理后台内容 -->
    <div v-if="user && isAdmin" class="admin-content">
      <RoleManagement v-if="currentView === 'roles'" />
      <UserManagement v-if="currentView === 'users'" />
    </div>
    
    <!-- 首页内容 -->
    <div v-if="currentView === 'home' || !isAdmin">
      <!-- 健康检查 -->
      <section class="health-check" v-if="user">
        <h2>系统状态</h2>
        <button @click="checkHealth" :disabled="isLoading">
          {{ isLoading ? '检查中...' : '检查健康状态' }}
        </button>
        
        <div v-if="isLoading" class="loading">
          加载中...
        </div>
        
        <div v-else-if="healthStatus" class="status success">
          <p>后端服务状态：{{ healthStatus.ok ? '正常' : '异常' }}</p>
        </div>
        
        <div v-else-if="errorMessage" class="status error">
          <p>{{ errorMessage }}</p>
        </div>
      </section>
      
      <!-- 文件上传 -->
      <section class="file-upload-section" v-if="user">
        <FileUpload 
          @upload-success="handleUploadSuccess"
          @upload-error="handleUploadError"
        />
      </section>
      
      <!-- 上传历史 -->
      <section v-if="user && uploadHistory.length > 0" class="upload-history">
        <h2>上传历史</h2>
        <div class="history-list">
          <div v-for="item in uploadHistory" :key="item.id" class="history-item">
            <div class="item-info">
              <h3>{{ item.fileName }}</h3>
              <p>大小：{{ (item.fileSize / 1024 / 1024).toFixed(2) }} MB</p>
              <p>类型：{{ item.fileType }}</p>
              <p>时间：{{ item.timestamp }}</p>
            </div>
          </div>
        </div>
      </section>
      
      <!-- 知识库文档 -->
      <section v-if="user" class="knowledge-base">
        <KnowledgeBase :initialDocuments="documents" @refresh="fetchDocuments" />
      </section>
      
      <!-- 聊天窗口（悬浮窗模式，带历史记录） -->
      <ChatPage v-if="user" />
    </div>
  </div>
</template>

<style scoped>
.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

h1 {
  color: #333;
  margin-bottom: 2rem;
  text-align: center;
}

h2 {
  color: #555;
  margin-top: 2rem;
  margin-bottom: 1rem;
}

/* 管理员导航菜单 */
.admin-nav {
  margin-bottom: 20px;
  background: #f0f2f5;
  padding: 10px;
  border-radius: 8px;
}

.nav-tabs {
  display: flex;
  gap: 10px;
}

.nav-tab {
  padding: 10px 20px;
  border: none;
  background: white;
  color: #666;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.nav-tab:hover {
  background: #e6f7ff;
  color: #1890ff;
}

.nav-tab.active {
  background: #1890ff;
  color: white;
}

.admin-content {
  margin-top: 20px;
  margin-bottom: 20px;
}

.auth-section {
  margin-bottom: 2rem;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.history-list {
  margin-top: 15px;
}

.history-item {
  background-color: white;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.item-info h3 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
  color: #333;
  text-align: left;
}

.item-info p {
  margin: 5px 0;
  font-size: 14px;
  color: #666;
}

button {
  background-color: #4CAF50;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
}

button:hover {
  background-color: #45a049;
}

button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.loading {
  margin-top: 20px;
  color: #666;
}

.status {
  margin-top: 20px;
  padding: 15px;
  border-radius: 5px;
}

.success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.auth-section {
  margin-bottom: 2rem;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.user-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-info span {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.logout-btn {
  background-color: #f44336;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.logout-btn:hover {
  background-color: #d32f2f;
}

.knowledge-base {
  margin-top: 2rem;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.documents-list {
  margin-top: 15px;
}

.document-item {
  background-color: white;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow-x: auto;
}

.document-item pre {
  margin: 0;
  font-size: 14px;
  line-height: 1.4;
  color: #333;
}
</style>

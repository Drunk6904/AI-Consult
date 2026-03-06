<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from '../utils/axios'

const props = defineProps({
  initialDocuments: {
    type: Object,
    default: () => ({ data: [] })
  }
})

const emit = defineEmits(['refresh'])

const documents = ref(props.initialDocuments)
const isLoading = ref(false)
const error = ref('')
const selectedDocs = ref([])
const showDeleteDialog = ref(false)
const deleteLoading = ref(false)
const deleteMessage = ref('')
const deleteSuccess = ref(false)
const deleteTarget = ref(null) // 'single' or 'batch'
const currentDocId = ref(null)
const showDocDetail = ref(false)
const currentDoc = ref(null)
const searchKeyword = ref('')
const statusFilter = ref('all')
const selectedKnowledgeBase = ref('public') // 'public' or 'private'

const fetchDocuments = async () => {
  isLoading.value = true
  error.value = ''
  try {
    const isPublic = selectedKnowledgeBase.value === 'public'
    const response = await axios.get('/api/v1/knowledge/documents', {
      params: { isPublic }
    })
    if (response.data.success) {
      // 正确处理响应结构：response.data.data.data 包含文档数组
      documents.value = {
        data: response.data.data.data || [],
        total: response.data.data.total || 0
      }
    } else {
      throw new Error(response.data.message || 'Failed to fetch documents')
    }
  } catch (err) {
    error.value = `Error: ${err.message}`
  } finally {
    isLoading.value = false
  }
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (timestamp) => {
  if (!timestamp) return 'N/A'
  const date = new Date(timestamp * 1000)
  return date.toLocaleString()
}

const toggleSelect = (docId) => {
  const index = selectedDocs.value.indexOf(docId)
  if (index > -1) {
    selectedDocs.value.splice(index, 1)
  } else {
    selectedDocs.value.push(docId)
  }
}

const selectAll = () => {
  if (selectedDocs.value.length === filteredDocuments.value.length) {
    selectedDocs.value = []
  } else {
    selectedDocs.value = filteredDocuments.value.map(doc => doc.id)
  }
}

const confirmDeleteSingle = (docId) => {
  currentDocId.value = docId
  deleteTarget.value = 'single'
  showDeleteDialog.value = true
}

const confirmDeleteBatch = () => {
  if (selectedDocs.value.length === 0) return
  deleteTarget.value = 'batch'
  showDeleteDialog.value = true
}

const deleteDocument = async () => {
  if (!currentDocId.value) return
  
  deleteLoading.value = true
  deleteMessage.value = ''
  deleteSuccess.value = false
  
  try {
    const isPublic = selectedKnowledgeBase.value === 'public'
    const response = await axios.delete(`/api/v1/knowledge/documents/${currentDocId.value}`, {
      params: { isPublic }
    })
    if (response.data.success) {
      deleteSuccess.value = true
      deleteMessage.value = '文档删除成功'
      await fetchDocuments()
      selectedDocs.value = []
    } else {
      throw new Error(response.data.message || '删除失败')
    }
  } catch (err) {
    deleteSuccess.value = false
    deleteMessage.value = `删除失败: ${err.message}`
  } finally {
    deleteLoading.value = false
    setTimeout(() => {
      showDeleteDialog.value = false
      deleteMessage.value = ''
    }, 2000)
  }
}

const batchDeleteDocuments = async () => {
  if (selectedDocs.value.length === 0) return
  
  deleteLoading.value = true
  deleteMessage.value = ''
  deleteSuccess.value = false
  
  try {
    const isPublic = selectedKnowledgeBase.value === 'public'
    const response = await axios.delete('/api/v1/knowledge/documents/batch', {
      params: {
        documentIds: selectedDocs.value,
        isPublic
      }
    })
    if (response.data.success) {
      deleteSuccess.value = true
      deleteMessage.value = `成功删除 ${response.data.data?.deletedIds?.length || 0} 个文档`
      await fetchDocuments()
      selectedDocs.value = []
    } else {
      throw new Error(response.data.message || '批量删除失败')
    }
  } catch (err) {
    deleteSuccess.value = false
    deleteMessage.value = `批量删除失败: ${err.message}`
  } finally {
    deleteLoading.value = false
    setTimeout(() => {
      showDeleteDialog.value = false
      deleteMessage.value = ''
    }, 2000)
  }
}

const handleDelete = async () => {
  if (deleteTarget.value === 'single') {
    await deleteDocument()
  } else if (deleteTarget.value === 'batch') {
    await batchDeleteDocuments()
  }
}

const openDocDetail = (doc) => {
  currentDoc.value = doc
  showDocDetail.value = true
}

const closeDocDetail = () => {
  showDocDetail.value = false
  currentDoc.value = null
}

const filteredDocuments = computed(() => {
  let filtered = documents.value.data || []
  
  // 按名称搜索
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase().trim()
    filtered = filtered.filter(doc => 
      doc.name.toLowerCase().includes(keyword)
    )
  }
  
  // 按状态筛选
  if (statusFilter.value !== 'all') {
    filtered = filtered.filter(doc => 
      doc.indexing_status === statusFilter.value
    )
  }
  
  return filtered
})

const isAllSelected = computed(() => {
  return filteredDocuments.value.length > 0 && selectedDocs.value.length === filteredDocuments.value.length
})

onMounted(() => {
  if (props.initialDocuments.data && props.initialDocuments.data.length === 0) {
    fetchDocuments()
  }
})
</script>

<template>
  <div class="knowledge-base">
    <h2>知识库文档</h2>
    
    <div class="header">
      <div class="header-actions">
        <button @click="fetchDocuments" :disabled="isLoading" class="refresh-btn">
          {{ isLoading ? '加载中...' : '刷新文档列表' }}
        </button>
        <button 
          @click="confirmDeleteBatch" 
          :disabled="selectedDocs.length === 0 || isLoading" 
          class="delete-btn"
        >
          批量删除 ({{ selectedDocs.length }})
        </button>
      </div>
      <div class="filter-section">
        <div class="knowledge-base-filter">
          <select v-model="selectedKnowledgeBase" @change="fetchDocuments" class="filter-select">
            <option value="public">公开知识库</option>
            <option value="private">私有知识库</option>
          </select>
        </div>
        <div class="search-box">
          <input 
            type="text" 
            v-model="searchKeyword" 
            placeholder="搜索文档名称..." 
            class="search-input"
          >
        </div>
        <div class="status-filter">
          <select v-model="statusFilter" class="filter-select">
            <option value="all">全部状态</option>
            <option value="completed">已完成</option>
            <option value="processing">处理中</option>
            <option value="failed">失败</option>
          </select>
        </div>
      </div>
      <div v-if="documents.data && documents.data.length > 0" class="document-count">
        共 {{ documents.total || documents.data.length }} 个文档 (筛选后: {{ filteredDocuments.length }})
      </div>
    </div>
    
    <div v-if="isLoading" class="loading">
      加载中...
    </div>
    
    <div v-else-if="error" class="status error">
      <p>{{ error }}</p>
    </div>
    
    <div v-else-if="documents.data && documents.data.length > 0" class="documents-list">
      <div class="list-header">
        <input 
          type="checkbox" 
          :checked="isAllSelected" 
          @change="selectAll"
          class="select-all"
        >
        <span class="header-title">全部选择</span>
      </div>
      <div v-for="doc in filteredDocuments" :key="doc.id" class="document-card" @click="openDocDetail(doc)">
        <div class="document-header">
          <div class="doc-header-left">
            <input 
              type="checkbox" 
              :checked="selectedDocs.includes(doc.id)" 
              @change="toggleSelect(doc.id)"
              class="doc-select"
              @click.stop
            >
            <h3 class="document-title">{{ doc.name }}</h3>
          </div>
          <div class="doc-header-right">
            <span :class="['status-badge', doc.indexing_status]">
              {{ doc.indexing_status === 'completed' ? '已完成' : doc.indexing_status }}
            </span>
            <button 
              @click.stop="confirmDeleteSingle(doc.id)" 
              class="delete-btn-small"
              title="删除文档"
            >
              ×
            </button>
          </div>
        </div>
        
        <div class="document-info">
          <div class="info-row">
            <span class="label">文件大小:</span>
            <span class="value">{{ formatFileSize(doc.data_source_detail_dict?.upload_file?.size || 0) }}</span>
          </div>
          <div class="info-row">
            <span class="label">创建时间:</span>
            <span class="value">{{ formatDate(doc.created_at) }}</span>
          </div>
          <div class="info-row">
            <span class="label">词数:</span>
            <span class="value">{{ doc.word_count || 0 }}</span>
          </div>
          <div class="info-row">
            <span class="label">Token数:</span>
            <span class="value">{{ doc.tokens || 0 }}</span>
          </div>
          <div class="info-row">
            <span class="label">引用次数:</span>
            <span class="value">{{ doc.hit_count || 0 }}</span>
          </div>
        </div>
        
        <div class="document-meta">
          <span class="meta-item">
            来源: {{ doc.created_from || '未知' }}
          </span>
          <span class="meta-item">
            状态: {{ doc.display_status || '未知' }}
          </span>
          <span class="meta-item">
            知识库: {{ selectedKnowledgeBase === 'public' ? '公开' : '私有' }}
          </span>
        </div>
      </div>
    </div>
    
    <div v-else class="empty-state">
      <p>知识库中暂无文档</p>
      <p class="empty-hint">请上传文件到知识库</p>
    </div>
    
    <!-- 删除确认对话框 -->
    <div v-if="showDeleteDialog" class="dialog-overlay">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ deleteTarget === 'single' ? '删除文档' : '批量删除文档' }}</h3>
        </div>
        <div class="dialog-content">
          <p v-if="deleteTarget === 'single'">确定要删除这个文档吗？此操作不可恢复。</p>
          <p v-else>确定要删除选中的 {{ selectedDocs.length }} 个文档吗？此操作不可恢复。</p>
          <div v-if="deleteMessage" :class="['dialog-message', deleteSuccess ? 'success' : 'error']">
            {{ deleteMessage }}
          </div>
        </div>
        <div class="dialog-actions">
          <button 
            @click="showDeleteDialog = false" 
            :disabled="deleteLoading"
            class="btn-cancel"
          >
            取消
          </button>
          <button 
            @click="handleDelete" 
            :disabled="deleteLoading"
            class="btn-confirm"
          >
            {{ deleteLoading ? '删除中...' : '确定删除' }}
          </button>
        </div>
      </div>
    </div>
    
    <!-- 文档详情对话框 -->
    <div v-if="showDocDetail" class="dialog-overlay">
      <div class="dialog doc-detail-dialog">
        <div class="dialog-header">
          <h3>文档详情</h3>
          <button @click="closeDocDetail" class="close-btn">×</button>
        </div>
        <div class="dialog-content">
          <div v-if="currentDoc" class="doc-detail-content">
            <div class="detail-row">
              <span class="detail-label">文档名称:</span>
              <span class="detail-value">{{ currentDoc.name }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">文件大小:</span>
              <span class="detail-value">{{ formatFileSize(currentDoc.data_source_detail_dict?.upload_file?.size || 0) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">创建时间:</span>
              <span class="detail-value">{{ formatDate(currentDoc.created_at) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">词数:</span>
              <span class="detail-value">{{ currentDoc.word_count || 0 }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Token数:</span>
              <span class="detail-value">{{ currentDoc.tokens || 0 }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">引用次数:</span>
              <span class="detail-value">{{ currentDoc.hit_count || 0 }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">来源:</span>
              <span class="detail-value">{{ currentDoc.created_from || '未知' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">状态:</span>
              <span class="detail-value">{{ currentDoc.display_status || '未知' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">索引状态:</span>
              <span class="detail-value">{{ currentDoc.indexing_status === 'completed' ? '已完成' : currentDoc.indexing_status }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">文档ID:</span>
              <span class="detail-value">{{ currentDoc.id }}</span>
            </div>
          </div>
        </div>
        <div class="dialog-actions">
          <button @click="closeDocDetail" class="btn-cancel">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.knowledge-base {
  background-color: #f9f9f9;
  padding: 24px;
  border-radius: 8px;
  margin: 2rem 0;
  position: relative;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

h2 {
  color: #333;
  margin: 0 0 20px 0;
  font-size: 1.2rem;
}

.refresh-btn {
  background-color: #4CAF50;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.refresh-btn:hover:not(:disabled) {
  background-color: #45a049;
}

.refresh-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.delete-btn {
  background-color: #f44336;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.delete-btn:hover:not(:disabled) {
  background-color: #d32f2f;
}

.delete-btn:disabled {
  background-color: #ffcdd2;
  cursor: not-allowed;
  color: #b71c1c;
}

.document-count {
  color: #666;
  font-size: 14px;
}

.filter-section {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-box {
  position: relative;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  width: 200px;
  transition: all 0.3s;
}

.search-input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.status-filter {
  position: relative;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
}

.filter-select:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.loading {
  text-align: center;
  padding: 20px;
  color: #666;
}

.status {
  padding: 15px;
  border-radius: 5px;
  margin: 10px 0;
}

.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.documents-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 16px;
}

.list-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.select-all {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.header-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.document-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.document-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.document-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.doc-header-left {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex: 1;
}

.doc-select {
  width: 16px;
  height: 16px;
  margin-top: 4px;
  cursor: pointer;
}

.document-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.doc-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  text-transform: capitalize;
}

.status-badge.completed {
  background-color: #e8f5e8;
  color: #2e7d32;
}

.status-badge.processing {
  background-color: #fff3e0;
  color: #ef6c00;
}

.status-badge.failed {
  background-color: #ffebee;
  color: #c62828;
}

.delete-btn-small {
  background: none;
  border: none;
  font-size: 20px;
  color: #ff5252;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.delete-btn-small:hover {
  background-color: #ffebee;
}

.document-info {
  margin-bottom: 16px;
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.label {
  width: 80px;
  color: #666;
  font-weight: 500;
}

.value {
  color: #333;
  flex: 1;
}

.document-meta {
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 12px;
  color: #888;
}

.meta-item {
  display: inline-block;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.empty-hint {
  margin-top: 8px;
  font-size: 14px;
  color: #999;
}

/* 对话框样式 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  background: white;
  border-radius: 8px;
  padding: 24px;
  width: 400px;
  max-width: 90%;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 12px;
}

.dialog-header h3 {
  margin: 0;
  color: #333;
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  line-height: 1;
  transition: color 0.3s;
}

.close-btn:hover {
  color: #333;
}

.doc-detail-dialog {
  width: 500px;
  max-width: 90%;
}

.doc-detail-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.detail-label {
  width: 100px;
  font-weight: 500;
  color: #666;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: #333;
  word-break: break-all;
}

.dialog-content {
  margin-bottom: 20px;
}

.dialog-content p {
  margin: 0 0 12px 0;
  color: #333;
  line-height: 1.5;
}

.dialog-message {
  padding: 12px;
  border-radius: 4px;
  margin-top: 12px;
  font-size: 14px;
}

.dialog-message.success {
  background-color: #e8f5e8;
  color: #2e7d32;
  border: 1px solid #c8e6c9;
}

.dialog-message.error {
  background-color: #ffebee;
  color: #c62828;
  border: 1px solid #ffcdd2;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

.btn-cancel {
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background: white;
  color: #333;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-cancel:hover:not(:disabled) {
  border-color: #1890ff;
  color: #1890ff;
}

.btn-cancel:disabled {
  color: #999;
  border-color: #d9d9d9;
  cursor: not-allowed;
}

.btn-confirm {
  padding: 8px 16px;
  border: 1px solid #f44336;
  border-radius: 4px;
  background: #f44336;
  color: white;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-confirm:hover:not(:disabled) {
  background: #d32f2f;
  border-color: #d32f2f;
}

.btn-confirm:disabled {
  background: #ffcdd2;
  border-color: #ffcdd2;
  color: #b71c1c;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-actions {
    flex-direction: column;
    align-items: flex-start;
    width: 100%;
  }
  
  .refresh-btn,
  .delete-btn {
    width: 100%;
    text-align: center;
  }
  
  .filter-section {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .search-box,
  .status-filter {
    width: 100%;
  }
  
  .search-input,
  .filter-select {
    width: 100%;
  }
  
  .document-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .doc-header-left {
    width: 100%;
  }
  
  .doc-header-right {
    align-self: flex-start;
  }
  
  .status-badge {
    align-self: flex-start;
  }
  
  .dialog {
    width: 95%;
    padding: 16px;
  }
  
  .doc-detail-dialog {
    width: 95%;
    max-height: 90vh;
    overflow-y: auto;
  }
  
  .detail-row {
    flex-direction: column;
    gap: 4px;
  }
  
  .detail-label {
    width: 100%;
  }
}
</style>
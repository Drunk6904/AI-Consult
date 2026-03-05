<script setup>
import { ref, onMounted } from 'vue'
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

const fetchDocuments = async () => {
  isLoading.value = true
  error.value = ''
  try {
    const response = await axios.get('/api/v1/knowledge/documents')
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
      <button @click="fetchDocuments" :disabled="isLoading" class="refresh-btn">
        {{ isLoading ? '加载中...' : '刷新文档列表' }}
      </button>
      <div v-if="documents.data && documents.data.length > 0" class="document-count">
        共 {{ documents.total || documents.data.length }} 个文档
      </div>
    </div>
    
    <div v-if="isLoading" class="loading">
      加载中...
    </div>
    
    <div v-else-if="error" class="status error">
      <p>{{ error }}</p>
    </div>
    
    <div v-else-if="documents.data && documents.data.length > 0" class="documents-list">
      <div v-for="doc in documents.data" :key="doc.id" class="document-card">
        <div class="document-header">
          <h3 class="document-title">{{ doc.name }}</h3>
          <span :class="['status-badge', doc.indexing_status]">
            {{ doc.indexing_status === 'completed' ? '已完成' : doc.indexing_status }}
          </span>
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
        </div>
      </div>
    </div>
    
    <div v-else class="empty-state">
      <p>知识库中暂无文档</p>
      <p class="empty-hint">请上传文件到知识库</p>
    </div>
  </div>
</template>

<style scoped>
.knowledge-base {
  background-color: #f9f9f9;
  padding: 24px;
  border-radius: 8px;
  margin: 2rem 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

.document-count {
  color: #666;
  font-size: 14px;
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
  display: grid;
  gap: 16px;
  margin-top: 16px;
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

.document-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  flex: 1;
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

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .document-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .status-badge {
    align-self: flex-start;
  }
}
</style>
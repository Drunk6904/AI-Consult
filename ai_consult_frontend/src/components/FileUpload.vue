<script setup>
import { ref } from 'vue'

const props = defineProps({
  maxSize: {
    type: Number,
    default: 50 * 1024 * 1024 // 50MB
  }
})

const emit = defineEmits(['upload-success', 'upload-error'])

const fileInput = ref(null)
const selectedFiles = ref([])
const uploadProgress = ref(0)
const isUploading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const selectedKnowledgeBase = ref(true) // true 表示公开知识库，false 表示私有知识库

const allowedTypes = [
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'application/vnd.ms-excel',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
]

const allowedExtensions = ['.pdf', '.doc', '.docx', '.xls', '.xlsx']

const handleFileChange = (event) => {
  const files = Array.from(event.target.files)
  if (files.length === 0) return

  const validFiles = []
  const errors = []

  files.forEach(file => {
    // 验证文件类型
    if (!allowedTypes.includes(file.type) && !allowedExtensions.some(ext => file.name.toLowerCase().endsWith(ext))) {
      errors.push(`${file.name}：只支持PDF、Word、Excel格式文件`)
      return
    }

    // 验证文件大小
    if (file.size > props.maxSize) {
      errors.push(`${file.name}：文件大小不能超过 ${(props.maxSize / 1024 / 1024).toFixed(0)}MB`)
      return
    }

    validFiles.push(file)
  })

  if (errors.length > 0) {
    errorMessage.value = errors.join('\n')
  } else {
    errorMessage.value = ''
  }

  selectedFiles.value = validFiles
  successMessage.value = ''
}

const uploadFile = async () => {
  if (selectedFiles.value.length === 0) {
    errorMessage.value = '请先选择文件'
    return
  }

  isUploading.value = true
  uploadProgress.value = 0
  errorMessage.value = ''
  successMessage.value = ''

  try {
    for (let i = 0; i < selectedFiles.value.length; i++) {
      const file = selectedFiles.value[i]
      const formData = new FormData()
      formData.append('file', file)
      formData.append('isPublic', selectedKnowledgeBase.value.toString())

      const response = await fetch('/api/v1/knowledge/upload', {
        method: 'POST',
        body: formData,
        headers: {
          // 注意：不要设置Content-Type，让浏览器自动设置
          ...(localStorage.getItem('token') ? { 'Authorization': `Bearer ${localStorage.getItem('token')}` } : {})
        }
      })

      if (!response.ok) {
        throw new Error(`上传失败: ${response.status}`)
      }

      const result = await response.json()
      if (result.success) {
        emit('upload-success', result)
        // 更新上传进度
        uploadProgress.value = Math.round(((i + 1) / selectedFiles.value.length) * 100)
      } else {
        throw new Error(result.message || '上传失败')
      }
    }

    successMessage.value = `成功上传 ${selectedFiles.value.length} 个文件`
    // 清空选择的文件
    selectedFiles.value = []
    if (fileInput.value) {
      fileInput.value.value = ''
    }
  } catch (error) {
    errorMessage.value = `错误: ${error.message}`
    emit('upload-error', error)
  } finally {
    isUploading.value = false
    uploadProgress.value = 0
  }
}

const clearFile = () => {
  selectedFiles.value = []
  errorMessage.value = ''
  successMessage.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const removeFile = (index) => {
  selectedFiles.value.splice(index, 1)
  if (selectedFiles.value.length === 0) {
    if (fileInput.value) {
      fileInput.value.value = ''
    }
  }
}
</script>

<template>
  <div class="file-upload">
    <h3>文件上传</h3>
    
    <div class="file-input-container">
      <input
        ref="fileInput"
        type="file"
        @change="handleFileChange"
        :disabled="isUploading"
        accept=".pdf,.doc,.docx,.xls,.xlsx"
        multiple
        class="file-input"
      />
      <label class="file-label" :for="fileInput">
        {{ selectedFiles.length > 0 ? `已选择 ${selectedFiles.length} 个文件` : '选择文件' }}
      </label>
    </div>

    <div v-if="selectedFiles.length > 0" class="file-info">
      <div class="files-list">
        <div v-for="(file, index) in selectedFiles" :key="index" class="file-item">
          <div class="file-item-info">
            <span class="file-name">{{ file.name }}</span>
            <span class="file-size">{{ (file.size / 1024 / 1024).toFixed(2) }} MB</span>
          </div>
          <button @click="removeFile(index)" class="remove-file-btn" title="移除文件">×</button>
        </div>
      </div>
      
      <!-- 知识库选择 -->
      <div class="knowledge-base-selection">
        <label>选择知识库:</label>
        <div class="radio-group">
          <label class="radio-option">
            <input type="radio" v-model="selectedKnowledgeBase" :value="true" />
            <span>公开知识库 (30%)</span>
          </label>
          <label class="radio-option">
            <input type="radio" v-model="selectedKnowledgeBase" :value="false" />
            <span>私有知识库 (70%)</span>
          </label>
        </div>
      </div>
      
      <div class="file-actions">
        <button @click="clearFile" class="clear-btn">清除所有文件</button>
      </div>
    </div>

    <div class="upload-actions">
      <button
        @click="uploadFile"
        :disabled="selectedFiles.length === 0 || isUploading"
        class="upload-btn"
      >
        {{ isUploading ? `上传中... (${uploadProgress}%)` : `上传 ${selectedFiles.length} 个文件` }}
      </button>
    </div>

    <div v-if="isUploading" class="progress-container">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: uploadProgress + '%' }"></div>
      </div>
      <p class="progress-text">上传进度: {{ uploadProgress }}%</p>
    </div>

    <div v-if="errorMessage" class="message error">
      <pre>{{ errorMessage }}</pre>
    </div>

    <div v-if="successMessage" class="message success">
      {{ successMessage }}
    </div>
  </div>
</template>

<style scoped>
.file-upload {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #f9f9f9;
}

h3 {
  margin-top: 0;
  color: #333;
  text-align: center;
}

.file-input-container {
  position: relative;
  margin: 20px 0;
}

.file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
  z-index: 2;
}

.file-label {
  display: block;
  padding: 15px;
  background-color: #f0f0f0;
  border: 2px dashed #ccc;
  border-radius: 4px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.file-label:hover {
  border-color: #4CAF50;
  background-color: #f8f8f8;
}

.file-info {
  margin: 15px 0;
  padding: 15px;
  background-color: #f0f8f0;
  border-radius: 4px;
}

.files-list {
  margin-bottom: 15px;
  max-height: 200px;
  overflow-y: auto;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background-color: white;
  border-radius: 4px;
  margin-bottom: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.file-item-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.file-name {
  font-size: 14px;
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 12px;
  color: #666;
  white-space: nowrap;
}

.remove-file-btn {
  background: none;
  border: none;
  font-size: 18px;
  color: #ff5252;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.remove-file-btn:hover {
  background-color: #ffebee;
}

/* 知识库选择样式 */
.knowledge-base-selection {
  margin: 15px 0;
  padding: 15px;
  background-color: #f0f8ff;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
}

.knowledge-base-selection label {
  display: block;
  font-weight: 500;
  margin-bottom: 10px;
  color: #333;
}

.radio-group {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.radio-option:hover {
  background-color: rgba(255, 255, 255, 0.5);
}

.radio-option input[type="radio"] {
  accent-color: #4CAF50;
}

.file-actions {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}

.clear-btn {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.clear-btn:hover {
  background-color: #d32f2f;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .file-upload {
    padding: 15px;
  }
  
  .file-input-container {
    margin: 15px 0;
  }
  
  .file-label {
    padding: 20px;
    font-size: 16px;
  }
  
  .files-list {
    max-height: 150px;
  }
  
  .file-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .file-item-info {
    width: 100%;
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .file-name {
    width: 100%;
    white-space: normal;
    word-break: break-all;
  }
  
  .radio-group {
    flex-direction: column;
    gap: 10px;
  }
  
  .radio-option {
    width: 100%;
    justify-content: space-between;
  }
  
  .upload-actions {
    margin: 15px 0;
  }
  
  .upload-btn {
    width: 100%;
    padding: 12px 24px;
    font-size: 16px;
  }
  
  .progress-container {
    margin: 15px 0;
  }
  
  .message {
    margin-top: 10px;
  }
}

.upload-actions {
  margin: 20px 0;
  text-align: center;
}

.upload-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s ease;
}

.upload-btn:hover:not(:disabled) {
  background-color: #45a049;
}

.upload-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.progress-container {
  margin: 20px 0;
}

.progress-bar {
  width: 100%;
  height: 20px;
  background-color: #f0f0f0;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 10px;
}

.progress-fill {
  height: 100%;
  background-color: #4CAF50;
  transition: width 0.3s ease;
}

.progress-text {
  text-align: center;
  font-size: 14px;
  color: #666;
  margin: 0;
}

.message {
  padding: 10px;
  border-radius: 4px;
  margin-top: 15px;
  text-align: center;
}

.message.success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.message.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.message pre {
  margin: 0;
  white-space: pre-wrap;
  text-align: left;
}
</style>

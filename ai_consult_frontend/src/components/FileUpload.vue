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
const selectedFile = ref(null)
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
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!allowedTypes.includes(file.type) && !allowedExtensions.some(ext => file.name.toLowerCase().endsWith(ext))) {
    errorMessage.value = '只支持PDF、Word、Excel格式文件'
    selectedFile.value = null
    return
  }

  // 验证文件大小
  if (file.size > props.maxSize) {
    errorMessage.value = `文件大小不能超过 ${(props.maxSize / 1024 / 1024).toFixed(0)}MB`
    selectedFile.value = null
    return
  }

  selectedFile.value = file
  errorMessage.value = ''
  successMessage.value = ''
}

const uploadFile = async () => {
  if (!selectedFile.value) {
    errorMessage.value = '请先选择文件'
    return
  }

  const formData = new FormData()
  formData.append('file', selectedFile.value)
  formData.append('isPublic', selectedKnowledgeBase.value.toString())

  isUploading.value = true
  uploadProgress.value = 0
  errorMessage.value = ''
  successMessage.value = ''

  try {
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
      successMessage.value = '文件上传成功'
      emit('upload-success', result)
      // 清空选择的文件
      selectedFile.value = null
      if (fileInput.value) {
        fileInput.value.value = ''
      }
    } else {
      throw new Error(result.message || '上传失败')
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
  selectedFile.value = null
  errorMessage.value = ''
  successMessage.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
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
        class="file-input"
      />
      <label class="file-label" :for="fileInput">
        {{ selectedFile ? selectedFile.name : '选择文件' }}
      </label>
    </div>

    <div v-if="selectedFile" class="file-info">
      <p>文件名: {{ selectedFile.name }}</p>
      <p>大小: {{ (selectedFile.size / 1024 / 1024).toFixed(2) }} MB</p>
      
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
      
      <button @click="clearFile" class="clear-btn">清除</button>
    </div>

    <div class="upload-actions">
      <button
        @click="uploadFile"
        :disabled="!selectedFile || isUploading"
        class="upload-btn"
      >
        {{ isUploading ? '上传中...' : '上传文件' }}
      </button>
    </div>

    <div v-if="isUploading" class="progress-container">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: uploadProgress + '%' }"></div>
      </div>
      <p class="progress-text">上传进度: {{ uploadProgress }}%</p>
    </div>

    <div v-if="errorMessage" class="message error">
      {{ errorMessage }}
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

.file-info p {
  margin: 5px 0;
  font-size: 14px;
}

/* 知识库选择样式 */
.knowledge-base-selection {
  margin: 15px 0;
  padding: 10px;
  background-color: #f0f8ff;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
}

.knowledge-base-selection label {
  display: block;
  font-weight: 500;
  margin-bottom: 8px;
  color: #333;
}

.radio-group {
  display: flex;
  gap: 20px;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  cursor: pointer;
}

.radio-option input[type="radio"] {
  accent-color: #4CAF50;
}

.clear-btn {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin-top: 10px;
}

.clear-btn:hover {
  background-color: #d32f2f;
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
</style>

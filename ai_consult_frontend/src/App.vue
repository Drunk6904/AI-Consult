<script setup>
import { ref, onMounted } from 'vue'

const healthStatus = ref(null)
const errorMessage = ref('')
const isLoading = ref(false)

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

onMounted(() => {
  checkHealth()
})
</script>

<template>
  <div class="container">
    <h1>健康检查</h1>
    <button @click="checkHealth" :disabled="isLoading">
      {{ isLoading ? '检查中...' : '检查健康状态' }}
    </button>
    
    <div v-if="isLoading" class="loading">
      加载中...
    </div>
    
    <div v-else-if="healthStatus" class="status success">
      <h2>后端服务状态</h2>
      <p>状态: {{ healthStatus.ok ? '正常' : '异常' }}</p>
    </div>
    
    <div v-else-if="errorMessage" class="status error">
      <h2>错误</h2>
      <p>{{ errorMessage }}</p>
    </div>
  </div>
</template>

<style scoped>
.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
  text-align: center;
}

h1 {
  color: #333;
  margin-bottom: 2rem;
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
  padding: 20px;
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
</style>

<script setup lang="ts">
import { onMounted, ref } from 'vue'

const healthStatus = ref<{ ok: boolean } | null>(null)
const errorMessage = ref('')
const isLoading = ref(false)

/**
 * 健康检查请求。
 * 说明：保留原有 /health 页面能力，确保旧功能可运行。
 */
const checkHealth = async () => {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const response = await fetch('/api/health')
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    healthStatus.value = await response.json()
  } catch (error: any) {
    errorMessage.value = `Error: ${error?.message || '请求失败'}`
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
    <div class="actions">
      <el-button type="primary" :loading="isLoading" @click="checkHealth">重新检查</el-button>
      <router-link to="/login">去登录页</router-link>
    </div>

    <div v-if="healthStatus" class="status success">
      后端状态：{{ healthStatus.ok ? '正常' : '异常' }}
    </div>
    <div v-else-if="errorMessage" class="status error">
      {{ errorMessage }}
    </div>
  </div>
</template>

<style scoped>
.container {
  max-width: 760px;
  margin: 0 auto;
  padding: 36px 16px;
}

.actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.status {
  margin-top: 18px;
  padding: 14px;
  border-radius: 8px;
}

.success {
  background: #dcfce7;
  color: #166534;
}

.error {
  background: #fee2e2;
  color: #991b1b;
}
</style>

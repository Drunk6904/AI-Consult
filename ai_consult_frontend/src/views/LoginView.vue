<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '../api/auth'

const router = useRouter()
const mode = ref<'login' | 'register'>('login')
const loading = ref(false)

const form = reactive({
  phone: '',
  password: ''
})

/**
 * 登录/注册提交逻辑。
 */
const submit = async () => {
  if (!form.phone || !form.password) {
    ElMessage.warning('请输入手机号和密码')
    return
  }

  loading.value = true
  try {
    const result =
      mode.value === 'register'
        ? await register(form.phone, form.password)
        : await login(form.phone, form.password)

    localStorage.setItem('ai_token', result.token)
    localStorage.setItem('ai_user', JSON.stringify(result.user))
    ElMessage.success(mode.value === 'register' ? '注册成功' : '登录成功')
    await router.push('/chat')
  } catch (error: any) {
    ElMessage.error(error?.message || '请求失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <div class="title">AI智能客服系统</div>
      </template>

      <el-segmented
        v-model="mode"
        :options="[
          { label: '登录', value: 'login' },
          { label: '注册', value: 'register' }
        ]"
        block
      />

      <el-form class="form" label-position="top">
        <el-form-item label="手机号">
          <el-input v-model="form.phone" maxlength="11" placeholder="请输入11位手机号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button class="submit-btn" type="primary" :loading="loading" @click="submit">
          {{ mode === 'register' ? '注册并进入聊天' : '登录进入聊天' }}
        </el-button>
      </el-form>

      <div class="footer-link">
        <router-link to="/health">查看健康检查页</router-link>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: linear-gradient(135deg, #f7f9fc 0%, #e7efff 100%);
}

.login-card {
  width: 100%;
  max-width: 420px;
}

.title {
  text-align: center;
  font-size: 20px;
  font-weight: 700;
}

.form {
  margin-top: 16px;
}

.submit-btn {
  width: 100%;
}

.footer-link {
  margin-top: 14px;
  text-align: center;
}
</style>

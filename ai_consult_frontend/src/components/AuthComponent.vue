<template>
  <div class="auth-container">
    <div class="auth-form">
      <h2>{{ isRegister ? '用户注册' : '用户登录' }}</h2>
      
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
      
      <form @submit.prevent="handleSubmit">
        <div class="form-group" v-if="isRegister">
          <label for="email">邮箱</label>
          <input 
            type="email" 
            id="email" 
            v-model="formData.email" 
            required 
            placeholder="请输入邮箱"
          >
        </div>
        
        <div class="form-group">
          <label for="username">用户名</label>
          <input 
            type="text" 
            id="username" 
            v-model="formData.username" 
            required 
            placeholder="请输入用户名"
          >
        </div>
        
        <div class="form-group">
          <label for="password">密码</label>
          <input 
            type="password" 
            id="password" 
            v-model="formData.password" 
            required 
            placeholder="请输入密码"
          >
        </div>
        
        <button type="submit" class="submit-btn">{{ isRegister ? '注册' : '登录' }}</button>
      </form>
      
      <div class="toggle-form">
        <span v-if="isRegister">已有账号？</span>
        <span v-else>还没有账号？</span>
        <button @click="toggleForm" class="toggle-btn">{{ isRegister ? '去登录' : '去注册' }}</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AuthComponent',
  data() {
    return {
      isRegister: true,
      formData: {
        username: '',
        email: '',
        password: ''
      },
      error: ''
    }
  },
  methods: {
    toggleForm() {
      this.isRegister = !this.isRegister
      this.error = ''
    },
    async handleSubmit() {
      this.error = ''
      
      try {
        const url = this.isRegister ? '/api/v1/auth/register' : '/api/v1/auth/login'
        
        const response = await fetch(url, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(this.formData)
        })
        
        // 获取响应文本以查看详细错误
        const responseText = await response.text()
        console.error('响应状态:', response.status)
        console.error('响应内容:', responseText)
        
        if (!response.ok) {
          try {
            const errorData = JSON.parse(responseText)
            throw new Error(errorData.error || errorData.message || '请求失败')
          } catch (parseError) {
            throw new Error(`HTTP ${response.status}: ${responseText || '服务暂时不可用，请稍后重试'}`)
          }
        }
        
        let data
        try {
          data = JSON.parse(responseText)
        } catch (jsonError) {
          throw new Error('服务返回的数据格式错误')
        }
        
        if (data.error) {
          throw new Error(data.error)
        }
        
        if (!this.isRegister) {
          // 登录成功，保存 token 和用户信息
          localStorage.setItem('token', data.token)
          localStorage.setItem('user', JSON.stringify(data.user))
          
          // 保存角色和权限信息
          if (data.user.roles) {
            localStorage.setItem('user_roles', JSON.stringify(data.user.roles))
          }
          if (data.user.permissions) {
            localStorage.setItem('user_permissions', JSON.stringify(data.user.permissions))
          }
          
          this.$emit('login-success', data.user)
        } else {
          // 注册成功，切换到登录表单
          this.toggleForm()
        }
        
        // 重置表单
        this.formData = {
          username: '',
          email: '',
          password: ''
        }
        
      } catch (error) {
        this.error = error.message
      }
    }
  }
}
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.auth-form {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

.error-message {
  background-color: #ffebee;
  color: #c62828;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #555;
}

input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  margin-top: 10px;
}

.submit-btn:hover {
  background-color: #45a049;
}

.toggle-form {
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
}

.toggle-btn {
  background: none;
  border: none;
  color: #4CAF50;
  cursor: pointer;
  font-size: 14px;
  padding: 0;
  margin-left: 5px;
}

.toggle-btn:hover {
  text-decoration: underline;
}
</style>
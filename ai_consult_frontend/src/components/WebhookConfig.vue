<template>
  <div class="webhook-config">
    <h2>Webhook 配置</h2>
    
    <div class="config-section">
      <h3>Webhook 配置信息</h3>
      
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      
      <div v-else-if="error" class="error-message">
        <p>{{ error }}</p>
        <button @click="fetchWebhookConfig" class="btn btn-primary">重试</button>
      </div>
      
      <div v-else class="config-details">
        <div class="form-group">
          <label for="webhookUrl">Webhook URL:</label>
          <input type="text" id="webhookUrl" v-model="webhookConfig.webhookUrl" class="form-control" disabled />
        </div>
        
        <div class="form-group">
          <label for="webhookDebugUrl">Webhook Debug URL:</label>
          <input type="text" id="webhookDebugUrl" v-model="webhookConfig.webhookDebugUrl" class="form-control" disabled />
        </div>
        
        <div class="test-section">
          <h4>测试 Webhook 连接</h4>
          
          <div class="form-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="useDebugUrl" />
              使用调试 URL
            </label>
          </div>
          
          <button @click="testWebhook" class="btn btn-primary" :disabled="testing">
            {{ testing ? '测试中...' : '测试连接' }}
          </button>
        </div>
        
        <div v-if="testResult" class="test-result">
          <h4>测试结果</h4>
          <div class="result-content">
            <p><strong>状态:</strong> {{ testResult.success ? '成功' : '失败' }}</p>
            <p><strong>消息:</strong> {{ testResult.message }}</p>
            <p><strong>URL:</strong> {{ testResult.url }}</p>
            <div v-if="testResult.response" class="response-details">
              <p><strong>响应:</strong></p>
              <pre>{{ JSON.stringify(testResult.response, null, 2) }}</pre>
            </div>
          </div>
        </div>
        
        <div class="send-section">
          <h4>发送自定义 Webhook</h4>
          
          <div class="form-group">
            <label for="payload">Payload (JSON):</label>
            <textarea id="payload" v-model="customPayload" class="form-control" rows="5" placeholder='{"key": "value"}'></textarea>
          </div>
          
          <button @click="sendWebhook" class="btn btn-primary" :disabled="sending">
            {{ sending ? '发送中...' : '发送 Webhook' }}
          </button>
        </div>
        
        <div v-if="sendResult" class="send-result">
          <h4>发送结果</h4>
          <div class="result-content">
            <p><strong>状态:</strong> {{ sendResult.success ? '成功' : '失败' }}</p>
            <p><strong>消息:</strong> {{ sendResult.message }}</p>
            <p><strong>URL:</strong> {{ sendResult.url }}</p>
            <div v-if="sendResult.response" class="response-details">
              <p><strong>响应:</strong></p>
              <pre>{{ JSON.stringify(sendResult.response, null, 2) }}</pre>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from '../utils/axios';

export default {
  name: 'WebhookConfig',
  data() {
    return {
      webhookConfig: {
        webhookUrl: '',
        webhookDebugUrl: ''
      },
      loading: true,
      error: null,
      testing: false,
      sending: false,
      useDebugUrl: false,
      testResult: null,
      sendResult: null,
      customPayload: '{"test": true, "message": "This is a test webhook request"}'
    };
  },
  mounted() {
    this.fetchWebhookConfig();
  },
  methods: {
    async fetchWebhookConfig() {
      this.loading = true;
      this.error = null;
      
      try {
        const response = await axios.get('/api/v1/webhook/config');
        if (response.data.success) {
          this.webhookConfig = response.data.data;
        } else {
          this.error = response.data.message || '获取 Webhook 配置失败';
        }
      } catch (error) {
        this.error = '获取 Webhook 配置失败: ' + (error.message || '未知错误');
      } finally {
        this.loading = false;
      }
    },
    
    async testWebhook() {
      this.testing = true;
      this.testResult = null;
      
      try {
        const response = await axios.post('/api/v1/webhook/test', {}, {
          params: { isDebug: this.useDebugUrl }
        });
        this.testResult = response.data;
      } catch (error) {
        this.testResult = {
          success: false,
          message: '测试失败: ' + (error.message || '未知错误'),
          url: this.useDebugUrl ? this.webhookConfig.webhookDebugUrl : this.webhookConfig.webhookUrl
        };
      } finally {
        this.testing = false;
      }
    },
    
    async sendWebhook() {
      this.sending = true;
      this.sendResult = null;
      
      try {
        let payload;
        try {
          payload = JSON.parse(this.customPayload);
        } catch (parseError) {
          this.sendResult = {
            success: false,
            message: '无效的 JSON 格式',
            url: this.useDebugUrl ? this.webhookConfig.webhookDebugUrl : this.webhookConfig.webhookUrl
          };
          return;
        }
        
        const response = await axios.post('/api/v1/webhook/send', payload, {
          params: { isDebug: this.useDebugUrl }
        });
        this.sendResult = response.data;
      } catch (error) {
        this.sendResult = {
          success: false,
          message: '发送失败: ' + (error.message || '未知错误'),
          url: this.useDebugUrl ? this.webhookConfig.webhookDebugUrl : this.webhookConfig.webhookUrl
        };
      } finally {
        this.sending = false;
      }
    }
  }
};
</script>

<style scoped>
.webhook-config {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

h2 {
  color: #333;
  margin-bottom: 20px;
  text-align: center;
}

h3 {
  color: #555;
  margin-top: 20px;
  margin-bottom: 15px;
  border-bottom: 1px solid #ddd;
  padding-bottom: 10px;
}

h4 {
  color: #666;
  margin-top: 15px;
  margin-bottom: 10px;
}

.config-section {
  margin-bottom: 30px;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
}

.loading-spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-left: 4px solid #3498db;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  background-color: #ffebee;
  color: #c62828;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.config-details {
  background-color: white;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #555;
}

.form-control {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-control:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.checkbox-label {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.checkbox-label input {
  margin-right: 8px;
}

.test-section,
.send-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.btn-primary {
  background-color: #3498db;
  color: white;
}

.btn-primary:hover {
  background-color: #2980b9;
}

.btn:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
}

.test-result,
.send-result {
  margin-top: 15px;
  padding: 15px;
  border-radius: 4px;
  background-color: #f8f9fa;
  border-left: 4px solid #3498db;
}

.result-content {
  font-size: 14px;
  line-height: 1.5;
}

.response-details {
  margin-top: 10px;
  padding: 10px;
  background-color: #e9ecef;
  border-radius: 4px;
  overflow-x: auto;
}

.response-details pre {
  margin: 0;
  font-family: monospace;
  font-size: 12px;
}
</style>

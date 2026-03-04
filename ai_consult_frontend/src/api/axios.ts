import axios from 'axios'

/**
 * axios实例：
 * baseURL 使用 /api，配合 Vite 代理转发至后端。
 */
const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

/**
 * 请求拦截器：
 * 自动注入 Authorization: Bearer <token>
 */
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('ai_token')
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default http

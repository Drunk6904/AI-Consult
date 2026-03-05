import axios from 'axios'

// 请求拦截器
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      // Token 过期或无效，清除本地存储并跳转到登录页
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('user_roles')
      localStorage.removeItem('user_permissions')
      window.location.reload()
    }
    return Promise.reject(error)
  }
)

export default axios

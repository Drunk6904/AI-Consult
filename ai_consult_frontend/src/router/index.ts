import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import ChatView from '../views/ChatView.vue'
import HealthView from '../views/HealthView.vue'

/**
 * 路由表：
 * /login 登录注册页
 * /chat  聊天页
 * /health 健康检查页（保留原有功能）
 */
const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/chat' },
  { path: '/login', component: LoginView },
  { path: '/chat', component: ChatView },
  { path: '/health', component: HealthView }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 路由守卫：
 * 1. /chat 需要登录；
 * 2. /health 可匿名访问；
 * 3. 已登录访问 /login 自动跳转 /chat。
 */
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('ai_token')
  if (to.path === '/health') {
    next()
    return
  }

  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }

  if (to.path === '/login' && token) {
    next('/chat')
    return
  }

  next()
})

export default router

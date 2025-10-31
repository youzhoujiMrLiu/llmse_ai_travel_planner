import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import HomeView from '@/views/HomeView.vue'
import CreatePlanView from '@/views/CreatePlanView.vue'
import { supabase } from '@/lib/supabase'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: LoginView
    },
    {
      path: '/home',
      name: 'Home',
      component: HomeView,
      meta: { requiresAuth: true }
    },
    {
      path: '/plan/create',
      name: 'CreatePlan',
      component: CreatePlanView,
      meta: { requiresAuth: true }
    },
    {
      path: '/',
      redirect: '/home'
    },
    // 兼容旧路由，重定向到新路由
    {
      path: '/dashboard',
      redirect: '/home'
    }
  ],
})

router.beforeEach(async (to, from, next) => {
  // 正确的解构方式获取 session
  const { data: { session } } = await supabase.auth.getSession()
  const isAuthenticated = !!session

  if (to.meta.requiresAuth && !isAuthenticated) {
    // 需要登录但未登录 → 跳转到登录页
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.name === 'Login' && isAuthenticated) {
    // 已登录还访问登录页 → 跳转到主页
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
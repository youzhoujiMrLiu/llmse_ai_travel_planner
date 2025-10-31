// src/api/authApi.ts
import { supabase } from '@/lib/supabase'
import axios from 'axios'

// 创建专用的 API 客户端（带拦截器）
const authApiClient = axios.create({
  baseURL: '/api', // 会被 Vite 代理到 Spring Boot
  timeout: 10000,
})

// 请求拦截器：自动附加 Supabase access_token
authApiClient.interceptors.request.use(
  async (config) => {
    // 确保在添加 Authorization 头之前获取最新的 session
    const {
      data: { session },
    } = await supabase.auth.getSession()
    if (session?.access_token) {
      config.headers.Authorization = `Bearer ${session.access_token}`
    } else {
      // 如果没有 session，删除可能存在的旧 Authorization 头
      delete config.headers.Authorization
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

authApiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // 401 错误时，清除本地 session
      supabase.auth.signOut()
    }
    return Promise.reject(error)
  }
)

// ======================
// Supabase 原生认证方法（前端直接调用）
// ======================

/**
 * 用户注册（邮箱+密码）
 */
export const signUpWithEmail = async (email: string, password: string) => {
  const { data, error } = await supabase.auth.signUp({
    email,
    password,
    options: {
      emailRedirectTo: `${window.location.origin}/auth/callback`, // 可选：用于邮件确认
    },
  })

  if (error) throw error
  return data
}

/**
 * 用户登录（邮箱+密码）
 */
export const signInWithEmail = async (email: string, password: string) => {
  const { data, error } = await supabase.auth.signInWithPassword({
    email,
    password,
  })

  if (error) throw error
  return data
}

/**
 * 退出登录
 */
export const signOut = async () => {
  const { error } = await supabase.auth.signOut()
  if (error) throw error
}

/**
 * 获取当前会话（用于启动时恢复登录状态）
 */
export const getCurrentSession = async () => {
  const { data } = await supabase.auth.getSession()
  return data.session
}

// ======================
// 调用你的 Spring Boot 后端（验证身份、获取用户资料等）
// ======================

/**
 * 调用后端验证 JWT 并获取用户资料（如关联的行程、偏好等）
 */
export const getBackendUserProfile = async () => {
  const res = await authApiClient.get('/user/profile')
  return res.data
}

/**
 * （可选）测试后端是否能识别当前用户
 */
export const testBackendAuth = async () => {
  const res = await authApiClient.get('/test/me')
  return res.data
}
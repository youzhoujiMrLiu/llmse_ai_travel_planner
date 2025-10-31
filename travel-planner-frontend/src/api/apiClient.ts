// src/api/apiClient.ts
import axios from 'axios'
import { supabase } from '@/lib/supabase' // 引入你的 supabase 客户端

const apiClient = axios.create({
  baseURL: '/api',
  timeout: 60000, // 60秒超时,适合 AI 请求等耗时操作
})

apiClient.interceptors.request.use(async (config) => {
  // 确保在添加 Authorization 头之前获取最新的 session
  const { data: { session } } = await supabase.auth.getSession()
  if (session?.access_token) {
    config.headers.Authorization = `Bearer ${session.access_token}`
  } else {
    // 如果没有 session，删除可能存在的旧 Authorization 头
    delete config.headers.Authorization
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // 401 错误时，清除本地 session
      supabase.auth.signOut()
    }
    return Promise.reject(error)
  }
)

export default apiClient
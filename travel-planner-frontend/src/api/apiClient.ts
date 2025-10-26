import axios from 'axios'

const apiClient = axios.create({
  baseURL: '/api', // 因为 Vite 代理了 /api
  timeout: 10000,
})

// 请求拦截器：自动添加 Authorization
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('sb-access-token') // Supabase token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default apiClient

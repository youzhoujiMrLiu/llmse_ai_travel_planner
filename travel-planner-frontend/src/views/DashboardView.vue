<!-- src/views/DashboardView.vue -->
<template>
  <div class="dashboard">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <h1>AI 旅行规划师</h1>
        </div>
        <div class="header-right">
          <span class="user-info">欢迎, {{ userEmail }}</span>
          <el-button type="danger" @click="handleLogout" size="small">
            退出登录
          </el-button>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main>
        <el-card class="welcome-card">
          <h2>🎉 欢迎回来！</h2>
          <p>您的用户 ID: <code>{{ userId }}</code></p>
          <p>在这里，您可以：</p>
          <ul>
            <li>创建新的旅行计划</li>
            <li>查看和管理已有行程</li>
            <li>记录旅行开销</li>
          </ul>
          <el-button type="primary" size="large" @click="goToPlan">
            开始规划旅程
          </el-button>
        </el-card>

        <!-- 后续可在此添加行程列表、地图等 -->
        <div class="placeholder">
          <!-- 预留：行程列表、预算图表、地图等 -->
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { supabase } from '@/lib/supabase'
import apiClient from '@/api/apiClient'

const router = useRouter()
const userId = ref<string>('')
const userEmail = ref<string>('')

// 获取用户信息（从 Supabase + 后端）
const fetchUserProfile = async () => {
  try {
    // 1. 从 Supabase 获取当前用户
    const { data: { user } } = await supabase.auth.getUser()
    if (user) {
      userEmail.value = user.email || ''
    }

    // 2. 从你的 Spring Boot 后端获取验证后的用户 ID（确保 JWT 有效）
    const res = await apiClient.get('/user/profile')
    userId.value = res.data.userId
  } catch (err: any) {
    console.error('获取用户信息失败:', err)
    // 如果是认证错误，则重定向到登录页
    if (err.response?.status === 401) {
      ElMessage.error('认证已过期，请重新登录')
      await supabase.auth.signOut()
      await router.push('/login')
    } else {
      ElMessage.error('用户信息加载失败')
    }
  }
}

// 退出登录
const handleLogout = async () => {
  const { error } = await supabase.auth.signOut()
  if (error) {
    ElMessage.error('退出失败')
  } else {
    ElMessage.success('已退出登录')
    await router.push('/login')
  }
}

// 开始规划（跳转到规划页，后续可实现）
const goToPlan = () => {
  // 暂时无规划页，可跳转到同一页面或提示
  ElMessage.info('旅行规划功能开发中...')
}

onMounted(() => {
  fetchUserProfile()
})
</script>

<style scoped>
.dashboard {
  height: 100vh;
  background-color: #f5f7fa;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 24px;
}

.header h1 {
  margin: 0;
  font-size: 20px;
  color: #1a1a1a;
}

.user-info {
  margin-right: 16px;
  color: #4b5563;
}

.welcome-card {
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
}

.welcome-card h2 {
  color: #1f2937;
}

.welcome-card ul {
  text-align: left;
  margin: 16px auto;
  padding-left: 20px;
  color: #4b5563;
}

.placeholder {
  margin-top: 32px;
  text-align: center;
  color: #9ca3af;
}
</style>

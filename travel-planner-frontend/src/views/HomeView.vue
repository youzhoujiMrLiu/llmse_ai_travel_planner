<!-- src/views/HomeView.vue -->
<template>
  <div class="home">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <h1>🗺️ AI 旅行规划师</h1>
        </div>
        <div class="header-right">
          <span class="user-info">{{ userEmail }}</span>
          <el-button type="danger" @click="handleLogout" size="small">
            退出登录
          </el-button>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <div class="content-wrapper">
          <!-- 页面标题 -->
          <div class="page-header">
            <h2>我的旅行计划</h2>
          </div>

          <!-- 创建新计划按钮 -->
          <div class="create-plan-section">
            <el-button 
              type="primary" 
              size="large" 
              class="create-btn"
              @click="handleCreatePlan"
            >
              <el-icon class="btn-icon"><Plus /></el-icon>
              创建新计划
            </el-button>
            <p class="create-hint">支持语音或文字输入，AI 将为您生成个性化行程</p>
          </div>

          <!-- 旅行计划列表 -->
          <div class="plans-section">
            <!-- 加载状态 -->
            <div v-if="loading" class="loading-state">
              <el-icon class="is-loading"><Loading /></el-icon>
              <p>加载中...</p>
            </div>

            <!-- 有计划时显示列表 -->
            <div v-else-if="travelPlans.length > 0" class="plans-list">
              <el-card 
                v-for="plan in travelPlans" 
                :key="plan.id" 
                class="plan-card"
                shadow="hover"
                @click="handleViewPlan(plan.id)"
              >
                <div class="plan-content">
                  <div class="plan-header">
                    <div class="plan-title-group">
                      <h3>{{ plan.destination }}</h3>
                      <el-tag :type="getStatusType(plan.status)">
                        {{ getStatusText(plan.status) }}
                      </el-tag>
                    </div>
                    <el-button 
                      type="danger" 
                      size="small"
                      text
                      circle
                      @click="handleDeletePlan($event, plan.id)"
                      title="删除计划"
                    >
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                  
                  <div class="plan-details">
                    <div class="detail-item">
                      <el-icon><Calendar /></el-icon>
                      <span>{{ plan.startDate }} ~ {{ plan.endDate }}</span>
                    </div>
                    <div class="detail-item">
                      <el-icon><Clock /></el-icon>
                      <span>{{ plan.duration }}天</span>
                    </div>
                    <div class="detail-item">
                      <el-icon><Money /></el-icon>
                      <span>预算: ¥{{ plan.budget.toLocaleString() }}</span>
                    </div>
                    <div class="detail-item">
                      <el-icon><User /></el-icon>
                      <span>{{ plan.travelers }}人</span>
                    </div>
                  </div>

                  <div class="plan-tags" v-if="plan.preferences && plan.preferences.length > 0">
                    <el-tag 
                      v-for="tag in plan.preferences" 
                      :key="tag" 
                      size="small"
                      effect="plain"
                    >
                      {{ tag }}
                    </el-tag>
                  </div>
                </div>
              </el-card>
            </div>

            <!-- 空状态 -->
            <el-empty 
              v-else 
              class="empty-state"
              description="还没有旅行计划"
            >
              <template #image>
                <div class="empty-icon">✈️</div>
              </template>
              <template #description>
                <p class="empty-text">还没有旅行计划？</p>
                <p class="empty-hint">点击上方按钮，开始你的第一次旅程！</p>
              </template>
            </el-empty>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Calendar, Clock, Money, User, Loading, Delete } from '@element-plus/icons-vue'
import { supabase } from '@/lib/supabase'
import apiClient from '@/api/apiClient'
import { getTravelPlans, deleteTravelPlan, type TravelPlan } from '@/api/travelPlanApi'

const router = useRouter()
const userId = ref<string>('')
const userEmail = ref<string>('')
const loading = ref<boolean>(true)

const travelPlans = ref<TravelPlan[]>([])

// 获取用户信息
const fetchUserProfile = async () => {
  try {
    // 1. 从 Supabase 获取当前用户
    const { data: { user } } = await supabase.auth.getUser()
    if (user) {
      userEmail.value = user.email || ''
    }

    // 2. 从后端获取验证后的用户 ID
    const res = await apiClient.get('/user/profile')
    userId.value = res.data.userId
  } catch (err: any) {
    console.error('获取用户信息失败:', err)
    if (err.response?.status === 401) {
      ElMessage.error('认证已过期，请重新登录')
      await supabase.auth.signOut()
      await router.push('/login')
    } else {
      ElMessage.error('用户信息加载失败')
    }
  }
}

// 获取旅行计划列表
const fetchTravelPlans = async () => {
  loading.value = true
  try {
    const plans = await getTravelPlans()
    travelPlans.value = plans
  } catch (err: any) {
    console.error('获取旅行计划失败:', err)
    if (err.response?.status === 401) {
      ElMessage.error('认证已过期，请重新登录')
      await supabase.auth.signOut()
      await router.push('/login')
    } else {
      ElMessage.error('加载旅行计划失败')
    }
  } finally {
    loading.value = false
  }
}

// 创建新计划
const handleCreatePlan = () => {
  router.push('/plan/create')
}

// 查看计划详情
const handleViewPlan = (planId: string) => {
  router.push(`/plan/${planId}`)
}

// 删除计划
const handleDeletePlan = async (event: Event, planId: string) => {
  event.stopPropagation() // 阻止事件冒泡,避免触发查看计划
  
  ElMessageBox.confirm(
    '确定要删除这个旅行计划吗？删除后将无法恢复。',
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    }
  ).then(async () => {
    try {
      await deleteTravelPlan(planId)
      ElMessage.success('计划已删除')
      // 重新加载列表
      await fetchTravelPlans()
    } catch (error: any) {
      console.error('删除计划失败:', error)
      ElMessage.error('删除失败,请重试')
    }
  }).catch(() => {
    // 用户取消
  })
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

// 获取状态类型
const getStatusType = (status: string) => {
  const types: Record<string, any> = {
    planning: 'warning',
    ongoing: 'success',
    completed: 'info'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    planning: '规划中',
    ongoing: '进行中',
    completed: '已完成'
  }
  return texts[status] || '未知'
}

onMounted(async () => {
  await fetchUserProfile()
  await fetchTravelPlans()
})
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 0 32px;
}

.header-left h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #1a1a1a;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  color: #4b5563;
  font-size: 14px;
}

.main-content {
  padding: 32px 16px;
  overflow-y: auto;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
}

.page-header h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 创建计划区域 */
.create-plan-section {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 32px;
  text-align: center;
  margin-bottom: 32px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.1);
}

.create-btn {
  height: 56px;
  padding: 0 48px;
  font-size: 18px;
  font-weight: 500;
  border-radius: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.btn-icon {
  margin-right: 8px;
  font-size: 20px;
}

.create-hint {
  margin-top: 16px;
  color: #6b7280;
  font-size: 14px;
}

/* 计划列表区域 */
.plans-section {
  min-height: 300px;
}

.loading-state {
  text-align: center;
  padding: 80px 0;
  color: #ffffff;
}

.loading-state .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.loading-state p {
  font-size: 16px;
}

.plans-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 24px;
}

.plan-card {
  cursor: pointer;
  border-radius: 16px;
  transition: all 0.3s ease;
  border: none;
}

.plan-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

.plan-content {
  padding: 8px;
}

.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.plan-title-group {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.plan-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.plan-details {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4b5563;
  font-size: 14px;
}

.detail-item .el-icon {
  color: #667eea;
}

.plan-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
}

/* 空状态 */
.empty-state {
  padding: 80px 0;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 18px;
  font-weight: 500;
  color: #ffffff;
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .plans-list {
    grid-template-columns: 1fr;
  }
  
  .plan-details {
    grid-template-columns: 1fr;
  }
  
  .create-btn {
    width: 100%;
  }
}
</style>

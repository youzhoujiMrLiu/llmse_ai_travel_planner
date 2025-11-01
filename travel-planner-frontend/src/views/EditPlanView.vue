<!-- src/views/EditPlanView.vue -->
<!-- 编辑已有旅行计划 -->
<template>
  <div class="edit-plan-view">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">
        <el-button text @click="goBack" :icon="ArrowLeft">
          返回
        </el-button>
        <h2>编辑旅行计划</h2>
      </div>
      <div class="header-right">
        <el-button 
          type="danger" 
          :icon="Delete"
          @click="handleDelete"
        >
          删除计划
        </el-button>
      </div>
    </el-header>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="48"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 计划编辑器 -->
    <PlanEditor
      v-else-if="plan"
      :plan="plan"
      :is-saving="isSaving"
      @save="handleSave"
      @cancel="goBack"
    />

    <!-- 错误状态 -->
    <el-empty 
      v-else 
      description="计划加载失败"
    >
      <el-button type="primary" @click="goBack">返回首页</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Delete, Loading } from '@element-plus/icons-vue'
import PlanEditor from '@/components/PlanEditor.vue'
import { getTravelPlanDetail, updateTravelPlan, deleteTravelPlan } from '@/api/travelPlanApi'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const isSaving = ref(false)
const plan = ref<any>(null)

// 获取计划详情
const fetchPlan = async () => {
  loading.value = true
  try {
    const planId = route.params.id as string
    const response = await getTravelPlanDetail(planId)
    
    // 解析AI生成的计划
    let parsedPlan = null
    if (response.aiGeneratedPlan) {
      try {
        parsedPlan = typeof response.aiGeneratedPlan === 'string' 
          ? JSON.parse(response.aiGeneratedPlan) 
          : response.aiGeneratedPlan
      } catch (e) {
        console.error('解析AI计划失败:', e)
      }
    }
    
    // 转换后端数据格式为前端格式
    plan.value = {
      id: response.id,
      destination: response.destination,
      startDate: response.startDate,
      endDate: response.endDate,
      duration: response.duration,
      budget: response.budget,
      travelers: response.travelers,
      preferences: response.preferences || [],
      dailyPlans: parsedPlan?.dailyPlans || [],
      tips: parsedPlan?.tips || []
    }
  } catch (error: any) {
    console.error('获取计划失败:', error)
    ElMessage.error('加载计划失败')
    plan.value = null
  } finally {
    loading.value = false
  }
}

// 保存计划
const handleSave = async (updatedPlan: any) => {
  isSaving.value = true
  try {
    const planId = route.params.id as string
    
    // 准备AI生成的计划数据
    const aiGeneratedPlan = JSON.stringify({
      dailyPlans: updatedPlan.dailyPlans,
      tips: updatedPlan.tips
    })
    
    // 准备更新数据
    const updateData = {
      destination: updatedPlan.destination,
      startDate: updatedPlan.startDate,
      endDate: updatedPlan.endDate,
      duration: updatedPlan.duration,
      budget: updatedPlan.budget,
      travelers: updatedPlan.travelers,
      preferences: updatedPlan.preferences,
      userInput: '', // 编辑模式不需要用户输入
      aiGeneratedPlan: aiGeneratedPlan // 保存更新后的详细计划
    }
    
    await updateTravelPlan(planId, updateData)
    ElMessage.success('计划已保存')
    
    // 返回首页
    setTimeout(() => {
      router.push('/home')
    }, 500)
  } catch (error: any) {
    console.error('保存计划失败:', error)
    ElMessage.error('保存失败,请重试')
  } finally {
    isSaving.value = false
  }
}

// 删除计划
const handleDelete = async () => {
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
      const planId = route.params.id as string
      await deleteTravelPlan(planId)
      ElMessage.success('计划已删除')
      router.push('/home')
    } catch (error: any) {
      console.error('删除计划失败:', error)
      ElMessage.error('删除失败,请重试')
    }
  }).catch(() => {
    // 用户取消
  })
}

// 返回
const goBack = () => {
  router.push('/home')
}

// 生命周期
onMounted(() => {
  fetchPlan()
})
</script>

<style scoped>
.edit-plan-view {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 24px;
  height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.loading-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.loading-container p {
  color: #666;
  font-size: 14px;
}
</style>

<!-- src/views/CreatePlanView.vue -->
<template>
  <div class="create-plan">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-button text @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h1>创建旅行计划</h1>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <div class="content-wrapper">
          <!-- 步骤指示器 -->
          <el-steps :active="currentStep" align-center class="steps">
            <el-step title="输入需求" icon="Edit" />
            <el-step title="AI 生成方案" icon="MagicStick" />
            <el-step title="确认并保存" icon="Check" />
          </el-steps>

          <!-- 步骤 1: 输入需求 -->
          <div v-if="currentStep === 0" class="step-content">
            <el-card class="input-card">
              <template #header>
                <div class="card-header">
                  <h2>✨ 告诉我你的旅行想法</h2>
                  <p>选择输入方式：智能语音或手动填写</p>
                </div>
              </template>

              <!-- 输入模式切换 -->
              <div class="mode-switch">
                <el-radio-group v-model="inputMode" size="large">
                  <el-radio-button label="smart">🎤 智能输入</el-radio-button>
                  <el-radio-button label="manual">📝 手动填写</el-radio-button>
                </el-radio-group>
              </div>

              <!-- 智能输入模式 -->
              <div v-if="inputMode === 'smart'" class="smart-input-mode">
                <!-- 语音/文字输入 -->
                <div class="voice-input-section">
                  <el-button
                    :type="isRecording ? 'danger' : 'primary'"
                    size="large"
                    class="voice-btn"
                    @click="toggleVoiceInput"
                    :loading="isRecording"
                  >
                    <el-icon class="voice-icon"><Microphone /></el-icon>
                    {{ isRecording ? '正在录音中...' : '点击开始语音输入' }}
                  </el-button>
                  <p class="voice-hint">
                    {{ isRecording ? '请说出你的旅行计划...' : '例如："我想去日本，5天，预算1万元，喜欢美食和动漫，带孩子"' }}
                  </p>
                </div>

                <el-divider>或者</el-divider>

                <!-- 文字输入 -->
                <el-form-item>
                  <el-input
                    v-model="form.userInput"
                    type="textarea"
                    :rows="4"
                    placeholder="输入你的旅行需求，例如：我想去日本东京玩5天，预算1万元，喜欢美食和动漫..."
                  />
                  <div class="input-hint">
                    💡 输入完成后，点击下方按钮让 AI 智能识别
                  </div>
                </el-form-item>

                <!-- 智能识别按钮 -->
                <div class="smart-parse-section">
                  <el-button
                    type="primary"
                    @click="handleSmartParse"
                    :disabled="!form.userInput || form.userInput.trim().length === 0"
                    :loading="parsing"
                  >
                    <el-icon><MagicStick /></el-icon>
                    {{ parsing ? '正在识别中...' : '智能识别旅行信息' }}
                  </el-button>
                  <p class="parse-hint">
                    使用本地 AI 解析技术,识别你的目的地、天数、预算等信息
                  </p>
                </div>

                <el-divider>AI 识别结果</el-divider>
              </div>

              <!-- 表单输入（两种模式共用，智能模式下为 AI 解析结果） -->
              <el-form
                ref="formRef"
                :model="form"
                :rules="rules"
                label-position="top"
                class="plan-form"
              >

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="目的地" prop="destination">
                      <el-input
                        v-model="form.destination"
                        placeholder="例如：日本东京"
                        clearable
                      >
                        <template #suffix v-if="inputMode === 'smart' && form.destination">
                          <el-icon color="#67c23a"><CircleCheck /></el-icon>
                        </template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="旅行天数" prop="duration">
                      <el-input-number
                        v-model="form.duration"
                        :min="1"
                        :max="30"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="预算（元）" prop="budget">
                      <el-input-number
                        v-model="form.budget"
                        :min="100"
                        :step="100"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="同行人数" prop="travelers">
                      <el-input-number
                        v-model="form.travelers"
                        :min="1"
                        :max="10"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-form-item label="旅行偏好">
                  <el-select
                    v-model="form.preferences"
                    multiple
                    placeholder="选择你的偏好"
                    style="width: 100%"
                  >
                    <el-option label="美食" value="美食" />
                    <el-option label="文化" value="文化" />
                    <el-option label="自然" value="自然" />
                    <el-option label="购物" value="购物" />
                    <el-option label="动漫" value="动漫" />
                    <el-option label="历史" value="历史" />
                    <el-option label="亲子" value="亲子" />
                    <el-option label="休闲" value="休闲" />
                    <el-option label="探险" value="探险" />
                  </el-select>
                </el-form-item>

                <el-alert
                  v-if="inputMode === 'smart' && form.userInput"
                  title="提示"
                  type="info"
                  :closable="false"
                  show-icon
                  style="margin-bottom: 16px"
                >
                  AI 已根据你的描述自动填充以上信息，请检查并调整
                </el-alert>

                <el-form-item>
                  <el-button
                    type="primary"
                    size="large"
                    @click="handleGeneratePlan"
                    :loading="generating"
                    style="width: 100%"
                  >
                    🤖 {{ generating ? '通义千问 AI 正在生成中...' : '让 AI 帮我生成计划' }}
                  </el-button>
                  <div class="ai-generate-hint">
                    <el-icon><InfoFilled /></el-icon>
                    使用通义千问 qwen-plus 模型生成个性化行程，预计需要 30-60 秒，请耐心等待
                  </div>
                </el-form-item>
              </el-form>
            </el-card>
          </div>

          <!-- 步骤 2: AI 生成的计划 -->
          <div v-if="currentStep === 1" class="step-content">
            <el-alert
              title="AI 生成完成 ✨"
              type="success"
              :closable="false"
              show-icon
              style="margin-bottom: 20px"
            >
              通义千问 qwen-plus 已根据你的需求生成个性化行程，请仔细查看并确认
            </el-alert>
            
            <el-card class="plan-preview-card">
              <template #header>
                <div class="card-header">
                  <h2>🎉 为你定制的旅行计划</h2>
                  <p>{{ generatedPlan?.summary }}</p>
                </div>
              </template>

              <!-- 预算分配图表 -->
              <div class="budget-section">
                <h3>💰 预算分配</h3>
                <div class="budget-cards">
                  <div class="budget-card">
                    <span class="budget-label">住宿</span>
                    <span class="budget-amount">¥{{ generatedPlan?.budgetBreakdown.accommodation }}</span>
                  </div>
                  <div class="budget-card">
                    <span class="budget-label">餐饮</span>
                    <span class="budget-amount">¥{{ generatedPlan?.budgetBreakdown.food }}</span>
                  </div>
                  <div class="budget-card">
                    <span class="budget-label">交通</span>
                    <span class="budget-amount">¥{{ generatedPlan?.budgetBreakdown.transport }}</span>
                  </div>
                  <div class="budget-card">
                    <span class="budget-label">景点</span>
                    <span class="budget-amount">¥{{ generatedPlan?.budgetBreakdown.attraction }}</span>
                  </div>
                </div>
              </div>

              <!-- 行程地图 -->
              <div class="map-section">
                <div class="map-header">
                  <h3>🗺️ 行程地图</h3>
                  <el-button-group>
                    <el-button
                      v-for="dayPlan in generatedPlan?.dailyPlans"
                      :key="dayPlan.day"
                      :type="selectedDay === dayPlan.day ? 'primary' : 'default'"
                      size="small"
                      @click="switchMapDay(dayPlan.day)"
                    >
                      第 {{ dayPlan.day }} 天
                    </el-button>
                  </el-button-group>
                </div>
                
                <el-alert
                  v-if="mapLoadingStatus === 'loading'"
                  title="正在定位行程中的地点..."
                  type="info"
                  :closable="false"
                  show-icon
                  style="margin-bottom: 16px"
                >
                  使用高德地图 API 进行地理编码,请稍候
                </el-alert>

                <el-alert
                  v-if="mapLoadingStatus === 'error'"
                  title="地图加载失败"
                  type="warning"
                  :closable="false"
                  show-icon
                  style="margin-bottom: 16px"
                >
                  {{ mapErrorMessage }}
                </el-alert>

                <el-alert
                  v-if="unlocatedPlaces.length > 0"
                  :title="`提示: 以下地点无法精确定位 (${unlocatedPlaces.length}/${totalPlaces})`"
                  type="warning"
                  :closable="false"
                  show-icon
                  style="margin-bottom: 16px"
                >
                  {{ unlocatedPlaces.join('、') }}
                </el-alert>

                <!-- 地图容器 -->
                <div id="amap-container" class="amap-container"></div>
              </div>

              <!-- 每日行程 -->
              <div class="daily-plans">
                <h3>📅 每日行程</h3>
                <el-timeline>
                  <el-timeline-item
                    v-for="dayPlan in generatedPlan?.dailyPlans"
                    :key="dayPlan.day"
                    :timestamp="`第 ${dayPlan.day} 天`"
                    placement="top"
                  >
                    <el-card class="day-card">
                      <div
                        v-for="(activity, index) in dayPlan.activities"
                        :key="index"
                        class="activity-item"
                      >
                        <div class="activity-header">
                          <el-tag :type="getActivityTypeTag(activity.type)">
                            {{ getActivityTypeText(activity.type) }}
                          </el-tag>
                          <span class="activity-time">{{ activity.time }}</span>
                        </div>
                        <h4>{{ activity.title }}</h4>
                        <p class="activity-desc">{{ activity.description }}</p>
                        <div class="activity-footer">
                          <span class="activity-location">
                            <el-icon><Location /></el-icon>
                            {{ activity.location }}
                          </span>
                          <span class="activity-cost">
                            预估: ¥{{ activity.estimatedCost }}
                          </span>
                        </div>
                      </div>
                    </el-card>
                  </el-timeline-item>
                </el-timeline>
              </div>

              <!-- 旅行建议 -->
              <div class="tips-section" v-if="generatedPlan?.tips && generatedPlan.tips.length > 0">
                <h3>💡 旅行建议</h3>
                <ul class="tips-list">
                  <li v-for="(tip, index) in generatedPlan.tips" :key="index">
                    {{ tip }}
                  </li>
                </ul>
              </div>

              <!-- 操作按钮 -->
              <div class="action-buttons">
                <el-button @click="currentStep = 0">
                  重新生成
                </el-button>
                <el-button
                  type="primary"
                  @click="handleSavePlan"
                  :loading="saving"
                >
                  保存计划
                </el-button>
              </div>
            </el-card>
          </div>

          <!-- 步骤 3: 保存成功 -->
          <div v-if="currentStep === 2" class="step-content">
            <el-result
              icon="success"
              title="计划创建成功！"
              sub-title="你的旅行计划已保存，随时可以查看和修改"
            >
              <template #extra>
                <el-button type="primary" @click="goToHome">
                  查看我的计划
                </el-button>
                <el-button @click="resetForm">
                  继续创建
                </el-button>
              </template>
            </el-result>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  ArrowLeft,
  Microphone,
  Location,
  CircleCheck,
  MagicStick,
  InfoFilled
} from '@element-plus/icons-vue'
import { generateTravelPlan, type GeneratedPlanResponse, type GeneratePlanRequest } from '@/api/aiApi'
import { createTravelPlan } from '@/api/travelPlanApi'
import { WebSpeechRecognition } from '@/services/speechService'
import { getAmapService, type Location as AmapLocation } from '@/services/amapService'

const router = useRouter()
const formRef = ref<FormInstance>()
const currentStep = ref(0)
const generating = ref(false)
const saving = ref(false)
const isRecording = ref(false)
const parsing = ref(false) // 智能识别加载状态
const generatedPlan = ref<GeneratedPlanResponse | null>(null)
const inputMode = ref<'smart' | 'manual'>('smart') // 输入模式：智能输入或手动填写

// 地图相关状态
const selectedDay = ref(1) // 当前选中的天数
const mapLoadingStatus = ref<'idle' | 'loading' | 'success' | 'error'>('idle')
const mapErrorMessage = ref('')
const unlocatedPlaces = ref<string[]>([]) // 无法定位的地点
const totalPlaces = ref(0) // 总地点数
const locationCache = ref<Map<string, AmapLocation>>(new Map()) // 地点坐标缓存

let speechRecognition: WebSpeechRecognition | null = null
let amapService = getAmapService()

// 表单数据
const form = reactive<{
  userInput: string
  destination: string
  duration: number
  budget: number
  travelers: number
  preferences: string[]
}>({
  userInput: '',
  destination: '',
  duration: 5,
  budget: 5000,
  travelers: 1,
  preferences: []
})

// 表单验证规则（动态验证，智能模式需要 userInput，手动模式不需要）
const rules = reactive<FormRules>({
  userInput: [
    {
      validator: (rule, value, callback) => {
        if (inputMode.value === 'smart' && !value) {
          callback(new Error('请描述你的旅行需求'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  destination: [
    { required: true, message: '请输入目的地', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入旅行天数', trigger: 'change' }
  ],
  budget: [
    { required: true, message: '请输入预算', trigger: 'change' }
  ],
  travelers: [
    { required: true, message: '请输入同行人数', trigger: 'change' }
  ]
})

// 返回上一页
const goBack = () => {
  router.back()
}

// 切换语音输入
const toggleVoiceInput = () => {
  if (isRecording.value) {
    stopVoiceInput()
  } else {
    startVoiceInput()
  }
}

// 开始语音输入
const startVoiceInput = () => {
  try {
    if (!speechRecognition) {
      speechRecognition = new WebSpeechRecognition()
    }

    isRecording.value = true
    
    speechRecognition.startRecognition(
      (text: string) => {
        form.userInput = text
        // 尝试从语音中提取信息
        parseUserInput(text)
      },
      (error: string) => {
        ElMessage.error(error)
        isRecording.value = false
      }
    )
  } catch (error: any) {
    ElMessage.error(error.message || '浏览器不支持语音识别')
  }
}

// 停止语音输入
const stopVoiceInput = () => {
  if (speechRecognition) {
    speechRecognition.stopRecognition()
  }
  isRecording.value = false
}

// 解析用户输入（增强的关键词提取和模式匹配）
const parseUserInput = (text: string) => {
  if (!text || text.trim().length === 0) {
    return
  }

  // 提取目的地（多种模式）
  const destinationPatterns = [
    /(?:去|到|想去|前往|计划去)([^\s,，。]+?)(?:[,，。\s]|旅游|旅行|玩)/,
    /([^\s,，。]+?)(?:旅游|旅行|自由行|跟团)/
  ]
  
  for (const pattern of destinationPatterns) {
    const match = text.match(pattern)
    if (match && match[1]) {
      form.destination = match[1].trim()
      break
    }
  }

  // 提取天数（多种表达方式）
  const durationPatterns = [
    /(\d+)\s*(?:天|日)/,
    /(?:玩|待|呆)\s*(\d+)\s*(?:天|日)/
  ]
  
  for (const pattern of durationPatterns) {
    const match = text.match(pattern)
    if (match && match[1]) {
      form.duration = parseInt(match[1])
      break
    }
  }

  // 提取预算（支持多种格式）
  const budgetPatterns = [
    /(?:预算|花费|费用|大概|大约).*?(\d+(?:\.\d+)?)\s*万/,
    /(\d+(?:\.\d+)?)\s*万.*?(?:预算|费用|元)/,
    /(?:预算|花费|费用).*?(\d+)\s*(?:元|块)/
  ]
  
  for (const pattern of budgetPatterns) {
    const match = text.match(pattern)
    if (match && match[1]) {
      const amount = parseFloat(match[1])
      form.budget = text.includes('万') ? Math.round(amount * 10000) : amount
      break
    }
  }

  // 提取同行人数
  const travelersPatterns = [
    /(\d+)\s*(?:个人|人|位)/,
    /(?:带|和|跟).*?(\d+)\s*(?:个人|人)/,
    /(?:一家|全家)\s*(\d+)\s*(?:口|人)/
  ]
  
  for (const pattern of travelersPatterns) {
    const match = text.match(pattern)
    if (match && match[1]) {
      form.travelers = parseInt(match[1])
      break
    }
  }
  
  // 特殊情况：识别亲子游、家庭游
  if (text.includes('带孩子') || text.includes('亲子') || text.includes('孩子')) {
    if (form.travelers <= 1) {
      form.travelers = 2 // 至少2人
    }
    if (!form.preferences.includes('亲子')) {
      form.preferences.push('亲子')
    }
  }

  // 提取偏好（使用关键词匹配）
  const preferenceKeywords = ['美食', '文化', '自然', '购物', '动漫', '历史', '亲子', '休闲', '探险']
  const extractedPreferences = preferenceKeywords.filter(keyword => text.includes(keyword))
  
  // 合并偏好（避免重复）
  extractedPreferences.forEach(pref => {
    if (!form.preferences.includes(pref)) {
      form.preferences.push(pref)
    }
  })

  // 显示提示
  if (form.destination || form.duration > 1 || form.budget > 100 || form.preferences.length > 0) {
    ElMessage.success('AI 已自动识别并填充信息，请检查确认')
  }
}

// 智能识别按钮处理
const handleSmartParse = () => {
  if (!form.userInput || form.userInput.trim().length === 0) {
    ElMessage.warning('请先输入旅行需求描述')
    return
  }

  parsing.value = true
  
  // 添加短暂延迟,让用户感受到 AI 在工作
  setTimeout(() => {
    parseUserInput(form.userInput)
    parsing.value = false
  }, 800)
}

// 生成旅行计划
const handleGeneratePlan = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    generating.value = true
    
    // 显示友好的加载提示
    ElMessage({
      message: '🤖 通义千问 AI 正在为你精心规划行程，这可能需要 30-60 秒，请稍候...',
      type: 'info',
      duration: 5000,
      showClose: true
    })

    try {
      const request: GeneratePlanRequest = {
        userInput: form.userInput,
        destination: form.destination,
        duration: form.duration,
        budget: form.budget,
        travelers: form.travelers,
        preferences: form.preferences.join(',')
      }

      generatedPlan.value = await generateTravelPlan(request)
      currentStep.value = 1
      
      ElMessage.success('🎉 计划生成成功！AI 已为你规划了详细的行程')
      
      // 等待 DOM 更新后初始化地图
      await nextTick()
      await initializeMap()
    } catch (error: any) {
      console.error('生成计划失败:', error)
      const errorMsg = error.response?.data?.error || error.message || '生成计划失败'
      
      // 根据错误类型提供不同的提示
      if (error.code === 'ECONNABORTED' || errorMsg.includes('timeout')) {
        ElMessage.error({
          message: '⏱️ AI 生成超时，可能是网络问题或请求过于复杂，请简化需求后重试',
          duration: 5000,
          showClose: true
        })
      } else {
        ElMessage.error({
          message: `❌ ${errorMsg}，请检查网络连接或稍后重试`,
          duration: 5000,
          showClose: true
        })
      }
    } finally {
      generating.value = false
    }
  })
}

// 保存计划
const handleSavePlan = async () => {
  if (!generatedPlan.value) return

  saving.value = true

  try {
    // 计算开始和结束日期
    const startDate = new Date()
    startDate.setDate(startDate.getDate() + 7) // 默认7天后出发
    const endDate = new Date(startDate)
    endDate.setDate(endDate.getDate() + form.duration - 1)

    await createTravelPlan({
      destination: form.destination,
      startDate: startDate.toISOString().split('T')[0] || '',
      endDate: endDate.toISOString().split('T')[0] || '',
      duration: form.duration,
      budget: form.budget,
      travelers: form.travelers,
      preferences: form.preferences,
      userInput: form.userInput
    })

    currentStep.value = 2
    ElMessage.success('计划保存成功！')
  } catch (error: any) {
    console.error('保存计划失败:', error)
    ElMessage.error(error.response?.data?.error || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

// 跳转到主页
const goToHome = () => {
  router.push('/home')
}

// 重置表单
const resetForm = () => {
  currentStep.value = 0
  generatedPlan.value = null
  form.userInput = ''
  form.destination = ''
  form.duration = 5
  form.budget = 5000
  form.travelers = 1
  form.preferences = []
  formRef.value?.clearValidate()
}

// 获取活动类型标签
const getActivityTypeTag = (type: string) => {
  const types: Record<string, any> = {
    transport: 'info',
    attraction: 'success',
    restaurant: 'warning',
    accommodation: 'danger'
  }
  return types[type] || ''
}

// 获取活动类型文本
const getActivityTypeText = (type: string) => {
  const texts: Record<string, string> = {
    transport: '交通',
    attraction: '景点',
    restaurant: '餐饮',
    accommodation: '住宿'
  }
  return texts[type] || type
}

// ==================== 地图相关功能 ====================

/**
 * 初始化地图并加载所有地点
 */
const initializeMap = async () => {
  try {
    mapLoadingStatus.value = 'loading'
    mapErrorMessage.value = ''
    unlocatedPlaces.value = []

    // 检查高德地图 SDK 是否加载
    if (!window.AMap) {
      mapLoadingStatus.value = 'error'
      mapErrorMessage.value = '高德地图 SDK 未加载,请检查网络连接或刷新页面'
      return
    }

    // 初始化地图
    amapService.initMap('amap-container', [116.397428, 39.90923], 12)

    // 收集所有地点并进行地理编码
    await geocodeAllActivities()

    // 显示第一天的路线
    selectedDay.value = 1
    await displayDayRoute(1)

    mapLoadingStatus.value = 'success'
  } catch (error: any) {
    console.error('地图初始化失败:', error)
    mapLoadingStatus.value = 'error'
    mapErrorMessage.value = error.message || '地图初始化失败'
  }
}

/**
 * 对所有活动进行地理编码
 */
const geocodeAllActivities = async () => {
  if (!generatedPlan.value) return

  const allPlaces: string[] = []
  
  // 收集所有地点
  generatedPlan.value.dailyPlans.forEach((dayPlan) => {
    dayPlan.activities.forEach((activity) => {
      if (activity.location) {
        // 组合目的地和具体地点,提高定位准确率
        const fullLocation = `${form.destination} ${activity.location}`
        if (!allPlaces.includes(fullLocation)) {
          allPlaces.push(fullLocation)
        }
      }
    })
  })

  totalPlaces.value = allPlaces.length

  // 批量地理编码
  const results = await amapService.batchGeocode(allPlaces)

  // 缓存结果并统计失败的地点
  results.forEach((result, index) => {
    const place = allPlaces[index]
    if (!place) return
    
    if (result.success && result.location) {
      locationCache.value.set(place, result.location)
    } else {
      const displayName = place.replace(`${form.destination} `, '')
      unlocatedPlaces.value.push(displayName)
    }
  })
}

/**
 * 显示指定天数的路线
 */
const displayDayRoute = async (day: number) => {
  if (!generatedPlan.value) return

  // 清除旧的标记和路线
  amapService.clearAll()

  // 找到对应天数的行程
  const dayPlan = generatedPlan.value.dailyPlans.find((d) => d.day === day)
  if (!dayPlan) return

  const locations: AmapLocation[] = []
  let markerIndex = 1

  // 添加标记点
  dayPlan.activities.forEach((activity) => {
    if (!activity.location) return

    const fullLocation = `${form.destination} ${activity.location}`
    const cachedLocation = locationCache.value.get(fullLocation)

    if (cachedLocation) {
      locations.push(cachedLocation)

      // 根据活动类型选择不同颜色的标记
      const iconColors: Record<string, string> = {
        transport: '#3b82f6',
        attraction: '#ef4444',
        restaurant: '#f59e0b',
        accommodation: '#10b981',
      }

      amapService.addMarker(
        cachedLocation,
        `${markerIndex}. ${activity.title}`,
        new window.AMap.Icon({
          size: new window.AMap.Size(32, 32),
          image: `https://webapi.amap.com/theme/v1.3/markers/n/mark_b${markerIndex}.png`,
        })
      )

      markerIndex++
    }
  })

  // 绘制路线
  if (locations.length >= 2) {
    amapService.drawPath(locations, '#3b82f6', true)
  }

  // 自动调整地图视野
  amapService.fitView()
}

/**
 * 切换显示的天数
 */
const switchMapDay = async (day: number) => {
  selectedDay.value = day
  await displayDayRoute(day)
}

// 监听生成的计划变化
watch(generatedPlan, async (newPlan) => {
  if (newPlan && currentStep.value === 1) {
    await nextTick()
    await initializeMap()
  }
})

// 组件卸载时清理地图
onUnmounted(() => {
  if (amapService) {
    amapService.destroy()
  }
})
</script>

<style scoped>
.create-plan {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 0 32px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #1a1a1a;
}

.main-content {
  padding: 32px 16px;
  overflow-y: auto;
}

.content-wrapper {
  max-width: 1000px;
  margin: 0 auto;
}

.steps {
  margin-bottom: 40px;
  background: rgba(255, 255, 255, 0.95);
  padding: 24px;
  border-radius: 12px;
}

.step-content {
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 输入卡片 */
.input-card {
  border-radius: 16px;
}

.card-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #1f2937;
}

.card-header p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

/* 语音输入 */
.voice-input-section {
  text-align: center;
  padding: 32px 0;
  margin-bottom: 32px;
  border-bottom: 1px solid #e5e7eb;
}

.voice-btn {
  height: 60px;
  padding: 0 48px;
  font-size: 18px;
  border-radius: 30px;
}

.voice-icon {
  margin-right: 8px;
  font-size: 20px;
}

.voice-hint {
  margin-top: 16px;
  color: #6b7280;
  font-size: 14px;
}

/* 输入模式切换 */
.mode-switch {
  text-align: center;
  margin-bottom: 32px;
  padding: 24px 0;
}

.mode-switch .el-radio-group {
  background: #f3f4f6;
  padding: 4px;
  border-radius: 12px;
}

.mode-switch .el-radio-button {
  min-width: 150px;
}

/* 智能输入模式 */
.smart-input-mode {
  margin-bottom: 24px;
}

.input-hint {
  margin-top: 8px;
  font-size: 13px;
  color: #6b7280;
}

/* 智能识别按钮区域 */
.smart-parse-section {
  text-align: center;
  padding: 20px 0;
  margin: 16px 0;
}

.smart-parse-section .el-button {
  padding: 12px 32px;
  font-size: 16px;
}

.parse-hint {
  margin-top: 12px;
  font-size: 13px;
  color: #6b7280;
}

/* AI 生成提示 */
.ai-generate-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 12px;
  padding: 12px;
  background: #f0f9ff;
  border-radius: 8px;
  font-size: 13px;
  color: #0369a1;
  line-height: 1.5;
}

.ai-generate-hint .el-icon {
  font-size: 16px;
}

/* 表单 */
.plan-form {
  margin-top: 24px;
}

/* 计划预览卡片 */
.plan-preview-card {
  border-radius: 16px;
}

.budget-section {
  margin-bottom: 32px;
}

.budget-section h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: #1f2937;
}

.budget-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.budget-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.budget-label {
  font-size: 14px;
  opacity: 0.9;
}

.budget-amount {
  font-size: 20px;
  font-weight: 600;
}

/* 地图区域 */
.map-section {
  margin-bottom: 32px;
}

.map-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.map-header h3 {
  margin: 0;
  font-size: 18px;
  color: #1f2937;
}

.amap-container {
  width: 100%;
  height: 500px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 每日行程 */
.daily-plans {
  margin-bottom: 32px;
}

.daily-plans h3 {
  margin: 0 0 24px 0;
  font-size: 18px;
  color: #1f2937;
}

.day-card {
  margin-top: 16px;
}

.activity-item {
  padding: 16px 0;
  border-bottom: 1px solid #e5e7eb;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.activity-time {
  color: #6b7280;
  font-size: 14px;
}

.activity-item h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #1f2937;
}

.activity-desc {
  margin: 0 0 12px 0;
  color: #4b5563;
  font-size: 14px;
  line-height: 1.6;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.activity-location {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #6b7280;
}

.activity-cost {
  color: #667eea;
  font-weight: 500;
}

/* 旅行建议 */
.tips-section {
  margin-bottom: 32px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 12px;
}

.tips-section h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: #1f2937;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  color: #4b5563;
  line-height: 2;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
}

/* 响应式 */
@media (max-width: 768px) {
  .budget-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

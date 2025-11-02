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
            <el-step title="输入需求" :icon="Edit" />
            <el-step title="AI 生成方案" :icon="MagicStick" />
            <el-step title="确认并保存" :icon="Check" />
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

                <el-form-item label="额外要求">
                  <el-input
                    v-model="form.additionalRequirements"
                    type="textarea"
                    :rows="3"
                    placeholder="例如：预算主要用于美食，住宿标准要高一些，早上不要安排太早的活动..."
                    maxlength="500"
                    show-word-limit
                  />
                  <div class="input-hint">
                    💡 可以补充特殊需求、注意事项等
                  </div>
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
                  :title="`正在定位行程中的地点... (${geocodedCount}/${totalPlaces})`"
                  type="info"
                  :closable="false"
                  show-icon
                  style="margin-bottom: 16px"
                >
                  <el-progress :percentage="totalPlaces > 0 ? Math.round((geocodedCount / totalPlaces) * 100) : 0" />
                  <div style="margin-top: 8px; font-size: 12px">使用高德地图 API 进行地理编码，每个地点间隔 500ms 以避免频率限制</div>
                </el-alert>

                <el-alert
                  v-if="mapLoadingStatus === 'error'"
                  :title="mapErrorMessage.includes('仅支持中国') ? '地图功能不可用' : '地图加载失败'"
                  :type="mapErrorMessage.includes('仅支持中国') ? 'info' : 'warning'"
                  :closable="false"
                  show-icon
                  style="margin-bottom: 16px"
                >
                  <div v-html="mapErrorMessage"></div>
                  <div v-if="mapErrorMessage.includes('仅支持中国')" style="margin-top: 8px; font-size: 12px; color: #909399">
                    💡 提示：您可以在下方查看 AI 生成的详细行程安排
                  </div>
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
                <div class="map-wrapper">
                  <div id="amap-container" class="amap-container"></div>
                  <!-- 地图加载遮罩 -->
                  <div v-if="mapLoadingStatus === 'loading'" class="map-loading-overlay">
                    <div class="map-loading-content">
                      <el-icon class="is-loading" :size="40">
                        <Loading />
                      </el-icon>
                      <div class="loading-text">正在加载地图...</div>
                      <div class="loading-subtext">{{ geocodedCount }}/{{ totalPlaces }} 个地点已定位</div>
                    </div>
                  </div>
                </div>
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
                        draggable="true"
                        @dragstart="handleDragStart(dayPlan.day, index, $event)"
                        @dragover="handleDragOver($event)"
                        @drop="handleDrop(dayPlan.day, index, $event)"
                        @dragend="handleDragEnd"
                      >
                        <div class="activity-view">
                          <div class="activity-header">
                            <div class="activity-header-left">
                              <el-icon class="drag-handle" title="拖动排序">
                                <Rank />
                              </el-icon>
                              <el-tag :type="getActivityTypeTag(activity.type)">
                                {{ getActivityTypeText(activity.type) }}
                              </el-tag>
                              <span class="activity-time">{{ activity.time }}</span>
                            </div>
                            <div class="activity-actions">
                              <el-button 
                                size="small" 
                                text 
                                @click="moveActivity(dayPlan.day, index, 'up')"
                                :icon="ArrowUp"
                                :disabled="index === 0"
                                title="上移"
                              />
                              <el-button 
                                size="small" 
                                text 
                                @click="moveActivity(dayPlan.day, index, 'down')"
                                :icon="ArrowDown"
                                :disabled="index === dayPlan.activities.length - 1"
                                title="下移"
                              />
                              <el-button 
                                size="small" 
                                text 
                                @click="openEditActivityDialog(dayPlan.day, index)"
                                :icon="Edit"
                              >
                                编辑
                              </el-button>
                              <el-button 
                                size="small" 
                                text 
                                type="danger"
                                @click="deleteActivity(dayPlan.day, index)"
                                :icon="Delete"
                              >
                                删除
                              </el-button>
                            </div>
                          </div>
                          <h4>{{ activity.title }}</h4>
                          <p class="activity-desc">{{ activity.description }}</p>
                          <div class="activity-footer">
                            <span class="activity-location">
                              <el-icon><Location /></el-icon>
                              <span v-if="activity.address">{{ activity.address }}</span>
                              <span v-else-if="geocodedCount < totalPlaces" class="locating">正在定位...</span>
                              <span v-else class="no-address">{{ activity.location }}</span>
                            </span>
                            <span class="activity-cost">
                              预估: ¥{{ activity.estimatedCost }}
                            </span>
                          </div>
                        </div>
                      </div>
                      
                      <!-- 添加新活动按钮 -->
                      <div class="add-activity">
                        <el-button 
                          text 
                          @click="openAddActivityDialog(dayPlan.day)"
                          :icon="Plus"
                        >
                          添加活动
                        </el-button>
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

          <!-- 添加/编辑活动对话框 -->
          <el-dialog
            v-model="activityDialogVisible"
            :title="activityDialogMode === 'add' ? '添加活动' : '编辑活动'"
            width="600px"
            :close-on-click-modal="false"
          >
            <el-form
              ref="activityFormRef"
              :model="activityForm"
              :rules="activityFormRules"
              label-position="top"
            >
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="活动类型" prop="type">
                    <el-select v-model="activityForm.type" style="width: 100%">
                      <el-option label="交通" value="transport" />
                      <el-option label="景点" value="attraction" />
                      <el-option label="餐饮" value="restaurant" />
                      <el-option label="住宿" value="accommodation" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="时间" prop="time">
                    <el-input v-model="activityForm.time" placeholder="09:00-12:00" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="活动标题" prop="title">
                <el-input v-model="activityForm.title" placeholder="请输入活动名称" />
              </el-form-item>

              <el-form-item label="活动描述" prop="description">
                <el-input
                  v-model="activityForm.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入活动描述"
                />
              </el-form-item>

              <el-form-item label="地点" prop="location">
                <el-autocomplete
                  v-model="activityForm.location"
                  :fetch-suggestions="searchLocationForDialog"
                  placeholder="输入地点名称搜索"
                  style="width: 100%"
                  @select="handleDialogLocationSelect"
                >
                  <template #default="{ item }">
                    <div class="search-item">
                      <div class="search-name">{{ item.value }}</div>
                      <div class="search-address">{{ item.address }}</div>
                    </div>
                  </template>
                </el-autocomplete>
                <div v-if="activityForm.address" class="current-address" style="margin-top: 8px">
                  <el-icon><Location /></el-icon>
                  <span>{{ activityForm.address }}</span>
                </div>
              </el-form-item>

              <el-form-item label="预估费用(元)" prop="estimatedCost">
                <el-input-number
                  v-model="activityForm.estimatedCost"
                  :min="0"
                  :step="10"
                  style="width: 100%"
                />
              </el-form-item>
            </el-form>

            <template #footer>
              <el-button @click="activityDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="handleActivityDialogConfirm">确定</el-button>
            </template>
          </el-dialog>

          <!-- 步骤 3: 保存成功 -->
          <div v-if="currentStep === 2" class="step-content">
            <el-card class="success-card">
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
            </el-card>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  ArrowLeft,
  Microphone,
  Location,
  CircleCheck,
  MagicStick,
  InfoFilled,
  Edit,
  Check,
  Delete,
  Plus,
  Rank,
  ArrowUp,
  ArrowDown,
  Loading
} from '@element-plus/icons-vue'
import { generateTravelPlan, parseUserInput, type GeneratedPlanResponse, type GeneratePlanRequest, type ParsedUserInput } from '@/api/aiApi'
import { createTravelPlan } from '@/api/travelPlanApi'
import { XFYunSpeechRecognition, WebSpeechRecognition } from '@/services/speechService'
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
const geocodedCount = ref(0) // 已完成地理编码的地点数
const locationCache = ref<Map<string, AmapLocation>>(new Map()) // 地点坐标缓存

// 活动对话框相关状态
const activityDialogVisible = ref(false)
const activityDialogMode = ref<'add' | 'edit'>('add')
const activityFormRef = ref<FormInstance>()
const activityForm = reactive({
  type: 'attraction' as 'transport' | 'attraction' | 'restaurant' | 'accommodation',
  time: '',
  title: '',
  description: '',
  location: '',
  address: '',
  estimatedCost: 0
})
const activityDialogContext = reactive({
  day: 1,
  activityIndex: -1
})

let speechRecognition: XFYunSpeechRecognition | WebSpeechRecognition | null = null
const useXFYun = ref(true)  // 是否使用科大讯飞（优先）
let amapService = getAmapService()

// 表单数据
const form = reactive<{
  userInput: string
  destination: string
  duration: number
  budget: number
  travelers: number
  preferences: string[]
  additionalRequirements: string
}>({
  userInput: '',
  destination: '',
  duration: 5,
  budget: 5000,
  travelers: 1,
  preferences: [],
  additionalRequirements: ''
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

// 活动表单验证规则
const activityFormRules: FormRules = {
  type: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  time: [{ required: true, message: '请输入活动时间', trigger: 'blur' }],
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }]
}

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
const startVoiceInput = async () => {
  try {
    if (!speechRecognition) {
      // 优先尝试使用科大讯飞
      if (useXFYun.value) {
        try {
          speechRecognition = new XFYunSpeechRecognition()
          console.log('✅ 使用科大讯飞语音识别')
        } catch (error) {
          console.warn('⚠️ 科大讯飞初始化失败，降级到Web Speech API')
          speechRecognition = new WebSpeechRecognition()
          useXFYun.value = false
        }
      } else {
        speechRecognition = new WebSpeechRecognition()
      }
    }

    isRecording.value = true
    
    // 使用统一的回调接口
    await speechRecognition.startRecognition(
      (text: string, isFinal: boolean) => {
        // 过滤掉只有标点符号的结果
        const trimmedText = text.trim()
        
        if (trimmedText && trimmedText !== '。' && trimmedText !== '，' && trimmedText !== '？' && trimmedText !== '！') {
          // 实时更新文本
          form.userInput = trimmedText
          console.log(`🎤 CreatePlanView - 识别结果: "${trimmedText}" (${isFinal ? '完成' : '进行中'})`)
        } else {
          console.warn(`⚠️ 忽略无效结果: "${text}"`)
        }
        
        // 只有在最终结果时才提示
        if (isFinal && trimmedText) {
          ElMessage.success('语音识别完成，请点击"智能识别"按钮进行解析')
          isRecording.value = false
        }
      },
      (error: string) => {
        ElMessage.error(error)
        isRecording.value = false
      }
    )
  } catch (error: any) {
    ElMessage.error(error.message || '语音识别初始化失败')
    isRecording.value = false
  }
}

// 停止语音输入
const stopVoiceInput = () => {
  console.log('🛑 停止语音识别')
  if (speechRecognition) {
    speechRecognition.stopRecognition()
  }
  isRecording.value = false
}

// 解析用户输入（增强的关键词提取和模式匹配）
// 旧的本地解析函数（已被 AI 解析替代，保留作为备用）
// const parseUserInputLocal = (text: string) => {
//   ... 原有代码 ...
// }

// 智能识别按钮处理
const handleSmartParse = async () => {
  if (!form.userInput || form.userInput.trim().length === 0) {
    ElMessage.warning('请先输入旅行需求描述')
    return
  }

  parsing.value = true
  
  try {
    ElMessage({
      message: '🤖 通义千问 AI 正在智能解析你的需求...',
      type: 'info',
      duration: 2000
    })
    
    const parsedData = await parseUserInput(form.userInput)
    
    // 填充表单数据
    if (parsedData.destination) {
      form.destination = parsedData.destination
    }
    if (parsedData.duration) {
      form.duration = parsedData.duration
    }
    if (parsedData.budget) {
      form.budget = parsedData.budget
    }
    if (parsedData.travelers) {
      form.travelers = parsedData.travelers
    }
    if (parsedData.preferences && parsedData.preferences.length > 0) {
      // 合并偏好（避免重复）
      parsedData.preferences.forEach((pref: string) => {
        if (!form.preferences.includes(pref)) {
          form.preferences.push(pref)
        }
      })
    }
    if (parsedData.additionalRequirements) {
      form.additionalRequirements = parsedData.additionalRequirements
    }
    
    ElMessage.success('✨ AI 已智能识别并填充信息，请检查确认')
  } catch (error: any) {
    console.error('智能解析失败:', error)
    ElMessage.error('智能解析失败，请手动填写表单')
  } finally {
    parsing.value = false
  }
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
        preferences: form.preferences.join(','),
        additionalRequirements: form.additionalRequirements
      }

      generatedPlan.value = await generateTravelPlan(request)
      currentStep.value = 1
      
      ElMessage.success('🎉 计划生成成功！AI 已为你规划了详细的行程')
      
      // 地图初始化由 watch(generatedPlan) 自动触发，不需要在这里调用
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

// ==================== 活动对话框相关函数 ====================

/**
 * 打开添加活动对话框
 */
const openAddActivityDialog = (day: number) => {
  activityDialogMode.value = 'add'
  activityDialogContext.day = day
  activityDialogContext.activityIndex = -1
  
  // 重置表单
  Object.assign(activityForm, {
    type: 'attraction',
    time: '00:00-00:00',
    title: '',
    description: '',
    location: '',
    address: '',
    estimatedCost: 0
  })
  
  activityDialogVisible.value = true
}

/**
 * 打开编辑活动对话框
 */
const openEditActivityDialog = (day: number, activityIndex: number) => {
  if (!generatedPlan.value) return
  
  const dayPlan = generatedPlan.value.dailyPlans.find(d => d.day === day)
  if (!dayPlan) return
  
  const activity = dayPlan.activities[activityIndex]
  if (!activity) return
  
  activityDialogMode.value = 'edit'
  activityDialogContext.day = day
  activityDialogContext.activityIndex = activityIndex
  
  // 填充表单
  Object.assign(activityForm, {
    type: activity.type,
    time: activity.time,
    title: activity.title,
    description: activity.description,
    location: activity.location,
    address: activity.address || '',
    estimatedCost: activity.estimatedCost
  })
  
  activityDialogVisible.value = true
}

/**
 * 对话框中的地点搜索
 */
const searchLocationForDialog = async (queryString: string, callback: (suggestions: any[]) => void) => {
  if (!queryString || queryString.length < 2) {
    callback([])
    return
  }

  try {
    const result = await amapService.searchPlace(queryString, form.destination)
    
    if (result.success && result.location) {
      callback([{
        value: result.location.name,
        address: result.location.address || '',
        location: {
          lng: result.location.lng,
          lat: result.location.lat
        }
      }])
    } else {
      callback([])
    }
  } catch (error) {
    console.error('地点搜索失败:', error)
    callback([])
  }
}

/**
 * 对话框中选择地点
 */
const handleDialogLocationSelect = (item: any) => {
  activityForm.location = item.value
  activityForm.address = item.address
  
  // 更新缓存
  if (item.location) {
    locationCache.value.set(item.value, {
      name: item.value,
      address: item.address,
      lng: item.location.lng,
      lat: item.location.lat
    })
  }
}

/**
 * 确认对话框(添加或编辑活动)
 */
const handleActivityDialogConfirm = async () => {
  if (!activityFormRef.value) return
  
  await activityFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (!generatedPlan.value) return
    
    const day = activityDialogContext.day
    const dayPlan = generatedPlan.value.dailyPlans.find(d => d.day === day)
    if (!dayPlan) return
    
    if (activityDialogMode.value === 'add') {
      // 添加新活动
      const newActivity = {
        type: activityForm.type,
        time: activityForm.time,
        title: activityForm.title,
        description: activityForm.description,
        location: activityForm.location,
        address: activityForm.address,
        estimatedCost: activityForm.estimatedCost,
        editing: false
      }
      
      dayPlan.activities.push(newActivity)
      ElMessage.success('活动已添加')
    } else {
      // 编辑活动
      const activity = dayPlan.activities[activityDialogContext.activityIndex]
      if (activity) {
        Object.assign(activity, {
          type: activityForm.type,
          time: activityForm.time,
          title: activityForm.title,
          description: activityForm.description,
          location: activityForm.location,
          address: activityForm.address,
          estimatedCost: activityForm.estimatedCost
        })
        ElMessage.success('活动已更新')
      }
    }
    
    // 重新绘制地图
    await displayDayRoute(day)
    
    // 关闭对话框
    activityDialogVisible.value = false
  })
}

// ==================== 结束活动对话框相关函数 ====================

/**
 * 删除活动
 */
const deleteActivity = (day: number, activityIndex: number) => {
  if (!generatedPlan.value) return

  ElMessageBox.confirm(
    '确定要删除这个活动吗？',
    '确认删除',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    const dayPlan = generatedPlan.value!.dailyPlans.find(d => d.day === day)
    if (!dayPlan) return

    dayPlan.activities.splice(activityIndex, 1)
    
    // 重新绘制地图
    await displayDayRoute(day)
    
    ElMessage.success('活动已删除')
  }).catch(() => {
    // 用户取消
  })
}

/**
 * 地点搜索建议
 */
interface LocationSuggestion {
  value: string
  address: string
  location?: {
    lng: number
    lat: number
  }
}

/**
 * 处理地图标记点击
 */
const handleMarkerClick = (day: number, activityIndex: number, activity: any) => {
  const activityType = getActivityTypeText(activity.type)
  
  // 禁用地图交互
  amapService.disableMapInteraction()
  
  ElMessageBox.confirm(
    `<div style="padding: 8px;">
      <div style="margin-bottom: 16px;">
        <div style="font-size: 16px; font-weight: 600; color: #1f2937; margin-bottom: 8px;">
          ${activity.title}
        </div>
        <div style="display: inline-block; padding: 2px 8px; background: #eff6ff; color: #3b82f6; border-radius: 4px; font-size: 12px; margin-bottom: 8px;">
          ${activityType}
        </div>
      </div>
      <div style="color: #6b7280; font-size: 14px; line-height: 1.8;">
        <div style="margin-bottom: 6px;">
          <span style="color: #3b82f6;">📍</span> ${activity.address || activity.location}
        </div>
        <div style="margin-bottom: 6px;">
          <span style="color: #f59e0b;">⏰</span> ${activity.time}
        </div>
        <div>
          <span style="color: #10b981;">💰</span> 预估费用: ¥${activity.estimatedCost}
        </div>
      </div>
    </div>`,
    '活动详情',
    {
      confirmButtonText: '删除此活动',
      cancelButtonText: '关闭',
      type: 'warning',
      dangerouslyUseHTMLString: true,
      distinguishCancelAndClose: true,
      center: false,
      lockScroll: false
    }
  ).then(async () => {
    // 用户点击"删除此活动"
    deleteActivity(day, activityIndex)
  }).catch(() => {
    // 用户点击"关闭"或关闭对话框
  }).finally(() => {
    // 无论如何都要恢复地图交互
    amapService.enableMapInteraction()
  })
}

// ==================== 活动排序相关函数 ====================

/**
 * 移动活动(上移/下移)
 */
const moveActivity = async (day: number, activityIndex: number, direction: 'up' | 'down') => {
  if (!generatedPlan.value) return

  const dayPlan = generatedPlan.value.dailyPlans.find(d => d.day === day)
  if (!dayPlan) return

  const targetIndex = direction === 'up' ? activityIndex - 1 : activityIndex + 1
  
  // 边界检查
  if (targetIndex < 0 || targetIndex >= dayPlan.activities.length) return

  // 交换活动位置
  const activities = dayPlan.activities
  const temp = activities[activityIndex]!
  activities[activityIndex] = activities[targetIndex]!
  activities[targetIndex] = temp

  // 重新绘制地图
  await displayDayRoute(day)
  
  ElMessage.success(direction === 'up' ? '已上移' : '已下移')
}

/**
 * 拖拽相关状态
 */
const dragState = ref<{
  day: number
  fromIndex: number
} | null>(null)

/**
 * 拖拽开始
 */
const handleDragStart = (day: number, index: number, event: DragEvent) => {
  dragState.value = { day, fromIndex: index }
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('text/plain', String(index))
  }
  // 添加拖拽样式
  const target = event.target as HTMLElement
  target.style.opacity = '0.5'
}

/**
 * 拖拽经过
 */
const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
}

/**
 * 放置
 */
const handleDrop = async (day: number, toIndex: number, event: DragEvent) => {
  event.preventDefault()
  
  if (!dragState.value || !generatedPlan.value) return
  if (dragState.value.day !== day) return // 只允许同一天内拖拽
  
  const fromIndex = dragState.value.fromIndex
  if (fromIndex === toIndex) return

  const dayPlan = generatedPlan.value.dailyPlans.find(d => d.day === day)
  if (!dayPlan) return

  // 重新排序活动
  const [movedActivity] = dayPlan.activities.splice(fromIndex, 1)
  if (movedActivity) {
    dayPlan.activities.splice(toIndex, 0, movedActivity)
  }

  // 重新绘制地图
  await displayDayRoute(day)
  
  ElMessage.success('活动已调整顺序')
}

/**
 * 拖拽结束
 */
const handleDragEnd = (event: DragEvent) => {
  // 恢复样式
  const target = event.target as HTMLElement
  target.style.opacity = '1'
  dragState.value = null
}

// ==================== 结束排序相关函数 ====================

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

    // 准备AI生成的计划数据
    const aiGeneratedPlan = JSON.stringify({
      dailyPlans: generatedPlan.value.dailyPlans,
      tips: generatedPlan.value.tips
    })

    await createTravelPlan({
      destination: form.destination,
      startDate: startDate.toISOString().split('T')[0] || '',
      endDate: endDate.toISOString().split('T')[0] || '',
      duration: form.duration,
      budget: form.budget,
      travelers: form.travelers,
      preferences: form.preferences,
      userInput: form.userInput,
      aiGeneratedPlan: aiGeneratedPlan
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
    // 防止重复初始化
    if (mapLoadingStatus.value === 'loading') {
      console.log('地图正在初始化中，跳过重复调用')
      return
    }

    mapLoadingStatus.value = 'loading'
    mapErrorMessage.value = ''
    unlocatedPlaces.value = []
    geocodedCount.value = 0

    // 检查目的地是否在中国境内（通过高德 API 验证，最精准！）
    console.log(`验证目的地 "${form.destination}" 是否在中国境内...`)
    const validation = await amapService.validateChinaDestination(form.destination)
    
    if (!validation.isChina) {
      console.warn(`目的地 "${form.destination}" 不在中国境内或无法定位，高德地图仅支持中国地区`)
      mapLoadingStatus.value = 'error'
      mapErrorMessage.value = `高德地图仅支持中国境内地点定位，"${form.destination}" 无法在地图上显示路线。您仍可以查看生成的行程计划。`
      return
    }
    
    console.log(`✅ 目的地 "${form.destination}" 验证通过，开始初始化地图...`)

    // 检查高德地图 SDK 是否加载
    if (!window.AMap) {
      console.error('高德地图 SDK 未加载')
      mapLoadingStatus.value = 'error'
      mapErrorMessage.value = '高德地图 SDK 未加载,请检查网络连接或刷新页面'
      return
    }

    console.log('开始初始化地图...')

    // 初始化地图
    const map = amapService.initMap('amap-container', [116.397428, 39.90923], 12)
    
    if (!map) {
      throw new Error('地图初始化失败')
    }

    // 等待一小段时间确保地图完全加载
    await new Promise((resolve) => setTimeout(resolve, 500))

    console.log('地图初始化完成，开始地理编码...')

    // 收集所有地点并进行地理编码
    await geocodeAllActivities()

    console.log('地理编码完成，显示第一天路线...')

    // 显示第一天的路线
    selectedDay.value = 1
    await displayDayRoute(1)

    // 启用地图点击添加活动功能
    enableMapClickToAdd()

    mapLoadingStatus.value = 'success'
    console.log('地图初始化和路线显示完成')
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
  
  // 收集所有地点（只存储原始地点名称）
  generatedPlan.value.dailyPlans.forEach((dayPlan) => {
    dayPlan.activities.forEach((activity) => {
      if (activity.location && !allPlaces.includes(activity.location)) {
        allPlaces.push(activity.location)
      }
    })
  })

  totalPlaces.value = allPlaces.length
  geocodedCount.value = 0
  unlocatedPlaces.value = []

  console.log(`开始地点搜索，共 ${allPlaces.length} 个地点`)

  // 逐个进行地点搜索，实时更新进度
  for (let i = 0; i < allPlaces.length; i++) {
    const placeName = allPlaces[i]
    if (!placeName) continue
    
    // 添加延迟避免频率限制
    if (i > 0) {
      await new Promise((resolve) => setTimeout(resolve, 500))
    }

    // 使用地点搜索 API（比地理编码更准确）
    const result = await amapService.searchPlace(placeName, form.destination)
    
    if (result.success && result.location) {
      // 使用原始地点名称作为 key
      locationCache.value.set(placeName, result.location)
      console.log(`✅ 地点搜索成功: ${placeName} -> ${result.location.name}`)
      
      // 更新所有匹配的 activity 的地址和坐标信息
      generatedPlan.value.dailyPlans.forEach((dayPlan) => {
        dayPlan.activities.forEach((activity) => {
          if (activity.location === placeName) {
            activity.address = result.location!.address || result.location!.name
            activity.coordinate = {
              latitude: result.location!.lat,
              longitude: result.location!.lng
            }
            console.log(`📍 更新地址和坐标: ${placeName} -> ${activity.address}`)
          }
        })
      })
    } else {
      unlocatedPlaces.value.push(placeName)
      console.warn(`❌ 地点搜索失败: ${placeName}`)
    }

    geocodedCount.value = i + 1
  }

  console.log(`地点搜索完成: 成功 ${totalPlaces.value - unlocatedPlaces.value.length}/${totalPlaces.value}`)
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
  dayPlan.activities.forEach((activity, activityIndex) => {
    if (!activity.location) return

    // 直接使用原始地点名称查询缓存
    const cachedLocation = locationCache.value.get(activity.location)

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
        }),
        {
          // 左键点击标记弹出操作菜单
          onClick: () => {
            handleMarkerClick(day, activityIndex, activity)
          },
          customData: {
            day,
            activityIndex,
            activity
          }
        }
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

/**
 * 启用地图点击添加活动功能
 */
const enableMapClickToAdd = () => {
  amapService.onMapClick(async (clickLocation) => {
    console.log('地图点击位置:', clickLocation)
    console.log('点击地址:', clickLocation.address)
    
    // 禁用地图交互
    amapService.disableMapInteraction()
    
    // 弹出确认对话框
    ElMessageBox.prompt('请输入活动名称', '添加活动到当前天', {
      confirmButtonText: '添加',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：中山陵、夫子庙等',
      inputPattern: /.+/,
      inputErrorMessage: '活动名称不能为空'
    }).then(async ({ value: activityName }) => {
      if (!generatedPlan.value) return
      
      const day = selectedDay.value
      const dayPlan = generatedPlan.value.dailyPlans.find(d => d.day === day)
      if (!dayPlan) return

      // 确定地址（如果逆地理编码失败，则主动查询）
      let activityAddress = clickLocation.address
      
      if (!activityAddress) {
        console.log('⏳ 逆地理编码未返回地址，使用searchPlace查询...')
        try {
          const result = await amapService.searchPlace(activityName, form.destination)
          if (result.success && result.location) {
            activityAddress = result.location.address || result.location.name
            console.log('✅ searchPlace查询成功:', activityAddress)
          } else {
            // 查询失败，使用活动名称
            activityAddress = activityName
            console.log('⚠️ searchPlace查询失败，使用活动名称')
          }
        } catch (error) {
          console.error('❌ searchPlace查询异常:', error)
          activityAddress = activityName
        }
      } else {
        console.log('✅ 使用逆地理编码地址:', activityAddress)
      }

      // 创建新活动（包含坐标信息和确定的地址）
      const newActivity = {
        time: '00:00-00:00',
        type: 'attraction' as const,
        title: activityName,
        description: '通过地图点击添加',
        location: activityName,
        address: activityAddress,
        coordinate: {
          latitude: clickLocation.lat,
          longitude: clickLocation.lng
        },
        estimatedCost: 0,
        editing: false
      }

      // 添加到当前天
      dayPlan.activities.push(newActivity)
      
      // 更新位置缓存
      locationCache.value.set(activityName, {
        name: activityName,
        address: newActivity.address,
        lng: clickLocation.lng,
        lat: clickLocation.lat
      })

      console.log('CreatePlanView - 活动已添加，最终地址:', newActivity.address)

      // 重新绘制地图
      await displayDayRoute(day)
      
      ElMessage.success(`已添加 "${activityName}" 到第 ${day} 天`)
    }).catch(() => {
      // 用户取消
    }).finally(() => {
      // 无论如何都要恢复地图交互
      amapService.enableMapInteraction()
    })
  })
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

/* 步骤指示器样式优化 */
.steps :deep(.el-step__icon) {
  width: 40px;
  height: 40px;
  font-size: 18px;
}

.steps :deep(.el-step__icon.is-text) {
  border: 2px solid #e0e0e0;
  background-color: #fff;
  color: #909399;
}

.steps :deep(.el-step__title) {
  font-size: 15px;
  font-weight: 500;
  color: #606266;
}

.steps :deep(.el-step__title.is-process) {
  font-weight: 600;
  color: #409eff;
}

.steps :deep(.el-step__title.is-finish) {
  color: #67c23a;
}

.steps :deep(.el-step__icon.is-icon) {
  background-color: #409eff;
  color: #fff;
  border: none;
}

.steps :deep(.el-step__line) {
  background-color: #e0e0e0;
}

.steps :deep(.el-step.is-process .el-step__icon) {
  background-color: #409eff;
  border-color: #409eff;
  color: #fff;
}

.steps :deep(.el-step.is-finish .el-step__icon) {
  background-color: #67c23a;
  border-color: #67c23a;
  color: #fff;
}

.steps :deep(.el-step.is-finish .el-step__line) {
  background-color: #67c23a;
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

/* 成功卡片 */
.success-card {
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 40px 20px;
}

.success-card :deep(.el-result__icon svg) {
  width: 80px;
  height: 80px;
}

.success-card :deep(.el-result__title) {
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
  margin-top: 24px;
}

.success-card :deep(.el-result__subtitle) {
  font-size: 16px;
  color: #6b7280;
  margin-top: 12px;
}

.success-card :deep(.el-result__extra) {
  margin-top: 32px;
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

.map-wrapper {
  position: relative;
  width: 100%;
}

.amap-container {
  width: 100%;
  height: 500px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 地图加载遮罩 */
.map-loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  border-radius: 12px;
  pointer-events: all; /* 阻止鼠标事件穿透 */
  cursor: not-allowed;
}

.map-loading-content {
  text-align: center;
  padding: 32px;
}

.map-loading-content .el-icon {
  color: #667eea;
  margin-bottom: 16px;
}

.loading-text {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.loading-subtext {
  font-size: 14px;
  color: #6b7280;
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
  cursor: move; /* 拖拽光标 */
  transition: background-color 0.2s, transform 0.2s;
}

.activity-item:hover {
  background-color: #f9fafb;
  transform: translateX(4px);
}

.activity-item:last-child {
  border-bottom: none;
}

/* 活动查看模式 */
.activity-view {
  position: relative;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 12px;
}

.activity-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.drag-handle {
  cursor: grab;
  color: #9ca3af;
  font-size: 18px;
  flex-shrink: 0;
  transition: color 0.2s;
}

.drag-handle:hover {
  color: #6b7280;
}

.drag-handle:active {
  cursor: grabbing;
}

.activity-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.activity-item:hover .activity-actions {
  opacity: 1;
}

.activity-time {
  color: #6b7280;
  font-size: 14px;
  flex-shrink: 0;
}

/* 活动编辑模式 */
.activity-edit {
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  margin: 8px 0;
}

.current-address {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  padding: 8px;
  background: #eff6ff;
  border-radius: 4px;
  font-size: 13px;
  color: #3b82f6;
}

.search-item {
  padding: 4px 0;
}

.search-name {
  font-size: 14px;
  color: #1f2937;
  margin-bottom: 2px;
}

.search-address {
  font-size: 12px;
  color: #6b7280;
}

/* 添加活动按钮 */
.add-activity {
  padding: 16px 0;
  text-align: center;
  border-top: 1px dashed #d1d5db;
  margin-top: 8px;
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
  max-width: 60%;
  overflow: hidden;
}

.activity-location .locating {
  color: #f59e0b;
  font-style: italic;
  animation: pulse 1.5s ease-in-out infinite;
}

.activity-location .no-address {
  color: #9ca3af;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
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

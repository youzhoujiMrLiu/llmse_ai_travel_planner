<!-- src/components/PlanEditor.vue -->
<!-- 可复用的计划编辑组件 -->
<template>
  <div class="plan-editor">
    <!-- 地图容器 -->
    <div class="map-container">
      <div class="map-header">
        <h3>🗺️ 行程地图</h3>
        <el-button-group>
          <el-button
            v-for="dayPlan in plan.dailyPlans"
            :key="dayPlan.day"
            :type="selectedDay === dayPlan.day ? 'primary' : 'default'"
            size="small"
            @click="switchMapDay(dayPlan.day)"
          >
            第 {{ dayPlan.day }} 天
          </el-button>
        </el-button-group>
      </div>
      
      <div id="plan-editor-map" class="map" ref="mapContainer"></div>
      
      <!-- 地图加载遮罩 -->
      <div v-if="isGeocoding" class="map-loading-overlay">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p>正在定位地点...</p>
        <p class="progress-text">{{ geocodedCount }} / {{ totalPlaces }}</p>
      </div>
    </div>

    <!-- 行程列表 -->
    <div class="itinerary-container">
      <div class="itinerary-header">
        <h3>📅 行程安排</h3>
        <div class="summary-info">
          <el-tag type="info">{{ plan.destination }}</el-tag>
          <el-tag type="success">{{ plan.duration }}天</el-tag>
          <el-tag type="warning">预算: ¥{{ plan.budget?.toLocaleString() }}</el-tag>
        </div>
      </div>

      <el-timeline class="timeline">
        <el-timeline-item
          v-for="dayPlan in plan.dailyPlans"
          :key="dayPlan.day"
          :hollow="true"
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

      <!-- 旅行建议 -->
      <div class="tips-section" v-if="plan.tips && plan.tips.length > 0">
        <h3>💡 旅行建议</h3>
        <ul class="tips-list">
          <li v-for="(tip, index) in plan.tips" :key="index">{{ tip }}</li>
        </ul>
      </div>

      <!-- 操作按钮 -->
      <div class="actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="isSaving">
          保存计划
        </el-button>
      </div>
    </div>

    <!-- 活动编辑对话框 -->
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
        <el-row :gutter="12">
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
          <el-input v-model="activityForm.title" placeholder="活动名称" />
        </el-form-item>
        
        <el-form-item label="活动描述">
          <el-input 
            v-model="activityForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="详细描述"
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
          <div v-if="activityForm.address" class="current-address">
            <el-icon><Location /></el-icon>
            <span>{{ activityForm.address }}</span>
          </div>
        </el-form-item>
        
        <el-form-item label="预估费用(元)">
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
        <el-button type="primary" @click="handleActivityDialogConfirm">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Edit,
  Delete,
  Location,
  Loading,
  Rank,
  ArrowUp,
  ArrowDown
} from '@element-plus/icons-vue'
import { getAmapService } from '@/services/amapService'

const amapService = getAmapService()

// Props
interface Props {
  plan: any
  isSaving?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isSaving: false
})

// Emits
const emit = defineEmits<{
  save: [plan: any]
  cancel: []
}>()

// 地图相关
const mapContainer = ref<HTMLElement>()
const isGeocoding = ref(false)
const geocodedCount = ref(0)
const totalPlaces = ref(0)
const locationCache = ref(new Map())
const selectedDay = ref(1) // 当前查看的天数

// 活动对话框
const activityDialogVisible = ref(false)
const activityDialogMode = ref<'add' | 'edit'>('add')
const activityFormRef = ref<FormInstance>()
const activityForm = reactive({
  type: 'attraction',
  time: '',
  title: '',
  description: '',
  location: '',
  address: '',
  estimatedCost: 0
})
const activityDialogContext = reactive({ day: 1, activityIndex: -1 })

// 表单验证规则
const activityFormRules: FormRules = {
  type: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  time: [{ required: true, message: '请输入活动时间', trigger: 'blur' }],
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }]
}

// 拖拽相关
let draggedDay = 0
let draggedIndex = 0

const handleDragStart = (day: number, index: number, event: DragEvent) => {
  draggedDay = day
  draggedIndex = index
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
  }
}

const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
}

const handleDrop = (day: number, index: number, event: DragEvent) => {
  event.preventDefault()
  
  if (draggedDay === day && draggedIndex !== index) {
    const dayPlan = props.plan.dailyPlans.find((d: any) => d.day === day)
    if (!dayPlan) return
    
    const activities = dayPlan.activities
    const draggedActivity = activities[draggedIndex]
    
    activities.splice(draggedIndex, 1)
    activities.splice(index, 0, draggedActivity)
    
    displayDayRoute(day)
  }
}

const handleDragEnd = () => {
  draggedDay = 0
  draggedIndex = 0
}

// 上移/下移活动
const moveActivity = async (day: number, index: number, direction: 'up' | 'down') => {
  const dayPlan = props.plan.dailyPlans.find((d: any) => d.day === day)
  if (!dayPlan) return
  
  const newIndex = direction === 'up' ? index - 1 : index + 1
  if (newIndex < 0 || newIndex >= dayPlan.activities.length) return
  
  const activities = dayPlan.activities
  const temp = activities[index]
  activities[index] = activities[newIndex]
  activities[newIndex] = temp
  
  await displayDayRoute(day)
}

// 打开添加活动对话框
const openAddActivityDialog = (day: number) => {
  activityDialogMode.value = 'add'
  activityDialogContext.day = day
  activityDialogContext.activityIndex = -1
  
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

// 打开编辑活动对话框
const openEditActivityDialog = (day: number, activityIndex: number) => {
  const dayPlan = props.plan.dailyPlans.find((d: any) => d.day === day)
  if (!dayPlan) return
  
  const activity = dayPlan.activities[activityIndex]
  if (!activity) return
  
  activityDialogMode.value = 'edit'
  activityDialogContext.day = day
  activityDialogContext.activityIndex = activityIndex
  
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

// 对话框中的地点搜索
const searchLocationForDialog = async (queryString: string, callback: (suggestions: any[]) => void) => {
  if (!queryString || queryString.length < 2) {
    callback([])
    return
  }

  try {
    const result = await amapService.searchPlace(queryString, props.plan.destination)
    
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

// 对话框中选择地点
const handleDialogLocationSelect = (item: any) => {
  activityForm.location = item.value
  activityForm.address = item.address
  
  if (item.location) {
    locationCache.value.set(item.value, {
      name: item.value,
      address: item.address,
      lng: item.location.lng,
      lat: item.location.lat
    })
  }
}

// 确认对话框
const handleActivityDialogConfirm = async () => {
  if (!activityFormRef.value) return
  
  await activityFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    const day = activityDialogContext.day
    const dayPlan = props.plan.dailyPlans.find((d: any) => d.day === day)
    if (!dayPlan) return
    
    if (activityDialogMode.value === 'add') {
      const newActivity = {
        type: activityForm.type,
        time: activityForm.time,
        title: activityForm.title,
        description: activityForm.description,
        location: activityForm.location,
        address: activityForm.address,
        estimatedCost: activityForm.estimatedCost
      }
      
      dayPlan.activities.push(newActivity)
      ElMessage.success('活动已添加')
    } else {
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
    
    await displayDayRoute(day)
    activityDialogVisible.value = false
  })
}

// 删除活动
const deleteActivity = (day: number, activityIndex: number) => {
  ElMessageBox.confirm(
    '确定要删除这个活动吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    const dayPlan = props.plan.dailyPlans.find((d: any) => d.day === day)
    if (!dayPlan) return

    dayPlan.activities.splice(activityIndex, 1)
    await displayDayRoute(day)
    ElMessage.success('活动已删除')
  }).catch(() => {})
}

// 地图相关函数
const displayDayRoute = async (day: number) => {
  const dayPlan = props.plan.dailyPlans.find((d: any) => d.day === day)
  if (!dayPlan) return

  // 检查地图是否已初始化（如果目的地在国外，地图不会被初始化）
  if (!window.AMap || !amapService) {
    console.log('地图未初始化，跳过路线显示')
    return
  }

  amapService.clearAll()
  
  console.log(`🗺️ 显示第 ${day} 天的路线，共 ${dayPlan.activities.length} 个活动`)
  
  // 第一步：收集所有需要查询的地点
  const locationsToQuery: string[] = []
  for (const activity of dayPlan.activities) {
    if (activity.location && !locationCache.value.has(activity.location)) {
      if (!locationsToQuery.includes(activity.location)) {
        locationsToQuery.push(activity.location)
      }
    }
  }
  
  // 第二步：批量查询缺失的地点（如果有的话）
  if (locationsToQuery.length > 0) {
    console.log(`⏳ 需要查询 ${locationsToQuery.length} 个地点的坐标`)
    for (const location of locationsToQuery) {
      try {
        const result = await amapService.searchPlace(location, props.plan.destination)
        if (result.success && result.location) {
          locationCache.value.set(location, result.location)
          
          // 更新活动地址
          dayPlan.activities.forEach((activity: any) => {
            if (activity.location === location && !activity.address) {
              activity.address = result.location!.address || result.location!.name
            }
          })
          
          console.log(`✅ 查询成功: ${location}`)
        } else {
          console.warn(`❌ 查询失败: ${location}`)
        }
      } catch (error) {
        console.error(`地理编码失败: ${location}`, error)
      }
    }
  }
  
  // 第三步：添加所有标记（此时locationCache已经包含所有需要的地点）
  const validLocations = []
  let markerIndex = 1
  
  for (const activity of dayPlan.activities) {
    const locationInfo = locationCache.value.get(activity.location)
    
    if (locationInfo) {
      validLocations.push(locationInfo)
      
      // 使用带序号的标记图标（与CreatePlanView一致）
      amapService.addMarker(
        locationInfo,
        `${markerIndex}. ${activity.title}`,
        new window.AMap.Icon({
          size: new window.AMap.Size(32, 32),
          image: `https://webapi.amap.com/theme/v1.3/markers/n/mark_b${markerIndex}.png`,
        }),
        {
          onClick: () => handleMarkerClick(day, dayPlan.activities.indexOf(activity), activity)
        }
      )
      
      markerIndex++
    } else {
      console.warn(`⚠️ 无法获取地点坐标: ${activity.location}`)
    }
  }
  
  console.log(`✅ 已添加 ${validLocations.length} 个标记`)
  
  // 第四步：绘制路线
  if (validLocations.length >= 2) {
    amapService.drawPath(validLocations, '#3b82f6', true)
  }
  
  // 第五步：调整视野
  if (validLocations.length > 0) {
    amapService.fitView()
  }
}

// 切换显示的天数
const switchMapDay = async (day: number) => {
  selectedDay.value = day
  await displayDayRoute(day)
}

const handleMarkerClick = (day: number, activityIndex: number, activity: any) => {
  const activityType = getActivityTypeText(activity.type)
  
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
      <div style="color: #6b7280; font-size: 14px; line-height: 1.6; margin-bottom: 12px;">
        ${activity.description}
      </div>
      <div style="display: flex; align-items: center; color: #9ca3af; font-size: 12px; margin-bottom: 8px;">
        <span style="margin-right: 4px;">⏰</span>
        <span>${activity.time}</span>
      </div>
      <div style="display: flex; align-items: center; color: #9ca3af; font-size: 12px; margin-bottom: 8px;">
        <span style="margin-right: 4px;">📍</span>
        <span>${activity.address || activity.location}</span>
      </div>
      <div style="display: flex; align-items: center; color: #9ca3af; font-size: 12px;">
        <span style="margin-right: 4px;">💰</span>
        <span>预估: ¥${activity.estimatedCost}</span>
      </div>
    </div>`,
    '活动详情',
    {
      confirmButtonText: '删除此活动',
      cancelButtonText: '关闭',
      type: 'info',
      dangerouslyUseHTMLString: true,
      distinguishCancelAndClose: true,
      confirmButtonClass: 'el-button--danger'
    }
  ).then(() => {
    deleteActivity(day, activityIndex)
  }).catch(() => {}).finally(() => {
    amapService.enableMapInteraction()
  })
}

// 工具函数
const getActivityTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    transport: '交通',
    attraction: '景点',
    restaurant: '餐饮',
    accommodation: '住宿'
  }
  return typeMap[type] || type
}

const getActivityTypeTag = (type: string) => {
  const tagMap: Record<string, any> = {
    transport: 'info',
    attraction: 'success',
    restaurant: 'warning',
    accommodation: 'danger'
  }
  return tagMap[type] || 'info'
}

const getActivityIcon = (type: string) => {
  const iconMap: Record<string, string> = {
    transport: '🚗',
    attraction: '🎯',
    restaurant: '🍽️',
    accommodation: '🏨'
  }
  return iconMap[type] || '📍'
}

// 初始化地图和地理编码
const initMapAndGeocode = async () => {
  try {
    // 检查目的地是否在中国境内（通过高德 API 验证，最精准！）
    console.log(`验证目的地 "${props.plan.destination}" 是否在中国境内...`)
    const validation = await amapService.validateChinaDestination(props.plan.destination as string)
    
    if (!validation.isChina) {
      console.warn(`目的地 "${props.plan.destination}" 不在中国境内或无法定位，高德地图仅支持中国地区`)
      ElMessage.warning(`高德地图仅支持中国境内地点定位，"${props.plan.destination}" 无法在地图上显示路线。您仍可以查看和编辑行程计划。`)
      // 不初始化地图，直接返回
      return
    }
    
    console.log(`✅ 目的地 "${props.plan.destination}" 验证通过，开始初始化地图...`)
    
    await amapService.initMap('plan-editor-map')
    
    console.log('🗺️ 初始化地图，检查已有坐标信息...')
    
    // 优化策略：优先使用activity.coordinate，避免重复查询API
    const allActivities = props.plan.dailyPlans.flatMap((day: any) => day.activities)
    
    // 从已有的坐标信息构建locationCache
    let cachedCount = 0
    for (const activity of allActivities) {
      if (!activity.location) continue
      
      // 如果activity有坐标信息，直接使用
      if (activity.coordinate?.latitude && activity.coordinate?.longitude) {
        locationCache.value.set(activity.location, {
          name: activity.location,
          address: activity.address || activity.location,
          lng: activity.coordinate.longitude,
          lat: activity.coordinate.latitude
        })
        cachedCount++
        console.log(`✅ 使用已有坐标: ${activity.location}`)
      }
    }
    
    console.log(`✅ 从坐标信息恢复了 ${cachedCount} 个地点，无需查询API`)
    
    // 只有在没有坐标信息的情况下才查询API
    const locationsNeedQuery = [...new Set(
      allActivities
        .filter((a: any) => a.location && !locationCache.value.has(a.location))
        .map((a: any) => a.location)
    )]
    
    if (locationsNeedQuery.length > 0) {
      console.log(`⏳ 需要查询坐标的地点: ${locationsNeedQuery.length} 个`)
      
      isGeocoding.value = true
      geocodedCount.value = 0
      totalPlaces.value = locationsNeedQuery.length
      
      for (const location of locationsNeedQuery) {
        try {
          if (geocodedCount.value > 0) {
            await new Promise((resolve) => setTimeout(resolve, 500))
          }
          
          const result = await amapService.searchPlace(location as string, props.plan.destination as string)
          if (result.success && result.location) {
            locationCache.value.set(location, result.location)
            
            // 更新活动的地址和坐标
            props.plan.dailyPlans.forEach((dayPlan: any) => {
              dayPlan.activities.forEach((activity: any) => {
                if (activity.location === location) {
                  if (!activity.address) {
                    activity.address = result.location!.address || result.location!.name
                  }
                  if (!activity.coordinate) {
                    activity.coordinate = {
                      latitude: result.location!.lat,
                      longitude: result.location!.lng
                    }
                  }
                  console.log(`📍 查询并更新: ${location} -> ${activity.address}`)
                }
              })
            })
          }
          geocodedCount.value++
        } catch (error) {
          console.error(`地理编码失败: ${location}`, error)
          geocodedCount.value++
        }
      }
      
      isGeocoding.value = false
    }
    
    console.log(`✅ 地图初始化完成，locationCache 包含 ${locationCache.value.size} 个地点`)
    
    if (props.plan.dailyPlans.length > 0) {
      await displayDayRoute(1)
    }
    
    // 启用地图点击添加活动功能
    enableMapClickToAdd()
  } catch (error) {
    console.error('初始化地图失败:', error)
    ElMessage.error('地图初始化失败')
    isGeocoding.value = false
  }
}

// 启用地图点击添加活动功能
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
      const day = selectedDay.value
      const dayPlan = props.plan.dailyPlans.find((d: any) => d.day === day)
      if (!dayPlan) return

      // 确定地址（如果逆地理编码失败，则主动查询）
      let activityAddress = clickLocation.address
      
      if (!activityAddress) {
        console.log('⏳ 逆地理编码未返回地址，使用searchPlace查询...')
        try {
          const result = await amapService.searchPlace(activityName, props.plan.destination)
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
        type: 'attraction',
        title: activityName,
        description: '通过地图点击添加',
        location: activityName,
        address: activityAddress,
        coordinate: {
          latitude: clickLocation.lat,
          longitude: clickLocation.lng
        },
        estimatedCost: 0
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

      console.log('PlanEditor - 活动已添加，最终地址:', newActivity.address)

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

// 事件处理
const handleSave = () => {
  emit('save', props.plan)
}

const handleCancel = () => {
  emit('cancel')
}

// 生命周期
onMounted(() => {
  initMapAndGeocode()
})
</script>

<style scoped>
.plan-editor {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  height: calc(100vh - 80px);
  padding: 24px;
}

.map-container {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.map {
  width: 100%;
  height: 100%;
}

.map-loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  pointer-events: all;
}

.map-loading-overlay p {
  margin-top: 12px;
  font-size: 14px;
  color: #666;
}

.progress-text {
  font-size: 12px;
  color: #999;
}

.itinerary-container {
  overflow-y: auto;
  padding: 0 8px;
}

.itinerary-header {
  margin-bottom: 24px;
}

.itinerary-header h3 {
  font-size: 20px;
  margin-bottom: 12px;
}

.summary-info {
  display: flex;
  gap: 8px;
}

.timeline {
  padding: 0;
}

.day-card {
  margin-bottom: 16px;
}

.activity-item {
  margin-bottom: 12px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  cursor: move;
  transition: all 0.3s;
}

.activity-item:hover {
  background: #f3f4f6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.activity-view {
  pointer-events: auto;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.activity-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.drag-handle {
  cursor: move;
  color: #9ca3af;
  font-size: 18px;
}

.activity-time {
  color: #6b7280;
  font-size: 14px;
}

.activity-actions {
  display: flex;
  gap: 4px;
}

.activity-view h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
}

.activity-desc {
  color: #6b7280;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.activity-location {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #6b7280;
}

.locating {
  color: #3b82f6;
  font-style: italic;
}

.no-address {
  color: #9ca3af;
}

.activity-cost {
  color: #059669;
  font-weight: 500;
}

.add-activity {
  margin-top: 12px;
  text-align: center;
}

.tips-section {
  margin: 24px 0;
  padding: 16px;
  background: #eff6ff;
  border-radius: 8px;
}

.tips-section h3 {
  font-size: 16px;
  margin-bottom: 12px;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
}

.tips-list li {
  margin-bottom: 8px;
  color: #1f2937;
  line-height: 1.6;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.current-address {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  padding: 8px;
  background: #f3f4f6;
  border-radius: 4px;
  font-size: 13px;
  color: #6b7280;
}

.search-item {
  padding: 8px 0;
}

.search-name {
  font-size: 14px;
  color: #1f2937;
  margin-bottom: 4px;
}

.search-address {
  font-size: 12px;
  color: #9ca3af;
}
</style>

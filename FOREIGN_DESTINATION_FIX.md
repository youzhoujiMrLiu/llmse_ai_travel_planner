# 国外目的地地图逻辑修复

## 问题描述

在创建计划时,如果目的地在国外,系统会正确屏蔽地图功能;但在查看和编辑已有计划时,地图依然会尝试执行搜索工作,导致:
1. 搜索到国内的错误地点
2. 地图标记位置错误
3. 用户体验不一致

## 根本原因

`CreatePlanView.vue` 在 `initializeMap()` 函数中有目的地验证逻辑:

```typescript
// 检查目的地是否在中国境内(通过高德 API 验证,最精准!)
const validation = await amapService.validateChinaDestination(form.destination)

if (!validation.isChina) {
  console.warn(`目的地 "${form.destination}" 不在中国境内或无法定位,高德地图仅支持中国地区`)
  mapLoadingStatus.value = 'error'
  mapErrorMessage.value = `高德地图仅支持中国境内地点定位,"${form.destination}" 无法在地图上显示路线。您仍可以查看生成的行程计划。`
  return
}
```

但 `PlanEditor.vue` 组件**缺少这个验证逻辑**,导致两者行为不一致。

## 解决方案

### 1. 在 PlanEditor.vue 的 initMapAndGeocode() 中添加验证

在地图初始化之前,先验证目的地是否在中国境内:

```typescript
// 初始化地图和地理编码
const initMapAndGeocode = async () => {
  try {
    // 检查目的地是否在中国境内(通过高德 API 验证,最精准!)
    console.log(`验证目的地 "${props.plan.destination}" 是否在中国境内...`)
    const validation = await amapService.validateChinaDestination(props.plan.destination as string)
    
    if (!validation.isChina) {
      console.warn(`目的地 "${props.plan.destination}" 不在中国境内或无法定位,高德地图仅支持中国地区`)
      ElMessage.warning(`高德地图仅支持中国境内地点定位,"${props.plan.destination}" 无法在地图上显示路线。您仍可以查看和编辑行程计划。`)
      // 不初始化地图,直接返回
      return
    }
    
    console.log(`✅ 目的地 "${props.plan.destination}" 验证通过,开始初始化地图...`)
    
    await amapService.initMap('plan-editor-map')
    
    // ... 后续地图初始化逻辑
  }
}
```

### 2. 在 displayDayRoute() 中添加防御性检查

虽然如果目的地在国外,地图不会被初始化,`displayDayRoute()` 也不会被调用,但为了防御性编程,还是添加了检查:

```typescript
const displayDayRoute = async (day: number) => {
  const dayPlan = props.plan.dailyPlans.find((d: any) => d.day === day)
  if (!dayPlan) return

  // 检查地图是否已初始化(如果目的地在国外,地图不会被初始化)
  if (!window.AMap || !amapService) {
    console.log('地图未初始化,跳过路线显示')
    return
  }

  amapService.clearAll()
  
  // ... 后续路线显示逻辑
}
```

## 验证逻辑

使用 `amapService.validateChinaDestination()` 方法:

```typescript
async validateChinaDestination(destination: string): Promise<{ isChina: boolean, error?: string }> {
  try {
    console.log(`🌍 验证目的地是否在中国: ${destination}`)
    
    const params = new URLSearchParams({
      address: destination
    })

    const response = await fetch(`http://localhost:8080/api/map/geocode?${params.toString()}`)
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const result = await response.json()
    
    console.log(`地理编码验证响应:`, result)

    // 高德 API 返回 status=1 且有 geocodes 数据,说明是中国境内地点
    if (result.status === '1' && result.geocodes && result.geocodes.length > 0) {
      console.log(`✅ "${destination}" 是中国境内地点`)
      return { isChina: true }
    } else {
      console.warn(`❌ "${destination}" 不是中国境内地点或无法定位`)
      return { 
        isChina: false, 
        error: result.info || '高德地图无法定位此地点'
      }
    }
  } catch (error: any) {
    console.error(`验证目的地异常: ${destination}`, error)
    // 网络错误等异常情况,保守处理,假定为中国境内(避免误判)
    return { 
      isChina: true, 
      error: '网络异常,无法验证目的地'
    }
  }
}
```

## 修改的文件

1. **travel-planner-frontend/src/components/PlanEditor.vue**
   - 在 `initMapAndGeocode()` 开头添加目的地验证逻辑
   - 在 `displayDayRoute()` 开头添加地图初始化检查

## 测试场景

### 场景1: 国内目的地
1. 创建一个目的地为"北京"的计划
2. 保存后查看该计划
3. **预期结果**: 
   - 地图正常显示
   - 所有景点标记正确
   - 可以点击地图添加新活动

### 场景2: 国外目的地
1. 创建一个目的地为"东京"的计划
2. 保存后查看该计划
3. **预期结果**: 
   - 显示提示消息: "高德地图仅支持中国境内地点定位,'东京' 无法在地图上显示路线。您仍可以查看和编辑行程计划。"
   - 地图不初始化
   - 不调用任何地图 API
   - 行程列表正常显示,可以编辑

### 场景3: 模糊目的地
1. 创建一个目的地为"Paris"的计划
2. 保存后查看该计划
3. **预期结果**: 
   - 高德 API 无法识别,返回 `isChina: false`
   - 显示提示消息
   - 地图不初始化

## 性能优化

1. **提前验证**: 在地图初始化之前就验证目的地,避免不必要的 API 调用
2. **优雅降级**: 国外目的地时不初始化地图,但不影响行程计划的查看和编辑
3. **用户友好**: 提供清晰的提示消息,告知用户为什么地图不可用

## 注意事项

1. **网络异常处理**: 如果验证 API 调用失败(网络异常),保守处理,假定为中国境内,避免误判
2. **一致性**: `CreatePlanView` 和 `PlanEditor` 使用相同的验证逻辑
3. **防御性编程**: 即使验证通过,在 `displayDayRoute()` 中仍然检查地图是否已初始化

## 相关代码位置

- **验证方法**: `travel-planner-frontend/src/services/amapService.ts` (第99-141行)
- **CreatePlanView 验证**: `travel-planner-frontend/src/views/CreatePlanView.vue` (第1311-1337行)
- **PlanEditor 验证**: `travel-planner-frontend/src/components/PlanEditor.vue` (第688-699行)
- **路线显示检查**: `travel-planner-frontend/src/components/PlanEditor.vue` (第520-524行)

## 修复日期

2024-01-XX (根据实际日期填写)

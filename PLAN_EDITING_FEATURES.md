# 旅行计划编辑功能升级说明

## 📋 概述
本次升级为旅行计划编辑功能添加了三大增强特性,大幅提升用户体验和交互效率。

---

## ✨ 新增功能

### 1. 智能占位符清空 (Placeholder Auto-Clear)

**功能描述:**
- 新增活动时,标题和描述字段会显示提示文本("新活动"、"请填写活动描述")
- 当用户**首次点击**输入框时,提示文本会自动清空
- 避免用户需要手动删除占位符文本

**技术实现:**
- 在 `Activity` 接口添加 `isPlaceholder?: boolean` 字段
- 使用 `@focus` 事件监听器检测首次聚焦
- 清空后自动设置 `isPlaceholder = false`

**使用场景:**
```typescript
// 用户点击"添加活动"按钮
→ 显示编辑表单,标题显示"新活动"
→ 用户点击标题输入框
→ 自动清空"新活动",光标定位,可直接输入
```

---

### 2. 地图交互式编辑 (Map Interactive Editing)

#### 2.1 右键删除活动
**功能描述:**
- 在地图上**右键点击标记点**可删除对应活动
- 显示确认对话框,防止误删
- 删除后自动更新地图和路线

**技术实现:**
- 扩展 `AmapService.addMarker()` 方法,支持 `onRightClick` 回调
- 标记携带 `customData` (day, activityIndex, activity)
- 右键触发 `ElMessageBox.confirm` 确认删除

**使用场景:**
```typescript
// 用户在地图上右键点击某个景点标记
→ 弹出确认对话框:"确定要从地图删除 '中山陵' 吗?"
→ 点击"删除"
→ 从当前天的活动列表移除该活动
→ 地图自动重新绘制路线
```

#### 2.2 点击地图添加活动
**功能描述:**
- **左键点击地图任意位置**可添加新活动到当前天
- 自动进行逆地理编码,获取地址信息
- 弹出输入框让用户命名活动

**技术实现:**
- 新增 `AmapService.onMapClick()` 监听地图点击事件
- 使用高德逆地理编码 API 获取点击位置的地址
- `ElMessageBox.prompt` 输入活动名称
- 自动添加到 `selectedDay` 对应的活动列表

**使用场景:**
```typescript
// 用户在地图上点击一个景点位置
→ 触发逆地理编码,获取地址
→ 弹出输入框:"请输入活动名称"
→ 用户输入"玄武湖"
→ 自动添加到当前选中天数的活动列表
→ 地图添加新标记并更新路线
```

---

### 3. 活动顺序调整 (Activity Reordering)

#### 3.1 按钮上移/下移
**功能描述:**
- 每个活动卡片右上角添加 ⬆️ 上移 / ⬇️ 下移 按钮
- 点击按钮即可快速调整活动顺序
- 自动禁用边界按钮(第一个活动禁用上移,最后一个禁用下移)

**技术实现:**
- 新增 `moveActivity(day, index, direction)` 函数
- 使用数组解构交换活动位置
- 调整后自动调用 `displayDayRoute()` 更新地图

**使用场景:**
```typescript
// 用户想把第3个活动移到第2个
→ 点击第3个活动的"上移"按钮
→ 活动位置交换
→ 地图标记序号自动更新(2→3, 3→2)
```

#### 3.2 拖拽排序
**功能描述:**
- 每个活动卡片左侧显示拖动手柄图标 (☰)
- 鼠标悬停时光标变为 `grab` 抓取样式
- 拖动时光标变为 `grabbing` 并显示半透明效果
- 支持在同一天内任意拖拽排序

**技术实现:**
- 使用 HTML5 原生 Drag and Drop API
- 监听 `dragstart`, `dragover`, `drop`, `dragend` 事件
- `dragState` 记录拖拽源位置
- 使用 `splice()` 重新排序数组

**使用场景:**
```typescript
// 用户想把第5个活动移到第2个位置
→ 按住第5个活动的拖动手柄
→ 拖动到第2个活动上方
→ 松开鼠标
→ 活动自动插入到第2个位置
→ 地图路线自动更新
```

---

## 🎨 UI/UX 改进

### 视觉反馈
- **拖拽手柄:** 灰色图标,悬停变深灰色
- **拖拽时:** 活动卡片透明度降低到 50%
- **悬停时:** 活动卡片背景变浅灰色,并向右平移 4px
- **操作按钮:** 默认隐藏,悬停时淡入显示(opacity: 0 → 1)

### 交互优化
- **右键菜单:** 使用 Element Plus 确认对话框,统一风格
- **地图点击:** 使用 `ElMessageBox.prompt` 输入,支持验证
- **拖拽限制:** 只允许同一天内拖拽,跨天拖拽无效

---

## 🔧 技术细节

### 核心文件修改

#### 1. `/travel-planner-frontend/src/api/aiApi.ts`
```typescript
export interface Activity {
  // ... 原有字段
  editing?: boolean
  originalData?: Activity
  isPlaceholder?: boolean  // ✨ 新增:占位符标记
}
```

#### 2. `/travel-planner-frontend/src/services/amapService.ts`
```typescript
// ✨ 新增:支持右键回调和自定义数据
addMarker(location, label?, icon?, options?: {
  onRightClick?: (marker) => void
  customData?: any
})

// ✨ 新增:地图点击监听
onMapClick(callback: (location: { lng, lat, address? }) => void)

// ✨ 新增:移除地图点击监听
offMapClick()
```

#### 3. `/travel-planner-frontend/src/views/CreatePlanView.vue`

**新增函数:**
- `moveActivity(day, index, direction)` - 按钮上移/下移
- `handleDragStart(day, index, event)` - 拖拽开始
- `handleDragOver(event)` - 拖拽经过
- `handleDrop(day, toIndex, event)` - 放置
- `handleDragEnd(event)` - 拖拽结束
- `enableMapClickToAdd()` - 启用地图点击添加
- `handlePlaceholderFocus(event, placeholderText)` - 占位符清空(已简化为内联)

**新增导入:**
```typescript
import { Rank, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
```

**新增状态:**
```typescript
const dragState = ref<{ day: number, fromIndex: number } | null>(null)
```

---

## 📊 功能对比

| 功能 | 升级前 | 升级后 |
|------|--------|--------|
| 文本输入 | 需手动删除占位符 | ✅ 首次点击自动清空 |
| 删除活动 | 只能在列表中删除 | ✅ 支持地图右键删除 |
| 添加活动 | 只能在列表底部添加 | ✅ 支持地图点击添加 |
| 调整顺序 | ❌ 不支持 | ✅ 上移/下移按钮 |
| 拖拽排序 | ❌ 不支持 | ✅ 拖拽手柄排序 |
| 地图交互 | 只能查看 | ✅ 可删除/添加活动 |

---

## 🚀 使用指南

### 编辑活动文本
1. 点击"添加活动"按钮
2. 直接点击"活动标题"或"活动描述"输入框
3. 提示文本自动清空,开始输入

### 通过地图删除活动
1. 在地图上找到要删除的活动标记
2. **右键点击**标记
3. 在确认对话框中点击"删除"

### 通过地图添加活动
1. 确保已选中要添加到的天数(通过上方天数切换按钮)
2. 在地图上**左键点击**目标位置
3. 在弹出的输入框中输入活动名称
4. 点击"添加"

### 调整活动顺序(按钮)
1. 找到要移动的活动
2. 将鼠标悬停在活动上,显示操作按钮
3. 点击 ⬆️ 或 ⬇️ 按钮调整位置

### 调整活动顺序(拖拽)
1. 找到要移动的活动
2. 按住活动左侧的拖动手柄 (☰ 图标)
3. 拖动到目标位置
4. 松开鼠标完成排序

---

## ⚠️ 注意事项

1. **拖拽限制:** 只能在同一天内拖拽,不支持跨天拖拽
2. **地图点击:** 需要在中国境内才能使用(高德地图限制)
3. **逆地理编码:** 地图点击添加的地址可能不精确,建议手动编辑
4. **操作确认:** 删除操作会弹出确认对话框,防止误删

---

## 🐛 已知问题
- 无

---

## 📝 更新日志

**2025-11-01**
- ✅ 实现占位符自动清空功能
- ✅ 实现地图右键删除活动
- ✅ 实现地图点击添加活动
- ✅ 实现按钮上移/下移功能
- ✅ 实现拖拽排序功能
- ✅ 优化拖拽视觉反馈效果
- ✅ 统一交互确认对话框样式

---

## 👨‍💻 开发者说明

### 扩展地图交互
如需添加更多地图交互功能,可以:

1. 在 `AmapService` 中添加新的事件监听方法
2. 在 `displayDayRoute()` 中配置标记的回调
3. 在 `enableMapClickToAdd()` 中扩展点击逻辑

### 自定义拖拽样式
修改 `<style scoped>` 中的以下类:
- `.activity-item` - 活动卡片样式
- `.drag-handle` - 拖动手柄样式
- `.activity-item:hover` - 悬停效果

### 调试拖拽功能
```typescript
// 在 handleDrop() 中添加
console.log('拖拽完成:', {
  fromIndex,
  toIndex,
  day,
  activities: dayPlan.activities.map(a => a.title)
})
```

---

## 📮 反馈与支持
如有问题或建议,请在项目 GitHub 提交 Issue。

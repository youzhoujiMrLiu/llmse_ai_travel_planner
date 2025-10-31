# 智能解析功能增强文档

## 📋 更新概述

本次更新改进了 `CreatePlanView` 的智能输入功能，主要包括：

1. ✨ **使用 Qwen API 进行智能解析**：替换了原有的简单正则表达式解析
2. ➕ **新增"额外要求"字段**：允许用户补充特殊需求和注意事项
3. 🎯 **提升解析准确性**：利用 AI 的自然语言理解能力，更准确地提取用户意图

---

## 🔧 后端修改

### 1. AIController.java

**新增接口：** `POST /api/ai/parse-input`

```java
@PostMapping("/parse-input")
public ResponseEntity<Map<String, Object>> parseUserInput(
        @RequestBody Map<String, String> request,
        @RequestHeader("Authorization") String authHeader) {
    jwtValidator.validateTokenAndGetUserId(authHeader);
    String userInput = request.get("userInput");
    Map<String, Object> parsedData = aiService.parseUserInput(userInput);
    return ResponseEntity.ok(parsedData);
}
```

**功能：** 接收用户的自然语言输入，调用 Qwen API 智能解析并返回结构化数据

---

### 2. AIService.java

#### 新增方法：`parseUserInput(String userInput)`

**功能：** 调用通义千问 API 智能解析用户输入

**返回格式：**
```java
{
  "destination": "目的地",
  "duration": 天数,
  "budget": 预算金额,
  "travelers": 人数,
  "preferences": ["偏好1", "偏好2"],
  "additionalRequirements": "额外要求"
}
```

#### 新增方法：`buildParsePrompt(String userInput)`

**功能：** 构建智能解析的 AI 提示词

**提示词关键内容：**
- 从用户输入中提取：目的地、天数、预算、人数
- 识别偏好标签：美食、文化、自然、购物、冒险、放松、摄影、历史
- 提取额外要求和特殊说明

#### 新增方法：`parseInputResponse(String aiResponse)`

**功能：** 解析 Qwen API 返回的 JSON 响应

#### 新增方法：`extractJSON(String aiResponse)`

**功能：** 提取并清理 AI 响应中的 JSON 字符串（移除 markdown 代码块标记）

---

### 3. GeneratePlanRequest.java

**新增字段：**
```java
private String additionalRequirements; // 额外要求
```

**Getter/Setter:**
```java
public String getAdditionalRequirements()
public void setAdditionalRequirements(String additionalRequirements)
```

---

### 4. AIService.buildPrompt()

**更新：** 在生成旅行计划的提示词中加入额外要求

```java
if (request.getAdditionalRequirements() != null && !request.getAdditionalRequirements().isEmpty()) {
    prompt.append("- 额外要求：").append(request.getAdditionalRequirements()).append("\n");
}
```

---

## 🎨 前端修改

### 1. aiApi.ts

#### 新增类型定义：

```typescript
export interface ParsedUserInput {
  destination: string
  duration: number | null
  budget: number | null
  travelers: number | null
  preferences: string[]
  additionalRequirements: string
}
```

#### 更新类型：

```typescript
export interface GeneratePlanRequest {
  // ... 原有字段
  additionalRequirements?: string  // 新增
}
```

#### 新增 API 方法：

```typescript
export const parseUserInput = async (userInput: string): Promise<ParsedUserInput> => {
  const response = await apiClient.post('/ai/parse-input', { userInput })
  return response.data
}
```

---

### 2. CreatePlanView.vue

#### 表单字段更新：

```typescript
const form = reactive<{
  // ... 原有字段
  additionalRequirements: string  // 新增
}>({
  // ... 原有初始值
  additionalRequirements: ''
})
```

#### UI 新增"额外要求"输入框：

```vue
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
```

#### 重写 `handleSmartParse()` 函数：

**旧版本：** 使用本地正则表达式解析  
**新版本：** 调用后端 `/api/ai/parse-input` 接口

```typescript
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
    if (parsedData.destination) form.destination = parsedData.destination
    if (parsedData.duration) form.duration = parsedData.duration
    if (parsedData.budget) form.budget = parsedData.budget
    if (parsedData.travelers) form.travelers = parsedData.travelers
    if (parsedData.preferences && parsedData.preferences.length > 0) {
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
```

#### 更新 `handleGeneratePlan()` 函数：

```typescript
const request: GeneratePlanRequest = {
  // ... 原有字段
  additionalRequirements: form.additionalRequirements  // 新增
}
```

#### 移除旧的本地解析函数：

```typescript
// 旧的 parseUserInput() 函数已被删除
// 现在使用 Qwen API 进行智能解析
```

#### 语音识别回调修改：

```typescript
speechRecognition.startRecognition(
  (text: string) => {
    form.userInput = text
    // 语音输入完成后，提示用户点击解析按钮
    ElMessage.success('语音识别完成，请点击"智能识别"按钮进行解析')
  },
  // ...
)
```

---

## 🚀 使用流程

### 用户体验流程：

1. **输入旅行需求**
   - 语音输入："我想去日本东京玩5天，预算1万元，喜欢美食和动漫"
   - 或文字输入

2. **点击"智能识别旅行信息"按钮**
   - 后端调用 Qwen API 解析
   - 显示提示："🤖 通义千问 AI 正在智能解析你的需求..."

3. **AI 自动填充表单**
   - 目的地：日本东京
   - 天数：5 天
   - 预算：10000 元
   - 人数：1 人
   - 偏好：美食、动漫

4. **用户检查并补充信息**
   - 调整任何字段
   - 在"额外要求"中补充："预算主要用于美食，住宿标准要高一些"

5. **生成计划**
   - AI 根据所有信息（包括额外要求）生成个性化行程

---

## 📊 技术改进对比

| 特性 | 旧版本 | 新版本 |
|------|--------|--------|
| 解析方式 | 本地正则表达式 | Qwen API 智能解析 |
| 准确性 | 有限，依赖固定模式 | 高，理解自然语言上下文 |
| 可扩展性 | 需要手动添加正则规则 | AI 自动理解新表达方式 |
| 额外要求 | ❌ 不支持 | ✅ 支持自由文本输入 |
| 用户体验 | 自动解析（可能不准确） | 用户主动触发，可预览结果 |

---

## 🔍 关键代码位置

### 后端：
- **接口定义：** `AIController.java` → `parseInput()`
- **解析逻辑：** `AIService.java` → `parseUserInput()`
- **DTO 更新：** `GeneratePlanRequest.java` → `additionalRequirements`

### 前端：
- **API 调用：** `aiApi.ts` → `parseUserInput()`
- **UI 组件：** `CreatePlanView.vue` → 额外要求输入框（第 175-183 行）
- **解析处理：** `CreatePlanView.vue` → `handleSmartParse()`（第 647-691 行）

---

## 🧪 测试建议

### 测试场景 1：基础信息解析
**输入：** "我想去北京玩3天，预算3000元，2个人"  
**预期：** 
- 目的地：北京
- 天数：3
- 预算：3000
- 人数：2

### 测试场景 2：复杂需求解析
**输入：** "想带孩子去上海迪士尼，5天4夜，预算1.5万，喜欢美食和亲子活动，住宿要舒适一些"  
**预期：**
- 目的地：上海
- 天数：5
- 预算：15000
- 偏好：美食、亲子
- 额外要求：住宿要舒适一些

### 测试场景 3：额外要求功能
1. 不填写额外要求 → 应正常生成计划
2. 填写"预算主要用于美食" → AI 应优先推荐美食相关活动
3. 填写"早上不要太早" → AI 应避免安排早起行程

---

## ⚠️ 注意事项

1. **API 调用成本：** 每次智能解析都会调用 Qwen API，请注意成本控制
2. **超时处理：** 如果 API 调用失败，前端会提示"智能解析失败，请手动填写表单"
3. **向后兼容：** `additionalRequirements` 为可选字段，不影响现有功能
4. **用户体验：** 语音输入后不再自动解析，需用户点击按钮，避免误操作

---

## 📝 待优化项

- [ ] 添加解析结果的可视化对比（原文 vs AI 理解）
- [ ] 支持多轮对话式解析（追问不明确的信息）
- [ ] 缓存常用解析结果，减少 API 调用
- [ ] 添加解析质量评分和用户反馈机制

---

## 🎉 总结

本次更新通过引入 Qwen API 智能解析和额外要求字段，显著提升了用户输入的灵活性和准确性。用户现在可以：

1. ✅ 使用更自然的语言描述需求
2. ✅ 补充详细的个性化要求
3. ✅ 获得更精准的 AI 解析结果
4. ✅ 享受更智能的行程规划体验

---

**更新日期：** 2025-01-XX  
**影响范围：** 创建计划功能  
**兼容性：** 向后兼容，无需数据迁移

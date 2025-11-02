# 语音识别功能配置指南

## 功能概述

本项目支持两种语音识别方案：

1. **科大讯飞语音识别** (推荐) - 高精度、稳定可靠
2. **Web Speech API** (备用) - 浏览器原生支持，无需配置

系统会优先尝试使用科大讯飞，如果配置缺失或初始化失败，会自动降级到Web Speech API。

---

## 方案一：科大讯飞语音识别 (推荐)

### 1. 注册科大讯飞账号

访问 [科大讯飞开放平台](https://www.xfyun.cn/)

### 2. 创建应用

1. 登录后进入控制台
2. 点击 **"创建应用"**
3. 填写应用信息：
   - 应用名称：`旅行规划助手`
   - 应用平台：选择 `WebAPI`
   - 应用类型：个人/企业

### 3. 开通语音听写服务

1. 进入应用详情
2. 找到 **"语音听写(流式版)WebAPI"**
3. 点击 **"开通"**
4. 免费版配额：
   - 每日500次调用
   - 并发1路

### 4. 获取API密钥

在应用详情页面，找到以下三个密钥：

```
APPID: xxxxxxxx
API Key: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
API Secret: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

### 5. 配置前端环境变量

复制 `.env.example` 为 `.env`：

```bash
cd travel-planner-frontend
cp .env.example .env
```

编辑 `.env` 文件，填入科大讯飞密钥：

```bash
# 科大讯飞语音识别 API
VITE_XFYUN_APPID=你的APPID
VITE_XFYUN_API_KEY=你的API_Key
VITE_XFYUN_API_SECRET=你的API_Secret
```

### 6. 重启开发服务器

```bash
npm run dev
```

---

## 方案二：Web Speech API (备用)

### 优点
- 无需注册账号
- 无需配置
- 浏览器原生支持

### 缺点
- 依赖浏览器支持（Chrome、Edge支持较好）
- 精度可能不如科大讯飞
- 网络依赖Google服务（国内可能不稳定）

### 使用方式

如果未配置科大讯飞，系统会自动使用Web Speech API，无需额外操作。

---

## 功能优化说明

### 问题1：语音识别停不下来 ✅ 已修复

**原因**：
- 之前的 `continuous = true` 导致识别持续进行
- `onresult` 事件不停触发，导致多次弹出提示

**解决方案**：
1. 将 `continuous` 改为 `false`
2. 只在最终结果时（`isFinal = true`）才显示提示
3. 点击停止按钮时，调用 `stopRecognition()` 停止识别

### 问题2：不停弹出识别完成提示 ✅ 已修复

**原因**：
- 中间结果和最终结果都触发了提示

**解决方案**：
```typescript
await speechRecognition.startRecognition(
  (text: string, isFinal: boolean) => {
    // 实时更新文本
    form.userInput = text
    
    // 只有在最终结果时才提示
    if (isFinal) {
      ElMessage.success('语音识别完成，请点击"智能识别"按钮进行解析')
    }
  },
  (error: string) => {
    ElMessage.error(error)
  }
)
```

---

## 使用流程

### 1. 开始录音

点击 **"点击开始语音输入"** 按钮：
- 按钮变为红色，显示 **"正在录音中..."**
- 开始实时识别语音

### 2. 说话

清晰地说出旅行需求，例如：
> "我想去日本，5天，预算1万元，喜欢美食和动漫，带孩子"

识别结果会实时显示在输入框中。

### 3. 停止录音

再次点击按钮（此时显示 **"正在录音中..."**）：
- 停止录音
- 弹出提示：**"语音识别完成，请点击'智能识别'按钮进行解析"**

### 4. 解析需求

点击 **"智能识别"** 按钮，AI 会解析你的需求并生成旅行计划。

---

## 技术实现

### 科大讯飞实现（`XFYunSpeechRecognition`）

```typescript
class XFYunSpeechRecognition {
  // 1. 生成WebSocket鉴权URL
  private getAuthUrl(): string {
    // 使用HMAC-SHA256签名
    // 格式: wss://iat-api.xfyun.cn/v2/iat?authorization=...
  }

  // 2. 建立WebSocket连接
  async startRecognition() {
    // 获取麦克风权限
    // 创建AudioContext处理音频
    // 建立WebSocket连接
    // 实时发送音频数据（16位PCM，16kHz）
  }

  // 3. 接收识别结果
  ws.onmessage = (event) => {
    // 解析JSON结果
    // 提取识别文本
    // 回调给UI更新
  }

  // 4. 停止识别
  stopRecognition() {
    // 发送结束标识
    // 关闭WebSocket
    // 释放资源
  }
}
```

### Web Speech API实现（`WebSpeechRecognition`）

```typescript
class WebSpeechRecognition {
  constructor() {
    this.recognition = new SpeechRecognition()
    this.recognition.lang = 'zh-CN'
    this.recognition.continuous = false  // ✅ 手动停止
    this.recognition.interimResults = true  // 显示中间结果
  }

  startRecognition(onResult, onError) {
    this.recognition.onresult = (event) => {
      // 区分中间结果和最终结果
      // 只在最终结果时触发isFinal=true
    }
  }
}
```

---

## 常见问题

### Q1: 为什么识别不了？

**A**: 检查以下几点：
1. 麦克风权限是否允许
2. 浏览器是否支持（推荐Chrome/Edge）
3. 网络连接是否正常
4. 科大讯飞配置是否正确

### Q2: 识别精度不高怎么办？

**A**: 
1. 确保使用科大讯飞（精度更高）
2. 说话清晰，语速适中
3. 减少环境噪音
4. 确保麦克风质量良好

### Q3: 提示"浏览器不支持语音识别"

**A**: 
1. 使用Chrome或Edge浏览器
2. 确保HTTPS环境（localhost除外）
3. 检查浏览器版本是否过旧

### Q4: 科大讯飞配额用完了怎么办？

**A**: 
1. 系统会自动降级到Web Speech API
2. 可以在科大讯飞控制台升级套餐
3. 或注册多个应用分散调用

---

## 开发调试

### 查看日志

打开浏览器开发者工具（F12），查看Console：

```
✅ 使用科大讯飞语音识别
✅ WebSocket连接成功
🎤 识别中: 我想去日本 (进行中)
🎤 识别中: 我想去日本五天 (进行中)
🎤 识别中: 我想去日本五天，预算一万元 (完成)
✅ 识别完成
```

### 测试切换

1. 不配置科大讯飞 → 自动使用Web Speech API
2. 配置错误的密钥 → 自动降级
3. 网络断开 → 显示错误提示

---

## 部署注意事项

### HTTPS要求

Web Speech API和麦克风权限都需要HTTPS环境（localhost除外）。

部署到生产环境时，确保：
1. 使用HTTPS协议
2. 配置有效的SSL证书
3. 环境变量正确设置

### 环境变量保护

不要将 `.env` 文件提交到Git：

```bash
# .gitignore
.env
.env.local
```

只提交 `.env.example` 作为模板。

---

## 总结

✅ **优先使用科大讯飞** - 配置简单，精度高  
✅ **自动降级** - 配置缺失时使用Web Speech API  
✅ **停止控制** - 点击停止按钮即可结束录音  
✅ **优化提示** - 只在识别完成时才显示提示  

现在你可以愉快地使用语音输入功能了！🎤

# API Key 安全迁移指南

## 🚨 安全问题说明

在前端存储 API Key 存在以下严重安全风险:

1. **完全暴露**: 任何人打开浏览器 F12 → Sources/Network 就能看到所有 API Key
2. **无法撤销**: 一旦泄露,攻击者可以永久使用(除非你更换 Key)
3. **费用风险**: 攻击者可以疯狂调用 API,产生巨额费用
4. **滥用风险**: 恶意用户可以使用你的配额进行非法操作

## ✅ 解决方案: 后端代理

所有敏感 API 调用都通过后端代理,前端只与自己的后端通信:

```
前端 → 后端 → 第三方 API (科大讯飞/高德地图)
     ↓
  只传输必要数据
  不暴露任何密钥
```

## 📋 迁移清单

### ✅ 已完成迁移

#### 1. **科大讯飞语音识别 API**

**迁移前** (前端直连,❌ 不安全):
```typescript
// 前端 speechService.ts
private readonly APPID = import.meta.env.VITE_XFYUN_APPID      // ❌ 暴露
private readonly API_KEY = import.meta.env.VITE_XFYUN_API_KEY  // ❌ 暴露
private readonly API_SECRET = import.meta.env.VITE_XFYUN_API_SECRET  // ❌ 暴露

// 直接连接科大讯飞
this.ws = new WebSocket('wss://iat-api.xfyun.cn/v2/iat?...')
```

**迁移后** (后端代理,✅ 安全):
```typescript
// 前端 speechService.ts
// 连接到后端 WebSocket 代理
this.ws = new WebSocket('ws://localhost:8080/api/speech/websocket')
// ✅ 无需任何 API Key
```

```java
// 后端 SpeechWebSocketController.java
@Value("${xfyun.app.id}")
private static String xfyunAppId;  // ✅ 安全存储在后端

// 前端连接 → 后端代理 → 科大讯飞
```

**后端配置** (`application.properties`):
```properties
xfyun.app.id=${XFYUN_APP_ID}
xfyun.api.key=${XFYUN_API_KEY}
xfyun.api.secret=${XFYUN_API_SECRET}
```

**环境变量** (`.env` 或系统环境变量):
```bash
XFYUN_APP_ID=your_app_id
XFYUN_API_KEY=your_api_key
XFYUN_API_SECRET=your_api_secret
```

#### 2. **高德地图 API Key**

**当前状态**: ✅ 已在后端,通过 `/api/map/*` 接口代理

**后端配置**:
```properties
amap.api.key=${AMAP_API_KEY}
```

**前端调用**:
```typescript
// amapService.ts - 通过后端代理调用
const response = await fetch(`http://localhost:8080/api/map/geocode?${params}`)
// ✅ 无需前端配置 API Key
```

#### 3. **通义千问 AI API**

**当前状态**: ✅ 已在后端

**后端配置**:
```properties
qwen.api.key=${QWEN_API_KEY}
qwen.api.url=https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation
```

### ⚠️ 特殊处理: Supabase

#### Supabase ANON_KEY

**说明**: 
- Supabase 的 `ANON_KEY` 是**设计为公开的**
- 安全性依赖于 **RLS (Row Level Security)** 策略
- 不需要迁移到后端

**最佳实践**:
```typescript
// supabase.ts - 可以保留在前端
const supabaseUrl = import.meta.env.VITE_SUPABASE_URL
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY  // ✅ 公开Key,安全

export const supabase = createClient(supabaseUrl, supabaseAnonKey)
```

**安全措施**:
1. ✅ 在 Supabase 控制台配置 RLS 策略
2. ✅ 限制表的访问权限(只允许已认证用户访问自己的数据)
3. ✅ 在后端验证 JWT Token

**RLS 策略示例**:
```sql
-- 只允许用户访问自己的旅行计划
CREATE POLICY "Users can only access their own travel plans"
ON travel_plans
FOR ALL
USING (auth.uid() = user_id);
```

## 📁 修改的文件

### 后端新增文件

1. **`SpeechWebSocketController.java`** - WebSocket 代理控制器
   - 接收前端连接
   - 转发到科大讯飞
   - 隐藏所有 API Key

2. **`WebSocketConfig.java`** - WebSocket 配置
   - 启用 `@ServerEndpoint` 支持

3. **`pom.xml`** - 添加依赖
   - `spring-boot-starter-websocket`
   - `tyrus-standalone-client`

### 前端修改文件

1. **`speechService.ts`** - 修改连接方式
   - ❌ 删除: `VITE_XFYUN_*` 环境变量引用
   - ❌ 删除: `getAuthUrl()` 鉴权方法
   - ❌ 删除: `CryptoJS` 依赖(不再需要)
   - ✅ 改为: 连接后端 WebSocket

2. **`.env.example`** - 更新配置模板
   - ❌ 删除: 科大讯飞配置说明
   - ✅ 保留: Supabase 配置(公开 Key)
   - ✅ 添加: 安全说明注释

## 🧪 测试步骤

### 1. 后端配置

在 `travel-planner-backend` 目录下创建 `.env` 文件或配置环境变量:

```bash
# Windows PowerShell
$env:XFYUN_APP_ID="your_app_id"
$env:XFYUN_API_KEY="your_api_key"
$env:XFYUN_API_SECRET="your_api_secret"
$env:AMAP_API_KEY="your_amap_key"
$env:QWEN_API_KEY="your_qwen_key"
$env:SUPABASE_JWT_SECRET="your_jwt_secret"
$env:SUPABASE_DB_PASSWORD="your_db_password"
```

### 2. 启动后端

```bash
cd travel-planner-backend
mvn clean install
mvn spring-boot:run
```

**验证 WebSocket 端点**:
```bash
# 检查是否成功启动
# 应该看到: Tomcat started on port(s): 8080
# WebSocket endpoint: ws://localhost:8080/api/speech/websocket
```

### 3. 前端配置

删除旧的科大讯飞配置(如果有):

```bash
# 编辑 .env.local (如果存在)
# 删除这些行:
# VITE_XFYUN_APPID=...
# VITE_XFYUN_API_KEY=...
# VITE_XFYUN_API_SECRET=...
```

### 4. 卸载不再需要的前端依赖

```bash
cd travel-planner-frontend
npm uninstall crypto-js  # 不再需要,后端处理鉴权
```

### 5. 测试语音识别功能

1. 启动前端: `npm run dev`
2. 打开 http://localhost:5173
3. 进入创建计划页面
4. 点击"语音输入"按钮
5. 观察控制台输出:
   ```
   ✅ 已连接到后端语音识别服务
   ```
6. 说话测试识别结果
7. 点击"停止录音"

### 6. 安全验证

**打开浏览器 F12**:

1. **Sources 标签页**:
   - 搜索 `XFYUN` → ❌ 应该找不到任何密钥
   - 搜索 `API_KEY` → ❌ 应该找不到科大讯飞密钥

2. **Network 标签页**:
   - 查看 WebSocket 连接
   - ✅ 应该连接到 `ws://localhost:8080/api/speech/websocket`
   - ❌ 不应该看到任何 `wss://iat-api.xfyun.cn` 连接

3. **Application 标签页**:
   - ✅ 只能看到 `VITE_SUPABASE_*` (公开 Key,安全)
   - ❌ 不应该看到 `VITE_XFYUN_*`

## 🔒 安全加固建议

### 1. 生产环境配置

**使用环境变量**:
```bash
# 不要把密钥写死在 application.properties
# 使用系统环境变量或 Docker Secrets

# Docker Compose 示例:
services:
  backend:
    environment:
      - XFYUN_APP_ID=${XFYUN_APP_ID}
      - XFYUN_API_KEY=${XFYUN_API_KEY}
      - XFYUN_API_SECRET=${XFYUN_API_SECRET}
```

### 2. 添加频率限制

```java
@Component
public class RateLimitInterceptor {
    // 限制每个用户每分钟只能调用 10 次语音识别
    private final RateLimiter rateLimiter = RateLimiter.create(10.0 / 60.0);
    
    public boolean checkRateLimit(String userId) {
        return rateLimiter.tryAcquire();
    }
}
```

### 3. 添加用户认证

```java
@ServerEndpoint("/api/speech/websocket")
public class SpeechWebSocketController {
    
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        // 验证 JWT Token
        if (!jwtService.validateToken(token)) {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized"));
            return;
        }
        
        // ... 继续处理
    }
}
```

### 4. 监控和日志

```java
@Slf4j
public class SpeechWebSocketController {
    
    @OnMessage
    public void onMessage(String message, Session session) {
        // 记录 API 调用
        log.info("User {} called speech recognition API", session.getId());
        
        // 可以接入监控系统(Prometheus, Grafana等)
    }
}
```

## 📊 迁移前后对比

| 项目 | 迁移前 | 迁移后 |
|------|--------|--------|
| 科大讯飞 API Key | ❌ 前端暴露 | ✅ 后端隐藏 |
| 高德地图 API Key | ✅ 后端代理 | ✅ 后端代理 |
| 通义千问 API Key | ✅ 后端 | ✅ 后端 |
| Supabase ANON_KEY | ✅ 前端(公开Key) | ✅ 前端(公开Key) |
| WebSocket 连接 | ❌ 直连科大讯飞 | ✅ 通过后端代理 |
| 前端依赖 | crypto-js (HMAC) | ❌ 无需加密库 |
| 安全性 | ❌ 低 | ✅ 高 |

## 🎯 总结

1. ✅ **科大讯飞 API** - 完全迁移到后端,前端无法访问密钥
2. ✅ **高德地图 API** - 已在后端代理
3. ✅ **通义千问 API** - 已在后端
4. ✅ **Supabase** - 使用公开 Key + RLS 策略,安全

**迁移后的优势**:
- 🔒 API Key 完全隐藏,无法通过 F12 获取
- 🚀 可以在后端添加频率限制、用户认证等安全措施
- 📊 统一监控和日志记录
- 💰 防止 API 配额被滥用,避免巨额费用

## 📚 相关文档

- [科大讯飞语音听写 WebAPI 文档](https://www.xfyun.cn/doc/asr/voicedictation/API.html)
- [Spring Boot WebSocket 文档](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- [Supabase Row Level Security](https://supabase.com/docs/guides/auth/row-level-security)

## 修复日期

2025-01-02

# 前端环境变量清理说明

## 📋 清理概述

本次清理优化了前端 `.env` 配置,明确了哪些配置需要保留、哪些可以删除,并提升了配置的安全性和可维护性。

---

## ✅ 保留的配置（必需）

### 1. **Supabase 认证配置**
```bash
VITE_SUPABASE_URL=https://szkvgppxywdatqqtcdbi.supabase.co
VITE_SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**保留原因:**
- 前端需要直接连接 Supabase 进行用户认证和登录
- `ANON_KEY` 是公开密钥,设计上就是暴露给前端的
- 配合 Supabase RLS (Row Level Security) 确保数据安全

**使用位置:**
- `src/lib/supabase.ts` - 初始化 Supabase 客户端
- `src/api/apiClient.ts` - 获取 JWT token

---

### 2. **高德地图 JS API Key**
```bash
VITE_AMAP_WEB_KEY=45492f44d1f375ac2dcaf78b7492d034
```

**保留原因:**
- 前端需要使用高德地图 JS SDK 进行地图可视化
- 用于地图显示、标记绘制、路线绘制等前端可视化功能

**安全性说明:**
- ✅ 该 Key 在高德控制台配置了域名白名单,只能从指定域名使用
- ✅ 敏感的地理编码、搜索等 Web 服务 API 已迁移到后端代理
- ✅ JS API Key 相对安全,不会造成额外费用或数据泄露风险

**使用位置:**
- `index.html` - 加载高德地图 JS SDK
- `src/services/amapService.ts` - 地图可视化服务

**改进:**
- ✨ 从 `index.html` 中的硬编码改为从环境变量读取
- ✨ 重命名为 `VITE_AMAP_WEB_KEY` 明确用途(区别于后端的 Web 服务 API Key)

---

## ❌ 删除的配置

### 1. **`VITE_AMAP_API_KEY`**
- **删除原因:** 改为 `VITE_AMAP_WEB_KEY`,名称更清晰
- **状态:** 已更新

### 2. **`VITE_API_BASE_URL`**
- **删除原因:** 代码中未使用,`apiClient.ts` 硬编码为 `/api`
- **状态:** 已删除

### 3. **科大讯飞 API 配置**
- **删除原因:** 已迁移到后端 WebSocket 代理
- **相关配置:** `VITE_XFYUN_APP_ID`, `VITE_XFYUN_API_KEY`, `VITE_XFYUN_API_SECRET`
- **状态:** 已在之前的迁移中删除

---

## 🔧 技术改进

### 1. **Vite 配置增强** (`vite.config.ts`)

添加了 HTML 环境变量替换功能:

```typescript
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  
  return {
    plugins: [
      vue(),
      vueDevTools(),
      // 自定义插件：在 HTML 中替换环境变量
      {
        name: 'html-transform',
        transformIndexHtml(html) {
          return html
            .replace(/%VITE_AMAP_WEB_KEY%/g, env.VITE_AMAP_WEB_KEY || '')
            .replace(/%VITE_AMAP_SECURITY_CODE%/g, env.VITE_AMAP_SECURITY_CODE || '')
        },
      },
    ],
    // ...
  }
})
```

**作用:**
- 在构建时将 `index.html` 中的 `%VITE_AMAP_WEB_KEY%` 替换为实际值
- 避免硬编码 API Key
- 支持不同环境使用不同的 Key

---

### 2. **index.html 优化**

**优化前:**
```html
<script src="https://webapi.amap.com/maps?v=2.0&key=45492f44d1f375ac2dcaf78b7492d034&plugin=..."></script>
```

**优化后:**
```html
<!-- 注意: 以下 key 参数会在构建时被 Vite 替换为环境变量 VITE_AMAP_WEB_KEY 的值 -->
<script src="https://webapi.amap.com/maps?v=2.0&key=%VITE_AMAP_WEB_KEY%&plugin=..."></script>
```

**改进点:**
- ✅ 不再硬编码 API Key
- ✅ 从环境变量读取,更灵活
- ✅ 添加了清晰的注释说明

---

## 📁 文件清单

### 修改的文件:
1. ✅ `travel-planner-frontend/.env` - 清理并重组配置
2. ✅ `travel-planner-frontend/.env.example` - 更新配置模板
3. ✅ `travel-planner-frontend/index.html` - 使用环境变量替换硬编码
4. ✅ `travel-planner-frontend/vite.config.ts` - 添加 HTML 环境变量替换

---

## 🎯 最佳实践总结

### ✅ 前端可以暴露的配置:
1. **Supabase ANON_KEY** - 设计上就是公开的
2. **高德地图 JS API Key** - 有域名白名单保护,用于前端可视化

### ❌ 前端不应暴露的配置:
1. **API Secret/私钥** - 任何签名用的密钥
2. **Web 服务 API Key** - 可能产生费用的 API 调用
3. **数据库密码** - 后端数据库凭证
4. **JWT Secret** - 用于签名 Token 的密钥

### 🔒 安全原则:
- **前端只暴露必需的配置**
- **敏感操作通过后端代理**
- **使用域名白名单、CORS 等额外保护**
- **定期轮换 API Key**

---

## 🚀 验证

### 启动测试:
```bash
cd travel-planner-frontend
npm run dev
```

### 预期结果:
- ✅ 前端成功启动在 http://localhost:5173
- ✅ 高德地图正常加载和显示
- ✅ Supabase 认证功能正常
- ✅ 无 API Key 相关的控制台错误

---

## 📝 后续建议

### 可选优化:
1. **启用高德地图安全密钥** (`VITE_AMAP_SECURITY_CODE`)
   - 在高德控制台启用 JS API 安全密钥
   - 提供额外的安全保护层

2. **配置环境特定的 Key**
   - 开发环境使用 `.env.local`
   - 生产环境使用 CI/CD 环境变量
   - 测试环境使用单独的 Key

3. **监控 API 使用量**
   - 在高德控制台定期检查 API 调用量
   - 设置用量告警,防止异常消耗

---

## 📚 相关文档

- [API_KEY_SECURITY_MIGRATION.md](./API_KEY_SECURITY_MIGRATION.md) - 后端 API 安全迁移文档
- [ENVIRONMENT_SETUP.md](./ENVIRONMENT_SETUP.md) - 环境配置说明
- [AMAP_INTEGRATION.md](./AMAP_INTEGRATION.md) - 高德地图集成说明

---

**更新时间:** 2025年11月2日  
**更新内容:** 清理前端环境变量,优化配置安全性和可维护性
 
 ---
 
 ## 🖥️ UI 变更 - 移除计划日期与状态展示

 为了简化首页卡片展示，已移除每个已创建旅行计划卡片上的以下字段：

- 计划状态标签（例如“规划中 / 进行中 / 已完成”）
- 计划显示日期（开始日期 ~ 结束日期）

实现位置：`src/views/HomeView.vue`。

验证：已启动前端开发服务器（见运行地址），并确认无编译错误；需要在浏览器中检查列表页以确认 UI 符合预期。

如需恢复或使用其他位置显示状态/日期（例如详情页），可将字段保留在 `EditPlanView.vue` 或 `PlanDetail` 页面。

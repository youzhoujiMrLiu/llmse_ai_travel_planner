# 🚀 快速启动指南

## 前提条件

- ✅ Java 17+
- ✅ Node.js 20+
- ✅ Maven
- ✅ 已在 Supabase 创建数据库表（见 `database/README.md`）

---

## 步骤 1: 设置环境变量

### Windows PowerShell

```powershell
# 设置环境变量（必需）
$env:SUPABASE_DB_PASSWORD="你的Supabase数据库密码"
$env:SUPABASE_JWT_SECRET="你的Supabase_JWT_Secret"
$env:QWEN_API_KEY="你的通义千问API_Key"

# 可选（用于地图和高级语音识别）
$env:AMAP_API_KEY="你的高德地图Key"
```

### Linux/Mac Bash

```bash
# 设置环境变量（必需）
export SUPABASE_DB_PASSWORD="你的Supabase数据库密码"
export SUPABASE_JWT_SECRET="你的Supabase_JWT_Secret"
export QWEN_API_KEY="你的通义千问API_Key"

# 可选
export AMAP_API_KEY="你的高德地图Key"
```

---

## 步骤 2: 启动后端

```bash
cd travel-planner-backend

# 清理并安装依赖
./mvnw clean install

# 启动后端服务
./mvnw spring-boot:run
```

**验证**: 浏览器访问 `http://localhost:8080/api/health`，应该返回 `OK`

---

## 步骤 3: 启动前端

新开一个终端：

```bash
cd travel-planner-frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

**验证**: 浏览器自动打开 `http://localhost:5173`

---

## 步骤 4: 测试功能

### 1. **注册/登录**
- 访问 `http://localhost:5173/login`
- 注册新账号或使用已有账号登录

### 2. **查看计划列表**
- 登录后自动跳转到主页
- 查看旅行计划列表（初次使用为空）

### 3. **创建新计划**
- 点击"创建新计划"按钮
- 尝试语音输入：
  - 点击"点击开始语音输入"
  - 说："我想去日本东京，5天，预算1万元，喜欢美食和动漫"
  - 观察表单自动填充
- 或手动填写表单
- 点击"让 AI 帮我生成计划"
- 等待 AI 生成行程（3-10秒）
- 查看生成的行程详情
- 点击"保存计划"

### 4. **查看已保存的计划**
- 返回主页
- 看到刚创建的计划卡片
- 点击卡片查看详情（待实现）

---

## 🔍 故障排查

### 问题 1: 后端启动失败

**错误**: `Supabase JWT secret is not configured`

**解决方案**: 检查环境变量是否设置正确
```powershell
# 验证环境变量
echo $env:SUPABASE_JWT_SECRET
```

### 问题 2: 无法连接数据库

**错误**: `Connection refused`

**解决方案**: 
1. 检查 Supabase 数据库密码
2. 确认已在 Supabase 创建数据库表
3. 检查网络连接

### 问题 3: AI 生成失败

**错误**: `调用 AI API 失败`

**解决方案**:
1. 检查通义千问 API Key 是否正确
2. 确认有足够的 API 配额
3. 查看后端控制台日志

### 问题 4: 语音识别不工作

**解决方案**:
1. 使用 Chrome 浏览器
2. 确保在 HTTPS 或 localhost 环境
3. 允许浏览器麦克风权限
4. 检查麦克风是否正常工作

### 问题 5: 登录失败

**错误**: `Network Error`

**解决方案**:
1. 确认后端已启动（`http://localhost:8080/api/health`）
2. 检查前端 API 配置（`apiClient.ts`）
3. 查看浏览器控制台错误

---

## 📚 相关文档

- **数据库设置**: `database/README.md`
- **环境变量配置**: `ENVIRONMENT_SETUP.md`
- **创建计划功能**: `CREATE_PLAN_FEATURE.md`
- **后端实现**: `BACKEND_IMPLEMENTATION_SUMMARY.md`

---

## 🎯 下一步

- [ ] 实现计划详情页面
- [ ] 集成高德地图展示
- [ ] 添加费用管理功能
- [ ] 实现行程编辑功能
- [ ] 添加图片和评论

---

## 💡 提示

### 通义千问 API 额度

- 免费版每月有一定额度
- 开发测试足够使用
- 生产环境建议付费

### 语音识别选择

- **Web Speech API**: 免费，Chrome 支持好，推荐开发使用
- **科大讯飞**: 更精准，需要配置，适合生产环境

### 数据库同步

- 使用 Supabase 的实时同步功能
- 数据自动备份
- 支持多设备访问

---

**祝你使用愉快！** 🎉

如有问题，请查看相关文档或查看代码注释。

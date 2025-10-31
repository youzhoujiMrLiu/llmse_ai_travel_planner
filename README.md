# 🌍 AI Travel Planner (智能旅行规划助手)

基于 AI 的智能旅行规划应用，提供语音输入、地图可视化、智能行程生成等功能。

## ✨ 主要特性

- 🤖 **AI 智能生成**: 使用阿里云通义千问 (qwen-plus) 生成个性化旅行计划
- 🎤 **语音输入**: 支持浏览器原生语音识别，快速输入旅行需求
- 🗺️ **地图可视化**: 集成高德地图，自动绘制旅行路线和景点标记
- 💰 **预算管理**: 智能分配预算，跟踪旅行花费
- 📱 **响应式设计**: 支持桌面和移动设备
- 🔐 **用户认证**: 基于 Supabase Auth 的安全认证系统

## 🚀 快速开始 (5 分钟)

### 前置要求

- **后端**: Java 17+, Maven 3.6+
- **前端**: Node.js 18+, npm/yarn/pnpm
- **数据库**: PostgreSQL (通过 Supabase)

### 1. 克隆项目

```bash
git clone <repository-url>
cd llmse_ai_travel_planner
```

### 2. 配置环境变量

```bash
# 后端环境变量
cd travel-planner-backend
cp .env.example .env

# 编辑 .env 文件，填入真实的 API Keys
# Windows: notepad .env
# Linux/Mac: nano .env

# 验证配置 (可选)
# Windows: .\validate-env.ps1
# Linux/Mac: ./validate-env.sh
```

**需要的 API Keys**:
- Supabase 数据库密码和 JWT Secret
- 阿里云通义千问 API Key
- 高德地图 API Key
- (可选) 科大讯飞语音 API

详细获取步骤: [ENVIRONMENT_SETUP.md](./ENVIRONMENT_SETUP.md)

### 3. 启动后端

```bash
# 在 travel-planner-backend 目录
./mvnw spring-boot:run   # Linux/Mac
.\mvnw.cmd spring-boot:run  # Windows
```

访问: http://localhost:8080/api/health

### 4. 启动前端

```bash
cd travel-planner-frontend
npm install
npm run dev
```

访问: http://localhost:5173

## 📁 项目结构

```
llmse_ai_travel_planner/
├── travel-planner-backend/     # Spring Boot 后端
│   ├── src/
│   │   └── main/java/com/shingeki/travelplannerbackend/
│   │       ├── controller/     # REST API 控制器
│   │       ├── service/        # 业务逻辑层
│   │       ├── repository/     # 数据访问层
│   │       ├── entity/         # JPA 实体
│   │       ├── dto/            # 数据传输对象
│   │       └── security/       # 安全配置 (.env 加载器)
│   ├── .env.example            # 环境变量模板
│   ├── validate-env.sh         # 配置验证脚本 (Linux/Mac)
│   ├── validate-env.ps1        # 配置验证脚本 (Windows)
│   ├── README.md               # 后端文档
│   └── MIGRATION_GUIDE.md      # IDEA 迁移指南
│
├── travel-planner-frontend/    # Vue 3 前端
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   ├── api/                # API 客户端
│   │   ├── services/           # 业务服务 (地图、语音、认证)
│   │   ├── router/             # 路由配置
│   │   └── stores/             # Pinia 状态管理
│   ├── public/
│   └── package.json
│
├── database/                   # 数据库迁移脚本
│   └── migrations/
│       └── 001_create_travel_plans_tables.sql
│
├── ENVIRONMENT_SETUP.md        # 🔧 环境变量完整配置指南
├── ENV_SYSTEM_SUMMARY.md       # .env 系统实现总结
├── QUICK_START.md              # 快速上手指南
├── BACKEND_IMPLEMENTATION_SUMMARY.md  # 后端实现总结
└── README.md                   # 本文件
```

## 📚 完整文档

- **[环境变量配置](./ENVIRONMENT_SETUP.md)** - 必读！完整的环境变量配置指南
- **[快速开始](./QUICK_START.md)** - 项目整体快速上手
- **[后端 README](./travel-planner-backend/README.md)** - 后端 API 文档和开发指南
- **[IDEA 迁移指南](./travel-planner-backend/MIGRATION_GUIDE.md)** - 从 IDEA 运行配置迁移到 .env
- **[后端实现总结](./BACKEND_IMPLEMENTATION_SUMMARY.md)** - 后端架构和实现细节
- **[数据库设计](./database/README.md)** - 数据库表结构和迁移

## 🔧 技术栈

### 后端
- **框架**: Spring Boot 3.5.7
- **语言**: Java 17
- **数据库**: PostgreSQL (Supabase)
- **认证**: Supabase Auth + JWT
- **AI**: 阿里云通义千问 (qwen-plus)
- **构建**: Maven

### 前端
- **框架**: Vue 3.5.22
- **语言**: TypeScript 5.6.3
- **UI 库**: Element Plus 2.11.5
- **地图**: 高德地图 JS API 2.0
- **语音**: Web Speech API
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **构建**: Vite 6

### 第三方服务
- **数据库**: [Supabase](https://supabase.com/)
- **AI**: [阿里云百炼 (通义千问)](https://dashscope.aliyun.com/)
- **地图**: [高德地图开放平台](https://lbs.amap.com/)
- **语音** (备选): [科大讯飞](https://www.xfyun.cn/)

## 🔐 环境变量配置

项目使用 **自动加载的 .env 文件系统**，支持跨平台 (Windows/Linux/macOS)。

### 核心特性

✅ **零配置**: 复制 `.env.example` → 填入真实值 → 自动加载  
✅ **跨平台**: Windows、Linux、macOS 完全兼容  
✅ **安全**: `.env` 已在 `.gitignore` 中排除  
✅ **易维护**: 团队协作简单，新成员快速上手  
✅ **验证工具**: 提供配置验证脚本

### 快速配置

```bash
# 1. 复制模板
cd travel-planner-backend
cp .env.example .env

# 2. 编辑文件 (填入真实 API Keys)
notepad .env  # Windows
nano .env     # Linux/Mac

# 3. 验证配置 (可选)
.\validate-env.ps1   # Windows PowerShell
./validate-env.sh    # Linux/Mac

# 4. 启动应用 (自动加载 .env)
./mvnw spring-boot:run
```

详细配置指南: [ENVIRONMENT_SETUP.md](./ENVIRONMENT_SETUP.md)

## 🎯 主要功能

### 1. 创建旅行计划
- 智能模式: 语音输入完整需求，AI 自动解析
- 手动模式: 表单填写目的地、天数、预算等
- 地图选点: 直接在地图上选择目的地

### 2. AI 智能生成
- 基于用户需求生成详细行程
- 智能分配每日活动
- 自动计算预算分配
- 推荐景点、餐厅、住宿

### 3. 地图可视化
- 自动标记景点位置
- 绘制每日旅行路线
- 支持切换查看不同天数的路线
- 地理编码和路径规划

### 4. 计划管理
- 查看所有旅行计划
- 编辑和删除计划
- 保存到个人账户

## 🐛 故障排查

### 后端启动失败

**问题**: 提示 "Supabase JWT secret is not configured"

**解决方案**:
1. 确认 `.env` 文件存在: `travel-planner-backend/.env`
2. 运行验证脚本: `.\validate-env.ps1` (Windows) 或 `./validate-env.sh` (Linux)
3. 检查环境变量格式: `KEY=value` (等号两边无空格)
4. 确认没有使用示例值 (包含 `your_` 或 `_here`)

详细排查: [ENVIRONMENT_SETUP.md#常见问题排查](./ENVIRONMENT_SETUP.md#常见问题排查)

### 前端地图不显示

**问题**: 高德地图无法加载

**解决方案**:
1. 检查 `index.html` 中的高德地图 API Key
2. 确认浏览器控制台没有 CORS 错误
3. 检查高德开放平台的 Key 配额和域名白名单

### AI 生成失败

**问题**: 调用通义千问 API 失败

**解决方案**:
1. 检查 `.env` 中的 `QWEN_API_KEY` 是否正确
2. 确认 API Key 有足够的配额
3. 检查网络连接和防火墙设置

## 🧪 测试

```bash
# 后端测试
cd travel-planner-backend
./mvnw test

# 前端测试 (如已配置)
cd travel-planner-frontend
npm run test
```

## 📦 构建部署

### 后端

```bash
cd travel-planner-backend
./mvnw clean package
java -jar target/travel-planner-backend-0.0.1-SNAPSHOT.jar
```

### 前端

```bash
cd travel-planner-frontend
npm run build
# 输出到 dist/ 目录
```

### Docker 部署

```bash
# 后端
cd travel-planner-backend
docker build -t travel-planner-backend .
docker run -d --env-file .env -p 8080:8080 travel-planner-backend

# 前端
cd travel-planner-frontend
docker build -t travel-planner-frontend .
docker run -d -p 80:80 travel-planner-frontend
```

详细部署指南: [ENVIRONMENT_SETUP.md#docker-部署环境变量](./ENVIRONMENT_SETUP.md#docker-部署环境变量)

## 🤝 贡献

欢迎贡献代码！

**注意事项**:
- ❌ 请勿提交 `.env` 文件
- ✅ 提交前运行测试
- ✅ 遵循现有代码风格
- ✅ 更新相关文档

## 📄 许可证

本项目采用 MIT 许可证。

## 📧 联系方式

- **Issues**: [GitHub Issues](https://github.com/your-repo/issues)
- **文档**: 见各目录下的 README.md 和 Markdown 文档

---

**快速链接**:
- 📖 [环境变量配置](./ENVIRONMENT_SETUP.md) ← 必读！
- 🚀 [快速开始](./QUICK_START.md)
- 🔧 [后端文档](./travel-planner-backend/README.md)
- 🔄 [IDEA 迁移指南](./travel-planner-backend/MIGRATION_GUIDE.md)

# Travel Planner Backend

Spring Boot 后端服务，提供旅行计划的 AI 生成、管理和地图集成功能。

## 🚀 快速开始

### 前置要求

- Java 17 或更高版本
- Maven 3.6+ (或使用项目自带的 `mvnw`)
- PostgreSQL 数据库 (通过 Supabase)

### 环境变量配置 (3 分钟)

1. **复制环境变量模板**
   ```bash
   cp .env.example .env
   ```

2. **编辑 .env 文件，填入真实的 API Keys**
   ```bash
   # Windows
   notepad .env
   
   # Linux/Mac
   nano .env
   ```

3. **验证配置 (可选但推荐)**
   ```bash
   # Windows PowerShell
   .\validate-env.ps1
   
   # Linux/Mac
   chmod +x validate-env.sh
   ./validate-env.sh
   ```
   
   验证脚本会检查:
   - ✅ .env 文件是否存在
   - ✅ 必需的环境变量是否已设置
   - ✅ 是否使用了示例值
   - ✅ 文件编码和权限 (Linux/Mac)

4. **启动应用**
   ```bash
   # Windows
   .\mvnw.cmd spring-boot:run
   
   # Linux/Mac
   ./mvnw spring-boot:run
   ```

4. **验证启动成功**
   
   访问健康检查接口:
   ```bash
   curl http://localhost:8080/api/health
   ```
   
   预期响应:
   ```json
   {"status":"OK","timestamp":"2024-01-01T00:00:00Z"}
   ```

**就这么简单！** 应用会在启动时自动加载 `.env` 文件中的环境变量。

## 📝 所需的 API Keys

本项目需要以下 API Keys (在 `.env` 文件中配置):

| API Key | 必需 | 用途 | 获取方式 |
|---------|------|------|---------|
| `SUPABASE_DB_PASSWORD` | ✅ | 数据库访问 | [Supabase Dashboard](https://app.supabase.com/) → Settings → Database |
| `SUPABASE_JWT_SECRET` | ✅ | 用户认证 | Supabase Dashboard → Settings → API → JWT Secret |
| `QWEN_API_KEY` | ✅ | AI 旅行计划生成 | [阿里云百炼](https://dashscope.aliyun.com/) → API-KEY 管理 |
| `AMAP_API_KEY` | ✅ | 地图和路线规划 | [高德开放平台](https://lbs.amap.com/) → 应用管理 → Web 端 Key |
| `XFYUN_APP_ID` | ⭕ | 语音识别 (备用) | [讯飞开放平台](https://www.xfyun.cn/) → 控制台 |
| `XFYUN_API_KEY` | ⭕ | 语音识别 (备用) | 同上 |
| `XFYUN_API_SECRET` | ⭕ | 语音识别 (备用) | 同上 |

**注**: 
- ✅ 必需: 应用启动必须配置
- ⭕ 可选: 当前使用浏览器 Web Speech API，科大讯飞为备选方案

详细的 API Key 获取步骤请参考: [ENVIRONMENT_SETUP.md](../ENVIRONMENT_SETUP.md)

## 🔧 开发

### 在 IDEA 中运行

1. 配置 `.env` 文件 (见上方)
2. 打开项目
3. 运行 `TravelPlannerBackendApplication`

无需在 IDEA 的 Run Configuration 中设置环境变量，应用会自动加载 `.env` 文件。

**从 IDEA 运行配置迁移？** 参考: [MIGRATION_GUIDE.md](./MIGRATION_GUIDE.md)

### 命令行运行

```bash
# 开发模式 (热重载)
./mvnw spring-boot:run

# 构建 JAR
./mvnw clean package

# 运行 JAR
java -jar target/travel-planner-backend-0.0.1-SNAPSHOT.jar
```

### Docker 运行

```bash
# 构建镜像
docker build -t travel-planner-backend .

# 使用 .env 文件运行
docker run -d \
  --env-file .env \
  -p 8080:8080 \
  travel-planner-backend
```

或使用 docker-compose:

```yaml
# docker-compose.yml
version: '3.8'
services:
  backend:
    build: .
    env_file: .env
    ports:
      - "8080:8080"
```

## 📚 API 文档

### 健康检查
```http
GET /api/health
```

### 用户配置
```http
GET /api/user-profile
POST /api/user-profile
```

### AI 生成旅行计划
```http
POST /api/ai/generate
Content-Type: application/json

{
  "requirements": "去北京旅游3天，预算5000元"
}
```

### 旅行计划管理
```http
GET /api/travel-plans          # 获取所有计划
GET /api/travel-plans/{id}     # 获取单个计划
POST /api/travel-plans         # 创建计划
PUT /api/travel-plans/{id}     # 更新计划
DELETE /api/travel-plans/{id}  # 删除计划
```

完整 API 文档: `http://localhost:8080/swagger-ui.html` (如已启用)

## 🗂️ 项目结构

```
src/main/java/com/shingeki/travelplannerbackend/
├── controller/          # REST API 控制器
│   ├── AIController.java
│   ├── TravelPlanController.java
│   └── UserProfileController.java
├── service/            # 业务逻辑层
│   ├── AIService.java
│   ├── TravelPlanService.java
│   └── UserProfileService.java
├── repository/         # 数据访问层
│   └── TravelPlanRepository.java
├── entity/            # JPA 实体
│   └── TravelPlan.java
├── dto/               # 数据传输对象
│   ├── GeneratePlanRequest.java
│   └── TravelPlanDTO.java
├── security/          # 安全配置
│   ├── DotEnvLoader.java       # .env 文件加载器
│   ├── JwksPublicKeyProvider.java
│   └── SupabaseJwtValidator.java
└── TravelPlannerBackendApplication.java
```

## 🔐 安全性

### 环境变量管理

- ✅ `.env` 文件已在 `.gitignore` 中排除
- ✅ `.env.example` 模板可安全提交到 Git
- ✅ 自动跳过示例值 (包含 `your_` 或 `_here`)
- ✅ 支持跨平台 (Windows/Linux/macOS)

### 认证机制

- 使用 Supabase JWT 进行用户认证
- JWKS 公钥验证
- 自定义 `SupabaseJwtValidator`

### 数据库安全

- PostgreSQL Row Level Security (RLS)
- 用户数据隔离
- 预编译语句防止 SQL 注入

## 🧪 测试

```bash
# 运行所有测试
./mvnw test

# 运行特定测试
./mvnw test -Dtest=TravelPlanServiceTest

# 生成测试覆盖率报告
./mvnw test jacoco:report
```

## 📦 构建部署

### 本地构建

```bash
# 清理并构建
./mvnw clean package

# 跳过测试构建
./mvnw clean package -DskipTests

# 生成的 JAR 位置
target/travel-planner-backend-0.0.1-SNAPSHOT.jar
```

### 生产环境部署

1. **云平台环境变量配置**
   
   不同云平台有不同的环境变量配置方式，详见: [ENVIRONMENT_SETUP.md#生产环境最佳实践](../ENVIRONMENT_SETUP.md#生产环境最佳实践)

2. **Docker 部署**
   
   使用 `env_file` 或 `environment` 配置环境变量

3. **Kubernetes 部署**
   
   使用 ConfigMap 或 Secret 管理环境变量

详细部署指南: [ENVIRONMENT_SETUP.md](../ENVIRONMENT_SETUP.md)

## 🐛 故障排查

### 问题: 应用启动失败，提示配置缺失

**症状**: 
```
Supabase JWT secret is not configured
```

**解决方案**:
1. 确认 `.env` 文件存在于项目根目录
2. 检查 `.env` 文件格式: `KEY=value` (等号两边无空格)
3. 确认没有使用示例值 (包含 `your_` 或 `_here`)
4. 查看启动日志，确认 .env 文件已加载:
   ```
   [DotEnvLoader] Successfully loaded X environment variables from .env file
   ```

### 问题: 找不到 .env 文件

**症状**:
```
[DotEnvLoader] No .env file found, using system environment variables
```

**解决方案**:
- Windows: 确保文件名是 `.env` 而不是 `.env.txt`
- 文件位置: `travel-planner-backend/.env` (与 `pom.xml` 同级)
- 使用命令查看: `dir .env` (Windows) 或 `ls -la .env` (Linux)

### 问题: API Key 配额不足

**症状**:
```
QwenApiException: Quota exceeded
```

**解决方案**:
1. 检查 API Key 的剩余额度
2. 升级到付费计划
3. 使用备用 API Key

更多问题排查: [ENVIRONMENT_SETUP.md#常见问题排查](../ENVIRONMENT_SETUP.md#常见问题排查)

## 📖 文档

- [环境变量配置](../ENVIRONMENT_SETUP.md) - 完整的环境变量配置指南
- [迁移指南](./MIGRATION_GUIDE.md) - 从 IDEA 运行配置迁移到 .env
- [快速开始](../QUICK_START.md) - 项目整体快速开始指南
- [后端实现总结](../BACKEND_IMPLEMENTATION_SUMMARY.md) - 后端架构和实现细节
- [数据库设计](../database/README.md) - 数据库表结构和迁移

## 🤝 贡献

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支: `git checkout -b feature/amazing-feature`
3. 提交更改: `git commit -m 'Add amazing feature'`
4. 推送到分支: `git push origin feature/amazing-feature`
5. 提交 Pull Request

**注意**: 
- 请勿提交 `.env` 文件到 Git
- 确保所有测试通过
- 遵循现有的代码风格

## 📄 许可证

本项目采用 MIT 许可证。

## 💡 技术栈

- **框架**: Spring Boot 3.5.7
- **Java**: 17
- **数据库**: PostgreSQL (Supabase)
- **认证**: Supabase Auth + JWT
- **AI**: 阿里云通义千问 (qwen-plus)
- **地图**: 高德地图 API
- **构建**: Maven 3.6+

## 🔗 相关资源

- [Spring Boot 文档](https://spring.io/projects/spring-boot)
- [Supabase 文档](https://supabase.com/docs)
- [通义千问 API 文档](https://help.aliyun.com/zh/dashscope/)
- [高德地图 API 文档](https://lbs.amap.com/api/javascript-api/summary)

---

**需要帮助？** 请查看 [ENVIRONMENT_SETUP.md](../ENVIRONMENT_SETUP.md) 或提交 [Issue](https://github.com/your-repo/issues)。

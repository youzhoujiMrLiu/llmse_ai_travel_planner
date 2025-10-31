# 环境变量配置说明

## 🚀 快速开始 (5 分钟上手)

```bash
# 1. 进入后端目录
cd travel-planner-backend

# 2. 复制环境变量模板
cp .env.example .env

# 3. 编辑 .env 文件，填入真实的 API Keys
# Windows: notepad .env
# Linux/Mac: nano .env

# 4. 启动应用（会自动加载 .env 文件）
./mvnw spring-boot:run

# 5. 验证启动成功
# 看到: [DotEnvLoader] Successfully loaded X environment variables from .env file
```

✅ **就这么简单！** 无需手动设置环境变量，无需配置 IDEA，跨平台自动加载。

---

## 后端环境变量

在启动后端服务之前，需要配置以下环境变量：

### 必需的环境变量

```bash
# Supabase 数据库配置
SUPABASE_DB_PASSWORD=你的Supabase数据库密码

# Supabase JWT 密钥
SUPABASE_JWT_SECRET=你的Supabase_JWT_Secret

# 通义千问 API
QWEN_API_KEY=你的通义千问API_Key

# 高德地图 API（前端使用）
AMAP_API_KEY=你的高德地图API_Key

# 科大讯飞语音识别（可选，前端使用 Web Speech API）
XFYUN_APP_ID=你的科大讯飞APP_ID
XFYUN_API_KEY=你的科大讯飞API_Key
XFYUN_API_SECRET=你的科大讯飞API_Secret
```

## 如何获取 API Keys

### 1. Supabase

1. 登录 [Supabase Dashboard](https://app.supabase.com/)
2. 选择你的项目
3. 进入 **Settings** → **Database**
4. 复制数据库密码
5. 进入 **Settings** → **API**
6. 复制 `JWT Secret`

### 2. 通义千问（阿里云百炼）

1. 访问 [阿里云百炼](https://dashscope.aliyun.com/)
2. 注册/登录账号
3. 进入 **API-KEY 管理**
4. 创建 API Key
5. 复制 API Key

**注意**: 通义千问有免费额度，适合开发测试使用。

### 3. 高德地图

1. 访问 [高德开放平台](https://lbs.amap.com/)
2. 注册/登录账号
3. 进入 **控制台** → **应用管理** → **我的应用**
4. 创建新应用
5. 添加 **Web端(JS API)** Key
6. 复制 Key

**注意**: 需要配置域名白名单（开发时使用 `localhost`）

### 4. 科大讯飞（可选）

1. 访问 [讯飞开放平台](https://www.xfyun.cn/)
2. 注册/登录账号
3. 进入 **控制台** → **语音听写（流式版）**
4. 创建应用
5. 复制 APPID、APIKey、APISecret

**注意**: 
- 当前实现使用了浏览器原生的 Web Speech API
- 科大讯飞接口代码已准备好，如需使用可切换
- Web Speech API 在 Chrome 中支持较好，且免费

## 手动设置环境变量（备用方案）

如果不想使用 .env 文件，也可以手动设置环境变量（不推荐）：

### Windows PowerShell

```powershell
# 临时设置（当前会话有效）
$env:SUPABASE_DB_PASSWORD="your_password"
$env:SUPABASE_JWT_SECRET="your_jwt_secret"
$env:QWEN_API_KEY="your_qwen_key"
$env:AMAP_API_KEY="your_amap_key"

# 启动后端
cd travel-planner-backend
./mvnw spring-boot:run
```

### Linux/Mac Bash

```bash
# 临时设置（当前会话有效）
export SUPABASE_DB_PASSWORD="your_password"
export SUPABASE_JWT_SECRET="your_jwt_secret"
export QWEN_API_KEY="your_qwen_key"
export AMAP_API_KEY="your_amap_key"

# 启动后端
cd travel-planner-backend
./mvnw spring-boot:run
```

**为什么不推荐手动设置？**
- ❌ 每次打开新终端都需要重新设置
- ❌ 容易拼写错误或遗漏
- ❌ 不便于团队协作
- ✅ 推荐使用 .env 文件，自动加载，跨平台兼容

## 使用 .env 文件(推荐)

### 快速开始

1. **复制模板文件**

```bash
# 在项目根目录
cd travel-planner-backend
cp .env.example .env
```

2. **编辑 .env 文件,填入真实的 API Keys**

```env
# 数据库配置 (必需)
SUPABASE_DB_PASSWORD=your_actual_password_here

# 认证配置 (必需)
SUPABASE_JWT_SECRET=your_actual_jwt_secret_here

# AI 服务 (必需)
QWEN_API_KEY=sk-your_actual_qwen_key_here

# 地图服务 (必需)
AMAP_API_KEY=your_actual_amap_key_here

# 语音服务 (可选,当前使用 Web Speech API)
XFYUN_APP_ID=your_xfyun_appid
XFYUN_API_KEY=your_xfyun_key
XFYUN_API_SECRET=your_xfyun_secret
```

3. **直接启动应用**

```bash
# Windows PowerShell
./mvnw spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

应用会在启动时自动加载 `.env` 文件中的环境变量,**无需手动设置任何环境变量**。

### .env 文件加载机制

本项目使用自定义的 `DotEnvLoader` 在 Spring Boot 启动前自动加载 `.env` 文件。支持:

✅ **跨平台支持**: Windows、Linux、macOS  
✅ **多路径搜索**: 项目根目录 → 父目录 → 工作目录 → 用户主目录  
✅ **UTF-8 编码**: 支持中文注释  
✅ **灵活格式**: 支持 `#` 注释、空行、带引号的值  
✅ **安全**: 自动跳过示例值 (包含 `your_` 或 `_here`)  
✅ **降级**: 未找到 .env 时自动使用系统环境变量  

启动成功后,控制台会显示:

```
[DotEnvLoader] Successfully loaded 7 environment variables from .env file: /path/to/.env
```

**注意**: 
- `.env` 文件已在 `.gitignore` 中排除,不会被提交到 Git
- `.env.example` 是模板文件,可以安全提交到 Git

### 从 IDEA 运行配置迁移

如果你之前使用 IntelliJ IDEA 的运行配置设置环境变量,现在可以迁移到 .env 文件:

#### 步骤 1: 导出现有的环境变量

1. 打开 IDEA,进入 **Run** → **Edit Configurations...**
2. 选择 `TravelPlannerBackendApplication`
3. 找到 **Environment variables** 字段
4. 复制所有环境变量的值 (格式: `KEY1=value1;KEY2=value2`)

#### 步骤 2: 创建 .env 文件

在 `travel-planner-backend` 目录下创建 `.env` 文件,将复制的值按行分隔:

```env
SUPABASE_DB_PASSWORD=从IDEA复制的值
SUPABASE_JWT_SECRET=从IDEA复制的值
QWEN_API_KEY=从IDEA复制的值
AMAP_API_KEY=从IDEA复制的值
```

#### 步骤 3: 测试 .env 加载

1. **移除 IDEA 运行配置中的环境变量** (可选,但推荐)
   - 清空 **Environment variables** 字段
   - 点击 **Apply** → **OK**

2. **启动应用**
   - 使用 IDEA 的运行按钮,或者在终端运行 `./mvnw spring-boot:run`
   - 检查控制台输出: `Successfully loaded X environment variables from .env file`

3. **验证功能**
   - 访问健康检查接口: `http://localhost:8080/api/health`
   - 测试 AI 生成: `POST http://localhost:8080/api/ai/generate`

#### 优先级说明

环境变量加载优先级 (从高到低):

1. **系统环境变量** (Windows 系统属性 / Linux export)
2. **IDEA 运行配置中的环境变量**
3. **.env 文件**

如果同一个变量在多处定义,优先级高的会覆盖低的。推荐只使用 .env 文件,保持配置简单统一。

### 跨平台兼容性

.env 文件在 Windows 和 Linux 环境下完全兼容:

| 特性 | Windows | Linux/Mac |
|------|---------|-----------|
| 文件编码 | UTF-8 ✅ | UTF-8 ✅ |
| 行结束符 | CRLF (`\r\n`) ✅ | LF (`\n`) ✅ |
| 路径分隔符 | `\` 或 `/` ✅ | `/` ✅ |
| 文件路径 | 自动搜索 ✅ | 自动搜索 ✅ |

**最佳实践**:
- 在 Windows 上创建的 .env 文件可以直接在 Linux 上使用
- 推荐使用 Git 管理 `.env.example`,团队成员复制后修改
- 不要在 .env 中使用反斜杠路径 (如果有路径配置)

## 验证配置

### 1. 检查 .env 加载

启动后端后，在控制台查找以下日志：

```
✅ 成功: [DotEnvLoader] Successfully loaded 7 environment variables from .env file: /path/to/.env
❌ 失败: [DotEnvLoader] No .env file found, using system environment variables
```

如果看到"No .env file found"，请检查：
- `.env` 文件是否存在于 `travel-planner-backend` 目录
- 文件名是否正确（不是 `.env.txt` 或 `.env.example`）
- 文件编码是否为 UTF-8

### 2. 检查应用启动

```
✅ 成功: Started TravelPlannerBackendApplication in X.XXX seconds
❌ 失败: Supabase JWT secret is not configured
```

如果启动失败，请检查 `.env` 文件中的变量值是否正确。

### 3. 测试 API 接口

```bash
# 健康检查
curl http://localhost:8080/api/health

# 预期响应
{"status":"OK","timestamp":"2024-01-01T00:00:00Z"}
```

### 4. 验证每个环境变量

使用以下接口测试各个服务：

| 环境变量 | 测试方法 | 预期结果 |
|---------|---------|---------|
| `SUPABASE_DB_PASSWORD` | 启动应用 | 无数据库连接错误 |
| `SUPABASE_JWT_SECRET` | 调用需要认证的接口 | JWT 验证通过 |
| `QWEN_API_KEY` | POST `/api/ai/generate` | 返回生成的旅行计划 |
| `AMAP_API_KEY` | (前端调用) | 地图正常加载 |

### 常见问题排查

#### 问题 1: 环境变量未加载

**症状**: 应用启动失败，提示配置缺失

**解决方案**:
1. 确认 `.env` 文件位置: `travel-planner-backend/.env`
2. 检查文件内容格式: `KEY=value` (等号两边不要有空格)
3. 确认没有使用示例值 (包含 `your_` 或 `_here`)
4. 尝试使用绝对路径查看文件: `cat /path/to/travel-planner-backend/.env`

#### 问题 2: Windows 下路径问题

**症状**: 提示找不到 .env 文件

**解决方案**:
- 确保 .env 文件在项目根目录: `G:\codes\code9\LLMSE\llmse_ai_travel_planner\travel-planner-backend\.env`
- 使用 PowerShell 查看: `Get-Content .env`
- 检查文件是否隐藏: 在文件资源管理器中显示隐藏文件

#### 问题 3: Linux 下权限问题

**症状**: 无法读取 .env 文件

**解决方案**:
```bash
# 确保文件可读
chmod 644 .env

# 检查文件所有者
ls -l .env
```

#### 问题 4: 值包含特殊字符

**症状**: 环境变量值被截断或解析错误

**解决方案**:
```env
# 使用双引号包裹含特殊字符的值
SUPABASE_JWT_SECRET="abc123!@#$%^&*()"
QWEN_API_KEY="sk-1234567890abcdef"
```

## 前端配置

前端不需要配置环境变量，API Keys 通过后端接口获取。

唯一需要的配置是 API 基础 URL（已在 `apiClient.ts` 中设置）：

```typescript
const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
})
```

生产环境需要修改为实际的后端地址。

## Docker 部署环境变量

如果使用 Docker 部署，有两种方式传递环境变量：

### 方式 1: 使用 .env 文件（推荐）

```bash
# docker-compose.yml
version: '3.8'
services:
  backend:
    build: ./travel-planner-backend
    env_file:
      - ./travel-planner-backend/.env
    ports:
      - "8080:8080"
```

### 方式 2: 直接在 docker-compose 中定义

```yaml
version: '3.8'
services:
  backend:
    build: ./travel-planner-backend
    environment:
      - SUPABASE_DB_PASSWORD=${SUPABASE_DB_PASSWORD}
      - SUPABASE_JWT_SECRET=${SUPABASE_JWT_SECRET}
      - QWEN_API_KEY=${QWEN_API_KEY}
      - AMAP_API_KEY=${AMAP_API_KEY}
    ports:
      - "8080:8080"
```

### 方式 3: Docker run 命令

```bash
docker run -d \
  -e SUPABASE_DB_PASSWORD="your_password" \
  -e SUPABASE_JWT_SECRET="your_jwt_secret" \
  -e QWEN_API_KEY="your_qwen_key" \
  -e AMAP_API_KEY="your_amap_key" \
  -p 8080:8080 \
  travel-planner-backend
```

**注意**: 
- Docker 容器内的 DotEnvLoader 也会正常工作
- 推荐使用 `env_file` 而不是 `environment`，避免敏感信息出现在 docker-compose.yml

## 生产环境最佳实践

### 云平台环境变量配置

不同云平台有不同的环境变量配置方式：

#### 阿里云 (Alibaba Cloud)
```bash
# 函数计算 (Function Compute)
# 在控制台 → 函数设置 → 环境变量 中配置

# 容器服务 (Container Service)
# 使用 ConfigMap 或 Secret 管理环境变量
```

#### AWS
```bash
# Elastic Beanstalk
# 在 Configuration → Software → Environment properties 中配置

# ECS
# 在 Task Definition 中配置 environment 字段
```

#### Azure
```bash
# App Service
# 在 Configuration → Application settings 中配置
```

### 密钥管理服务

对于生产环境，建议使用专业的密钥管理服务：

| 服务商 | 产品名称 | 说明 |
|--------|---------|------|
| 阿里云 | 密钥管理服务 (KMS) | 集成密钥存储、轮换、审计 |
| AWS | Secrets Manager | 自动轮换密钥 |
| Azure | Key Vault | 统一密钥管理 |
| HashiCorp | Vault | 开源密钥管理 |

### 环境隔离

建议为不同环境使用不同的 .env 文件：

```
travel-planner-backend/
├── .env                 # 本地开发 (不提交到 Git)
├── .env.development     # 开发环境模板
├── .env.staging         # 预发布环境模板
├── .env.production      # 生产环境模板
└── .env.example         # 示例模板 (提交到 Git)
```

使用时根据环境复制对应的模板：
```bash
# 开发环境
cp .env.development .env

# 生产环境
cp .env.production .env
```

## 安全建议

1. ✅ **永远不要将 .env 文件提交到 Git**
   - 已在 `.gitignore` 中配置
   - 定期检查 `git status` 确保 .env 未被追踪
   - 使用 `git secrets` 工具防止意外提交

2. ✅ **使用强密钥**
   - JWT Secret 至少 256 位随机字符
   - 定期轮换 API Keys (建议每 90 天)
   - 不要使用默认或示例密钥

3. ✅ **最小权限原则**
   - API Keys 只授予必需的权限
   - 使用只读 Key (如果可能)
   - 设置 IP 白名单限制

4. ✅ **生产环境加固**
   - 使用 HTTPS 传输
   - 启用 API 调用频率限制
   - 记录所有 API 调用审计日志
   - 使用云平台的密钥管理服务

5. ✅ **监控和告警**
   - 监控 API Key 使用量异常
   - 设置成本告警 (防止 API Key 泄露导致费用激增)
   - 定期审查访问日志

6. ✅ **团队协作**
   - 通过安全渠道分享 .env 内容 (如 1Password, Bitwarden)
   - 离职员工及时轮换所有密钥
   - 使用 `.env.example` 让新成员快速上手

## 附录: 环境变量完整列表

| 变量名 | 必需 | 说明 | 获取方式 |
|--------|------|------|---------|
| `SUPABASE_DB_PASSWORD` | ✅ | 数据库密码 | Supabase Dashboard → Settings → Database |
| `SUPABASE_JWT_SECRET` | ✅ | JWT 签名密钥 | Supabase Dashboard → Settings → API |
| `QWEN_API_KEY` | ✅ | 通义千问 API | 阿里云百炼 → API-KEY 管理 |
| `AMAP_API_KEY` | ✅ | 高德地图 API | 高德开放平台 → 应用管理 |
| `XFYUN_APP_ID` | ⭕ | 讯飞语音 App ID | 讯飞开放平台 → 控制台 |
| `XFYUN_API_KEY` | ⭕ | 讯飞语音 API Key | 讯飞开放平台 → 控制台 |
| `XFYUN_API_SECRET` | ⭕ | 讯飞语音 API Secret | 讯飞开放平台 → 控制台 |

**符号说明**:
- ✅ 必需: 应用启动必须配置
- ⭕ 可选: 当前使用 Web Speech API，科大讯飞为备选方案

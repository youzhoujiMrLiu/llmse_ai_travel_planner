# 🚀 AI 旅行规划助手 - Docker 部署指南

## 📋 目录
- [快速开始](#快速开始)
- [详细步骤](#详细步骤)
- [配置说明](#配置说明)
- [常见问题](#常见问题)

---

## 🎯 快速开始

### 前置要求
- 已安装 Docker（[下载地址](https://www.docker.com/get-started)）
- 有网络连接

### 一键启动（推荐）

1. **创建配置文件**
```bash
# Linux/macOS
cat > .env << 'EOF'
# 后端配置
SUPABASE_DB_PASSWORD=提供的数据库密码
SUPABASE_JWT_SECRET=提供的JWT密钥
QWEN_API_KEY=提供的通义千问Key
AMAP_API_KEY=提供的高德后端Key

# 前端配置
VITE_SUPABASE_URL=提供的Supabase地址
VITE_SUPABASE_ANON_KEY=提供的Supabase匿名Key
VITE_AMAP_WEB_KEY=提供的高德前端Key
EOF

# Windows PowerShell
@"
SUPABASE_DB_PASSWORD=提供的数据库密码
SUPABASE_JWT_SECRET=提供的JWT密钥
QWEN_API_KEY=提供的通义千问Key
AMAP_API_KEY=提供的高德后端Key
VITE_SUPABASE_URL=提供的Supabase地址
VITE_SUPABASE_ANON_KEY=提供的Supabase匿名Key
VITE_AMAP_WEB_KEY=提供的高德前端Key
"@ | Out-File -FilePath .env -Encoding utf8
```

2. **拉取并运行镜像**
```bash
docker run -d \
  --name travel-planner \
  -p 80:80 \
  --env-file .env \
  --restart unless-stopped \
  registry.cn-hangzhou.aliyuncs.com/你的命名空间/travel-planner:latest
```

3. **访问应用**
- 打开浏览器访问：http://localhost

---

## 📖 详细步骤

### 步骤 1: 安装 Docker

**Windows:**
1. 下载 [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop)
2. 双击安装，重启电脑
3. 验证安装：`docker --version`

**macOS:**
```bash
brew install --cask docker
# 或者下载 Docker Desktop for Mac
```

**Linux (Ubuntu/Debian):**
```bash
# 安装 Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 添加当前用户到 docker 组
sudo usermod -aG docker $USER
newgrp docker

# 验证安装
docker --version
```

### 步骤 2: 登录阿里云镜像仓库（如果是私有仓库）

```bash
# 如果镜像仓库设置为公开，可以跳过此步骤
docker login --username=阿里云账号 registry.cn-hangzhou.aliyuncs.com
# 输入密码（使用镜像仓库的独立密码，不是阿里云登录密码）
```

### 步骤 3: 拉取镜像

```bash
# 拉取最新版本
docker pull registry.cn-hangzhou.aliyuncs.com/你的命名空间/travel-planner:latest

# 查看已下载的镜像
docker images | grep travel-planner
```

### 步骤 4: 准备配置文件

创建 `.env` 文件（将下面的值替换为实际提供的 Key）：

```bash
# ============================================
# 后端环境变量（必需）
# ============================================
SUPABASE_DB_PASSWORD=实际的数据库密码
SUPABASE_JWT_SECRET=实际的JWT密钥
QWEN_API_KEY=sk-实际的通义千问Key
AMAP_API_KEY=实际的高德Web服务Key

# ============================================
# 前端环境变量（必需）
# ============================================
VITE_SUPABASE_URL=https://xxxxx.supabase.co
VITE_SUPABASE_ANON_KEY=实际的Supabase匿名密钥
VITE_AMAP_WEB_KEY=实际的高德JS_API_Key

# ============================================
# 语音服务（可选，不填则禁用语音功能）
# ============================================
XFYUN_APP_ID=
XFYUN_API_KEY=
XFYUN_API_SECRET=
```

### 步骤 5: 运行容器

**方式 A - 使用 .env 文件（推荐）：**
```bash
docker run -d \
  --name travel-planner \
  -p 80:80 \
  --env-file .env \
  --restart unless-stopped \
  registry.cn-hangzhou.aliyuncs.com/你的命名空间/travel-planner:latest
```

**方式 B - 直接传递环境变量：**
```bash
docker run -d \
  --name travel-planner \
  -p 80:80 \
  -e SUPABASE_DB_PASSWORD="xxx" \
  -e SUPABASE_JWT_SECRET="xxx" \
  -e QWEN_API_KEY="sk-xxx" \
  -e AMAP_API_KEY="xxx" \
  -e VITE_SUPABASE_URL="https://xxx.supabase.co" \
  -e VITE_SUPABASE_ANON_KEY="xxx" \
  -e VITE_AMAP_WEB_KEY="xxx" \
  --restart unless-stopped \
  registry.cn-hangzhou.aliyuncs.com/你的命名空间/travel-planner:latest
```

**方式 C - 挂载 .env 文件到容器内：**
```bash
# Linux/macOS
docker run -d \
  --name travel-planner \
  -p 80:80 \
  -v $(pwd)/.env:/app/.env \
  --restart unless-stopped \
  registry.cn-hangzhou.aliyuncs.com/你的命名空间/travel-planner:latest

# Windows PowerShell
docker run -d `
  --name travel-planner `
  -p 80:80 `
  -v ${PWD}/.env:/app/.env `
  --restart unless-stopped `
  registry.cn-hangzhou.aliyuncs.com/你的命名空间/travel-planner:latest
```

### 步骤 6: 验证运行状态

```bash
# 查看容器状态
docker ps

# 查看启动日志
docker logs travel-planner

# 实时查看日志
docker logs -f travel-planner
```

如果看到类似输出，说明启动成功：
```
==========================================
AI Travel Planner - Starting Services
==========================================

Checking environment variables...
✓ SUPABASE_DB_PASSWORD is configured
✓ SUPABASE_JWT_SECRET is configured
✓ QWEN_API_KEY is configured
✓ AMAP_API_KEY (Backend) is configured
✓ VITE_SUPABASE_URL (Frontend) is configured
✓ VITE_SUPABASE_ANON_KEY (Frontend) is configured
✓ VITE_AMAP_WEB_KEY (Frontend) is configured
```

### 步骤 7: 访问应用

在浏览器中打开：
- **本地访问**: http://localhost
- **局域网访问**: http://你的IP地址

---

## ⚙️ 配置说明

### 端口映射

| 容器端口 | 宿主机端口 | 用途 |
|---------|-----------|------|
| 80      | 80        | 前端界面 + API 代理 |

> 💡 如果 80 端口被占用，可以改为其他端口，例如 `-p 8000:80`，然后访问 http://localhost:8000

### 环境变量详解

| 变量名 | 用途 | 必需 | 示例 |
|--------|------|------|------|
| `SUPABASE_DB_PASSWORD` | 数据库密码 | ✅ | `your_password_123` |
| `SUPABASE_JWT_SECRET` | JWT 验证密钥 | ✅ | `your-jwt-secret-here` |
| `QWEN_API_KEY` | 通义千问 AI | ✅ | `sk-xxxxxxxxxxxx` |
| `AMAP_API_KEY` | 高德 Web 服务 | ✅ | `abc123def456` |
| `VITE_SUPABASE_URL` | Supabase 地址 | ✅ | `https://xxx.supabase.co` |
| `VITE_SUPABASE_ANON_KEY` | Supabase 公开密钥 | ✅ | `eyJhbGc...` |
| `VITE_AMAP_WEB_KEY` | 高德 JS API | ✅ | `xyz789abc123` |
| `XFYUN_APP_ID` | 讯飞语音 ID | ⭕ | `12345678` |
| `XFYUN_API_KEY` | 讯飞 API Key | ⭕ | `your_key` |
| `XFYUN_API_SECRET` | 讯飞 API Secret | ⭕ | `your_secret` |

---

## 🔧 管理命令

### 查看日志
```bash
# 查看最近 100 行日志
docker logs --tail 100 travel-planner

# 实时跟踪日志
docker logs -f travel-planner
```

### 停止容器
```bash
docker stop travel-planner
```

### 启动容器
```bash
docker start travel-planner
```

### 重启容器
```bash
docker restart travel-planner
```

### 删除容器
```bash
# 停止并删除
docker stop travel-planner
docker rm travel-planner
```

### 更新镜像
```bash
# 1. 拉取最新镜像
docker pull registry.cn-hangzhou.aliyuncs.com/你的命名空间/travel-planner:latest

# 2. 停止并删除旧容器
docker stop travel-planner
docker rm travel-planner

# 3. 使用新镜像启动容器（使用之前的命令）
docker run -d \
  --name travel-planner \
  -p 80:80 \
  --env-file .env \
  --restart unless-stopped \
  registry.cn-hangzhou.aliyuncs.com/你的命名空间/travel-planner:latest
```

### 进入容器（调试用）
```bash
docker exec -it travel-planner sh

# 在容器内可以：
# - 查看日志：tail -f /var/log/nginx/error.log
# - 查看环境变量：env | grep VITE
# - 编辑配置：vi /app/.env（需要先 apk add vim）
```

---

## ❓ 常见问题

### 1. 端口被占用

**问题：** `Error starting userland proxy: listen tcp4 0.0.0.0:80: bind: address already in use`

**解决：** 改用其他端口
```bash
docker run -d \
  --name travel-planner \
  -p 8080:80 \
  --env-file .env \
  registry.cn-hangzhou.aliyuncs.com/你的命名空间/travel-planner:latest
```
然后访问 http://localhost:8080

### 2. 无法访问外网

**问题：** 容器内无法连接到 Supabase、通义千问等服务

**解决：** 检查网络和 DNS
```bash
# 测试容器网络
docker exec travel-planner ping -c 4 baidu.com

# 如果不通，使用宿主机网络
docker run -d \
  --name travel-planner \
  --network host \
  --env-file .env \
  registry.cn-hangzhou.aliyuncs.com/你的命名空间/travel-planner:latest
```

### 3. 前端页面空白或报错

**原因：** 前端环境变量未正确注入

**排查步骤：**
```bash
# 1. 查看启动日志
docker logs travel-planner | grep "Injecting Frontend"

# 应该看到：
# ✓ Injected VITE_SUPABASE_URL
# ✓ Injected VITE_SUPABASE_ANON_KEY
# ✓ Injected VITE_AMAP_WEB_KEY

# 2. 检查环境变量
docker exec travel-planner env | grep VITE

# 3. 进入容器检查前端文件
docker exec travel-planner sh
cat /usr/share/nginx/html/index.html | grep "webapi.amap.com"
# 应该看到实际的 Key，而不是 __AMAP_WEB_KEY__
```

### 4. 后端 API 报 500 错误

**原因：** 数据库连接失败或 API Key 错误

**排查：**
```bash
# 查看后端日志
docker logs travel-planner 2>&1 | grep -i error

# 检查后端环境变量
docker exec travel-planner env | grep -E "SUPABASE|QWEN|AMAP"
```

### 5. 修改环境变量后不生效

**解决：** 需要重启容器
```bash
# 方式 1: 重启现有容器（环境变量已通过 -e 或 --env-file 传入）
docker restart travel-planner

# 方式 2: 如果挂载了 .env 文件，修改后重启即可
# 编辑 .env 文件
vi .env
# 重启容器
docker restart travel-planner

# 方式 3: 删除重建容器（如果需要改变端口等）
docker stop travel-planner
docker rm travel-planner
docker run -d --name travel-planner -p 80:80 --env-file .env ...
```

### 6. Windows 防火墙阻止访问

**问题：** 本机可以访问，但局域网其他设备无法访问

**解决：**
```powershell
# 以管理员身份运行 PowerShell
New-NetFirewallRule -DisplayName "Docker Travel Planner" -Direction Inbound -LocalPort 80 -Protocol TCP -Action Allow
```

### 7. 清理 Docker 占用的磁盘空间

```bash
# 删除未使用的镜像
docker image prune -a

# 删除未使用的容器
docker container prune

# 清理所有未使用资源
docker system prune -a
```

---

## 🌐 生产环境部署建议

### 使用 HTTPS

建议在生产环境使用 Nginx 反向代理 + Let's Encrypt 证书：

```nginx
# /etc/nginx/sites-available/travel-planner
server {
    listen 443 ssl http2;
    server_name yourdomain.com;
    
    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;
    
    location / {
        proxy_pass http://localhost:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 使用 Docker Compose

创建 `docker-compose.yml`：
```yaml
version: '3.8'

services:
  travel-planner:
    image: registry.cn-hangzhou.aliyuncs.com/你的命名空间/travel-planner:latest
    container_name: travel-planner
    ports:
      - "80:80"
    env_file:
      - .env
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

启动：
```bash
docker-compose up -d
```

---

## 📞 获取帮助

如果遇到问题：
1. 查看容器日志：`docker logs travel-planner`
2. 检查环境变量配置是否正确
3. 确认网络连接正常
4. 联系项目维护者

---

## 📝 更新日志

- **v1.0.0** - 初始版本，支持前后端一体化部署

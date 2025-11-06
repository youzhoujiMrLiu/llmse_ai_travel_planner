# 🚀 快速使用指南

## 给使用者：如何运行镜像

### 方法 1️⃣：一键部署脚本（最简单）

**Windows 用户：**
```powershell
# 1. 下载脚本
Invoke-WebRequest -Uri "镜像仓库地址/quick-deploy.ps1" -OutFile "quick-deploy.ps1"

# 2. 运行脚本（会自动引导配置）
.\quick-deploy.ps1
```

**Linux/macOS 用户：**
```bash
# 1. 下载脚本
curl -O 镜像仓库地址/quick-deploy.sh

# 2. 添加执行权限
chmod +x quick-deploy.sh

# 3. 运行脚本（会自动引导配置）
./quick-deploy.sh
```

脚本会自动完成：
- ✅ 检查 Docker 是否安装
- ✅ 拉取最新镜像
- ✅ 引导填写配置
- ✅ 启动容器
- ✅ 验证运行状态

---

### 方法 2️⃣：手动部署（需要基础 Docker 知识）

#### 步骤 1：创建配置文件

创建 `.env` 文件，填入以下内容（替换为实际的 Key）：

```bash
# 后端配置
SUPABASE_DB_PASSWORD=提供的数据库密码
SUPABASE_JWT_SECRET=提供的JWT密钥
QWEN_API_KEY=提供的千问Key
AMAP_API_KEY=提供的高德后端Key

# 前端配置
VITE_SUPABASE_URL=https://xxxxx.supabase.co
VITE_SUPABASE_ANON_KEY=提供的Supabase匿名Key
VITE_AMAP_WEB_KEY=提供的高德前端Key
```

#### 步骤 2：运行容器

```bash
# 拉取镜像
docker pull registry.cn-hangzhou.aliyuncs.com/命名空间/travel-planner:latest

# 运行容器
docker run -d \
  --name travel-planner \
  -p 80:80 \
  --env-file .env \
  --restart unless-stopped \
  registry.cn-hangzhou.aliyuncs.com/命名空间/travel-planner:latest
```

#### 步骤 3：访问应用

浏览器打开：http://localhost

---

## 📋 需要的配置信息（找项目提供者获取）

| 配置项 | 说明 | 示例 |
|--------|------|------|
| SUPABASE_DB_PASSWORD | 数据库密码 | `your_password_123` |
| SUPABASE_JWT_SECRET | JWT密钥 | `your-jwt-secret` |
| QWEN_API_KEY | AI服务Key | `sk-xxxx` |
| AMAP_API_KEY | 高德后端Key | `abc123` |
| VITE_SUPABASE_URL | Supabase地址 | `https://xxx.supabase.co` |
| VITE_SUPABASE_ANON_KEY | Supabase公钥 | `eyJhbG...` |
| VITE_AMAP_WEB_KEY | 高德前端Key | `xyz789` |

---

## 🔧 常用命令

```bash
# 查看日志
docker logs -f travel-planner

# 停止服务
docker stop travel-planner

# 启动服务
docker start travel-planner

# 重启服务
docker restart travel-planner

# 删除容器
docker stop travel-planner && docker rm travel-planner
```

---

## ❓ 常见问题

**Q: 80 端口被占用怎么办？**
```bash
# 改用其他端口，例如 8080
docker run -d --name travel-planner -p 8080:80 --env-file .env ...
# 然后访问 http://localhost:8080
```

**Q: 如何修改配置？**
```bash
# 方法1：修改 .env 文件后重启
vi .env
docker restart travel-planner

# 方法2：删除重建容器
docker stop travel-planner
docker rm travel-planner
docker run -d --name travel-planner -p 80:80 --env-file .env ...
```

**Q: 如何更新到最新版本？**
```bash
# 1. 拉取最新镜像
docker pull registry.cn-hangzhou.aliyuncs.com/命名空间/travel-planner:latest

# 2. 停止并删除旧容器
docker stop travel-planner && docker rm travel-planner

# 3. 使用新镜像启动（配置不变）
docker run -d --name travel-planner -p 80:80 --env-file .env ...
```

---

## 📖 详细文档

查看完整部署文档：[DOCKER_DEPLOYMENT_GUIDE.md](./DOCKER_DEPLOYMENT_GUIDE.md)

---

## 📞 技术支持

遇到问题请联系项目维护者，并提供：
1. Docker 版本：`docker --version`
2. 操作系统版本
3. 错误日志：`docker logs travel-planner`

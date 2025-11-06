# 📦 Docker 镜像分发指南

## 给项目维护者：如何分享镜像给其他人

### 🎯 方案 A：公开镜像仓库（推荐）

#### 1. 设置阿里云镜像仓库为公开

1. 登录 [阿里云容器镜像服务控制台](https://cr.console.aliyun.com/)
2. 进入「个人实例」或「企业实例」
3. 找到你的镜像仓库 `travel-planner`
4. 点击「基本信息」
5. 将「仓库类型」设置为「公开」

#### 2. 获取公开地址

```
registry.cn-hangzhou.aliyuncs.com/你的命名空间/travel-planner:latest
```

#### 3. 分享给其他人

**简单版（直接给命令）：**

```bash
# Linux/macOS
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
  registry.cn-hangzhou.aliyuncs.com/你的命名空间/travel-planner:latest
```

**完整版（提供文档和脚本）：**

将以下文件打包发送给使用者：
- `QUICK_START_FOR_USERS.md` - 快速开始指南
- `quick-deploy.ps1` - Windows 一键部署脚本
- `quick-deploy.sh` - Linux/macOS 一键部署脚本
- `.env.docker.example` - 配置文件模板

并告知所有必需的环境变量值。

---

### 🎯 方案 B：私有镜像仓库

如果不想公开镜像，使用者需要先登录：

```bash
docker login --username=你的阿里云账号 registry.cn-hangzhou.aliyuncs.com
# 输入密码（使用镜像仓库的独立密码）
```

然后提供给使用者：
1. 阿里云账号
2. 镜像仓库密码（在控制台设置）
3. 镜像完整地址
4. 所有环境变量的值

---

### 🎯 方案 C：导出镜像文件

适合无法访问互联网或内网部署的场景。

#### 1. 导出镜像

```bash
# 导出为 tar 文件（约 500MB - 1GB）
docker save registry.cn-hangzhou.aliyuncs.com/你的命名空间/travel-planner:latest \
  -o travel-planner.tar

# 压缩（可选，进一步减小体积）
gzip travel-planner.tar
# 得到 travel-planner.tar.gz
```

#### 2. 分享文件

通过网盘、U盘等方式分享 `travel-planner.tar.gz`

#### 3. 使用者导入镜像

```bash
# 如果是 .tar.gz 先解压
gunzip travel-planner.tar.gz

# 导入镜像
docker load -i travel-planner.tar

# 查看导入的镜像
docker images | grep travel-planner

# 运行（使用完整镜像名）
docker run -d \
  --name travel-planner \
  -p 80:80 \
  --env-file .env \
  registry.cn-hangzhou.aliyuncs.com/你的命名空间/travel-planner:latest
```

---

## 📋 需要提供给使用者的信息

### 必需信息

**镜像信息：**
- 镜像地址：`registry.cn-hangzhou.aliyuncs.com/你的命名空间/travel-planner:latest`
- 是否公开：是/否
- 如果私有，提供：阿里云账号 + 镜像仓库密码

**环境变量（后端）：**
```
SUPABASE_DB_PASSWORD=实际值
SUPABASE_JWT_SECRET=实际值
QWEN_API_KEY=实际值
AMAP_API_KEY=实际值
```

**环境变量（前端）：**
```
VITE_SUPABASE_URL=实际值
VITE_SUPABASE_ANON_KEY=实际值
VITE_AMAP_WEB_KEY=实际值
```

### 可选信息

**语音服务（如果启用）：**
```
XFYUN_APP_ID=实际值
XFYUN_API_KEY=实际值
XFYUN_API_SECRET=实际值
```

### 使用文档

提供以下文件：
- ✅ `QUICK_START_FOR_USERS.md` - 简明使用指南
- ✅ `DOCKER_DEPLOYMENT_GUIDE.md` - 详细部署文档
- ✅ `quick-deploy.ps1` / `quick-deploy.sh` - 一键部署脚本
- ✅ `.env.docker.example` - 配置模板

---

## 📧 通知模板

可以用以下模板通知使用者：

```
【AI 旅行规划助手 - Docker 镜像部署说明】

镜像地址：
registry.cn-hangzhou.aliyuncs.com/xxx/travel-planner:latest

部署方式：
1. 快速部署（推荐）：
   运行附件中的 quick-deploy.ps1 (Windows) 或 quick-deploy.sh (Linux/macOS)
   脚本会自动引导你配置环境变量并启动服务

2. 手动部署：
   参考附件 QUICK_START_FOR_USERS.md

所需配置信息：
- Supabase 数据库密码: [提供实际值]
- Supabase JWT密钥: [提供实际值]
- 通义千问 API Key: [提供实际值]
- 高德地图后端 Key: [提供实际值]
- Supabase URL: [提供实际值]
- Supabase ANON Key: [提供实际值]
- 高德地图前端 Key: [提供实际值]

访问地址：
启动成功后，浏览器访问 http://localhost

如有问题，请查看详细文档 DOCKER_DEPLOYMENT_GUIDE.md
或联系我获取技术支持。
```

---

## ⚠️ 安全建议

1. **不要在公开场合分享环境变量**
   - 通过加密邮件、私信等方式传递
   - 考虑使用密码管理工具（如 1Password、LastPass）

2. **定期更新密钥**
   - 如果密钥泄露，及时更换
   - 使用者只需修改 .env 文件并重启容器

3. **生产环境建议**
   - 使用 HTTPS（通过 Nginx 反向代理）
   - 启用防火墙规则
   - 定期备份数据库
   - 监控容器运行状态

---

## 🔄 版本更新流程

当你发布新版本时：

1. **构建新镜像**
```bash
# 使用版本号标签
docker build -t registry.cn-hangzhou.aliyuncs.com/xxx/travel-planner:v1.1.0 .
docker tag registry.cn-hangzhou.aliyuncs.com/xxx/travel-planner:v1.1.0 \
           registry.cn-hangzhou.aliyuncs.com/xxx/travel-planner:latest

# 推送到仓库
docker push registry.cn-hangzhou.aliyuncs.com/xxx/travel-planner:v1.1.0
docker push registry.cn-hangzhou.aliyuncs.com/xxx/travel-planner:latest
```

2. **通知使用者更新**
```
新版本 v1.1.0 已发布！

更新方法：
docker pull registry.cn-hangzhou.aliyuncs.com/xxx/travel-planner:latest
docker stop travel-planner && docker rm travel-planner
docker run -d --name travel-planner -p 80:80 --env-file .env ...

更新内容：
- 修复了 XXX 问题
- 新增了 YYY 功能
- 优化了 ZZZ 性能
```

---

## 📊 使用统计（可选）

如果需要了解镜像使用情况：

1. 阿里云镜像仓库提供下载次数统计
2. 可以在应用中添加匿名使用统计（需用户同意）
3. 建立用户反馈渠道

---

## 🎓 培训材料（可选）

为使用者准备：
- 📹 部署视频教程
- 📄 功能使用说明
- 🐛 常见问题 FAQ
- 💬 技术支持群组

---

## ✅ 检查清单

分发前确认：

- [ ] 镜像已成功推送到仓库
- [ ] 镜像可以正常拉取和运行
- [ ] 所有环境变量值已准备好
- [ ] 文档和脚本已更新（替换实际的命名空间）
- [ ] 测试过一键部署脚本
- [ ] 准备好技术支持渠道
- [ ] 编写了版本说明和更新日志

---

## 📞 获取帮助

使用者遇到问题时：
1. 查看日志：`docker logs travel-planner`
2. 检查配置是否正确
3. 参考详细文档：DOCKER_DEPLOYMENT_GUIDE.md
4. 联系你获取技术支持

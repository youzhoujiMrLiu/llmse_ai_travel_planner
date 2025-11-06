# ============================================
# AI 旅行规划助手 - Windows 快速部署脚本
# ============================================

$ErrorActionPreference = "Stop"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "AI 旅行规划助手 - 快速部署向导" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 镜像信息（根据实际情况修改）
$REGISTRY = "registry.cn-hangzhou.aliyuncs.com"
$NAMESPACE = "你的命名空间"  # 修改为实际的命名空间
$IMAGE_NAME = "travel-planner"
$VERSION = "latest"
$FULL_IMAGE = "${REGISTRY}/${NAMESPACE}/${IMAGE_NAME}:${VERSION}"

# 检查 Docker 是否安装
try {
    $dockerVersion = docker --version
    Write-Host "✓ Docker 已安装: $dockerVersion" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ 未检测到 Docker，请先安装 Docker" -ForegroundColor Red
    Write-Host "   下载地址: https://www.docker.com/get-started" -ForegroundColor Yellow
    exit 1
}

# 询问是否需要登录镜像仓库
$needLogin = Read-Host "是否需要登录阿里云镜像仓库？(如果是公开仓库选 n) [y/N]"
if ($needLogin -match "^[Yy]$") {
    Write-Host ""
    Write-Host "请输入阿里云账号信息..." -ForegroundColor Yellow
    docker login --username=你的用户名 $REGISTRY
    Write-Host ""
}

# 拉取镜像
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "正在拉取镜像..." -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
docker pull $FULL_IMAGE
Write-Host ""
Write-Host "✓ 镜像拉取成功" -ForegroundColor Green
Write-Host ""

# 检查是否已有配置文件
$USE_EXISTING = $false
if (Test-Path ".env") {
    Write-Host "检测到已存在 .env 配置文件" -ForegroundColor Yellow
    $useExisting = Read-Host "是否使用现有配置？[Y/n]"
    if ($useExisting -notmatch "^[Nn]$") {
        $USE_EXISTING = $true
    }
}

# 创建配置文件
if (-not $USE_EXISTING) {
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host "配置环境变量" -ForegroundColor Cyan
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host "请输入以下配置信息（直接回车跳过可选项）：" -ForegroundColor Yellow
    Write-Host ""
    
    # 后端配置
    Write-Host "--- 后端配置（必需）---" -ForegroundColor Yellow
    $SUPABASE_DB_PASSWORD = Read-Host "Supabase 数据库密码"
    $SUPABASE_JWT_SECRET = Read-Host "Supabase JWT 密钥"
    $QWEN_API_KEY = Read-Host "通义千问 API Key"
    $AMAP_API_KEY = Read-Host "高德地图 API Key (后端)"
    Write-Host ""
    
    # 前端配置
    Write-Host "--- 前端配置（必需）---" -ForegroundColor Yellow
    $VITE_SUPABASE_URL = Read-Host "Supabase URL (如 https://xxx.supabase.co)"
    $VITE_SUPABASE_ANON_KEY = Read-Host "Supabase ANON Key"
    $VITE_AMAP_WEB_KEY = Read-Host "高德地图 Web Key (前端)"
    Write-Host ""
    
    # 可选配置
    Write-Host "--- 语音服务配置（可选，直接回车跳过）---" -ForegroundColor Yellow
    $XFYUN_APP_ID = Read-Host "讯飞 APP ID (可选)"
    $XFYUN_API_KEY = Read-Host "讯飞 API Key (可选)"
    $XFYUN_API_SECRET = Read-Host "讯飞 API Secret (可选)"
    Write-Host ""
    
    # 生成 .env 文件
    $envContent = @"
# 后端配置
SUPABASE_DB_PASSWORD=$SUPABASE_DB_PASSWORD
SUPABASE_JWT_SECRET=$SUPABASE_JWT_SECRET
QWEN_API_KEY=$QWEN_API_KEY
AMAP_API_KEY=$AMAP_API_KEY

# 前端配置
VITE_SUPABASE_URL=$VITE_SUPABASE_URL
VITE_SUPABASE_ANON_KEY=$VITE_SUPABASE_ANON_KEY
VITE_AMAP_WEB_KEY=$VITE_AMAP_WEB_KEY

# 语音服务（可选）
XFYUN_APP_ID=$XFYUN_APP_ID
XFYUN_API_KEY=$XFYUN_API_KEY
XFYUN_API_SECRET=$XFYUN_API_SECRET
"@
    
    $envContent | Out-File -FilePath ".env" -Encoding UTF8
    Write-Host "✓ 配置文件已保存到 .env" -ForegroundColor Green
    Write-Host ""
}

# 询问端口
$PORT = Read-Host "请输入要映射的端口 [默认 80]"
if ([string]::IsNullOrWhiteSpace($PORT)) {
    $PORT = "80"
}

# 检查端口是否被占用
$portInUse = Get-NetTCPConnection -LocalPort $PORT -ErrorAction SilentlyContinue
if ($portInUse) {
    Write-Host "⚠️  警告: 端口 $PORT 已被占用" -ForegroundColor Yellow
    $continueAnyway = Read-Host "是否继续？(可能会失败) [y/N]"
    if ($continueAnyway -notmatch "^[Yy]$") {
        Write-Host "已取消部署"
        exit 1
    }
}

# 检查是否已有同名容器
$existingContainer = docker ps -a --format "{{.Names}}" | Select-String -Pattern "^travel-planner$"
if ($existingContainer) {
    Write-Host ""
    Write-Host "检测到已存在 travel-planner 容器" -ForegroundColor Yellow
    $removeOld = Read-Host "是否删除旧容器并重新创建？[Y/n]"
    if ($removeOld -notmatch "^[Nn]$") {
        Write-Host "正在删除旧容器..."
        docker stop travel-planner 2>$null
        docker rm travel-planner 2>$null
        Write-Host "✓ 旧容器已删除" -ForegroundColor Green
    } else {
        Write-Host "已取消部署"
        exit 1
    }
    Write-Host ""
}

# 启动容器
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "正在启动容器..." -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

docker run -d `
  --name travel-planner `
  -p ${PORT}:80 `
  --env-file .env `
  --restart unless-stopped `
  $FULL_IMAGE

# 等待容器启动
Write-Host ""
Write-Host "等待服务启动..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

# 检查容器状态
$containerRunning = docker ps | Select-String "travel-planner"
if ($containerRunning) {
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host "✅ 部署成功！" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "访问地址:" -ForegroundColor Cyan
    Write-Host "  本地: http://localhost:$PORT" -ForegroundColor White
    Write-Host ""
    Write-Host "常用命令:" -ForegroundColor Cyan
    Write-Host "  查看日志: docker logs -f travel-planner" -ForegroundColor White
    Write-Host "  停止服务: docker stop travel-planner" -ForegroundColor White
    Write-Host "  启动服务: docker start travel-planner" -ForegroundColor White
    Write-Host "  重启服务: docker restart travel-planner" -ForegroundColor White
    Write-Host "  删除容器: docker stop travel-planner; docker rm travel-planner" -ForegroundColor White
    Write-Host ""
    
    $showLogs = Read-Host "是否查看启动日志？[Y/n]"
    if ($showLogs -notmatch "^[Nn]$") {
        Write-Host ""
        docker logs travel-planner
    }
} else {
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Red
    Write-Host "❌ 部署失败" -ForegroundColor Red
    Write-Host "==========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "请查看错误日志:" -ForegroundColor Yellow
    docker logs travel-planner
    exit 1
}

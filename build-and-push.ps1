# ============================================
# Docker 镜像构建和推送脚本 (Windows PowerShell)
# ============================================

# 配置变量 - 请根据你的阿里云镜像仓库信息修改
$REGISTRY = "crpi-dxqp49wtchucqdiv.cn-hangzhou.personal.cr.aliyuncs.com"
$NAMESPACE = "shingeki"  # 修改为你的命名空间
$IMAGE_NAME = "travel-planner"
$VERSION = "latest"

$FULL_IMAGE_NAME = "${REGISTRY}/${NAMESPACE}/${IMAGE_NAME}:${VERSION}"

Write-Host "=========================================="
Write-Host "Building Docker Image"
Write-Host "=========================================="
Write-Host "Image: $FULL_IMAGE_NAME"
Write-Host ""

# 构建镜像
docker build -t $FULL_IMAGE_NAME .

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "✓ Build successful!" -ForegroundColor Green
Write-Host ""

# 询问是否推送
$push = Read-Host "Do you want to push to registry? (y/n)"

if ($push -eq "y" -or $push -eq "Y") {
    Write-Host ""
    Write-Host "=========================================="
    Write-Host "Logging in to Aliyun Container Registry"
    Write-Host "=========================================="
    
    docker login --username=your-username $REGISTRY
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Login failed!" -ForegroundColor Red
        exit 1
    }
    
    Write-Host ""
    Write-Host "=========================================="
    Write-Host "Pushing to Registry"
    Write-Host "=========================================="
    
    docker push $FULL_IMAGE_NAME
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Push failed!" -ForegroundColor Red
        exit 1
    }
    
    Write-Host ""
    Write-Host "✓ Push successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Image: $FULL_IMAGE_NAME"
}

Write-Host ""
Write-Host "=========================================="
Write-Host "Done!"
Write-Host "=========================================="
Write-Host ""
Write-Host "📦 下一步：分发镜像给其他人" -ForegroundColor Cyan
Write-Host ""
Write-Host "1️⃣  确保镜像仓库已设置为公开（或提供登录凭据）" -ForegroundColor Yellow
Write-Host "2️⃣  准备好所有环境变量的值" -ForegroundColor Yellow
Write-Host "3️⃣  将以下文件发送给使用者：" -ForegroundColor Yellow
Write-Host "    - QUICK_START_FOR_USERS.md" -ForegroundColor White
Write-Host "    - quick-deploy.ps1 / quick-deploy.sh" -ForegroundColor White
Write-Host "    - .env.docker.example" -ForegroundColor White
Write-Host ""
Write-Host "详细分发指南：DISTRIBUTION_GUIDE.md" -ForegroundColor Green
Write-Host ""

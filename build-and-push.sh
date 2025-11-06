#!/bin/bash
# ============================================
# Docker 镜像构建和推送脚本 (Linux/macOS)
# ============================================

# 配置变量 - 请根据你的阿里云镜像仓库信息修改
REGISTRY="crpi-dxqp49wtchucqdiv.cn-hangzhou.personal.cr.aliyuncs.com"
NAMESPACE="shingeki"  # 修改为你的命名空间
IMAGE_NAME="travel-planner"
VERSION="latest"

FULL_IMAGE_NAME="${REGISTRY}/${NAMESPACE}/${IMAGE_NAME}:${VERSION}"

echo "=========================================="
echo "Building Docker Image"
echo "=========================================="
echo "Image: ${FULL_IMAGE_NAME}"
echo ""

# 构建镜像
docker build -t ${FULL_IMAGE_NAME} .

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo ""
echo "✓ Build successful!"
echo ""

# 询问是否推送
read -p "Do you want to push to registry? (y/n) " push

if [ "$push" = "y" ] || [ "$push" = "Y" ]; then
    echo ""
    echo "=========================================="
    echo "Logging in to Aliyun Container Registry"
    echo "=========================================="
    
    docker login --username=your-username ${REGISTRY}
    
    if [ $? -ne 0 ]; then
        echo "❌ Login failed!"
        exit 1
    fi
    
    echo ""
    echo "=========================================="
    echo "Pushing to Registry"
    echo "=========================================="
    
    docker push ${FULL_IMAGE_NAME}
    
    if [ $? -ne 0 ]; then
        echo "❌ Push failed!"
        exit 1
    fi
    
    echo ""
    echo "✓ Push successful!"
    echo ""
    echo "Image: ${FULL_IMAGE_NAME}"
fi

echo ""
echo "=========================================="
echo "Done!"
echo "=========================================="
echo ""
echo "📦 下一步：分发镜像给其他人"
echo ""
echo "1️⃣  确保镜像仓库已设置为公开（或提供登录凭据）"
echo "2️⃣  准备好所有环境变量的值"
echo "3️⃣  将以下文件发送给使用者："
echo "    - QUICK_START_FOR_USERS.md"
echo "    - quick-deploy.ps1 / quick-deploy.sh"
echo "    - .env.docker.example"
echo ""
echo "详细分发指南：DISTRIBUTION_GUIDE.md"
echo ""

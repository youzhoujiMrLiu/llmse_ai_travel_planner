#!/bin/bash
# ============================================
# AI 旅行规划助手 - 快速部署脚本
# ============================================

set -e

echo "=========================================="
echo "AI 旅行规划助手 - 快速部署向导"
echo "=========================================="
echo ""

# 镜像信息（根据实际情况修改）
REGISTRY="registry.cn-hangzhou.aliyuncs.com"
NAMESPACE="你的命名空间"  # 修改为实际的命名空间
IMAGE_NAME="travel-planner"
VERSION="latest"
FULL_IMAGE="${REGISTRY}/${NAMESPACE}/${IMAGE_NAME}:${VERSION}"

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ 未检测到 Docker，请先安装 Docker"
    echo "   下载地址: https://www.docker.com/get-started"
    exit 1
fi

echo "✓ Docker 已安装: $(docker --version)"
echo ""

# 询问是否需要登录镜像仓库
read -p "是否需要登录阿里云镜像仓库？(如果是公开仓库选 n) [y/N]: " need_login
if [[ "$need_login" =~ ^[Yy]$ ]]; then
    echo ""
    echo "请输入阿里云账号信息..."
    docker login --username=你的用户名 ${REGISTRY}
    echo ""
fi

# 拉取镜像
echo "=========================================="
echo "正在拉取镜像..."
echo "=========================================="
docker pull ${FULL_IMAGE}
echo ""
echo "✓ 镜像拉取成功"
echo ""

# 询问是否已有配置文件
if [ -f ".env" ]; then
    echo "检测到已存在 .env 配置文件"
    read -p "是否使用现有配置？[Y/n]: " use_existing
    if [[ ! "$use_existing" =~ ^[Nn]$ ]]; then
        USE_EXISTING=true
    fi
fi

# 创建配置文件
if [ "$USE_EXISTING" != "true" ]; then
    echo ""
    echo "=========================================="
    echo "配置环境变量"
    echo "=========================================="
    echo "请输入以下配置信息（直接回车跳过可选项）："
    echo ""
    
    # 后端配置
    echo "--- 后端配置（必需）---"
    read -p "Supabase 数据库密码: " SUPABASE_DB_PASSWORD
    read -p "Supabase JWT 密钥: " SUPABASE_JWT_SECRET
    read -p "通义千问 API Key: " QWEN_API_KEY
    read -p "高德地图 API Key (后端): " AMAP_API_KEY
    echo ""
    
    # 前端配置
    echo "--- 前端配置（必需）---"
    read -p "Supabase URL (如 https://xxx.supabase.co): " VITE_SUPABASE_URL
    read -p "Supabase ANON Key: " VITE_SUPABASE_ANON_KEY
    read -p "高德地图 Web Key (前端): " VITE_AMAP_WEB_KEY
    echo ""
    
    # 可选配置
    echo "--- 语音服务配置（可选，直接回车跳过）---"
    read -p "讯飞 APP ID (可选): " XFYUN_APP_ID
    read -p "讯飞 API Key (可选): " XFYUN_API_KEY
    read -p "讯飞 API Secret (可选): " XFYUN_API_SECRET
    echo ""
    
    # 生成 .env 文件
    cat > .env << EOF
# 后端配置
SUPABASE_DB_PASSWORD=${SUPABASE_DB_PASSWORD}
SUPABASE_JWT_SECRET=${SUPABASE_JWT_SECRET}
QWEN_API_KEY=${QWEN_API_KEY}
AMAP_API_KEY=${AMAP_API_KEY}

# 前端配置
VITE_SUPABASE_URL=${VITE_SUPABASE_URL}
VITE_SUPABASE_ANON_KEY=${VITE_SUPABASE_ANON_KEY}
VITE_AMAP_WEB_KEY=${VITE_AMAP_WEB_KEY}

# 语音服务（可选）
XFYUN_APP_ID=${XFYUN_APP_ID}
XFYUN_API_KEY=${XFYUN_API_KEY}
XFYUN_API_SECRET=${XFYUN_API_SECRET}
EOF
    
    echo "✓ 配置文件已保存到 .env"
    echo ""
fi

# 询问端口
read -p "请输入要映射的端口 [默认 80]: " PORT
PORT=${PORT:-80}

# 检查端口是否被占用
if lsof -Pi :${PORT} -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo "⚠️  警告: 端口 ${PORT} 已被占用"
    read -p "是否继续？(可能会失败) [y/N]: " continue_anyway
    if [[ ! "$continue_anyway" =~ ^[Yy]$ ]]; then
        echo "已取消部署"
        exit 1
    fi
fi

# 检查是否已有同名容器
if docker ps -a --format '{{.Names}}' | grep -q "^travel-planner$"; then
    echo ""
    echo "检测到已存在 travel-planner 容器"
    read -p "是否删除旧容器并重新创建？[Y/n]: " remove_old
    if [[ ! "$remove_old" =~ ^[Nn]$ ]]; then
        echo "正在删除旧容器..."
        docker stop travel-planner 2>/dev/null || true
        docker rm travel-planner 2>/dev/null || true
        echo "✓ 旧容器已删除"
    else
        echo "已取消部署"
        exit 1
    fi
    echo ""
fi

# 启动容器
echo "=========================================="
echo "正在启动容器..."
echo "=========================================="
docker run -d \
  --name travel-planner \
  -p ${PORT}:80 \
  --env-file .env \
  --restart unless-stopped \
  ${FULL_IMAGE}

# 等待容器启动
echo ""
echo "等待服务启动..."
sleep 3

# 检查容器状态
if docker ps | grep -q "travel-planner"; then
    echo ""
    echo "=========================================="
    echo "✅ 部署成功！"
    echo "=========================================="
    echo ""
    echo "访问地址:"
    echo "  本地: http://localhost:${PORT}"
    echo ""
    echo "常用命令:"
    echo "  查看日志: docker logs -f travel-planner"
    echo "  停止服务: docker stop travel-planner"
    echo "  启动服务: docker start travel-planner"
    echo "  重启服务: docker restart travel-planner"
    echo "  删除容器: docker stop travel-planner && docker rm travel-planner"
    echo ""
    
    read -p "是否查看启动日志？[Y/n]: " show_logs
    if [[ ! "$show_logs" =~ ^[Nn]$ ]]; then
        echo ""
        docker logs travel-planner
    fi
else
    echo ""
    echo "=========================================="
    echo "❌ 部署失败"
    echo "=========================================="
    echo ""
    echo "请查看错误日志:"
    docker logs travel-planner
    exit 1
fi

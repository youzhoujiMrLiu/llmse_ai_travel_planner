#!/bin/bash
# ============================================
# Docker 运行示例脚本
# ============================================

# 方式 1: 使用环境变量直接传递
docker run -d \
  --name travel-planner \
  -p 80:80 \
  -p 8080:8080 \
  -e SUPABASE_DB_PASSWORD="your_db_password" \
  -e SUPABASE_JWT_SECRET="your_jwt_secret" \
  -e QWEN_API_KEY="sk-your-qwen-key" \
  -e AMAP_API_KEY="your_amap_backend_key" \
  -e VITE_SUPABASE_URL="https://your-project.supabase.co" \
  -e VITE_SUPABASE_ANON_KEY="your_anon_key" \
  -e VITE_AMAP_WEB_KEY="your_amap_web_key" \
  -e XFYUN_APP_ID="your_xfyun_app_id" \
  -e XFYUN_API_KEY="your_xfyun_api_key" \
  -e XFYUN_API_SECRET="your_xfyun_api_secret" \
  registry.cn-hangzhou.aliyuncs.com/your-namespace/travel-planner:latest

# 方式 2: 使用 .env 文件
# docker run -d \
#   --name travel-planner \
#   -p 80:80 \
#   -p 8080:8080 \
#   --env-file .env \
#   registry.cn-hangzhou.aliyuncs.com/your-namespace/travel-planner:latest

# 方式 3: 挂载 .env 文件到容器内
# docker run -d \
#   --name travel-planner \
#   -p 80:80 \
#   -p 8080:8080 \
#   -v $(pwd)/.env:/app/.env \
#   registry.cn-hangzhou.aliyuncs.com/your-namespace/travel-planner:latest

# 查看日志
# docker logs -f travel-planner

# 进入容器编辑环境变量
# docker exec -it travel-planner sh
# vi /app/.env  (需要先安装 vi: apk add --no-cache vim)
# 或者使用 echo 追加
# docker exec -it travel-planner sh -c "echo 'QWEN_API_KEY=sk-xxx' >> /app/.env"
